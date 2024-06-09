package pt.ipca.keystore.data

data class User(
    val username: String,
    val email: String
){
    constructor(): this("","")
}
