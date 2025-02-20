package com.example.traveldiary

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

class ProfileActivity : AppCompatActivity() {

    private lateinit var textUsername: TextView
    private lateinit var textEmail: TextView
    private lateinit var imageProfile: TextView
    private lateinit var buttonLogout: Button
    private lateinit var btnBack: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        textUsername = findViewById(R.id.textUsername)
        textEmail = findViewById(R.id.textEmail)
        imageProfile = findViewById(R.id.textViewProfilePicture)
        buttonLogout = findViewById(R.id.buttonLogout)
        btnBack = findViewById(R.id.btnBack)

        textUsername.setOnClickListener {
            showEditUsernameDialog()  // Call the method to show the dialog
        }

        btnBack.setOnClickListener {
            finish()
        }

        // Load user data
        loadUserProfile()

        // Set up logout button
        buttonLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showEditUsernameDialog() {
        val currentUsername = textUsername.text.toString()
        val dialog = EditUsernameDialog(currentUsername) { updatedUsername ->
            // Update the TextView with the new username
            textUsername.text = updatedUsername
            setProfilePicture(updatedUsername) // Update the profile picture with the new username
            updateUsernameInFirestore(updatedUsername) // Update Firestore with the new username
        }
        dialog.show(supportFragmentManager, "EditUsernameDialog")
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        user?.let {
            textEmail.text = it.email

            // Fetching username from Firestore
            db.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val username = document.getString("username")
                        textUsername.text = username ?: "No username available"

                        // Generate a default profile picture if not present
                        if (username != null) {
                            setProfilePicture(username)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setProfilePicture(username: String) {
        // Get the first letter of the username
        val firstLetter = username.first().uppercaseChar()

        // Generate random colors for background and text
        val backgroundColor = generateRandomColor()
        val textColor = getContrastingTextColor(backgroundColor) // Get a contrasting text color

        // Set background color and text color
        imageProfile.setBackgroundColor(backgroundColor)
        imageProfile.setTextColor(textColor)

        // Set the text to the first letter of the username
        imageProfile.text = firstLetter.toString()
        imageProfile.textSize = 32f // Adjust text size to make it bigger
    }

    private fun generateRandomColor(): Int {
        val random = Random
        return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256))
    }

    // Function to determine a contrasting text color
    private fun getContrastingTextColor(backgroundColor: Int): Int {
        // Calculate luminance
        val r = Color.red(backgroundColor) / 255.0
        val g = Color.green(backgroundColor) / 255.0
        val b = Color.blue(backgroundColor) / 255.0
        val luminance = 0.299 * r + 0.587 * g + 0.114 * b

        // Return black for light backgrounds and white for dark backgrounds
        return if (luminance > 0.5) {
            Color.BLACK // Dark text for light background
        } else {
            Color.WHITE // Light text for dark background
        }
    }


    private fun updateUsernameInFirestore(newUsername: String) {
        val user = auth.currentUser
        user?.let {
            db.collection("users").document(it.uid).update("username", newUsername)
                .addOnSuccessListener {
                    Toast.makeText(this, "Username updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update username: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
