package org.example.routes.v2

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import ISlotService
import SlotAPI
import SlotResponseAPI
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import models.SlotResponse
import paginationConvert

fun Route.slotRoutesV2(slotService: ISlotService) {
    route("/slots") {
        authenticate("auth-jwt") {
            get {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.getClaim("role", String::class)
                    val resId = principal?.getClaim("residentialId", String::class)
                        ?: ""
                    if (role == UserRoleAPI.GUEST.toString())
                        return@get call.respond(
                            HttpStatusCode.Forbidden,
                            mapOf("error" to "Access denied: GUEST role not allowed")
                        )
                    val page = call.parameters["pagesNum"]?.toInt() ?: 0
                    val count = call.parameters["elemCount"]?.toInt() ?: 0
                    val response = slotService.getSlots(resId, page, count)
                    call.respond(convertResponse(response))
                } catch (e: Exception) {
                    return@get call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "$e")
                    )
                }
            }

            post {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.getClaim("role", String::class)
                    if (role != UserRoleAPI.ADMIN.toString())
                        call.respond(
                            HttpStatusCode.InternalServerError, "Wrong role"
                        )
                    val requestBody = call.receive<Map<String, String>>()
                    val id = requestBody["residentialId"] ?: return@post call.respondText(
                        "Missing id", status = HttpStatusCode.BadRequest
                    )
                    val number = requestBody["slotNumber"]?.toIntOrNull() ?: return@post call.respondText(
                        "Missing number", status = HttpStatusCode.BadRequest
                    )
                    val slotId = slotService.addSlot(id, number)
                    call.respond(
                        status = HttpStatusCode.Created,
                        hashMapOf("slotId" to slotId))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError, "Registration failed: ${e.message}")
                }
            }
        }
    }
}

private fun convertResponse(response: SlotResponse): SlotResponseAPI {
    val list = mutableListOf<SlotAPI>()
    for (slot in response.slots) {
        list.add(SlotAPI(
            slot.id,
            slot.number,
            SlotStatusAPI.valueOf(slot.status.toString())
        ))
    }
    return SlotResponseAPI(list, paginationConvert(response.pagination))
}
