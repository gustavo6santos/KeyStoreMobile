package pt.ipca.keystore.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Specs(
    val cpuModel: String,
    val gpuModel: String,
    val ram: Int,
    val osType: String
):Parcelable {
    constructor(): this("","",0,"")
}
