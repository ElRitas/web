package org.example.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ICheckpointPassService

fun Route.checkpointRoutesV1(checkpointService: ICheckpointPassService) {
    route("/checkpoint") {

        post("/pass") {
            try {
                val request = call.receive<Map<String, String>>()
                val address = request["address"]
                val checkpoint = request["checkpoint"]
                val fio = request["fio"]

                if (address == null || checkpoint == null || fio == null)
                    return@post call.respondText("Missing parameters", status = HttpStatusCode.BadRequest)

                val passed = checkpointService.passCheckpoint(address, checkpoint, fio)
                call.respond(mapOf("passed" to passed))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Failed to process checkpoint pass: ${e.message}")
            }
        }
    }
}
