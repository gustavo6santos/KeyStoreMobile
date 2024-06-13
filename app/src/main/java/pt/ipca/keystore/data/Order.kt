package pt.ipca.keystore.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random.Default.nextLong

data class Order (
    val totalPrice: Float,
    val products: List<CartProduct>,
    val gameKey: String,
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()),
    val orderId: Long = nextLong(0,100_000_000_000) + totalPrice.toLong()
) {

}