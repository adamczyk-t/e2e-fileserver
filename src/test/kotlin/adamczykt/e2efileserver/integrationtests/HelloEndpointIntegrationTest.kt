package adamczykt.e2efileserver.integrationtests

import adamczykt.e2efileserver.auth.service.TokenService
import adamczykt.e2efileserver.user.entity.User
import adamczykt.e2efileserver.user.repository.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension::class)
internal class HelloEndpointIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @MockBean
    private lateinit var tokenService: TokenService

    @Test
    fun hello_shouldRespondWithUnauthorizedStatusCode_whenRequestIsUnauthorized() {
        mockMvc
            .perform(get("/api/hello").header("Authorization", "Bearer myToken"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @DirtiesContext
    fun hello_shouldRespondWithHello_whenRequestIsAuthorized() {
        userRepository.save(User("user1", ""))
        `when`(tokenService.validateAccessToken("myToken"))
            .thenReturn(TokenService.ValidatedToken("user1"))

        mockMvc
            .perform(get("/api/hello").header("Authorization", "Bearer myToken"))
            .andExpect(status().isOk)
            .andExpect(content().string("Hello"))
    }
}