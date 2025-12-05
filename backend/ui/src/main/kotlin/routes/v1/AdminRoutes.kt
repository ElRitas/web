package org.example.routes.v1

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import IAdminService
import errors.RepositoryException
import io.ktor.server.request.receive

fun Route.adminRoutesV1(adminService: IAdminService) {
    route("/admin") {

        get("/reservations") {
            try {
                val reservations = adminService.getAllReservations()
                call.respond(reservations)
            } catch (e: RepositoryException) {
                call.respond(HttpStatusCode.InternalServerError, "Failed to fetch reservations: ${e.message}")
            }
        }

        post("/reservation/by-slot") {
            val request = call.receive<Map<String, String>>()
            val address = request["address"]
            val number = request["number"]?.toIntOrNull()

            if (address == null || number == null) {
                return@post call.respond(HttpStatusCode.BadRequest, "Missing or invalid parameters")
            }

            try {
                val result = adminService.getReservationBySlot(address, number)
                call.respond(result)
            } catch (e: RepositoryException) {
                return@post call.respond(HttpStatusCode.NotFound)
            }
        }

    }
}
