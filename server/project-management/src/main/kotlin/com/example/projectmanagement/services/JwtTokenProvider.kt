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
    private val userDetailsService: UserDetailsService
) {

    @Value("\${jwt.secret}")
    private lateinit var secretKeyString: String

    @Value("\${jwt.expiration}")
    private var validityInMilliseconds: Long = 3600000

    private lateinit var secretKey: SecretKey

    @PostConstruct
    protected fun init() {
        secretKey = Keys.hmacShaKeyFor(secretKeyString.toByteArray())
    }

    fun createToken(login: String, userId: Long /*, roles: List<String> */): String {
        val now = Date()
        val validity = Date(now.time + validityInMilliseconds)

        val claimsMap = mutableMapOf<String, Any>()
        claimsMap["userId"] = userId

        return Jwts.builder()
            .setClaims(claimsMap)
            .setSubject(login)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(secretKey)
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
            null
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
