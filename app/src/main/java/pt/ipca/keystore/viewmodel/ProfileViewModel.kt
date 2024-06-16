package pt.ipca.keystore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipca.keystore.data.Address
import pt.ipca.keystore.data.Card
import pt.ipca.keystore.data.User
import pt.ipca.keystore.util.Resource
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _user = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val user = _user.asStateFlow()

    private val _cards = MutableStateFlow<Resource<List<Card>>>(Resource.Unspecified())
    val cards = _cards.asStateFlow()

    private val _addCardResult = MutableStateFlow<Resource<Void>>(Resource.Unspecified())
    val addCardResult = _addCardResult.asStateFlow()

    private val _removeCardResult = MutableStateFlow<Resource<Void>>(Resource.Unspecified())
    val removeCardResult = _removeCardResult.asStateFlow()

    private val _updateNameResult = MutableStateFlow<Resource<Void>>(Resource.Unspecified())
    val updateNameResult = _updateNameResult.asStateFlow()

    private val _updateAddressResult = MutableStateFlow<Resource<Void>>(Resource.Unspecified())
    val updateAddressResult = _updateAddressResult.asStateFlow()

    private val _address = MutableStateFlow<Resource<Address>>(Resource.Unspecified())
    val address = _address.asStateFlow()

    init {
        getUser()
        getAddress()
    }

    fun getUser() {
        viewModelScope.launch {
            _user.emit(Resource.Loading())
        }
        firestore.collection("user").document(auth.uid!!)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    viewModelScope.launch {
                        _user.emit(Resource.Error(error.message.toString()))
                    }
                } else {
                    val user = value?.toObject(User::class.java)
                    user?.let {
                        viewModelScope.launch {
                            _user.emit(Resource.Success(user))
                        }
                    }
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

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

    fun updateUserName(newName: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            viewModelScope.launch { _updateNameResult.emit(Resource.Error("User not logged in")) }
            return
        }

        viewModelScope.launch {
            _updateNameResult.emit(Resource.Loading())
            firestore.collection("user").document(userId)
                .update("username", newName)
                .addOnSuccessListener {
                    _updateNameResult.value = Resource.Success(null)
                }
                .addOnFailureListener { e ->
                    _updateNameResult.value = Resource.Error(e.message ?: "Failed to update name")
                }
        }
    }

    fun updateAddress(street: String, number: String, city: String, country: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            viewModelScope.launch { _updateAddressResult.emit(Resource.Error("User not logged in")) }
            return
        }

        val address = Address(street, number, city, country)

        viewModelScope.launch {
            _updateAddressResult.emit(Resource.Loading())
            firestore.collection("user").document(userId).collection("address").document("userAddress")
                .set(address)
                .addOnSuccessListener {
                    _updateAddressResult.value = Resource.Success(null)
                }
                .addOnFailureListener { e ->
                    _updateAddressResult.value = Resource.Error(e.message ?: "Failed to update address")
                }
        }
    }

    fun getAddress() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            viewModelScope.launch { _address.emit(Resource.Error("User not logged in")) }
            return
        }

        firestore.collection("user").document(userId).collection("address").document("userAddress")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val address = document.toObject(Address::class.java)
                    address?.let {
                        viewModelScope.launch { _address.emit(Resource.Success(it)) }
                    } ?: run {
                        viewModelScope.launch { _address.emit(Resource.Error("No address found")) }
                    }
                } else {
                    viewModelScope.launch { _address.emit(Resource.Error("No address found")) }
                }
            }
            .addOnFailureListener { exception ->
                viewModelScope.launch { _address.emit(Resource.Error(exception.message ?: "Error fetching address")) }
            }
    }
}
