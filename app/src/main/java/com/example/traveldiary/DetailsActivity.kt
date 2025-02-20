
package com.example.traveldiary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Base64
import androidx.appcompat.app.AlertDialog
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.storage

class DetailsActivity : AppCompatActivity() {

    private lateinit var locationImageView: ImageView
    private lateinit var locationNameTextView: TextView
    private lateinit var locationAddressTextView: TextView
    private lateinit var locationDateTextView: TextView
    private lateinit var locationNotesTextView: TextView
    private lateinit var editButton: Button
    private lateinit var deleteButton: Button
    private lateinit var backButton: Button
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details) // Replace with your actual layout file

        locationImageView = findViewById(R.id.locationImageView)
        locationNameTextView = findViewById(R.id.locationNameTextView)
        locationAddressTextView = findViewById(R.id.locationAddressTextView)
        locationDateTextView = findViewById(R.id.locationDateTextView)
        locationNotesTextView = findViewById(R.id.locationNotesTextView)
        backButton = findViewById(R.id.backButton)
        editButton = findViewById(R.id.editbutton)
        deleteButton = findViewById(R.id.deletebutton)

        db = FirebaseFirestore.getInstance()

        backButton.setOnClickListener {
            finish()
        }

        val documentId = intent.getStringExtra("documentId")
        val name = intent.getStringExtra("name")
        val address = intent.getStringExtra("address")
        val date = intent.getStringExtra("date")
        val notes = intent.getStringExtra("notes")
        val imageUrl = intent.getStringExtra("imageUrl")

        if (documentId != null) {
            setDataToViews(name, address, date, notes, imageUrl)
            Log.d("DetailsActivity", "Image URL: $imageUrl")

            editButton.setOnClickListener {
                    val editIntent = Intent(this, MainActivity5::class.java)
                    editIntent.putExtra("name", name)
                    editIntent.putExtra("address", address)
                    editIntent.putExtra("date", date)
                    editIntent.putExtra("notes", notes)
                    editIntent.putExtra("locationId", documentId)
                    editIntent.putExtra("imageUrl", imageUrl)
                    startActivity(editIntent)
            }
            deleteButton.setOnClickListener {
                showDeleteConfirmationDialog(documentId)
            }
        } else {
            Toast.makeText(this, "Location ID is null", Toast.LENGTH_SHORT).show()
            Log.e("DetailsActivity", "Location ID is null")
            finish()
        }
    }

    private fun setDataToViews(
        name: String?,
        address: String?,
        date: String?,
        notes: String?,
        image: String?
    ) {
        locationNameTextView.text = "Name: $name"
        locationAddressTextView.text = "Address: $address"
        locationDateTextView.text = "Date Visited: $date"
        locationNotesTextView.text = "Notes: $notes"

        if (!image.isNullOrEmpty()) {
            Glide.with(this)
                .load(image) // Load directly from Base64 string
                .into(locationImageView)
        } else {
            locationImageView.setImageResource(R.mipmap.logo)
        }
    }


    private fun showDeleteConfirmationDialog(documentId: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Location")
            .setMessage("Are you sure you want to delete this location?")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteLocation(documentId)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteLocation(documentId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Get user ID
        if (userId != null) {
            db.collection("users").document(userId).collection("locations").document(documentId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Location deleted", Toast.LENGTH_SHORT).show()
                    finish() // Close DetailsActivity after deletion
                }
                .addOnFailureListener { e ->
                    Log.w("DetailsActivity", "Error deleting location", e)
                    Toast.makeText(this, "Error deleting location", Toast.LENGTH_SHORT).show()
                }
        }
    }
}