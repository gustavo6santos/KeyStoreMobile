package pt.ipca.keystore.util

import android.util.Patterns

fun validateEmail(EmailAdress:String): RegisterValidation{
    if (EmailAdress.isEmpty())
        return RegisterValidation.Failed("Email cannot be empty")

    if(!Patterns.EMAIL_ADDRESS.matcher(EmailAdress).matches())
        return RegisterValidation.Failed("Wrong Email Format")

    return RegisterValidation.Sucess

}

fun validatePassword(Password: String) : RegisterValidation {
    if(Password.isEmpty())
        return RegisterValidation.Failed("Passowrd cannot be empety")

    if(Password.length < 6)
        return RegisterValidation.Failed("Password should contains 6 char")

    return RegisterValidation.Sucess
}