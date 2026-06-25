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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.paws.R
import com.google.firebase.firestore.FirebaseFirestore

class PuppyDetailsFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private var puppyPost: PuppyPost? = null

    companion object {
        fun newInstance(post: PuppyPost): PuppyDetailsFragment {
            val fragment = PuppyDetailsFragment()
            fragment.puppyPost = post
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_puppy_details, container, false)
        db = FirebaseFirestore.getInstance()

        // Implementiamo lo swipe to favorite (verso destra)
        val gestureDetector = android.view.GestureDetector(requireContext(), object : android.view.GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: android.view.MotionEvent?, e2: android.view.MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 != null && e2.x - e1.x > 100 && Math.abs(velocityX) > 100) {
                    puppyPost?.let { favoritePostAndGoBack(it) }
                    return true
                }
                return false
            }
        })

        view.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }

        val ivImage = view.findViewById<ImageView>(R.id.ivDetailPuppyImage)
        val tvName = view.findViewById<TextView>(R.id.tvDetailPuppyName)
        val ivGender = view.findViewById<ImageView>(R.id.ivDetailGenderIcon)
        val containerGender = view.findViewById<View>(R.id.containerGender)
        val tvType = view.findViewById<TextView>(R.id.tvDetailType)
        val tvAge = view.findViewById<TextView>(R.id.tvDetailAge)
        val tvCaption = view.findViewById<TextView>(R.id.tvPuppyCaption)
        val btnBack = view.findViewById<View>(R.id.btnBackFromDetails)

        val tvUsername = view.findViewById<TextView>(R.id.tvOwnerUsername)
        val tvPhone = view.findViewById<TextView>(R.id.tvOwnerPhone)
        val tvLocation = view.findViewById<TextView>(R.id.tvOwnerLocation)

        puppyPost?.let { post ->
            tvName.text = post.name
            tvType.text = post.type
            tvAge.text = post.age
            tvCaption.text = post.caption

            if (post.imageUrl.isNotEmpty()) {
                val radiusPx = (20 * resources.displayMetrics.density).toInt()
                Glide.with(this)
                    .load(post.imageUrl)
                    .transform(CenterCrop(), RoundedCorners(radiusPx))
                    .into(ivImage)
            }

            if (post.gender.lowercase() == "female") {
                ivGender.setImageResource(R.drawable.ic_female)
                containerGender.setBackgroundResource(R.drawable.bg_gender_female_circle)
            } else {
                ivGender.setImageResource(R.drawable.ic_male)
                containerGender.setBackgroundResource(R.drawable.bg_gender_circle)
            }

            // Load Owner Details
            db.collection("users").document(post.uid).get().addOnSuccessListener { doc ->
                if (isAdded && doc.exists()) {
                    tvUsername.text = doc.getString("username") ?: doc.getString("firstName") ?: "Unknown"
                    tvPhone.text = doc.getString("phone") ?: "N/A"
                    val city = doc.getString("city") ?: ""
                    val province = doc.getString("province") ?: ""
                    tvLocation.text = if (city.isNotEmpty() && province.isNotEmpty()) "$city ($province)" else city
                }
            }
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }

    private fun favoritePostAndGoBack(post: PuppyPost) {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val currentUser = auth.currentUser ?: return
        val favId = "${currentUser.uid}_${post.id}"

        db.collection("favorites").document(favId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                parentFragmentManager.popBackStack()
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
                    if (isAdded) {
                        android.widget.Toast.makeText(requireContext(), "${post.name} salvato!", android.widget.Toast.LENGTH_SHORT).show()
                        
                        // Invia notifica al proprietario
                        db.collection("users").document(currentUser.uid).get().addOnSuccessListener { userDoc ->
                            val currentUsername = userDoc.getString("username") ?: "someone"
                            val notificationData = hashMapOf(
                                "targetUid" to post.uid,
                                "text" to "$currentUsername added ${post.name} to favorites",
                                "postImageUrl" to post.imageUrl,
                                "timestamp" to com.google.firebase.Timestamp.now()
                            )
                            db.collection("notifications").add(notificationData)
                        }

                        parentFragmentManager.popBackStack()
                    }
                }
        }
    }
}
