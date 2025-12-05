package org.example

import IAdminService
import ICheckpointPassService
import ICheckpointServiceV2
import com.auth0.jwt.*
import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.*
import org.example.routes.v1.*
import org.example.routes.v2.*
import IUserService
import IReservationService
import IReservationServiceV2
import ISlotService
import ITransportService
import ITransportServiceV2
import routes.v1.transportRoutesV1
import routes.v2.checkpointRoutesV2
import routes.v2.reservationRoutesV2
import routes.v2.transportRoutesV2

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String
)

fun Application.configureSecurity() {
    val config = ConfigFactory.load("application.conf")
    val jwtSecret = config.getString("jwt.secret")
    val jwtIssuer = config.getString("jwt.issuer")
    val jwtAudience = config.getString("jwt.audience")
    val jwtRealm = config.getString("jwt.realm")

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("fio").asString().isNotEmpty()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}

fun Application.configureRouting(
    userService: IUserService,
    transportService: ITransportService,
    adminService: IAdminService,
    reservationService: IReservationService,
    checkpointPassService: ICheckpointPassService,
    slotService: ISlotService,
    transportServiceV2: ITransportServiceV2,
    checkpointServiceV2: ICheckpointServiceV2,
    reservationServiceV2: IReservationServiceV2
) {
    val config = ConfigFactory.load("application.conf")
    val jwtConfig = JwtConfig(
        secret = config.getString("jwt.secret"),
        issuer = config.getString("jwt.issuer"),
        audience = config.getString("jwt.audience"),
        realm = config.getString("jwt.realm")
    )
    routing {
        swaggerUI(path = "/api/v1", swaggerFile = "swagger-v1.yaml")
        route("/api/v1") {
            userRoutesV1(userService)
            transportRoutesV1(transportService)
            adminRoutesV1(adminService)
            reservationRoutesV1(reservationService)
            checkpointRoutesV1(checkpointPassService)
        }
    }

    routing {
        swaggerUI(path = "/api/v2", swaggerFile = "swagger-v2.yaml")
        route("/api/v2") {
            userRoutesV2(userService, jwtConfig)
            transportRoutesV2(transportServiceV2)
            slotRoutesV2(slotService)
            reservationRoutesV2(reservationServiceV2, transportServiceV2, slotService)
            checkpointRoutesV2(checkpointServiceV2)
        }
    }
}
