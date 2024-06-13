package pt.ipca.keystore.viewmodel

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import pt.ipca.keystore.R
import pt.ipca.keystore.data.CartProduct
import pt.ipca.keystore.data.Product
import pt.ipca.keystore.data.Specs
import pt.ipca.keystore.firebase.FirebaseCommon
import pt.ipca.keystore.util.Resource
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    application: Application,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommon
) : AndroidViewModel(application) {

    private val _addToCart = MutableStateFlow<Resource<CartProduct>>(Resource.Unspecified())
    private val _checkCompatibility = MutableStateFlow<Resource<String>>(Resource.Unspecified())
    val checkCompatibility: StateFlow<Resource<String>> = _checkCompatibility.asStateFlow()
    val addToCart = _addToCart.asStateFlow()

    fun addUpdateProductInCart(cartProduct: CartProduct) {
        viewModelScope.launch { _addToCart.emit(Resource.Loading()) }

        firestore.collection("user").document(auth.uid!!)
            .collection("cart")
            .whereEqualTo("product.id", cartProduct.product.id).get()
            .addOnSuccessListener {
                it.documents.let {
                    if (it.isEmpty()) {
                        addNewProduct(cartProduct)
                    } else {
                        val product = it.first().toObject(CartProduct::class.java)
                        if (product == cartProduct) {
                            val documentId = it.first().id
                            increaseQuantity(documentId, cartProduct)
                        } else {
                            addNewProduct(cartProduct)
                        }
                    }
                }
            }.addOnFailureListener {
                viewModelScope.launch { _addToCart.emit(Resource.Error(it.message.toString())) }
            }
    }

    private fun addNewProduct(cartProduct: CartProduct) {
        firebaseCommon.addProductToCart(cartProduct) { addedProduct, e ->
            viewModelScope.launch {
                if (e == null)
                    _addToCart.emit(Resource.Success(addedProduct!!))
                else
                    _addToCart.emit(Resource.Error(e.message.toString()))
            }
        }
    }

    private fun increaseQuantity(documentId: String, cartProduct: CartProduct) {
        firebaseCommon.increaseQuantity(documentId) { _, e ->
            viewModelScope.launch {
                if (e == null)
                    _addToCart.emit(Resource.Success(cartProduct))
                else
                    _addToCart.emit(Resource.Error(e.message.toString()))
            }
        }
    }

    fun checkCompatibility(userSpecs: Specs, product: Product) {
        viewModelScope.launch {
            _checkCompatibility.value = Resource.Loading()
            try {
                val resources = getApplication<Application>().resources
                val cpuBenchmarks = loadBenchmarksFromXml(resources, R.xml.specs, "cpu_model_options")
                val gpuBenchmarks = loadBenchmarksFromXml(resources, R.xml.specs, "gpu_model_options")

                val userCpuBenchmark = cpuBenchmarks[userSpecs.cpuModel] ?: 0
                val userGpuBenchmark = gpuBenchmarks[userSpecs.gpuModel] ?: 0
                val productCpuBenchmark = cpuBenchmarks[product.cpuModel] ?: 0
                val productGpuBenchmark = gpuBenchmarks[product.gpuModel] ?: 0

                val cpuScore = getScore(userCpuBenchmark, productCpuBenchmark)
                val gpuScore = getScore(userGpuBenchmark, productGpuBenchmark)
                val ramScore = getScore(userSpecs.ram, product.ram ?: 0)
                val osScore = if (userSpecs.osType == product.osType) 5 else 1

                val overallScore = (cpuScore + gpuScore + ramScore + osScore) / 4

                val compatibilityMessage = when (overallScore) {
                    5 -> "Highly Recommended"
                    4 -> "Recommended"
                    3 -> "Compatible"
                    2 -> "Possibly Compatible"
                    else -> "Not Recommended"
                }

                _checkCompatibility.value = Resource.Success(compatibilityMessage)
            } catch (e: Exception) {
                _checkCompatibility.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
            }
        }
    }

    private fun getScore(userValue: Int, gameValue: Int): Int {
        val ratio = userValue.toDouble() / gameValue
        return when {
            ratio >= 1.5 -> 5
            ratio >= 1.2 -> 4
            ratio >= 1.0 -> 3
            ratio >= 0.8 -> 2
            else -> 1
        }
    }

    private fun loadBenchmarksFromXml(resources: Resources, xmlResource: Int, arrayName: String): Map<String, Int> {
        val benchmarks = mutableMapOf<String, Int>()
        val parser = resources.getXml(xmlResource)
        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "string-array" && parser.getAttributeValue(null, "name") == arrayName) {
                parser.next()
                while (parser.eventType != XmlPullParser.END_TAG || parser.name != "string-array") {
                    if (parser.eventType == XmlPullParser.START_TAG && parser.name == "item") {
                        val name = parser.getAttributeValue(null, "name")
                        val bench = parser.getAttributeValue(null, "bench").toIntOrNull() ?: 0
                        benchmarks[name] = bench
                    }
                    parser.next()
                }
            }
            eventType = parser.next()
        }
        return benchmarks
    }
}
