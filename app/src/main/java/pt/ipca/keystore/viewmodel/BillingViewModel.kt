package pt.ipca.keystore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipca.keystore.data.Card
import pt.ipca.keystore.util.Resource
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _cards = MutableStateFlow<Resource<List<Card>>>(Resource.Unspecified())
    val cards = _cards.asStateFlow()

    private val _addCardResult = MutableStateFlow<Resource<Void>>(Resource.Unspecified())
    val addCardResult = _addCardResult.asStateFlow()

    private val _removeCardResult = MutableStateFlow<Resource<Void>>(Resource.Unspecified())
    val removeCardResult = _removeCardResult.asStateFlow()

    fun getCards() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            viewModelScope.launch { _cards.emit(Resource.Error("User not logged in")) }
            return
        }

        firestore.collection("user").document(userId).collection("cards")
            .get()
            .addOnSuccessListener { snapshot ->
                val cardsList = snapshot.documents.mapNotNull { doc ->
                    val card = doc.toObject(Card::class.java)
                    card?.copy(id = doc.id) // Ensure the card object has the document ID
                }
                viewModelScope.launch { _cards.emit(Resource.Success(cardsList)) }
            }
            .addOnFailureListener { exception ->
                viewModelScope.launch { _cards.emit(Resource.Error(exception.message ?: "Error fetching cards")) }
            }
    }

    fun addCard(name: String, cardNumber: String, expiryDate: String, cvc: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            viewModelScope.launch { _addCardResult.emit(Resource.Error("User not logged in")) }
            return
        }

        // Generate a new document reference
        val newDocRef = firestore.collection("user").document(userId).collection("cards").document()
        val cardWithId = Card(
            id = newDocRef.id,
            name = name,
            cardNumber = cardNumber,
            expiryDate = expiryDate,
            cvc = cvc
        )

        viewModelScope.launch {
            _addCardResult.emit(Resource.Loading())
            newDocRef.set(cardWithId)
                .addOnSuccessListener {
                    _addCardResult.value = Resource.Success(null)
                    getCards() // Refresh the list of cards
                }
                .addOnFailureListener { e ->
                    _addCardResult.value = Resource.Error(e.message ?: "Failed to add card")
                }
        }
    }

    fun removeCard(card: Card) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            viewModelScope.launch { _removeCardResult.emit(Resource.Error("User not logged in")) }
            return
        }

        firestore.collection("user").document(userId).collection("cards").document(card.id).delete()
            .addOnSuccessListener {
                viewModelScope.launch { _removeCardResult.emit(Resource.Success(null)) }
                getCards() // Refresh the list of cards
            }
            .addOnFailureListener { e ->
                viewModelScope.launch { _removeCardResult.emit(Resource.Error(e.message ?: "Failed to remove card")) }
            }
    }
}
