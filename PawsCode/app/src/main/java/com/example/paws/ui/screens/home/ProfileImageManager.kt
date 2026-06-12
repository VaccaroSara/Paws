package com.example.paws.ui.screens.home

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.paws.R
import com.example.paws.network.SupabaseManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Gestore per le immagini tramite Supabase.
 */
object ProfileImageManager {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private const val CORNER_RADIUS_DP = 24
    private const val TAG = "ProfileImageManager"
    private const val BUCKET_NAME = "paws-images"

    fun loadProfileImage(context: Context, imageView: ImageView) {
        val uid = auth.currentUser?.uid ?: return
        val radiusPx = (CORNER_RADIUS_DP * context.resources.displayMetrics.density).toInt()

        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            val remoteUri = doc.getString("profileImageUri")
            if (!remoteUri.isNullOrEmpty()) {
                Glide.with(context)
                    .load(remoteUri)
                    .transform(CenterCrop(), RoundedCorners(radiusPx))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(imageView)
                imageView.background = null
                imageView.setPadding(0, 0, 0, 0)
            } else {
                loadLocalFallback(context, imageView, uid, radiusPx)
            }
        }.addOnFailureListener {
            loadLocalFallback(context, imageView, uid, radiusPx)
        }
    }

    private fun loadLocalFallback(context: Context, imageView: ImageView, uid: String, radiusPx: Int) {
        val file = File(context.filesDir, "profile_$uid.jpg")
        if (file.exists()) {
            Glide.with(context)
                .load(file)
                .transform(CenterCrop(), RoundedCorners(radiusPx))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imageView)
            imageView.background = null
            imageView.setPadding(0, 0, 0, 0)
        } else {
            imageView.setImageResource(R.drawable.sign_ic_user)
            val padding = (20 * context.resources.displayMetrics.density).toInt()
            imageView.setPadding(padding, padding, padding, padding)
            imageView.setBackgroundResource(R.drawable.bg_avatar)
        }
    }

    /**
     * Carica l'immagine profilo su Supabase e aggiorna Firestore.
     */
    fun saveProfileImageLocally(context: Context, imageUri: Uri, onComplete: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes != null) {
                    val fileName = "profile_images/$uid.jpg"
                    val storage = SupabaseManager.client.storage.from(BUCKET_NAME)
                    
                    // Caricamento su Supabase
                    storage.upload(fileName, bytes) {
                        upsert = true
                    }

                    // Ottenimento URL pubblico
                    val publicUrl = storage.publicUrl(fileName)

                    withContext(Dispatchers.Main) {
                        db.collection("users").document(uid)
                            .update("profileImageUri", publicUrl)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Foto profilo aggiornata!", Toast.LENGTH_SHORT).show()
                                onComplete()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Errore database: ${e.message}", Toast.LENGTH_SHORT).show()
                                onComplete()
                            }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Supabase upload error", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Errore caricamento: ${e.message}", Toast.LENGTH_LONG).show()
                    onComplete()
                }
            }
        }
    }

    fun saveImageFile(context: Context, imageUri: Uri, fileName: String, onComplete: (File) -> Unit) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            onComplete(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}