package pt.ipca.keystore.util

sealed class RegisterValidation() {
    object Sucess: RegisterValidation()
    data class Failed(val message: String): RegisterValidation()
}

data class RegisterFieldState(
    val  EmailAdress: RegisterValidation,
    val Password: RegisterValidation
)