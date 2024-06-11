package pt.ipca.keystore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipca.keystore.data.Category
import pt.ipca.keystore.data.Product
import pt.ipca.keystore.util.Resource

class CategoryModel constructor(
    private val firestore: FirebaseFirestore,
    private val category: Category
): ViewModel() {

    private val _offerProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val offerProducts = _offerProducts.asStateFlow()

    private val _bestProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val bestProducts = _bestProducts.asStateFlow()

    init {
        fetchOfferProducts()
        fetchBestProducts()

    }

    fun fetchOfferProducts() {

        viewModelScope.launch {
                    _offerProducts.emit(Resource.Loading())

        }
        firestore.collection("Products")
            .whereEqualTo("category", category.category)
            .get()
            .addOnSuccessListener {
                val products = it.toObjects(Product::class.java)
                viewModelScope.launch {
                    _offerProducts.emit(Resource.Success(products))
                }
            }.addOnFailureListener{
                viewModelScope.launch{
                    _offerProducts.emit(Resource.Error(it.message.toString()))
                }

            }
    }

    fun fetchBestProducts() {

        viewModelScope.launch {
            _bestProducts.emit(Resource.Loading())

        }
        firestore.collection("Products")
            .whereEqualTo("category", category.category)
            .get()
            .addOnSuccessListener {
                val products = it.toObjects(Product::class.java)
                viewModelScope.launch {
                    _bestProducts.emit(Resource.Success(products))
                }
            }.addOnFailureListener{
                viewModelScope.launch{
                    _bestProducts.emit(Resource.Error(it.message.toString()))
                }

            }
    }
}