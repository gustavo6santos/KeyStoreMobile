package pt.ipca.keystore.viewmodel

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestoreSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipca.keystore.data.Order
import pt.ipca.keystore.util.Resource
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
): ViewModel() {
    private val _order = MutableStateFlow<Resource<Order>>(Resource.Unspecified())
    val order = _order.asStateFlow()

    fun placeOrder(order: Order) {
        viewModelScope.launch {
            _order.emit(Resource.Loading())
        }
        firestore.runBatch{batch ->

            //guardar order no user
            firestore.collection("user")
                .document(auth.uid!!)
                .collection("orders")
                .document()
                .set(order)


            //guarder order nas orders
            firestore.collection("orders").document().set(order)

            //remover produtos do carrinho quando feito uma compra

            firestore.collection("user").document(auth.uid!!).collection("cart").get()
                .addOnSuccessListener {
                    it.documents.forEach{
                        it.reference.delete()
                    }
                }
        }.addOnSuccessListener {
            viewModelScope.launch {
                _order.emit(Resource.Success(order))
            }
        }.addOnFailureListener{
            viewModelScope.launch {
                _order.emit(Resource.Error(it.message.toString()))
            }
        }

    }
}