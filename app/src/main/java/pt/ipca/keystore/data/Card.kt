package pt.ipca.keystore.data

data class Card(
    val id: String = "",
    val name: String = "",
    val cardNumber: String = "",
    val expiryDate: String = "",
    val cvc: String = ""
)