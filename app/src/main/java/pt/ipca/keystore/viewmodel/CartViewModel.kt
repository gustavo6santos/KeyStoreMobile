package pt.ipca.keystore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipca.keystore.data.CartProduct
import pt.ipca.keystore.data.Product
import pt.ipca.keystore.firebase.FirebaseCommon
import pt.ipca.keystore.util.Resource
import javax.inject.Inject
@HiltViewModel
class CartViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommon
): ViewModel() {

    private val _cartProducts =
        MutableStateFlow<Resource<List<CartProduct>>>(Resource.Unspecified())
    val cartProducts = _cartProducts.asStateFlow()

    private var cartProductDocuments = emptyList<DocumentSnapshot>()
    init {
        getCartProducts()
    }

    private fun getCartProducts() {
        viewModelScope.launch { _cartProducts.emit(Resource.Loading()) }

        firestore.collection("user").document(auth.uid!!).collection("cart")
            .addSnapshotListener { value, error ->
                if (error != null || value == null) {
                    viewModelScope.launch { _cartProducts.emit(Resource.Error(error?.message.toString())) }
                } else {
                    cartProductDocuments = value.documents
                    val cartProducts = value.toObjects(CartProduct::class.java)
                    viewModelScope.launch { _cartProducts.emit(Resource.Success(cartProducts)) }
                }

            }
    }

    fun changeQuantity(
        cartProduct: CartProduct,
        quantityChanging: FirebaseCommon.quantityChanging
    ) {

        val index = cartProducts.value.data?.indexOf(cartProduct)


        if(index!=null &&  index!= -1){
            val documentId = cartProductDocuments[index].id
            when(quantityChanging){
                FirebaseCommon.quantityChanging.INCREASE ->{
                    increaseQuantity(documentId)
                }
                FirebaseCommon.quantityChanging.DECREASE ->{
                    descreaseQuantity(documentId)
                }
            }
        }

    }

    private fun descreaseQuantity(documentId: String) {
        firebaseCommon.decreaseQuantity(documentId){ result, exception ->
            if(exception != null)
                viewModelScope.launch { _cartProducts.emit(Resource.Error(exception.message.toString())) }


        }
    }

    private fun increaseQuantity(documentId: String) {
        firebaseCommon.increaseQuantity(documentId){ result, exception ->
            if(exception != null)
                viewModelScope.launch { _cartProducts.emit(Resource.Error(exception.message.toString())) }


        }
    }
}