package com.example.paws.ui.screens.home

import android.text.Editable
import android.text.TextWatcher
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
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
    private lateinit var tvEmptyFavorites: View
    private var allFavoritePosts = mutableMapOf<String, PuppyPost>()
    
    // Filter states
    private var filterGender: String? = null
    private var filterType: String? = null
    private var filterAge: String? = null
    private var filterUserType: String? = null
    private var searchQuery: String = ""
    private var currentFavIds: Set<String> = emptySet()

    private var favoritesListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var notificationsListener: com.google.firebase.firestore.ListenerRegistration? = null
    private val postListeners = mutableMapOf<String, com.google.firebase.firestore.ListenerRegistration>()

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
        val viewBadge = view.findViewById<View>(R.id.viewBadgeFav)
        val ivFilter = view.findViewById<View>(R.id.ivFilterFav)
        val etSearch = view.findViewById<EditText>(R.id.etSearchFav)
        tvEmptyFavorites = view.findViewById(R.id.tvEmptyFavorites)

        etSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString() ?: ""
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        ivFilter?.setOnClickListener { showFilterDialog() }

        ivBell.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.content_frame, NotificationsFragment())
                .addToBackStack("Notifications")
                .commit()
        }
        
        adapter = FavoritesAdapter(emptyList(), { post ->
            val detailsFragment = PuppyDetailsFragment.newInstance(post)
            parentFragmentManager.beginTransaction()
                .add(R.id.content_frame, detailsFragment)
                .hide(this)
                .addToBackStack(null)
                .commit()
        }, { post ->
            unlikePost(post)
        })
        
        rvFavorites.layoutManager = GridLayoutManager(requireContext(), 2)
        rvFavorites.adapter = adapter

        // Hide keyboard when clicking background or list
        view.setOnClickListener { hideKeyboard() }
        rvFavorites.setOnTouchListener { _, _ -> 
            hideKeyboard()
            false 
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Load User Name
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (isAdded && document != null && document.exists()) {
                        val firstName = document.getString("firstName")
                        if (!firstName.isNullOrEmpty()) {
                            tvWelcomeName.text = "Hi, $firstName"
                        }
                    }
                }
            
            loadFavorites(currentUser.uid)

            // Listen for notifications to show badge
            notificationsListener?.remove()
            notificationsListener = db.collection("notifications")
                .whereEqualTo("targetUid", currentUser.uid)
                .addSnapshotListener { snapshots, _ ->
                    if (isAdded && snapshots != null) {
                        viewBadge.visibility = if (snapshots.isEmpty) View.GONE else View.VISIBLE
                    }
                }
        }

        return view
    }

    override fun onDestroyView() {
        favoritesListener?.remove()
        notificationsListener?.remove()
        postListeners.values.forEach { it.remove() }
        postListeners.clear()
        super.onDestroyView()
    }

    private fun loadFavorites(uid: String) {
        favoritesListener?.remove()
        favoritesListener = db.collection("favorites")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { favoritesSnapshot, _ ->
                if (favoritesSnapshot != null && isAdded) {
                    val documents = favoritesSnapshot.documents
                    
                    if (documents.isEmpty()) {
                        postListeners.values.forEach { it.remove() }
                        postListeners.clear()
                        adapter.updateFavorites(emptyList())
                        return@addSnapshotListener
                    }

                    val currentFavoriteIds = documents.mapNotNull { it.getString("postId") }.toSet()
                    currentFavIds = currentFavoriteIds
                    
                    // Rimuovi listener per post non più presenti nei preferiti
                    val iter = postListeners.iterator()
                    while (iter.hasNext()) {
                        val entry = iter.next()
                        if (entry.key !in currentFavoriteIds) {
                            entry.value.remove()
                            iter.remove()
                            allFavoritePosts.remove(entry.key)
                        }
                    }

                    for (doc in documents) {
                        val postId = doc.getString("postId") ?: ""
                        if (postId.isEmpty()) continue

                        // Se non abbiamo già un listener per questo post, crealo
                        if (!postListeners.containsKey(postId)) {
                            postListeners[postId] = db.collection("posts").document(postId)
                                .addSnapshotListener { postSnapshot, _ ->
                                    if (postSnapshot != null && postSnapshot.exists() && isAdded) {
                                        val post = PuppyPost(
                                            id = postSnapshot.id,
                                            uid = postSnapshot.getString("uid") ?: "",
                                            name = postSnapshot.getString("name") ?: "",
                                            imageUrl = postSnapshot.getString("imageUrl") ?: "",
                                            gender = postSnapshot.getString("gender") ?: "male",
                                            type = postSnapshot.getString("type") ?: "Dog",
                                            age = postSnapshot.getString("age") ?: "1 years",
                                            caption = postSnapshot.getString("caption") ?: "",
                                            userType = postSnapshot.getString("userType") ?: "Private"
                                        )
                                        
                                        allFavoritePosts[post.id] = post
                                        applyFilters()
                                    } else if ((postSnapshot == null || !postSnapshot.exists()) && isAdded) {
                                        allFavoritePosts.remove(postId)
                                        applyFilters()
                                    }
                                }
                        }
                    }
                    applyFilters()
                }
            }
    }

    private fun showFilterDialog() {
        val options = arrayOf("Age", "Gender", "Animal Type", "User Type", "Clear All Filters")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Filter Favorites")
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
        val sortedList = currentFavIds.mapNotNull { allFavoritePosts[it] }.let { list ->
            var result = list
            if (searchQuery.isNotEmpty()) {
                result = result.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
            filterAge?.let { age -> result = result.filter { it.age == age } }
            filterGender?.let { gender -> result = result.filter { it.gender.lowercase() == gender } }
            filterType?.let { type -> result = result.filter { it.type == type } }
            filterUserType?.let { ut -> result = result.filter { it.userType == ut } }
            result
        }
        
        if (::tvEmptyFavorites.isInitialized) {
            tvEmptyFavorites.visibility = if (sortedList.isEmpty() && searchQuery.isEmpty()) View.VISIBLE else View.GONE
        }

        adapter.updateFavorites(sortedList)
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

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}