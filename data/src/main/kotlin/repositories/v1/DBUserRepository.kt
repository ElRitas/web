package repositories.v1

import UserRepository
import DatabaseFactory.dbQuery
import errors.RepositoryException
import models.User
import models.UserRole
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

class DBUserRepository : UserRepository {

    private val logger = LoggerFactory.getLogger(DBUserRepository::class.java)

    override suspend fun addUser(user: User) {
        logger.info("Adding new user: $user")
        dbQuery { connection ->
            try {
                logger.info("Start adding")
                val query = "INSERT INTO users(id, fio, email, password, residential_id, role) " +
                        "VALUES (?, ?, ?, ?, ?, ?)"
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(user.id))
                    statement.setString(2, user.fio)
                    statement.setString(3, user.email)
                    statement.setString(4, user.password)
                    statement.setObject(5, UUID.fromString(user.residentialId))
                    statement.setString(6, user.role.name)
                    statement.executeUpdate()
                    logger.info("User added successfully with id=${user.id}")
                }
            } catch (e: SQLException) {
                logger.info("Failed to add user: ${e.message}", e)
                if (e.sqlState == "23505") {
                    throw RepositoryException.AlreadyExistsException(
                        "User with id=${user.id} or email=${user.email} already exists")
                }
                throw RepositoryException.NoDataAccessException("Database error while adding user: ${e.message}")
            } catch (e: Exception) {
                logger.info("Fialed to add user: ${e.message}", e)
            }
            logger.info("Super good success")
        }
    }

    override suspend fun deleteUser(user: User) {
        logger.info("Deleting user with id=${user.id}")
        dbQuery { connection ->
            try {
                val query = "DELETE FROM users WHERE id = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(user.id))
                    val rowsAffected = statement.executeUpdate()
                    if (rowsAffected == 0) {
                        logger.warn("User not found for deletion: ${user.id}")
                        throw RepositoryException.NotFoundException("User with id=${user.id} not found")
                    }
                    logger.debug("User deleted successfully: ${user.id}")
                }
            } catch (e: SQLException) {
                logger.error("Error deleting user: ${e.message}", e)
                throw RepositoryException.NoDataAccessException("Database error while deleting user: ${e.message}")
            }
        }
    }

    override suspend fun getUserByFIO(fio: String): User {
        logger.info("Retrieving user by FIO='$fio'")
        return dbQuery { connection ->
            try {
                val query = "SELECT * FROM users WHERE fio = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, fio)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val user = resultSet.toUser()
                        logger.debug("User found: $user")
                        user
                    } else {
                        logger.warn("No user found for FIO='$fio'")
                        throw RepositoryException.NotFoundException("User with FIO '$fio' not found")
                    }
                }
            } catch (e: SQLException) {
                logger.error("Error retrieving user: ${e.message}", e)
                throw RepositoryException.NoDataAccessException("Database error while retrieving user: ${e.message}")
            }
        }
    }

    override suspend fun getUserByEmailPassword(email: String, password: String): User {
        logger.info("Retrieving user by email='$email' and password")
        return dbQuery { connection ->
            try {
                val query = "SELECT * FROM users WHERE email = ? AND password = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, email)
                    statement.setString(2, password)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val user = resultSet.toUser()
                        logger.debug("User found: $user")
                        user
                    } else {
                        logger.warn("No user found for email='$email' or wrong password")
                        throw RepositoryException.NotFoundException(
                            "User with email '$email' not found or wrong password")
                    }
                }
            } catch (e: SQLException) {
                logger.error("Error retrieving user: ${e.message}", e)
                throw RepositoryException.NoDataAccessException("Database error while retrieving user: ${e.message}")
            }
        }
    }

    private fun ResultSet.toUser(): User {
        return User(
            id = getString("id"),
            fio = getString("fio"),
            email = getString("email"),
            password = getString("password"),
            residentialId = getString("residential_id"),
            role = UserRole.valueOf(getString("role"))
        )
    }
}
