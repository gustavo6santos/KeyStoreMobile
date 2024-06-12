package pt.ipca.keystore.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: String,
    val title: String,
    val category: String,
    val genre: String,
    val price: Float,
    val stock: Float,
    val description: String,
    val images: List<String>,
    val cpuModel: String? = null,
    val gpuModel: String? = null,
    val ram: Int? = null,
    val osType: String? = null
):Parcelable{
    constructor():this("0","","","",0f,0f,"",images = emptyList())
}