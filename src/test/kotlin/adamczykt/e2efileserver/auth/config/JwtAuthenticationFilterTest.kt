package adamczykt.e2efileserver.auth.config

import adamczykt.e2efileserver.auth.model.UserAuthDetails
import adamczykt.e2efileserver.auth.service.TokenService
import adamczykt.e2efileserver.user.entity.User
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.annotation.DirtiesContext

@ExtendWith(MockitoExtension::class)
internal class JwtAuthenticationFilterTest {
    @Mock
    private lateinit var userDetailsService: UserDetailsService
    @Mock
    private lateinit var tokenService: TokenService
    @InjectMocks
    private lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    @AfterEach
    fun clearSecurityContext() = SecurityContextHolder.clearContext()

    @ParameterizedTest
    @CsvSource(nullValues = ["null"], value = ["null", "iwaod901i2unea", "bearer mytoken"])
    fun shouldNotAuthorizeRequestsWithMalformedAuthorizationHeader(authorizationHeader: String?) {
        // given
        mockTokenValidity()
        mockUser()

        // when
        filterRequestWithAuthorizationHeader(authorizationHeader)

        // then
        assertThatRequestIsNotAuthorized()
    }

    @Test
    fun shouldNotAuthorizeRequestWhenTokenIsInvalid() {
        // given
        mockUser()

        // when
        filterRequestWithAuthorizationHeader("Bearer mytoken")

        // then
        assertThatRequestIsNotAuthorized()
    }

    @Test
    fun shouldNotAuthorizeRequestWhenTheUserDoesNotExist() {
        // given
        mockTokenValidity()

        // when
        filterRequestWithAuthorizationHeader("Bearer mytoken")

        // then
        assertThatRequestIsNotAuthorized()
    }

    @Test
    @DirtiesContext
    fun shouldAuthorizeRequestWhenTokenIsValidAndUserExists() {
        // given
        mockTokenValidity()
        mockUser()

        // when
        filterRequestWithAuthorizationHeader("Bearer mytoken")

        // then
        assertThatRequestIsAuthorized()
    }

    private fun mockUser() {
        lenient().`when`(userDetailsService.loadUserByUsername("user1"))
            .thenReturn(UserAuthDetails(User("user1", "")))
    }

    private fun mockTokenValidity() {
        lenient().`when`(tokenService.validateAccessToken("mytoken"))
            .thenReturn(TokenService.ValidatedToken("user1"))
    }

    private fun filterRequestWithAuthorizationHeader(authorizationHeader: String?) {
        val request = mock(HttpServletRequest::class.java)
        `when`(request.getHeader("Authorization")).thenReturn(authorizationHeader)

        jwtAuthenticationFilter.doFilter(
            request,
            mock(HttpServletResponse::class.java),
            mock(FilterChain::class.java)
        )
    }

    private fun assertThatRequestIsAuthorized() {
        assertNotNull(SecurityContextHolder.getContext().authentication)
    }

    private fun assertThatRequestIsNotAuthorized() {
        assertNull(SecurityContextHolder.getContext().authentication)
    }
}