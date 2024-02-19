package adamczykt.e2efileserver.auth.rest

import adamczykt.e2efileserver.auth.rest.model.AuthRequest
import adamczykt.e2efileserver.auth.rest.model.AuthResponse
import adamczykt.e2efileserver.auth.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {
    @PostMapping
    fun authenticate(@RequestBody authRequest: AuthRequest): ResponseEntity<AuthResponse> =
        authService.validateCredentialsAndGenerateTokenSet(authRequest.username, authRequest.password)
            ?. let { ResponseEntity.ok(AuthResponse(it)) }
            ?: ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
}