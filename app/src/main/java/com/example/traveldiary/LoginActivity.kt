package com.example.traveldiary

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.material.textfield.TextInputEditText


class LoginActivity : AppCompatActivity() {
    private lateinit var email: TextInputEditText
    private lateinit var pass: TextInputEditText
    private lateinit var btnlReg: ImageView
    private lateinit var mauth: FirebaseAuth
    private lateinit var textRegi: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var fpass: TextView
    private lateinit var btn_google: AppCompatButton
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var gso : GoogleSignInOptions
    private lateinit var gsc : GoogleSignInClient


    override fun onStart() {
        super.onStart()
        val currentUser = mauth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity3::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        enableEdgeToEdge()

        supportActionBar?.hide()

        email = findViewById(R.id.edtEmail)
        pass = findViewById(R.id.edtPassword)
        btnlReg = findViewById(R.id.img_btn)
        mauth = FirebaseAuth.getInstance()
        fpass = findViewById(R.id.tvfp)
        progressBar = findViewById(R.id.progressBar)
        textRegi = findViewById(R.id.textView2)
        btn_google = findViewById(R.id.button1)

        // Configure Google Sign-In
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id)) // Use actual client ID from Firebase
            .requestEmail()
            .build()

        gsc = GoogleSignIn.getClient(this, gso)

        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            progressBar.visibility = ProgressBar.GONE
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Log.w("LoginActivity", "Google sign in failed", e)
                    Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Handle Google Sign-In button click
        btn_google.setOnClickListener {
            progressBar.visibility = ProgressBar.VISIBLE
            gsc.signOut().addOnCompleteListener {
                val signInIntent = gsc.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        }

        // Handle Register button click
        textRegi.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Handle Forgot Password
        fpass.setOnClickListener {
            val emailInput = email.text.toString()
            forgotPassword(emailInput)
        }

        // Handle Login button click
        btnlReg.setOnClickListener {
            val emailInput = email.text.toString()
            val passInput = pass.text.toString()
            login(emailInput, passInput)
        }
    }

    private fun forgotPassword(email: String) {
        if (email.isEmpty()) {
            Toast.makeText(baseContext, "Enter E-mail", Toast.LENGTH_SHORT).show()
            return
        }
        mauth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Check email to reset password!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to send reset password email!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mauth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, move to MainActivity
                    val user = mauth.currentUser
                    val intent = Intent(this, MainActivity3::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun login(email: String, pass: String) {
        progressBar.visibility = ProgressBar.VISIBLE

        if (email.isEmpty()) {
            Toast.makeText(baseContext, "Enter E-mail", Toast.LENGTH_SHORT).show()
            progressBar.visibility = ProgressBar.GONE
            return
        }

        if (pass.isEmpty()) {
            Toast.makeText(baseContext, "Enter Password", Toast.LENGTH_SHORT).show()
            progressBar.visibility = ProgressBar.GONE
            return
        }

        mauth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = ProgressBar.GONE
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, "Authentication successful.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity3::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
