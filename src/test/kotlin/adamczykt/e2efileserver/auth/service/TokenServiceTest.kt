package adamczykt.e2efileserver.auth.service

import adamczykt.e2efileserver.user.entity.User
import io.jsonwebtoken.Jwts
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.RuntimeException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.spec.SecretKeySpec

internal class TokenServiceTest {
    private val keyBase64 = "+LekaSYD2hUyw8YQL0GVaSvTJY5aF1crfWpyx2fHRK8="
    private val key = SecretKeySpec(Base64.getDecoder().decode(keyBase64), "AES")
    private val expirationMs = 3600_000L
    private val currentTime = Instant.ofEpochMilli(1704067200000L)
    private val clock = Clock.fixed(currentTime, ZoneId.of("UTC"))
    private val tokenService = TokenService(keyBase64, expirationMs, clock)

    @Test
    fun shouldThrowExceptionOnInitializationWithTooShortKey() {
        val shortKey = Base64.getEncoder().encodeToString(ByteArray(31))
        assertThrows<RuntimeException> { TokenService(shortKey, 1000L, clock) }
    }

    @Test
    fun shouldCreateTokenWithCorrectData() {
        // given
        val user = User("Resu", "")
        val accessToken = tokenService.generateAccessToken(user)

        // when
        val claims = Jwts.parser().decryptWith(key)
            .clock { Date.from(clock.instant().plusMillis(expirationMs / 2)) }
            .build().parseEncryptedClaims(accessToken).payload

        // then
        assertEquals("Resu", claims.subject)
        assertEquals(currentTime, claims.issuedAt.toInstant())
        assertEquals(currentTime.plusMillis(expirationMs), claims.expiration.toInstant())
    }

    @Test
    fun shouldNotValidateMalformedToken() {
        assertNull(tokenService.validateAccessToken("someMalformedToken"))
    }

    @Test
    fun shouldNotValidateExpiredToken() {
        // given
        val creationTime = currentTime.minus(1, ChronoUnit.DAYS)
        val expirationTime = creationTime.plusMillis(expirationMs)
        val accessToken = Jwts.builder()
            .encryptWith(key, Jwts.ENC.A256GCM)
            .subject("subject")
            .issuedAt(Date.from(creationTime))
            .expiration(Date.from(expirationTime))
            .compact()

        // then
        assertNull(tokenService.validateAccessToken(accessToken))
    }
}