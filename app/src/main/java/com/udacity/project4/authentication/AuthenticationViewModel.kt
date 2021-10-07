package com.udacity.project4.authentication

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthenticationViewModel(
    firebaseAuth: FirebaseAuth,
    private val app: Application
) : ViewModel() {

    val user: LiveData<FirebaseUser>
        get() = _user

    private val _lastSigned = MutableLiveData<GoogleSignInAccount>()
    private val _user = MutableLiveData<FirebaseUser>()

    init {
        _user.value = firebaseAuth.currentUser
        getLastSigned()
    }

    private fun getLastSigned() {
        val account = GoogleSignIn.getLastSignedInAccount(app.applicationContext)
        _lastSigned.value = account
    }
}
