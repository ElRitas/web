package org.example.utils

import net.datafaker.Faker
import kotlinx.coroutines.runBlocking
import models.*
import repositories.v1.DBCheckpointRepository
import repositories.v1.DBReservationRepository
import repositories.v1.DBResidentialRepository
import repositories.v1.DBSlotRepository
import repositories.v1.DBTransportRepository
import repositories.v1.DBUserRepository
import repositories.v1.DBUserTransportRepository
import java.util.*
import kotlin.random.Random

object DatabaseFaker {
    private val faker = Faker(Locale("ru"))
    private val userRepository = DBUserRepository()
    private val transportRepository = DBTransportRepository()
    private val slotRepository = DBSlotRepository()
    private val reservationRepository = DBReservationRepository()
    private val checkpointRepository = DBCheckpointRepository()
    private val residentialRepository = DBResidentialRepository()
    private val userTransportRepository = DBUserTransportRepository()

    fun populateDatabase() = runBlocking {
        val residentials = createResidentials(5)
        val users = createUsers(10, residentials.map { it.id })
        val transports = createTransports(10)
        val slots = createSlots(10, residentials.map { it.id })
        createReservations(5, slots, transports)
        createCheckpoints(3, residentials.map { it.id })
        linkUsersWithTransports(users, transports)

        println("База данных успешно заполнена тестовыми данными!")
    }

    private suspend fun createResidentials(count: Int): List<Residential> {
        return List(count) {
            val residential = Residential(
                id = UUID.randomUUID().toString(),
                name = faker.address().cityName(),
                address = "${faker.address().streetName()}, ${faker.address().buildingNumber()}",
                type = ParkingType.entries.random(),
                slotsNumber = Random.nextInt(10, 100)
            )
            residentialRepository.addResidential(residential)
            residential
        }
    }

    private suspend fun createUsers(count: Int, residentialIds: List<String>): List<User> {
        return List(count) {
            val user = User(
                id = UUID.randomUUID().toString(),
                fio = "${faker.name().lastName()} ${faker.name().firstName()} ${faker.name().firstName()}",
                email = faker.internet().emailAddress(),
                password = faker.internet().password(),
                residentialId = residentialIds.random(),
                role = UserRole.entries.random()
            )
            userRepository.addUser(user)
            user
        }
    }

    private suspend fun createTransports(count: Int): List<Transport> {
        return List(count) {
            val transport = Transport(
                id = UUID.randomUUID().toString(),
                number = generateRussianCarNumber(),
                model = faker.company().name(),
                color = faker.color().name(),
                insurance = Random.nextBoolean()
            )
            transportRepository.addTransport(transport)
            transport
        }
    }

    private suspend fun createSlots(count: Int, residentialIds: List<String>): List<Slot> {
        return List(count) {
            val slot = Slot(
                id = UUID.randomUUID().toString(),
                residential = residentialIds.random(),
                number = Random.nextInt(1, 500),
                status = SlotStatus.entries.random()
            )
            slotRepository.addSlot(slot)
            slot
        }
    }

    private suspend fun createReservations(count: Int, slots: List<Slot>, transports: List<Transport>): List<Reservation> {
        return List(count) {
            val reservation = Reservation(
                id = UUID.randomUUID().toString(),
                slotId = slots.random().id,
                transportId = transports.random().id,
                status = ReservationStatus.entries.random()
            )
            reservationRepository.addReservation(reservation)
            reservation
        }
    }

    private suspend fun createCheckpoints(count: Int, residentialIds: List<String>): List<Checkpoint> {
        return List(count) {
            val checkpoint = Checkpoint(
                id = UUID.randomUUID().toString(),
                guardFio = "${faker.name().lastName()} ${faker.name().firstName()} ${faker.name().firstName()}",
                guardPhone = faker.phoneNumber().phoneNumber(),
                residentialId = residentialIds.random(),
                number = Random.nextInt(1, 50).toString(),
                status = CheckpointStatus.entries.random()
            )
            checkpointRepository.addCheckpoint(checkpoint)
            checkpoint
        }
    }

    private suspend fun linkUsersWithTransports(users: List<User>, transports: List<Transport>): List<UserTransport> {
        return users.map { user ->
            val userTransport = UserTransport(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                transportId = transports.random().id
            )
            userTransportRepository.addUserTransport(userTransport)
            userTransport
        }
    }

    private fun generateRussianCarNumber(): String {
        val letters = "АВЕКМНОРСТУХ"
        val region = Random.nextInt(1, 199).toString().padStart(2, '0')
        return "${letters.random()}${Random.nextInt(0, 9)}${Random.nextInt(0, 9)}${Random.nextInt(0, 9)}${letters.random()}${letters.random()} ${region}"
    }
}
