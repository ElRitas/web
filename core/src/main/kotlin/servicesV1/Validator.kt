package servicesV1

object Validator {
    private val emailRegex = Regex("""^\w+@\w+\.\w+$""")

    fun validateEmail(email: String): Boolean {
        return emailRegex.matches(email)
    }

    fun validatePassword(password: String): Boolean {
        return password.length >= 8
    }

    fun validateFIO(fio: String): Boolean {
        return fio.split(" ").size == 3
    }
}
