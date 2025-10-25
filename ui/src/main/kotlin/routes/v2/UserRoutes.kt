package org.example.routes.v2

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.User
import models.UserRole
import org.example.JwtConfig
import IUserService
import java.util.Date


fun Route.userRoutesV2(userService: IUserService, jwtConfig: JwtConfig) {
    route("/users") {
        post("/register") {
            try {
                val userAndAddress = call.receive<Map<String, String>>()
                val name = userAndAddress["name"] ?: return@post call.respondText("Missing name", status = HttpStatusCode.BadRequest)
                val email = userAndAddress["email"] ?: return@post call.respondText("Missing email", status = HttpStatusCode.BadRequest)
                val password = userAndAddress["password"] ?: return@post call.respondText("Missing password", status = HttpStatusCode.BadRequest)
                val address = userAndAddress["address"] ?: return@post call.respondText("Missing address", status = HttpStatusCode.BadRequest)

                var user = User(fio=name, email=email, password=password,
                    residentialId="", role=UserRole.RESIDENT)
                user = userService.register(user, address)
                val token = JWT.create()
                    .withSubject(user.id)
                    .withIssuer(jwtConfig.issuer)
                    .withAudience(jwtConfig.audience)
                    .withClaim("id", user.id)
                    .withClaim("fio", user.fio)
                    .withClaim("residentialId", user.residentialId)
                    .withClaim("role", user.role.name)
                    .withExpiresAt(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                    .sign(Algorithm.HMAC256(jwtConfig.secret)).toString()
                call.respond(hashMapOf("token" to token))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Registration failed: ${e.message}")
            }
        }

        post("/authorize") {
            try {
                val requestBody = call.receive<Map<String, String>>()
                val email = requestBody["email"] ?: return@post call.respondText(
                    "Missing email", status = HttpStatusCode.BadRequest
                )
                val password = requestBody["password"] ?: return@post call.respondText(
                    "Missing password", status = HttpStatusCode.BadRequest
                )
                val user = userService.authorize(email, password)
                val token = JWT.create()
                    .withSubject(user?.id)
                    .withIssuer(jwtConfig.issuer)
                    .withAudience(jwtConfig.audience)
                    .withClaim("id", user?.id)
                    .withClaim("fio", user?.fio)
                    .withClaim("residentialId", user?.residentialId)
                    .withClaim("role", user?.role?.name)
                    .withExpiresAt(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                    .sign(Algorithm.HMAC256(jwtConfig.secret)).toString()
                call.respond(hashMapOf("token" to token))
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError, "Registration failed: ${e.message}")
            }
        }

        authenticate("auth-jwt") {
            post("/guest") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val residentialId = principal?.getClaim("residentialId", String::class)
                        ?: ""
                    val role = principal?.getClaim("role", String::class)

                    if (role == UserRole.GUEST.toString())
                        call.respond(HttpStatusCode.InternalServerError, "Wrong role")
                    val requestBody = call.receive<Map<String, String>>()
                    val name = requestBody["fio"] ?: return@post call.respondText(
                        "Missing name", status = HttpStatusCode.BadRequest
                    )
                    val email = requestBody["email"] ?: return@post call.respondText(
                        "Missing email", status = HttpStatusCode.BadRequest
                    )
                    val password = requestBody["password"] ?: return@post call.respondText(
                        "Missing password", status = HttpStatusCode.BadRequest
                    )
                    val guest = User(
                        fio = name,
                        email = email,
                        password = password,
                        residentialId = residentialId,
                        role = UserRole.GUEST
                    )
                    userService.addGuest(guest)
                    call.respond(HttpStatusCode.Created, message = "Guest added")
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError, message = "Adding guest failed: ${e.message}"
                    )
                }
            }
        }
    }
}
