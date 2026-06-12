package com.example.paws.ui.screens.home

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
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
import java.util.UUID

class CreatePostFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedImageUri: Uri? = null
    private var existingPostId: String? = null
    private var existingImageUrl: String? = null
    private val TAG = "CreatePostFragment"

    companion object {
        private const val BUCKET_NAME = "paws-images"
        private const val ARG_IMAGE_URI = "image_uri"
        private const val ARG_POST_ID = "post_id"
        
        fun newInstance(uri: Uri?, postId: String? = null): CreatePostFragment {
            val args = Bundle().apply { 
                putParcelable(ARG_IMAGE_URI, uri)
                putString(ARG_POST_ID, postId)
            }
            val fragment = CreatePostFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedImageUri = arguments?.getParcelable(ARG_IMAGE_URI)
        existingPostId = arguments?.getString(ARG_POST_ID)
    }

    private var currentGender = "male"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_post, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val ivPreview = view.findViewById<ImageView>(R.id.ivPreviewPost)
        val etName = view.findViewById<EditText>(R.id.etPuppyName)
        val etCaption = view.findViewById<EditText>(R.id.etCaption)
        val tvType = view.findViewById<TextView>(R.id.tvPuppyType)
        val tvAge = view.findViewById<TextView>(R.id.tvPuppyAge)
        val ivGender = view.findViewById<ImageView>(R.id.ivGenderIcon)
        
        val containerGender = view.findViewById<View>(R.id.containerGender)
        val containerType = view.findViewById<View>(R.id.containerType)
        val containerAge = view.findViewById<View>(R.id.containerAge)

        val btnShare = view.findViewById<Button>(R.id.btnSharePost)
        val btnBack = view.findViewById<ImageView>(R.id.btnBackCreate)
        val tvHeader = view.findViewById<TextView>(R.id.tvHeaderTitle)

        if (existingPostId != null) {
            btnShare.text = "SAVE CHANGES"
            tvHeader.text = "Edit Post"
        }

        selectedImageUri?.let {
            val radiusPx = (24 * resources.displayMetrics.density).toInt()
            Glide.with(this)
                .load(it)
                .transform(CenterCrop(), RoundedCorners(radiusPx))
                .into(ivPreview)
        }

        // If editing, load data
        existingPostId?.let { postId ->
            db.collection("posts").document(postId).get().addOnSuccessListener { doc ->
                if (isAdded && doc.exists()) {
                    etName.setText(doc.getString("name"))
                    etCaption.setText(doc.getString("caption"))
                    tvType.text = doc.getString("type") ?: "Dog"
                    tvAge.text = doc.getString("age") ?: "1 years"
                    currentGender = doc.getString("gender") ?: "male"
                    existingImageUrl = doc.getString("imageUrl")
                    
                    if (currentGender == "female") {
                        ivGender.setImageResource(R.drawable.ic_female)
                        containerGender.setBackgroundResource(R.drawable.bg_gender_female_circle)
                    } else {
                        ivGender.setImageResource(R.drawable.ic_male)
                        containerGender.setBackgroundResource(R.drawable.bg_gender_circle)
                    }
                    
                    if (selectedImageUri == null && !existingImageUrl.isNullOrEmpty()) {
                        val radiusPx = (24 * resources.displayMetrics.density).toInt()
                        Glide.with(this)
                            .load(existingImageUrl)
                            .transform(CenterCrop(), RoundedCorners(radiusPx))
                            .into(ivPreview)
                    }
                }
            }
        }

        // Selectors using Dialogs
        containerGender.setOnClickListener {
            val genders = arrayOf("Male", "Female")
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Select Gender")
                .setItems(genders) { _, which ->
                    currentGender = genders[which].lowercase()
                    if (currentGender == "female") {
                        ivGender.setImageResource(R.drawable.ic_female)
                        containerGender.setBackgroundResource(R.drawable.bg_gender_female_circle)
                    } else {
                        ivGender.setImageResource(R.drawable.ic_male)
                        containerGender.setBackgroundResource(R.drawable.bg_gender_circle)
                    }
                }
                .show()
        }

        containerType.setOnClickListener {
            val types = arrayOf("Dog", "Cat", "Bird", "Other")
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Select Type")
                .setItems(types) { _, which ->
                    tvType.text = types[which]
                }
                .show()
        }

        containerAge.setOnClickListener {
            val ages = arrayOf("1 months", "6 months", "1 years", "2 years", "3 years", "5+ years")
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Select Age")
                .setItems(ages) { _, which ->
                    tvAge.text = ages[which]
                }
                .show()
        }

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        btnShare.setOnClickListener {
            val name = etName.text.toString()
            val caption = etCaption.text.toString()
            val type = tvType.text.toString()
            val age = tvAge.text.toString()

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Inserisci un nome", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (existingPostId != null) {
                updatePost(existingPostId!!, name, caption, type, age)
            } else {
                sharePost(name, caption, type, age)
            }
        }

        return view
    }

    private fun updatePost(postId: String, name: String, caption: String, type: String, age: String) {
        val currentUser = auth.currentUser ?: return
        
        db.collection("users").document(currentUser.uid).get().addOnSuccessListener { userDoc ->
            val userType = userDoc.getString("accountType") ?: "Private User"
            
            val updateData = mutableMapOf<String, Any>(
                "name" to name,
                "caption" to caption,
                "type" to type,
                "age" to age,
                "gender" to currentGender,
                "userType" to userType
            )
            
            if (selectedImageUri != null) {
                sharePost(name, caption, type, age, isUpdate = true, updateId = postId)
            } else {
                db.collection("posts").document(postId)
                    .update(updateData)
                    .addOnSuccessListener {
                        if (isAdded) {
                            Toast.makeText(requireContext(), "Modifiche salvate!", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()
                        }
                    }
            }
        }
    }

    private fun sharePost(name: String, caption: String, type: String, age: String, isUpdate: Boolean = false, updateId: String? = null) {
        val uid = auth.currentUser?.uid ?: return
        val postId = updateId ?: UUID.randomUUID().toString()
        val uri = selectedImageUri ?: return

        Toast.makeText(requireContext(), if (isUpdate) "Salvataggio..." else "Caricamento in corso...", Toast.LENGTH_SHORT).show()

        db.collection("users").document(uid).get().addOnSuccessListener { userDoc ->
            val userType = userDoc.getString("accountType") ?: "Private User"

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        val fileName = "posts/$postId.jpg"
                        val storage = SupabaseManager.client.storage.from(BUCKET_NAME)
                        
                        storage.upload(fileName, bytes) {
                            upsert = true // Allow overwrite if updating
                        }

                        val publicUrl = storage.publicUrl(fileName)

                        withContext(Dispatchers.Main) {
                            val postData = hashMapOf(
                                "id" to postId,
                                "uid" to uid,
                                "name" to name,
                                "imageUrl" to publicUrl,
                                "gender" to currentGender,
                                "type" to type,
                                "age" to age,
                                "caption" to caption,
                                "userType" to userType,
                                "timestamp" to com.google.firebase.Timestamp.now()
                            )

                            db.collection("posts").document(postId)
                                .set(postData)
                                .addOnSuccessListener {
                                    if (isAdded) {
                                        Toast.makeText(requireContext(), if (isUpdate) "Modifiche salvate!" else "Post condiviso!", Toast.LENGTH_SHORT).show()
                                        parentFragmentManager.popBackStack()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    if (isAdded) Toast.makeText(requireContext(), "Errore database", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        if (isAdded) Toast.makeText(requireContext(), "Errore: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}