package errors

open class RepositoryException(message: String) : Throwable(message) {
    class NotFoundException(message: String) : RepositoryException(message)
    class AlreadyExistsException(message: String) : RepositoryException(message)
    class NoDataAccessException(message: String) : RepositoryException(message)
}