package com.example.traveldiary // Replace with your actual package name

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity5 : AppCompatActivity() {

    private lateinit var datePicker: MaterialDatePicker<Long>
    private lateinit var nameEdit: TextInputEditText
    private lateinit var editImageView: ImageView
    private lateinit var addressEdit: TextInputEditText
    private lateinit var notesEdit: TextInputEditText
    private lateinit var savebtn: Button
    private lateinit var progressBar : ProgressBar
    private lateinit var selectDate: Button
    private lateinit var db: FirebaseFirestore
    private var locationId: String? = null
    private var imageUrl : String? = null
    private var newimageUrl : String? = null
    private var isImageUploaded = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main5) // Replace with your layout file

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main5)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize view references
        progressBar = findViewById(R.id.progressBar5)
        nameEdit = findViewById(R.id.editLocationName)
        addressEdit = findViewById(R.id.editLocationAddress)
        notesEdit = findViewById(R.id.editLocationNotes)
        savebtn = findViewById(R.id.saveButton)
        editImageView = findViewById(R.id.editImageView)
        selectDate = findViewById(R.id.editLocationDate)

        db = FirebaseFirestore.getInstance()

        val pickImgLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                progressBar.visibility = View.VISIBLE
                uploadImageToStorage(uri) { imageUrl ->
                    newimageUrl = imageUrl
                    isImageUploaded = true
                    // Hide progress bar after image upload
                    progressBar.visibility = View.GONE
                    Glide.with(this).load(imageUrl).into(editImageView)
                }
            }
        }
        // Date Picker setup
        val constraintBuilder = CalendarConstraints.Builder()
        datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setCalendarConstraints(constraintBuilder.build())
            .build()

        selectDate.setOnClickListener {
            datePicker.show(supportFragmentManager, "DatePicker")
        }

        datePicker.addOnPositiveButtonClickListener { selection ->
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val selectedDate = simpleDateFormat.format(Date(selection))
            selectDate.text = selectedDate
        }

        // Image Selection setup
        editImageView.setOnClickListener {
            pickImgLauncher.launch("image/*")
        }

        // Retrieve location details from intent extras
        locationId = intent.getStringExtra("locationId")
        nameEdit.setText(intent.getStringExtra("name"))
        addressEdit.setText(intent.getStringExtra("address"))
        notesEdit.setText(intent.getStringExtra("notes"))
        selectDate.text = intent.getStringExtra("date")

        // In EditActivity
        val imageUrl = intent.getStringExtra("imageUrl")
        Glide.with(this).load(imageUrl).into(editImageView) // Load directly from URL

        savebtn.setOnClickListener {
            Log.d("FirestoreUpdate", "locationId: $locationId")
            if (locationId == null) {
                Toast.makeText(this, "Location ID is null", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }
            saveChangesToFirestore()
        }
    }

    private fun uploadImageToStorage(imageUri: Uri, callback: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val imageName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        if (userId != null) {
            val imageRef = storageRef.child("images/$userId/$locationId.jpg")
            val uploadTask = imageRef.putFile(imageUri)
            uploadTask.addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    callback(imageUrl)
                    Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("EditActivity", "Image upload failed", e)
                progressBar.visibility = View.GONE
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
        }
    }
    private fun saveChangesToFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val updateName = nameEdit.text.toString().trim()
            val updateAddress = addressEdit.text.toString().trim()
            val updateDate = selectDate.text.toString().trim()
            val updateNotes = notesEdit.text.toString().trim()

            if (updateName.isEmpty() || updateAddress.isEmpty() || updateDate.isEmpty() || updateNotes.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return
            }

            progressBar.visibility =View.VISIBLE

            if (isImageUploaded && newimageUrl != null) {
                // Use the new image URL
                updateLocationData(userId, updateName, updateAddress, updateDate, updateNotes, newimageUrl!!)
            } else if (!imageUrl.isNullOrEmpty()) {
                // Use the existing image URL
                updateLocationData(userId, updateName, updateAddress, updateDate, updateNotes, imageUrl!!)
            } else {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLocationData(userId: String, name: String, address: String, date: String, notes: String, imageUrl: String) {
        val updatedData = hashMapOf(
            "name" to name,
            "address" to address,
            "date" to date,
            "notes" to notes,
            "imageUrl" to imageUrl
        )
        locationId?.let {
            Log.d(
                "FirestoreSave",
                "Firestore update starting for userId: $userId and locationId: $locationId"
            )
            db.collection("users").document(userId)
                .collection("locations").document(locationId!!)
                .update(updatedData as Map<String, Any>)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FirestoreSave", "Firestore update successful")
                        Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.GONE
                        val intent = Intent(this, MainActivity3::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val exception = task.exception
                        Log.e("FirestoreSave", "Firestore update failed", exception)
                        Toast.makeText(
                            this,
                            "Data update failed: ${exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        progressBar.visibility = View.GONE
                    }
                }
        }
    }
}