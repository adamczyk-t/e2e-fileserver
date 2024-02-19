package adamczykt.e2efileserver.auth.config

import adamczykt.e2efileserver.auth.service.TokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.web.filter.OncePerRequestFilter

private const val AUTHORIZATION_HEADER = "Authorization"

internal class JwtAuthenticationFilter(
    private val userDetailsService: UserDetailsService,
    private val tokenService: TokenService
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        request.getHeader(AUTHORIZATION_HEADER)
            ?.let { extractToken(it) }
            ?.let { injectAuthenticationIntoContextIfTokenIsValid(it) }

        filterChain.doFilter(request, response)
    }

    private fun extractToken(header: String)
            = header.takeIf { it.startsWith("Bearer") }?.substring(7)

    private fun injectAuthenticationIntoContextIfTokenIsValid(token: String) {
        val username = tokenService.validateAccessToken(token)?.username
        val context = SecurityContextHolder.getContext()
        if (username == null || context.authentication != null) return

        userDetailsService.loadUserByUsername(username)?.let {
            context.authentication = UsernamePasswordAuthenticationToken(it, null, listOf())
        }
    }
}