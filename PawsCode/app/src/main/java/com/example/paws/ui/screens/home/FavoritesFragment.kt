package com.example.paws.ui.screens.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.paws.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: FavoritesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val tvWelcomeName = view.findViewById<TextView>(R.id.tvWelcomeNameFav)
        val rvFavorites = view.findViewById<RecyclerView>(R.id.rvFavorites)
        val ivBell = view.findViewById<View>(R.id.ivBellFav)

        ivBell.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.content_frame, NotificationsFragment())
                .addToBackStack(null)
                .commit()
        }
        
        adapter = FavoritesAdapter(emptyList()) { post ->
            unlikePost(post)
        }
        
        rvFavorites.layoutManager = GridLayoutManager(requireContext(), 2)
        rvFavorites.adapter = adapter

        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Load User Name
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (isAdded && document != null && document.exists()) {
                        val firstName = document.getString("firstName")
                        if (!firstName.isNullOrEmpty()) {
                            tvWelcomeName.text = "hi, $firstName"
                        }
                    }
                }
            
            loadFavorites(currentUser.uid)
        }

        return view
    }

    private fun loadFavorites(uid: String) {
        db.collection("favorites")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshots, e ->
                if (snapshots != null && isAdded) {
                    val favPosts = snapshots.documents.mapNotNull { doc ->
                        PuppyPost(
                            id = doc.getString("postId") ?: "",
                            uid = doc.getString("uid") ?: "",
                            name = doc.getString("name") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            gender = doc.getString("gender") ?: "male",
                            type = doc.getString("type") ?: "Dog",
                            age = doc.getString("age") ?: "1 years",
                            caption = doc.getString("caption") ?: ""
                        )
                    }
                    adapter.updateFavorites(favPosts)
                }
            }
    }

    private fun unlikePost(post: PuppyPost) {
        val currentUser = auth.currentUser ?: return
        val favId = "${currentUser.uid}_${post.id}"
        
        db.collection("favorites").document(favId)
            .delete()
            .addOnSuccessListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "${post.name} rimosso dai preferiti", Toast.LENGTH_SHORT).show()
                }
            }
    }
}