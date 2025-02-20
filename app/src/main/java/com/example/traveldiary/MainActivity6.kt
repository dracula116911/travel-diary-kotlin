package com.example.traveldiary

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.enableEdgeToEdge
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity6 : AppCompatActivity() {

    private lateinit var savebtn: Button
    private lateinit var tvlocation: TextInputEditText
    private lateinit var tvadress: TextInputEditText
    private lateinit var tvnotes: TextInputEditText
    private lateinit var selectImage: ImageView
    private lateinit var selectdate: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var selectimglauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main6)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main6)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        progressBar = findViewById(R.id.progressBar6)
        tvlocation = findViewById(R.id.edtName)
        tvadress = findViewById(R.id.edtAddress)
        selectdate = findViewById(R.id.btnDate)
        tvnotes = findViewById(R.id.edtNotes)
        savebtn = findViewById(R.id.btnSave)
        selectImage = findViewById(R.id.selectImg)

        selectimglauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imgUri = result.data?.data
                if (imgUri != null) {
                    handleImageSelection(imgUri)
                }
            }
        }

        savebtn.setOnClickListener {
            saveDataToFirestore()
        }

        selectdate.setOnClickListener {
            showDatePicker()
        }

        selectImage.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        selectimglauncher.launch(intent)
    }

    private fun showDatePicker() {
        val datepicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .build()

        datepicker.show(supportFragmentManager, "DATE_PICKER")

        datepicker.addOnPositiveButtonClickListener {
            val date = Date(it)
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = format.format(date)
            selectdate.text = formattedDate
        }
    }

    private fun handleImageSelection(imgUri: Uri) {
        progressBar.visibility = View.VISIBLE
        Glide.with(this).load(imgUri)
            .fitCenter()
            .into(selectImage)
        progressBar.visibility = View.GONE
    }

    private fun uploadImageToStorage(imageUri: Uri, callback: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val locationId = userId?.let {
            FirebaseFirestore.getInstance().collection("users").document(it).collection("locations").document().id
        }

        if (locationId != null && userId != null) {
            val imageRef = storageRef.child("images/$userId/$locationId.jpg")

            val uploadTask = imageRef.putFile(imageUri)
            uploadTask.addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    callback(imageUrl)
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("InsertActivity", "Image upload failed", e)
                Log.d("Upload", "Storage path: images/$userId/$locationId.jpg")
                progressBar.visibility = View.GONE
            }
        } else {
            Toast.makeText(this, "User not logged in or location ID not generated", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
        }
    }

    private fun saveLocationDataWithImageUrl(imageUrl: String, locationId: String, userId: String) {
        val name = tvlocation.text.toString().trim()
        val address = tvadress.text.toString().trim()
        val date = selectdate.text.toString().trim()
        val notes = tvnotes.text.toString().trim()

        val entry = hashMapOf(
            "name" to name,
            "address" to address,
            "date" to date,
            "notes" to notes,
            "imageUrl" to imageUrl
        )

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).collection("locations").document(locationId)
            .set(entry as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity3::class.java)
                startActivity(intent)
                finish()
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Data save failed: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("InsertActivity", "Data save failed", e)
                progressBar.visibility = View.GONE
            }
    }

    private fun saveDataToFirestore() {
        val name = tvlocation.text.toString().trim()
        val address = tvadress.text.toString().trim()
        val date = selectdate.text.toString().trim()
        val notes = tvnotes.text.toString().trim()

        if (name.isEmpty() || address.isEmpty() || date.isEmpty() || notes.isEmpty() || selectImage.drawable == null) {
            Toast.makeText(this, "Please fill in all fields and select an image", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val locationId = userId?.let {
            FirebaseFirestore.getInstance().collection("users").document(it).collection("locations").document().id
        }

        if (locationId != null && userId != null) {
            progressBar.visibility = View.VISIBLE
            val imageUri = getImageUriFromImageView(selectImage)
            if (imageUri != null) {
                uploadImageToStorage(imageUri) { imageUrl ->
                    saveLocationDataWithImageUrl(imageUrl, locationId, userId)
                }
            } else {
                Toast.makeText(this, "Image URI not found", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
        } else {
            Toast.makeText(this, "User not logged in or location ID not generated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getImageUriFromImageView(imageView: ImageView): Uri? {
        val drawable = imageView.drawable
        if (drawable != null) {
            val bitmap = drawable.toBitmap()
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes)
            val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
            return Uri.parse(path)
        }
        return null
    }
}