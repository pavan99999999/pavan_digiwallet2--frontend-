package com.simats.pavan_digi_wallet

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class AiAssistantActivity : AppCompatActivity() {
    
    private lateinit var tvStatus: TextView
    private lateinit var btnUpload: Button
    private lateinit var btnDelete: Button
    
    // Step 6 UI
    private lateinit var etChatMessage: EditText
    private lateinit var btnSendChat: View
    private lateinit var tvAiReply: TextView
    private lateinit var tvAiReasoning: TextView

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadStatement(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ai_assistant)

        window.statusBarColor = Color.parseColor("#0F1520")
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        findViewById<View>(R.id.btn_back).setOnClickListener {
            finish()
        }

        tvStatus = findViewById(R.id.tv_upload_status)
        btnUpload = findViewById(R.id.btn_upload_statement)
        btnDelete = findViewById(R.id.btn_delete_statement)

        btnUpload.setOnClickListener {
            // Updated to accept both Images and PDFs
            selectImageLauncher.launch("*/*")
        }

        btnDelete.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete AI Records")
                .setMessage("This will delete all activity income and activity expense records added by the AI Statement Reader. Continue?")
                .setPositiveButton("Delete All") { _, _ -> deleteAiRecords() }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Initialize Step 6
        etChatMessage = findViewById(R.id.et_chat_message)
        btnSendChat = findViewById(R.id.btn_send_chat)
        tvAiReply = findViewById(R.id.tv_ai_reply)
        tvAiReasoning = findViewById(R.id.tv_ai_explanation)
        
        btnSendChat.setOnClickListener { 
            val msg = etChatMessage.text.toString()
            if (msg.isNotEmpty()) performAiChat(msg)
        }
    }

    private fun performAiChat(message: String) {
        tvAiReply.text = "Thinking..."
        tvAiReasoning.visibility = View.GONE
        etChatMessage.setText("")
        
        val prefs = getSharedPreferences("digi_wallet", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", 1)
        val request = ChatRequest(userId = userId, message = message)
        
        RetrofitClient.apiService.geminiChatPro(request).enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    tvAiReply.text = body?.reply ?: "Sorry, I couldn't process that."
                } else {
                    tvAiReply.text = "Server error: ${response.code()}"
                }
            }
            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                tvAiReply.text = "❌ Network Error\nVerify IP in RetrofitClient.kt"
            }
        })
    }


    private fun deleteAiRecords() {
        tvStatus.text = "Deleting AI records..."
        btnDelete.isEnabled = false
        
        val prefs = getSharedPreferences("digi_wallet", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", 1)
        
        RetrofitClient.apiService.deleteAiTransactions(userId = userId).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                btnDelete.isEnabled = true
                if (response.isSuccessful) {
                    tvStatus.text = "All AI records deleted! 🗑️"
                    btnDelete.visibility = View.GONE
                    Toast.makeText(this@AiAssistantActivity, "Records Removed", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    tvStatus.text = "Delete failed: $errorMsg"
                    Toast.makeText(this@AiAssistantActivity, "Failed: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                btnDelete.isEnabled = true
                Toast.makeText(this@AiAssistantActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun uploadStatement(uri: Uri) {
        tvStatus.text = "📡 Scanning statement with OCR… please wait"
        btnUpload.isEnabled = false

        try {
            val file = copyUriToInternalStorage(uri)

            // Detect correct MIME type (PDF vs image)
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            // Get saved user_id from SharedPreferences
            val prefs = getSharedPreferences("digi_wallet", MODE_PRIVATE)
            val userId = prefs.getInt("user_id", 1)

            RetrofitClient.apiService.uploadStatement(userId, body)
                .enqueue(object : Callback<StatementUploadResponse> {
                    override fun onResponse(
                        call: Call<StatementUploadResponse>,
                        response: Response<StatementUploadResponse>
                    ) {
                        btnUpload.isEnabled = true
                        if (response.isSuccessful) {
                            val count = response.body()?.transactionsAdded ?: 0
                            tvStatus.text = if (count > 0)
                                "✅ Successfully added $count transactions!"
                            else
                                "⚠️ No transactions detected. Try a clearer image."
                            if (count > 0) btnDelete.visibility = View.VISIBLE
                            Toast.makeText(
                                this@AiAssistantActivity,
                                "Statement processed: $count transactions",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val errBody = response.errorBody()?.string() ?: response.message()
                            tvStatus.text = "❌ Error processing statement"
                            Toast.makeText(this@AiAssistantActivity, "Failed: $errBody", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<StatementUploadResponse>, t: Throwable) {
                        btnUpload.isEnabled = true
                        tvStatus.text = "❌ Network Error — is the backend running?"
                        Toast.makeText(this@AiAssistantActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } catch (e: Exception) {
            btnUpload.isEnabled = true
            tvStatus.text = "❌ Could not read file"
            Toast.makeText(this, "File error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyUriToInternalStorage(uri: Uri): File {
        val mimeType = contentResolver.getType(uri) ?: ""
        val extension = when {
            mimeType.contains("pdf") -> "pdf"
            mimeType.contains("png") -> "png"
            mimeType.contains("jpeg") || mimeType.contains("jpg") -> "jpg"
            else -> android.webkit.MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(mimeType) ?: "jpg"
        }

        val inputStream = contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open URI")
        val file = File(cacheDir, "temp_statement.$extension")
        FileOutputStream(file).use { output ->
            inputStream.use { input -> input.copyTo(output) }
        }
        return file
    }
}
