import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection

object DatabaseFactory {
    private lateinit var dataSource: HikariDataSource

    fun init(url: String, driver: String, user: String, passwd: String) {
        val config = HikariConfig().apply {
            jdbcUrl = url
            driverClassName = driver
            username = user
            password = passwd

            maximumPoolSize = 10          // максимум 10 подключений
            minimumIdle = 1               // только 1 подключение в простое
            idleTimeout = 10000           // через сколько ms закрывать неиспользуемые соединения
            maxLifetime = 1800000         // максимум времени жизни соединения (30 минут)
            connectionTimeout = 5000      // таймаут ожидания соединения из пула
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }

        dataSource = HikariDataSource(config)
    }

    fun getConnection(): Connection = dataSource.connection

    suspend fun <T> dbQuery(block: (Connection) -> T): T =
        withContext(Dispatchers.IO) {
            getConnection().use { connection ->
                try {
                    val result = block(connection)
                    connection.commit()
                    result
                } catch (e: Exception) {
                    connection.rollback()
                    throw e
                }
            }
        }
}
