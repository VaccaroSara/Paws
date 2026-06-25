package com.example.paws.ui.screens.home

import android.text.Editable
import android.text.TextWatcher
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
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
    private lateinit var adapter: SearchAdapter
    private lateinit var tvPuppiesLabel: View
    private var allPosts: MutableList<PuppyPost> = mutableListOf()
    private var favoritedPostIds: Set<String> = emptySet()
    
    // Filter states
    private var filterGender: String? = null
    private var filterType: String? = null
    private var filterAge: String? = null
    private var filterUserType: String? = null
    private var searchQuery: String = ""

    private var favoritesListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var feedListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var notificationsListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var etSearch: EditText? = null

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
        val viewBadge = view.findViewById<View>(R.id.viewBadgeHome)
        etSearch = view.findViewById<EditText>(R.id.etSearchHome)
        etSearch?.hint = "search for a user..."
        tvPuppiesLabel = view.findViewById(R.id.tvPuppiesLabel)

        etSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString() ?: ""
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        ivBell?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.content_frame, NotificationsFragment())
                .addToBackStack("Notifications")
                .commit()
        }

        ivFilter?.setOnClickListener {
            showFilterDialog()
        }

        // Setup RecyclerView with SearchAdapter
        adapter = SearchAdapter(emptyList(), { post ->
            val detailsFragment = PuppyDetailsFragment.newInstance(post)
            parentFragmentManager.beginTransaction()
                .add(R.id.content_frame, detailsFragment)
                .hide(this)
                .addToBackStack(null)
                .commit()
        }, { user ->
            // Close keyboard before navigating
            hideKeyboard()
            // Navigate to User Profile - Using add instead of replace to keep Home state
            val profileFragment = UserProfileFragment.newInstance(user.uid)
            parentFragmentManager.beginTransaction()
                .add(R.id.content_frame, profileFragment)
                .hide(this)
                .addToBackStack(null)
                .commit()
        })
        rvFeed.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rvFeed.adapter = adapter
        
        // Hide keyboard when clicking background or list
        view.setOnClickListener { hideKeyboard() }
        rvFeed.setOnTouchListener { _, _ -> 
            hideKeyboard()
            false 
        }
        
        // SnapHelper makes it feel like swiping through pages/cards
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rvFeed)

        // Setup Swipe to Favorite (RIGHT)
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder): Boolean = false
            
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val item = adapter.getItemAt(position)
                if (item is PuppyPost) {
                    favoritePost(item)
                    favoritedPostIds = favoritedPostIds + item.id
                    applyFilters()
                }
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
                            tvWelcomeName.text = "Hi, $firstName"
                        }
                    }
                }
            
            // First load favorites, then load feed
            loadFavoritesAndFeed(currentUser.uid)

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
        feedListener?.remove()
        notificationsListener?.remove()
        etSearch = null
        super.onDestroyView()
    }

    fun resetSearch() {
        searchQuery = ""
        etSearch?.setText("")
        applyFilters()
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun showFilterDialog() {
        val options = arrayOf("Age", "Gender", "Animal Type", "User Type", "Clear All Filters")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Filter Puppies")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showSubFilterDialog("Age", arrayOf("All", "1 months", "6 months", "1 years", "2 years", "3 years", "5+ years"))
                    1 -> showSubFilterDialog("Gender", arrayOf("All", "Male", "Female"))
                    2 -> showSubFilterDialog("Animal Type", arrayOf("All", "Dog", "Cat", "Bird"))
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
        // Show or hide "Puppies for you" label based on search query
        if (::tvPuppiesLabel.isInitialized) {
            tvPuppiesLabel.visibility = if (searchQuery.isEmpty()) View.VISIBLE else View.GONE
        }

        // First filter the posts
        val filteredPosts = allPosts.filter { it.id !in favoritedPostIds }.let { list ->
            var result = list
            // If searchQuery is present, we filter by name
            if (searchQuery.isNotEmpty()) {
                result = result.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
            // Always apply these filters if they are set
            filterAge?.let { age -> result = result.filter { it.age == age } }
            filterGender?.let { gender -> result = result.filter { it.gender.lowercase() == gender } }
            filterType?.let { type -> result = result.filter { it.type == type } }
            filterUserType?.let { ut -> result = result.filter { it.userType == ut } }
            result
        }

        if (searchQuery.isNotEmpty() && searchQuery.length >= 2) {
            val currentUid = auth.currentUser?.uid ?: ""
            // Search for users
            db.collection("users")
                .whereGreaterThanOrEqualTo("username", searchQuery)
                .whereLessThanOrEqualTo("username", searchQuery + "\uf8ff")
                .get()
                .addOnSuccessListener { snapshots ->
                    if (isAdded) {
                        val users = snapshots.toObjects(User::class.java)
                            .mapIndexed { index, user -> user.copy(uid = snapshots.documents[index].id) }
                            .filter { it.uid != currentUid } // Don't search for self

                        val combinedList = mutableListOf<Any>()
                        combinedList.addAll(users)
                        combinedList.addAll(filteredPosts)
                        adapter.updateItems(combinedList)
                    }
                }
        } else {
            // If search is empty or too short, just show filtered posts
            adapter.updateItems(filteredPosts)
        }
    }

    private fun loadFavoritesAndFeed(currentUid: String) {
        favoritesListener?.remove()
        favoritesListener = db.collection("favorites")
            .whereEqualTo("uid", currentUid)
            .addSnapshotListener { snapshots, e ->
                if (snapshots != null && isAdded) {
                    favoritedPostIds = snapshots.documents.mapNotNull { it.getString("postId") }.toSet()
                    if (feedListener == null) {
                        loadGlobalFeed(currentUid)
                    } else {
                        applyFilters()
                    }
                }
            }
    }

    private fun favoritePost(post: PuppyPost) {
        val currentUser = auth.currentUser ?: return
        val favId = "${currentUser.uid}_${post.id}"

        // Controlliamo se è già nei preferiti per evitare incrementi doppi
        db.collection("favorites").document(favId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                if (isAdded) Toast.makeText(requireContext(), "Già nei preferiti!", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            // Se non esiste, procediamo al salvataggio
            db.collection("users").document(currentUser.uid).get().addOnSuccessListener { userDoc ->
                val currentUsername = userDoc.getString("username") ?: "someone"
                
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

                db.collection("favorites").document(favId)
                    .set(favoriteData)
                    .addOnSuccessListener {
                        if (isAdded) {
                            Toast.makeText(requireContext(), "${post.name} salvato nei preferiti!", Toast.LENGTH_SHORT).show()
                        }
                        
                        // Notifica al proprietario
                        val notificationData = hashMapOf(
                            "targetUid" to post.uid,
                            "text" to "$currentUsername added ${post.name} to favorites",
                            "postImageUrl" to post.imageUrl,
                            "timestamp" to com.google.firebase.Timestamp.now()
                        )
                        db.collection("notifications").add(notificationData)
                    }
            }
        }
    }

    private fun loadGlobalFeed(currentUid: String) {
        feedListener?.remove()
        feedListener = db.collection("posts")
            .whereNotEqualTo("uid", currentUid)
            .addSnapshotListener { snapshots, e ->
                if (snapshots != null && isAdded) {
                    allPosts = snapshots.toObjects(PuppyPost::class.java).toMutableList()
                    applyFilters()
                }
            }
    }
}