package com.example.projectmanagement.security

import com.example.projectmanagement.models.User
import io.jsonwebtoken.*
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val userDetailsService: UserDetailsService // Для получения UserDetails при валидации
) {

    @Value("\${jwt.secret}") // Секретный ключ, хранится в application.properties
    private lateinit var secretKeyString: String

    @Value("\${jwt.expiration}") // Время жизни токена в миллисекундах
    private var validityInMilliseconds: Long = 3600000 // 1 час по умолчанию

    private lateinit var secretKey: SecretKey

    @PostConstruct
    protected fun init() {
        // Генерируем безопасный ключ из строки. Длина строки должна быть достаточной для выбранного алгоритма.
        // Для HS256 (HMAC-SHA256) рекомендуется ключ не менее 256 бит (32 байта в Base64).
        // ВНИМАНИЕ: Этот секретный ключ должен быть СИЛЬНЫМ и ХРАНИТЬСЯ БЕЗОПАСНО в продакшене (например, в переменных окружения).
        // Для примера: "MyVeryStrongAndSuperSecretKeyForJWTGenerationWhichIsLongEnough"
        secretKey = Keys.hmacShaKeyFor(secretKeyString.toByteArray())
    }

    fun createToken(login: String, userId: Long /*, roles: List<String> */): String {
        val now = Date()
        val validity = Date(now.time + validityInMilliseconds)

        val claimsMap = mutableMapOf<String, Any>()
        claimsMap["userId"] = userId
        // if (roles.isNotEmpty()) {
        //     claimsMap["roles"] = roles
        // }
        // Вы можете добавлять и другие стандартные Claims здесь, если нужно, например, 'iss' (issuer)
        // claimsMap[Claims.ISSUER] = "your-app-name" 

        return Jwts.builder()
            .setClaims(claimsMap) // Устанавливаем все наши кастомные клэймы как карту
            .setSubject(login)    // Стандартный клэйм "sub"
            .setIssuedAt(now)     // Стандартный клэйм "iat"
            .setExpiration(validity)// Стандартный клэйм "exp"
            .signWith(secretKey)  // Для jjwt 0.12+ signWith(Key) предпочтительнее, алгоритм выведется из типа ключа (HS256 для HmacKey)
                                // Если вы хотите явно указать алгоритм: .signWith(secretKey, Jwts.SIG.HS256) <-- используйте Jwts.SIG
            .compact()
    }

    fun getAuthentication(token: String): Authentication {
        val userDetails = userDetailsService.loadUserByUsername(getLogin(token))
        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }

    fun getLogin(token: String): String {
        val jwtParser: JwtParser = Jwts.parser().setSigningKey(secretKey).build()
        return jwtParser.parseClaimsJws(token).body.subject
    }

    fun getUserId(token: String): Long? {
        return try {
            val jwtParser: JwtParser = Jwts.parser().setSigningKey(secretKey).build()
            val claims = jwtParser.parseClaimsJws(token).body
            claims.get("userId", java.lang.Long::class.java)?.toLong()
        } catch (e: Exception) {
            println("Could not get userId from token: ${e.message}")
            null // <--- ЯВНО ВОЗВРАЩАЕМ NULL
        }
    }

    fun resolveToken(req: HttpServletRequest): String? {
        val bearerToken = req.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }

    fun validateToken(token: String): Boolean {
        try {
            val jwtParser: JwtParser = Jwts.parser().setSigningKey(secretKey).build()
            val claimsJws: Jws<Claims> = jwtParser.parseClaimsJws(token)
            return !claimsJws.body.expiration.before(Date())
        } catch (e: JwtException) { /* ... */ }
        catch (e: IllegalArgumentException) { /* ... */ }
        return false
    }
}
