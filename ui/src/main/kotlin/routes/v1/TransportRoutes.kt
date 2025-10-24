package routes.v1

import ITransportService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.Transport


fun Route.transportRoutesV1(transportService: ITransportService) {
    route("/transport") {

        post("/add") {
            try {
                val data = call.receive<Map<String, String>>()
                val userId = data["userId"] ?: return@post call.respondText("Missing userId", status = HttpStatusCode.BadRequest)
                val transport = Transport(
                    id = data["id"].toString(),
                    number = data["number"].toString(),
                    model = data["model"].toString(),
                    color = data["color"].toString(),
                    insurance = data["insurance"].toBoolean()
                )
                transportService.addTransport(userId, transport)
                call.respondText("Transport added")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Failed to add transport: ${e.message}")
            }
        }

        get("/get") {
            val number = call.request.queryParameters["number"]
            val model = call.request.queryParameters["model"]

            if (number == null || model == null)
                return@get call.respondText("Missing transport info", status = HttpStatusCode.BadRequest)

            try {
                val transport = transportService.getTransportByNumberAndModel(number, model)
                if (transport != null) call.respond(transport) else call.respondText("Not found", status = HttpStatusCode.NotFound)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Failed to retrieve transport: ${e.message}")
            }
        }
    }
}
