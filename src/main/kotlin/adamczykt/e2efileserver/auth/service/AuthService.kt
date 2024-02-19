package adamczykt.e2efileserver.auth.service

import adamczykt.e2efileserver.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val tokenService: TokenService,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun validateCredentialsAndGenerateTokenSet(username: String, password: String): String? =
        userRepository.findByUsername(username)
            ?.takeIf { passwordEncoder.matches(password, it.password) }
            ?.let { tokenService.generateAccessToken(it) }
}