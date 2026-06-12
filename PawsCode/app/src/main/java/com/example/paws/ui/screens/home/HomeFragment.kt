package com.example.paws.ui.screens.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.paws.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: FeedAdapter
    private var allPosts: MutableList<PuppyPost> = mutableListOf()
    private var favoritedPostIds: Set<String> = emptySet()
    
    // Filter states
    private var filterGender: String? = null
    private var filterType: String? = null
    private var filterAge: String? = null
    private var filterUserType: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val tvWelcomeName = view.findViewById<TextView>(R.id.tvWelcomeName)
        val rvFeed = view.findViewById<RecyclerView>(R.id.rvGlobalFeed)
        val ivBell = view.findViewById<View>(R.id.ivBellHome)
        val ivFilter = view.findViewById<View>(R.id.ivFilterHome)

        ivBell?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.content_frame, NotificationsFragment())
                .addToBackStack(null)
                .commit()
        }

        ivFilter?.setOnClickListener {
            showFilterDialog()
        }

        // Setup RecyclerView with vertical snapping (like cards)
        adapter = FeedAdapter(emptyList()) { post ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.content_frame, PuppyDetailsFragment.newInstance(post))
                .addToBackStack(null)
                .commit()
        }
        rvFeed.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rvFeed.adapter = adapter
        
        // SnapHelper makes it feel like swiping through pages/cards
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rvFeed)

        // Setup Swipe to Favorite (RIGHT)
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder): Boolean = false
            
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val post = adapter.getPostAt(position)
                favoritePost(post)
                
                // Add to local filtered set immediately to avoid re-showing if snapshot triggers
                favoritedPostIds = favoritedPostIds + post.id
                
                allPosts.removeAt(position)
                applyFilters()
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(rvFeed)

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
            
            // First load favorites, then load feed
            loadFavoritesAndFeed(currentUser.uid)
        }

        return view
    }

    private fun showFilterDialog() {
        val options = arrayOf("Age", "Gender", "Animal Type", "User Type", "Clear All Filters")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Filter Puppies")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showSubFilterDialog("Age", arrayOf("1 months", "6 months", "1 years", "2 years", "3 years", "5+ years"))
                    1 -> showSubFilterDialog("Gender", arrayOf("Male", "Female"))
                    2 -> showSubFilterDialog("Animal Type", arrayOf("Dog", "Cat", "Bird", "Other"))
                    3 -> showSubFilterDialog("User Type", arrayOf("Private User", "Animal Shelter"))
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
                when (title) {
                    "Age" -> filterAge = selected
                    "Gender" -> filterGender = selected.lowercase()
                    "Animal Type" -> filterType = selected
                    "User Type" -> filterUserType = selected
                }
                applyFilters()
            }
            .show()
    }

    private fun applyFilters() {
        var filteredList = allPosts.filter { it.id !in favoritedPostIds }

        filterAge?.let { age -> filteredList = filteredList.filter { it.age == age } }
        filterGender?.let { gender -> filteredList = filteredList.filter { it.gender.lowercase() == gender } }
        filterType?.let { type -> filteredList = filteredList.filter { it.type == type } }
        filterUserType?.let { ut -> filteredList = filteredList.filter { it.userType == ut } }

        adapter.updatePosts(filteredList)
    }

    private fun loadFavoritesAndFeed(currentUid: String) {
        db.collection("favorites")
            .whereEqualTo("uid", currentUid)
            .get()
            .addOnSuccessListener { snapshots ->
                if (isAdded) {
                    favoritedPostIds = snapshots.documents.mapNotNull { it.getString("postId") }.toSet()
                    loadGlobalFeed(currentUid)
                }
            }
    }

    private fun favoritePost(post: PuppyPost) {
        val currentUser = auth.currentUser ?: return
        
        // Fetch current user's name first for the notification
        db.collection("users").document(currentUser.uid).get().addOnSuccessListener { userDoc ->
            val currentUserName = userDoc.getString("firstName") ?: "someone"
            
            val favoriteData = hashMapOf(
                "uid" to currentUser.uid,
                "postId" to post.id,
                "name" to post.name,
                "imageUrl" to post.imageUrl,
                "gender" to post.gender,
                "type" to post.type,
                "age" to post.age,
                "caption" to post.caption,
                "userType" to post.userType,
                "timestamp" to com.google.firebase.Timestamp.now()
            )

            val favId = "${currentUser.uid}_${post.id}"
            
            db.collection("favorites").document(favId)
                .set(favoriteData)
                .addOnSuccessListener {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "${post.name} salvato nei preferiti!", Toast.LENGTH_SHORT).show()
                    }
                    
                    // Create Notification for the post owner
                    val notificationData = hashMapOf(
                        "targetUid" to post.uid,
                        "text" to "$currentUserName liked your post",
                        "timestamp" to com.google.firebase.Timestamp.now()
                    )
                    db.collection("notifications").add(notificationData)
                }
        }
    }

    private fun loadGlobalFeed(currentUid: String) {
        db.collection("posts")
            .whereNotEqualTo("uid", currentUid)
            .orderBy("uid") 
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshots, e ->
                if (snapshots != null && isAdded) {
                    allPosts = snapshots.toObjects(PuppyPost::class.java).toMutableList()
                    applyFilters()
                }
            }
    }
}