package pt.ipca.keystore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import pt.ipca.keystore.util.Resource
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
): ViewModel() {

    private val _login = MutableSharedFlow<Resource<FirebaseUser>>()
    var login = _login.asSharedFlow()

    private val _passwordResetStatus = MutableSharedFlow<Resource<Void>>()
    var passwordResetStatus = _passwordResetStatus.asSharedFlow()

    fun login(email: String, password: String){
         viewModelScope.launch { _login.emit(Resource.Loading()) }
        firebaseAuth.signInWithEmailAndPassword(
            email,password
        ).addOnSuccessListener {
            viewModelScope.launch {
                it.user?.let {
                    _login.emit(Resource.Success(it))
                }
            }
        }.addOnFailureListener{
            viewModelScope.launch {
                _login.emit(Resource.Error(it.message.toString()))
            }

        }
    }

    // Function to send a password reset email
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch { _passwordResetStatus.emit(Resource.Loading()) }
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                viewModelScope.launch {
                    _passwordResetStatus.emit(Resource.Success(null)) // Emit success with no data
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _passwordResetStatus.emit(Resource.Error(it.message.toString()))
                }
            }
    }
}