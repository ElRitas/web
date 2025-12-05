package org.example.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.User
import models.UserRole
import IUserService

fun Route.userRoutesV1(userService: IUserService) {
    route("/user") {

        post("/register") {
            try {
                val userAndAddress = call.receive<Map<String, String>>()
                val name = userAndAddress["name"] ?: return@post call.respondText("Missing name", status = HttpStatusCode.BadRequest)
                val email = userAndAddress["email"] ?: return@post call.respondText("Missing email", status = HttpStatusCode.BadRequest)
                val password = userAndAddress["password"] ?: return@post call.respondText("Missing password", status = HttpStatusCode.BadRequest)
                val address = userAndAddress["address"] ?: return@post call.respondText("Missing address", status = HttpStatusCode.BadRequest)

                val user = User(fio=name, email=email, password=password,
                    residentialId="", role= UserRole.RESIDENT)
                userService.register(user, address)
                call.respond(user)
//                call.respondText("User registered")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Registration failed: ${e.message}")
            }
        }

        post("/guest") {
            try {
                val guest = call.receive<User>()
                userService.addGuest(guest)
                call.respondText("Guest added")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Guest adding failed: ${e.message}")
            }
        }

        post("/authorize") {
            try {
                val credentials = call.receive<Map<String, String>>()
                val email = credentials["email"] ?: return@post call.respondText("Missing email", status = HttpStatusCode.BadRequest)
                val password = credentials["password"] ?: return@post call.respondText("Missing password", status = HttpStatusCode.BadRequest)

                val user = userService.authorize(email, password)
                if (user != null) {
                    call.respond(user)
                } else {
                    call.respondText("Invalid credentials", status = HttpStatusCode.Unauthorized)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Authorization failed: ${e.message}")
            }
        }
    }
}
