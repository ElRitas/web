package servicesV1

import IUserService
import ResidentialRepository
import UserRepository
import models.User
import errors.RepositoryException
import org.slf4j.LoggerFactory
import org.slf4j.Logger

class UserService(private val userRepo: UserRepository,
                  private val residRepo: ResidentialRepository) : IUserService {

    private val logger: Logger = LoggerFactory.getLogger(UserService::class.java)

    override suspend fun addGuest(user: User) {
        logger.info("Attempting to add new guest user: ${user.email}")

        try {
            userRepo.addUser(user)
            logger.info("Guest user ${user.email} added successfully")
        } catch (e: RepositoryException) {
            logger.error("Error adding guest user ${user.email}", e)
            throw e
        }
    }

    override suspend fun authorize(email: String, password: String): User? {
        logger.info("Attempting to authorize user with email: $email")

        if (!Validator.validateEmail(email)) {
            logger.error("Invalid email format: $email")
            throw IllegalArgumentException("Invalid email format")
        }

        if (!Validator.validatePassword(password)) {
            logger.error("Invalid password format for email: $email")
            throw IllegalArgumentException("Invalid password format")
        }

        try {
            return userRepo.getUserByEmailPassword(email, password)
        } catch (e: RepositoryException) {
            logger.error("Error authorizing user with email: $email", e)
            throw e
        }
    }

    override suspend fun register(user: User, address: String): User {
        logger.info("Attempting to register new user: ${user.email}")

        try {
            val resId: String = residRepo.getResidentialIdByAddress(address)
            val userWithResId = User(
                user.id,
                user.fio,
                user.email,
                user.password,
                resId,
                user.role)
            userRepo.addUser(userWithResId)
            logger.info("User ${user.fio} registered successfully")
            return userWithResId
        } catch (e: RepositoryException) {
            logger.error("Error registering user ${user.email}", e)
            throw e
        }
    }
}
