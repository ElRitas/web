package di

import CheckpointRepository
import CheckpointRepositoryV2
import IAdminService
import ICheckpointPassService
import ICheckpointServiceV2
import IReservationService
import IReservationServiceV2
import ISlotService
import ITransportService
import ITransportServiceV2
import IUserService
import ReservationRepository
import ReservationRepositoryV2
import ResidentialRepository
import SlotRepository
import SlotRepositoryV2
import TransportRepository
import TransportRepositoryV2
import UserRepository
import UserTransportRepository
import org.koin.dsl.module
import repositories.v1.DBCheckpointRepository
import repositories.v1.DBReservationRepository
import repositories.v1.DBResidentialRepository
import repositories.v1.DBSlotRepository
import repositories.v1.DBTransportRepository
import repositories.v1.DBUserRepository
import repositories.v1.DBUserTransportRepository
import repositories.v2.DBCheckpointRepositoryV2
import repositories.v2.DBReservationRepositoryV2
import repositories.v2.DBSlotRepositoryV2
import repositories.v2.DBTransportRepositoryV2
import servicesV1.*
import servicesV2.CheckpointServiceV2
import servicesV2.ReservationServiceV2
import servicesV2.SlotService
import servicesV2.TransportServiceV2

val DIContainer = module {
    // Репозитории v1
    single<UserRepository> { DBUserRepository() }
    single<TransportRepository> { DBTransportRepository() }
    single<UserTransportRepository> { DBUserTransportRepository() }
    single<ReservationRepository> { DBReservationRepository() }
    single<CheckpointRepository> { DBCheckpointRepository() }
    single<SlotRepository> { DBSlotRepository() }
    single<ResidentialRepository> { DBResidentialRepository() }

    // Сервисы v1
    single<IUserService> { UserService(get(), get()) }
    single<ITransportService> { TransportService(get(), get()) }
    single<IReservationService> { ReservationService(get(), get(), get(), get()) }
    single<IAdminService> { AdminService(get(), get(), get()) }
    single<ICheckpointPassService> { CheckpointPassService(get(), get(), get()) }

    // Репозитории v2
    single<SlotRepositoryV2> { DBSlotRepositoryV2() }
    single<TransportRepositoryV2> { DBTransportRepositoryV2() }
    single<CheckpointRepositoryV2> { DBCheckpointRepositoryV2() }
    single<ReservationRepositoryV2> { DBReservationRepositoryV2() }

    // Сервисы v2
    single<ISlotService> { SlotService(get()) }
    single<ITransportServiceV2> { TransportServiceV2(get(), get(), get()) }
    single<ICheckpointServiceV2> { CheckpointServiceV2(get()) }
    single<IReservationServiceV2> { ReservationServiceV2(get(), get(), get()) }
}