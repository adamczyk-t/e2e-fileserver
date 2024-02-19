package adamczykt.e2efileserver.auth.service

import adamczykt.e2efileserver.user.entity.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Clock
import java.util.*
import javax.crypto.spec.SecretKeySpec

@Service
class TokenService(
    @Value("\${application.security.token.key}") key: String,
    @Value("\${application.security.token.expirationMs}") private val tokenDurationMs: Long,
    private val clock: Clock,
) {
    class ValidatedToken(val username: String)

    private val secretKey = SecretKeySpec(Base64.getDecoder().decode(key), 0, 32, "AES")

    fun validateAccessToken(token: String): ValidatedToken? =
        extractClaims(token)
            ?.let { ValidatedToken(it.subject) }

    fun generateAccessToken(user: User): String =
        Jwts.builder()
            .encryptWith(secretKey, Jwts.ENC.A256GCM)
            .subject(user.username)
            .issuedAt(Date.from(clock.instant()))
            .expiration(Date.from(clock.instant().plusMillis(tokenDurationMs)))
            .compact()

    private fun extractClaims(token: String): Claims? = runCatching {
        Jwts.parser()
            .decryptWith(secretKey)
            .clock { Date.from(clock.instant()) }
            .build()
            .parseEncryptedClaims(token).payload
    }.getOrNull()
}
