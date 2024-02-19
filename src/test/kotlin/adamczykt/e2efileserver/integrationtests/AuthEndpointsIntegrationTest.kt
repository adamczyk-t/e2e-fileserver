package adamczykt.e2efileserver.integrationtests

import adamczykt.e2efileserver.auth.rest.model.AuthRequest
import adamczykt.e2efileserver.auth.rest.model.AuthResponse
import adamczykt.e2efileserver.auth.service.TokenService
import adamczykt.e2efileserver.user.entity.User
import adamczykt.e2efileserver.user.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
internal class AuthEndpointsIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var tokenService: TokenService

    @Test
    @DirtiesContext
    fun authenticate_shouldRespondWithBadRequestStatus_WhenRequestBodyIsInvalid() {
        mockMvc.perform(
            post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"userWithoutPassword\"}")
        ).andExpect(status().isBadRequest)
    }

    @DirtiesContext
    @ParameterizedTest
    @CsvSource("test-user-1", "test-user-2")
    fun authenticate_shouldRespondWithOkStatusAndTokens_WhenCredentialsAreValid(username: String) {
        // given
        val password = "test1"
        val user = User(username, passwordEncoder.encode(password))
        userRepository.save(user)

        // then
        mockMvc.perform(
            post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(AuthRequest(username, password)))
        ).andExpect(status().isOk)
            .andExpect(validTokensForUser(user))
    }

    @Test
    fun authenticate_shouldRespondWithUnauthorizedStatus_WhenCredentialsAreInvalid() {
        // given
        val username = "test-user"
        val password = "test1"
        val badPassword = "test2"
        val user = User(username, password)
        userRepository.save(user)

        // then
        mockMvc.perform(
            post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(AuthRequest(username, badPassword)))
        ).andExpect(status().isUnauthorized)
    }

    private fun validTokensForUser(user: User): ResultMatcher {
        return ResultMatcher {
            val response = objectMapper.readValue(it.response.contentAsByteArray, AuthResponse::class.java)
            val token = tokenService.validateAccessToken(response.token)
            assert(token != null)
            assert(token?.username == user.username)
        }
    }
}