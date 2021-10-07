package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import kotlinx.android.synthetic.main.activity_authentication.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private val authenticationViewModel by viewModel<AuthenticationViewModel>()

    private val requestCode = 1234
    lateinit var availableProviders: MutableList<AuthUI.IdpConfig>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

//          TODO: If the user was authenticated, send him to RemindersActivity

//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

        availableProviders = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        initLoginClick()
    }

    override fun onStart() {
        super.onStart()
        isUserLoggedIn()
    }

    private fun initLoginClick() {
        login_button.setOnClickListener {
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(availableProviders)
                    .build(),
                requestCode
            )
        }
    }

    private fun isUserLoggedIn() {
        if (authenticationViewModel.user.value != null) {
            goToRemindersActivity()
        }
    }

    private fun goToRemindersActivity() {
        val intent = Intent(this@AuthenticationActivity, RemindersActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == requestCode) {
            when (resultCode) {
                Activity.RESULT_OK -> goToRemindersActivity()
                else -> {
                    val response = IdpResponse.fromResultIntent(data)
                    Log.i(
                        "TEST",
                        "AuthenticationActivity.onActivityResult${response?.error.toString()}"
                    )
                }
            }
        }
    }
}
