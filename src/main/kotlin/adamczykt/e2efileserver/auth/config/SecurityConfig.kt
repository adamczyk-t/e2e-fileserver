package adamczykt.e2efileserver.auth.config

import adamczykt.e2efileserver.auth.model.UserAuthDetails
import adamczykt.e2efileserver.auth.service.TokenService
import adamczykt.e2efileserver.user.repository.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
internal class SecurityConfig {
    @Bean
    fun filterChain(
        http: HttpSecurity,
        authManager: AuthenticationManager,
        jwtAuthenticationFilter: JwtAuthenticationFilter
    ): SecurityFilterChain {
        http.invoke {
            authorizeHttpRequests {
                authorize("/api/auth", permitAll)
                authorize("/api/**", authenticated)
            }

            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }

            authenticationManager = authManager

            addFilterBefore<UsernamePasswordAuthenticationFilter>(jwtAuthenticationFilter)

            httpBasic {  }

            csrf { disable() }
            cors { disable() }
        }

        return http.build()
    }

    @Bean
    fun userDetailsService(userRepository: UserRepository) = UserDetailsService { username ->
        userRepository.findByUsername(username)?.let { UserAuthDetails(it) }
    }

    @Bean
    fun jwtAuthFilter(userDetailsService: UserDetailsService, tokenService: TokenService) =
        JwtAuthenticationFilter(userDetailsService, tokenService)

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun authenticationProvider(
        userDetailsService: UserDetailsService,
        passwordEncoder: PasswordEncoder
    ): AuthenticationProvider {
        return DaoAuthenticationProvider().apply {
            setPasswordEncoder(passwordEncoder)
            setUserDetailsService(userDetailsService)
        }
    }

    @Bean
    fun authenticationManager(authenticationProvider: AuthenticationProvider): AuthenticationManager =
        ProviderManager(authenticationProvider)
}