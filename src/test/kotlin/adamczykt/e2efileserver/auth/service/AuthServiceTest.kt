package adamczykt.e2efileserver.auth.service

import adamczykt.e2efileserver.user.entity.User
import adamczykt.e2efileserver.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockitoExtension::class)
internal class AuthServiceTest {
    private val username = "myUser"
    private val invalidPassword = "invalidPassword"
    private val validPassword = "validPassword"
    private val validPasswordHash = "validPasswordHash"
    private val user = User(username, validPasswordHash)
    private val validToken = "token"

    @Mock
    private lateinit var tokenService: TokenService
    @Mock
    private lateinit var userRepository: UserRepository
    @Mock
    private lateinit var passwordEncoder: PasswordEncoder
    @InjectMocks
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        lenient().`when`(passwordEncoder.matches(eq(validPassword), eq(validPasswordHash))).thenReturn(true)
        lenient().`when`(tokenService.generateAccessToken(user)).thenReturn(validToken)
    }

    @Test
    fun validateCredentialsAndGenerateTokenSet_shouldNotGenerateTokenIfTheUserDoesNotExist() {
        // when
        val token = authService.validateCredentialsAndGenerateTokenSet(username, validPassword)

        // then
        assertNull(token)
    }

    @Test
    fun validateCredentialsAndGenerateTokenSet_shouldNotGenerateTokenIfPasswordsDoNotMatch() {
        // given
        mockUser()

        // when
        val token = authService.validateCredentialsAndGenerateTokenSet(username, invalidPassword)

        // then
        assertNull(token)
    }

    @Test
    fun validateCredentialsAndGenerateTokenSet_shouldGenerateTokenPasswordsIfUserExistsAndPasswordsMatch() {
        // given
        mockUser()

        // when
        val token = authService.validateCredentialsAndGenerateTokenSet(username, validPassword)

        // then
        assertEquals(validToken, token)
    }

    private fun mockUser() {
        `when`(userRepository.findByUsername(username)).thenReturn(user)
    }
}