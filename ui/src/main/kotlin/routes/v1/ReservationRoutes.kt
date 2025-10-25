package org.example.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import IReservationService

fun Route.reservationRoutesV1(reservationService: IReservationService) {
    route("/reservation") {

        post("/create") {
            try {
                val data = call.receive<Map<String, String>>()
                val residential = data["residential"] ?: return@post call.respondText(
                    "Missing residential", status = HttpStatusCode.BadRequest)
                val number = data["number"] ?: return@post call.respondText(
                    "Missing number", status = HttpStatusCode.BadRequest)
                val transportNumber = data["transportNumber"] ?: return@post call.respondText(
                    "Missing transportNumber", status = HttpStatusCode.BadRequest)
                val transportModel = data["transportModel"] ?: return@post call.respondText(
                    "Missing transportModel", status = HttpStatusCode.BadRequest)


                reservationService.createReservation(
                    residential,
                    number.toInt(),
                    transportNumber,
                    transportModel)
                call.respondText("Reservation created")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Failed to create reservation: ${e.message}")
            }
        }

        delete("/cancel") {
            val data = call.receive<Map<String, String>>()
            val number = data["number"] ?: return@delete call.respondText(
                "Missing name", status = HttpStatusCode.BadRequest)
            val model = data["model"] ?: return@delete call.respondText(
                "Missing email", status = HttpStatusCode.BadRequest)

            try {
                reservationService.cancelReservation(number, model)
                call.respondText("Reservation cancelled")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Failed to cancel reservation: ${e.message}")
            }
        }
    }
}
