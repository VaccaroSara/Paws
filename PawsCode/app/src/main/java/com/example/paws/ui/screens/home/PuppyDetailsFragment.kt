package com.example.paws.ui.screens.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
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
}