package org.example

import com.typesafe.config.ConfigFactory
import config.configureSerialization
import di.DIContainer
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import DatabaseFactory
import IAdminService
import ICheckpointPassService
import ICheckpointServiceV2
import IReservationService
import IReservationServiceV2
import ISlotService
import ITransportService
import ITransportServiceV2
import IUserService
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.get
import org.slf4j.LoggerFactory

fun Application.module() {
    configureSerialization()

    val userService = get<IUserService>(IUserService::class.java)
    val transportService = get<ITransportService>(ITransportService::class.java)
    val adminService = get<IAdminService>(IAdminService::class.java)
    val reservationService = get<IReservationService>(IReservationService::class.java)
    val checkpointPassService = get<ICheckpointPassService>(ICheckpointPassService::class.java)

    val slotService = get<ISlotService>(ISlotService::class.java)
    val transportServiceV2 = get<ITransportServiceV2>(ITransportServiceV2::class.java)
    val checkpointServiceV2 = get<ICheckpointServiceV2>(ICheckpointServiceV2::class.java)
    val reservationServiceV2 = get<IReservationServiceV2>(IReservationServiceV2::class.java)

    configureSecurity()

    configureRouting(
        userService = userService,
        transportService = transportService,
        adminService = adminService,
        reservationService = reservationService,
        checkpointPassService = checkpointPassService,
        slotService = slotService,
        transportServiceV2 = transportServiceV2,
        checkpointServiceV2 = checkpointServiceV2,
        reservationServiceV2 = reservationServiceV2
    )
}

fun main() {
    val config = ConfigFactory.load("application.conf")
    val logger = LoggerFactory.getLogger("org.example.Main")

    try {
        DatabaseFactory.init(
            url = config.getString("database.url"),
            driver = config.getString("database.driver"),
            user = config.getString("database.user"),
            passwd = config.getString("database.password")
        )
    } catch (_: Exception) {
        logger.error("Ошибка при подключении к базе данных.")
        return
    }

    try {
        startKoin {
            modules(DIContainer)
        }
    } catch (e: Exception) {
        logger.error("Ошибка при запуске Koin.", e)
        return
    }

    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}
