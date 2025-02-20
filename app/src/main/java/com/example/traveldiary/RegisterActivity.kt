package com.example.traveldiary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var email: TextInputEditText
    private lateinit var pass: TextInputEditText
    private lateinit var username: TextInputEditText
    private lateinit var btnLogin: ImageView
    private lateinit var mauth: FirebaseAuth
    private lateinit var textRegi: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var firestore: FirebaseFirestore

    override fun onStart() {
        super.onStart()
        val currentUser = mauth.currentUser
        if (currentUser != null) {
            // User is already logged in, navigate to ProfileActivity
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.hide()

        // Initialize views
        email = findViewById(R.id.edtEmail1)
        pass = findViewById(R.id.edtPassword1)
        username = findViewById(R.id.edtName)  // Username field
        btnLogin = findViewById(R.id.img_btn2)
        mauth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        progressBar = findViewById(R.id.progressBar1)
        textRegi = findViewById(R.id.tvlogin)

        textRegi.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnLogin.setOnClickListener {
            progressBar.visibility = ProgressBar.VISIBLE
            val email1 = email.text.toString().trim()
            val pass1 = pass.text.toString().trim()
            val username1 = username.text.toString().trim()  // Get the username input

            if (email1.isEmpty()) {
                Toast.makeText(baseContext, "Enter E-mail", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass1.isEmpty()) {
                Toast.makeText(baseContext, "Enter Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (username1.isEmpty()) {
                Toast.makeText(baseContext, "Enter Username", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("Registration", "Email: $email1, Password: $pass1, Username: $username1")
            mauth.createUserWithEmailAndPassword(email1, pass1)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Save username to Firestore
                        val userId = mauth.currentUser?.uid
                        val user = hashMapOf(
                            "username" to username1,
                            "email" to email1
                        )

                        userId?.let {
                            firestore.collection("users").document(it)
                                .set(user)
                                .addOnSuccessListener {
                                    progressBar.visibility = ProgressBar.GONE
                                    Toast.makeText(baseContext, "Registration successful.", Toast.LENGTH_SHORT).show()
                                    // Navigate to ProfileActivity
                                    val intent = Intent(this, MainActivity3::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    progressBar.visibility = ProgressBar.GONE
                                    Log.e("Firestore", "Error saving user data", e)
                                }
                        }
                    } else if (task.exception is FirebaseAuthUserCollisionException) {
                        progressBar.visibility = ProgressBar.GONE
                        Toast.makeText(this, "Email address is already in use.", Toast.LENGTH_SHORT).show()
                    } else {
                        progressBar.visibility = ProgressBar.GONE
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("RegisterActivity", "Error creating user", exception)
                }
        }
    }
}
