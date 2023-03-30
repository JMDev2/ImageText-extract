package com.example.scanner

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.example.scanner.databinding.ActivityMainBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imagePicker()
        deleteText()
        copyText()
    }

    private fun copyText() {
        binding.copy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val text = binding.textScan.text.toString()
            val clip = ClipData.newPlainText("EditText", text)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(this, "Copied to Clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteText() {
        binding.clear.setOnClickListener {
            binding.textScan.setText("")

        }
    }

    private fun imagePicker() {
        binding.camera.setOnClickListener {
            ImagePicker.with(this)
                .crop()                     //Crop image(Optional), Check Customization for more option
                .compress(1024)             //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)  //Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            val uri: Uri = data?.data!!

            recogniseText(uri)
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun recogniseText(imageUri: Uri) {
        val image: InputImage
        try {
            image = InputImage.fromFilePath(this, imageUri)

            val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val recognizedText = visionText.text

                    // Create a new Editable object from the recognized text
                    val editableText = Editable.Factory.getInstance().newEditable(recognizedText)

                    // Set the editable text to the EditText
                    binding.textScan.setText(editableText)

                    // Set the text size of the EditText
                    binding.textScan.textSize = 18f

                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to recognize text", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}
