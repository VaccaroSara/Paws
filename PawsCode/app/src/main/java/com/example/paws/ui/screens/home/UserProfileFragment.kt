package com.example.paws.ui.screens.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.paws.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var userId: String? = null
    private lateinit var adapter: GridPostAdapter
    private var allUserPosts: List<PuppyPost> = emptyList()

    private var userPhone: String = ""

    // Filter states
    private var filterGender: String? = null
    private var filterType: String? = null
    private var filterAge: String? = null

    companion object {
        private const val ARG_USER_ID = "user_id"
        fun newInstance(uid: String): UserProfileFragment {
            val fragment = UserProfileFragment()
            val args = Bundle()
            args.putString(ARG_USER_ID, uid)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getString(ARG_USER_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val ivAvatar = view.findViewById<ImageView>(R.id.ivUserProfileAvatar)
        val tvHeaderName = view.findViewById<TextView>(R.id.tvUserProfileHeaderName)
        val tvFullName = view.findViewById<TextView>(R.id.tvUserProfileFullName)
        val tvAccountType = view.findViewById<TextView>(R.id.tvUserProfileAccountType)
        val tvLocation = view.findViewById<TextView>(R.id.tvUserProfileLocation)
        val btnBack = view.findViewById<View>(R.id.btnBackFromUserProfile)
        val rvPosts = view.findViewById<RecyclerView>(R.id.rvUserProfilePosts)
        val ivFilter = view.findViewById<View>(R.id.ivFilterUserProfile)
        val btnShare = view.findViewById<View>(R.id.btnShareUserProfile)
        val btnCall = view.findViewById<View>(R.id.btnCallUserProfile)

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        ivFilter.setOnClickListener { showFilterDialog() }
        
        btnShare.setOnClickListener { 
            val username = tvHeaderName.text.toString()
            shareUserProfile(username) 
        }

        btnCall.setOnClickListener {
            if (userPhone.isNotEmpty()) {
                makeCall(userPhone)
            } else {
                android.widget.Toast.makeText(requireContext(), "Phone number not available", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        // Setup RecyclerView as Grid
        adapter = GridPostAdapter(emptyList(), { post ->
            val detailsFragment = PuppyDetailsFragment.newInstance(post)
            parentFragmentManager.beginTransaction()
                .add(R.id.content_frame, detailsFragment)
                .hide(this)
                .addToBackStack(null)
                .commit()
        }, { post ->
            favoritePost(post)
        })
        
        rvPosts.layoutManager = GridLayoutManager(requireContext(), 2)
        rvPosts.adapter = adapter

        // Hide keyboard when touching the list
        rvPosts.setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }

        userId?.let { uid ->
            loadUserData(uid, ivAvatar, tvHeaderName, tvFullName, tvAccountType, tvLocation)
            loadUserPosts(uid)
        }

        return view
    }

    private fun makeCall(phone: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phone")
        startActivity(intent)
    }

    private fun shareUserProfile(username: String) {
        val shareText = "Check out $username's profile on Paws!"
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "Paws Profile")
        intent.putExtra(Intent.EXTRA_TEXT, shareText)
        startActivity(Intent.createChooser(intent, "Share via"))
    }

    private fun loadUserData(uid: String, ivAvatar: ImageView, tvHeaderName: TextView, tvFullName: TextView, tvAccountType: TextView, tvLocation: TextView) {
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            if (isAdded && doc.exists()) {
                val user = doc.toObject(User::class.java)?.copy(uid = uid)
                user?.let {
                    tvHeaderName.text = it.username
                    tvFullName.text = "${it.firstName} ${it.lastName}"
                    tvAccountType.text = it.accountType
                    userPhone = it.phone
                    val loc = if (it.city.isNotEmpty()) "${it.city} (${it.province})" else "N/A"
                    tvLocation.text = loc
                    ProfileImageManager.loadProfileImageForUid(requireContext(), uid, ivAvatar)
                }
            }
        }
    }

    private fun loadUserPosts(uid: String) {
        db.collection("posts")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshots, _ ->
                if (isAdded && snapshots != null) {
                    allUserPosts = snapshots.toObjects(PuppyPost::class.java)
                    applyFilters()
                }
            }
    }

    private fun showFilterDialog() {
        val options = arrayOf("Age", "Gender", "Animal Type", "Clear All Filters")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Filter Posts")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showSubFilterDialog("Age", arrayOf("All", "1 months", "6 months", "1 years", "2 years", "3 years", "5+ years"))
                    1 -> showSubFilterDialog("Gender", arrayOf("All", "Male", "Female"))
                    2 -> showSubFilterDialog("Animal Type", arrayOf("All", "Dog", "Cat", "Bird"))
                    3 -> {
                        filterAge = null
                        filterGender = null
                        filterType = null
                        applyFilters()
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
                    }
                } else {
                    when (title) {
                        "Age" -> filterAge = selected
                        "Gender" -> filterGender = selected.lowercase()
                        "Animal Type" -> filterType = selected
                    }
                }
                applyFilters()
            }
            .show()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun applyFilters() {
        var filtered = allUserPosts
        filterAge?.let { age -> filtered = filtered.filter { it.age == age } }
        filterGender?.let { gender -> filtered = filtered.filter { it.gender.lowercase() == gender } }
        filterType?.let { type -> filtered = filtered.filter { it.type == type } }
        
        adapter.updatePosts(filtered)
    }

    private fun favoritePost(post: PuppyPost) {
        val currentUser = auth.currentUser ?: return
        val favId = "${currentUser.uid}_${post.id}"

        db.collection("favorites").document(favId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                db.collection("favorites").document(favId).delete()
                    .addOnSuccessListener {
                        if (isAdded) android.widget.Toast.makeText(requireContext(), "Rimosso dai preferiti", android.widget.Toast.LENGTH_SHORT).show()
                    }
                return@addOnSuccessListener
            }

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

            db.collection("favorites").document(favId).set(favoriteData)
                .addOnSuccessListener {
                    if (isAdded) android.widget.Toast.makeText(requireContext(), "${post.name} salvato!", android.widget.Toast.LENGTH_SHORT).show()
                }
        }
    }
}