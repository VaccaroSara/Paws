package com.example.paws.ui.screens.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.paws.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddPuppyFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: PostAdapter

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            if (imageUri != null) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.content_frame, CreatePostFragment.newInstance(imageUri))
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_puppy, container, false)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val tvWelcomeName = view.findViewById<TextView>(R.id.tvWelcomeNameAdd)
        val rvRecent = view.findViewById<RecyclerView>(R.id.rvRecentPosts)

        // Setup RecyclerView with 2 columns Grid
        adapter = PostAdapter(
            posts = emptyList(),
            onAddClick = { openGallery() },
            onEditClick = { post -> 
                parentFragmentManager.beginTransaction()
                    .replace(R.id.content_frame, CreatePostFragment.newInstance(null, post.id))
                    .addToBackStack(null)
                    .commit()
            },
            onDeleteClick = { post -> deletePost(post) }
        )
        rvRecent.layoutManager = GridLayoutManager(requireContext(), 2)
        rvRecent.adapter = adapter

        // Load name
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (isAdded && document != null && document.exists()) {
                        val firstName = document.getString("firstName") ?: ""
                        if (firstName.isNotEmpty()) {
                            tvWelcomeName.text = "hi, $firstName"
                        }
                    }
                }
            
            loadRecentPosts(currentUser.uid)
        }

        return view
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun loadRecentPosts(uid: String) {
        db.collection("posts")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshots, e ->
                if (snapshots != null && isAdded) {
                    val posts = snapshots.toObjects(PuppyPost::class.java)
                    adapter.updatePosts(posts)
                }
            }
    }

    private fun deletePost(post: PuppyPost) {
        // 1. Delete from 'posts' collection
        db.collection("posts").document(post.id)
            .delete()
            .addOnSuccessListener {
                if (isAdded) {
                    android.widget.Toast.makeText(requireContext(), "${post.name} eliminato definitivamente", android.widget.Toast.LENGTH_SHORT).show()
                }
                
                // 2. Delete all related favorites for this post ID across all users
                db.collection("favorites")
                    .whereEqualTo("postId", post.id)
                    .get()
                    .addOnSuccessListener { snapshots ->
                        val batch = db.batch()
                        for (doc in snapshots) {
                            batch.delete(doc.reference)
                        }
                        batch.commit()
                    }
            }
            .addOnFailureListener {
                if (isAdded) {
                    android.widget.Toast.makeText(requireContext(), "Errore durante l'eliminazione", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
    }
}