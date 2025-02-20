package com.example.traveldiary

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment


class EditUsernameDialog(private val currentUsername: String, private val onUsernameUpdated: (String) -> Unit) : DialogFragment() {
    private lateinit var editTextUsername: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val view = requireActivity().layoutInflater.inflate(R.layout.edit_username_dialog, null)

        editTextUsername = view.findViewById(R.id.editTextUsername)
        editTextUsername.setText(currentUsername)

        builder.setView(view)
            .setPositiveButton("Update") { _, _ ->
                val updatedUsername = editTextUsername.text.toString()
                onUsernameUpdated(updatedUsername) // Return the updated username
            }
            .setNegativeButton("Cancel", null)

        return builder.create()
    }
}
