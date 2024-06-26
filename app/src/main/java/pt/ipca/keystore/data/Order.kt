package pt.ipca.keystore.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random.Default.nextLong
@Parcelize
data class Order (
    val totalPrice: Float = 0f,
    val products: List<CartProduct> = emptyList(),
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()),
    val orderId: Long = nextLong(0, 100_000_000_000) + totalPrice.toLong(),
    //val gameKey: String = generateGameKey() // Add this line
) : Parcelable {
    /*companion object {
        fun generateGameKey(): String {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            return (1..4).joinToString("-") {
                (1..4).map { chars.random() }.joinToString("")
            }
        }
    }

     */
}
