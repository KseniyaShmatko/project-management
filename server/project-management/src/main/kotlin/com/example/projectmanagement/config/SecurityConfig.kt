// src/main/kotlin/com/example/projectmanagement/config/SecurityConfig.kt
package com.example.projectmanagement.config // или com.example.projectmanagement.security

import com.example.projectmanagement.security.JwtTokenFilter
import com.example.projectmanagement.security.JwtTokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity // Включает веб-безопасность Spring
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("http://localhost:3000")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
            .csrf { csrf -> csrf.disable() }
            .sessionManagement { session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Разрешаем OPTIONS запросы
                    .requestMatchers("/users/login").permitAll()            // Путь для входа
                    .requestMatchers("/users/register").permitAll()         // Путь для регистрации
                    .requestMatchers("/uploads/**").permitAll()             // ДОБАВЛЯЕМ ЭТУ СТРОКУ!
                    .requestMatchers("/users/search").authenticated()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(JwtTokenFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
