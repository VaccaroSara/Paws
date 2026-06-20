package com.example.paws.ui.screens.home

import android.text.Editable
import android.text.TextWatcher
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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
    private var allMyPosts = mutableListOf<PuppyPost>()

    // Filter states
    private var filterGender: String? = null
    private var filterType: String? = null
    private var filterAge: String? = null
    private var filterUserType: String? = null
    private var searchQuery: String = ""

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
        val ivBell = view.findViewById<View>(R.id.ivBellAdd)
        val ivFilter = view.findViewById<View>(R.id.ivFilterAdd)
        val etSearch = view.findViewById<EditText>(R.id.etSearchAdd)

        etSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString() ?: ""
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        ivFilter?.setOnClickListener { showFilterDialog() }

        ivBell?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.content_frame, NotificationsFragment())
                .addToBackStack("Notifications")
                .commit()
        }

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

        // Hide keyboard when clicking background or list
        view.setOnClickListener { hideKeyboard() }
        rvRecent.setOnTouchListener { _, _ -> 
            hideKeyboard()
            false 
        }

        // Load name
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (isAdded && document != null && document.exists()) {
                        val firstName = document.getString("firstName") ?: ""
                        if (firstName.isNotEmpty()) {
                            tvWelcomeName.text = "Hi, $firstName"
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
                    allMyPosts = snapshots.toObjects(PuppyPost::class.java).toMutableList()
                    applyFilters()
                }
            }
    }

    private fun showFilterDialog() {
        val options = arrayOf("Age", "Gender", "Animal Type", "User Type", "Clear All Filters")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Filter My Posts")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showSubFilterDialog("Age", arrayOf("All", "1 months", "6 months", "1 years", "2 years", "3 years", "5+ years"))
                    1 -> showSubFilterDialog("Gender", arrayOf("All", "Male", "Female"))
                    2 -> showSubFilterDialog("Animal Type", arrayOf("All", "Dog", "Cat", "Bird", "Other"))
                    3 -> showSubFilterDialog("User Type", arrayOf("All", "Private User", "Animal Shelter"))
                    4 -> {
                        filterAge = null
                        filterGender = null
                        filterType = null
                        filterUserType = null
                        applyFilters()
                        Toast.makeText(requireContext(), "Filters cleared", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    private fun showSubFilterDialog(title: String, items: Array<String>) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select $title")
            .setItems(items) { _, which ->
                val selected = items[which]
                if (selected == "All") {
                    when (title) {
                        "Age" -> filterAge = null
                        "Gender" -> filterGender = null
                        "Animal Type" -> filterType = null
                        "User Type" -> filterUserType = null
                    }
                } else {
                    when (title) {
                        "Age" -> filterAge = selected
                        "Gender" -> filterGender = selected.lowercase()
                        "Animal Type" -> filterType = selected
                        "User Type" -> filterUserType = selected
                    }
                }
                applyFilters()
            }
            .show()
    }

    private fun applyFilters() {
        val filteredList = allMyPosts.let { list ->
            var result = list.toList()
            if (searchQuery.isNotEmpty()) {
                result = result.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
            filterAge?.let { age -> result = result.filter { it.age == age } }
            filterGender?.let { gender -> result = result.filter { it.gender.lowercase() == gender } }
            filterType?.let { type -> result = result.filter { it.type == type } }
            filterUserType?.let { ut -> result = result.filter { it.userType == ut } }
            result
        }
        adapter.updatePosts(filteredList)
    }

    private fun deletePost(post: PuppyPost) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminare ${post.name}?")
            .setMessage("Sei sicuro di voler eliminare definitivamente questo post? L'azione non può essere annullata.")
            .setPositiveButton("ELIMINA") { _, _ ->
                performActualDeletion(post)
            }
            .setNegativeButton("ANNULLA", null)
            .show()
    }

    private fun performActualDeletion(post: PuppyPost) {
        // 1. Notify users who favorited this post before deleting favorites entries
        db.collection("favorites")
            .whereEqualTo("postId", post.id)
            .get()
            .addOnSuccessListener { snapshots ->
                val batch = db.batch()
                for (doc in snapshots) {
                    val favoritedUid = doc.getString("uid")
                    if (favoritedUid != null && favoritedUid != post.uid) {
                        // Create a notification for this user
                        val notifId = db.collection("notifications").document().id
                        val notificationData = hashMapOf(
                            "id" to notifId,
                            "targetUid" to favoritedUid,
                            "text" to "${post.name} non è più disponibile (eliminato dal proprietario)",
                            "timestamp" to com.google.firebase.Timestamp.now()
                        )
                        batch.set(db.collection("notifications").document(notifId), notificationData)
                    }
                    // Delete the favorite entry
                    batch.delete(doc.reference)
                }
                
                // Commit notifications and favorite removals
                batch.commit().addOnSuccessListener {
                    // 2. Finally delete from 'posts' collection
                    db.collection("posts").document(post.id)
                        .delete()
                        .addOnSuccessListener {
                            if (isAdded) {
                                Toast.makeText(requireContext(), "${post.name} eliminato definitivamente", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            if (isAdded) {
                                Toast.makeText(requireContext(), "Errore durante l'eliminazione", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}