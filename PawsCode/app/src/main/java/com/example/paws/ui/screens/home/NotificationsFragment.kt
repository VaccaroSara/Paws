package com.example.paws.ui.screens.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.paws.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val tvWelcomeName = view.findViewById<TextView>(R.id.tvWelcomeNameNotif)
        val rvNotifications = view.findViewById<RecyclerView>(R.id.rvNotifications)
        val btnClear = view.findViewById<View>(R.id.btnClearNotifications)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyNotifications)
        val ivBell = view.findViewById<View>(R.id.ivBellNotif)

        adapter = NotificationAdapter(emptyList())
        rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        rvNotifications.adapter = adapter

        // Hide keyboard when clicking background or list
        view.setOnClickListener { hideKeyboard() }
        rvNotifications.setOnTouchListener { _, _ -> 
            hideKeyboard()
            false 
        }

        btnClear.setOnClickListener {
            clearAllNotifications()
        }

        ivBell.setOnClickListener {
            parentFragmentManager.popBackStack()
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
            
            loadNotifications(currentUser.uid, tvEmpty)
        }

        return view
    }

    private fun clearAllNotifications() {
        val currentUser = auth.currentUser ?: return
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminare tutte le notifiche?")
            .setMessage("Questa azione non può essere annullata.")
            .setPositiveButton("ELIMINA TUTTO") { _, _ ->
                db.collection("notifications")
                    .whereEqualTo("targetUid", currentUser.uid)
                    .get()
                    .addOnSuccessListener { snapshots ->
                        val batch = db.batch()
                        for (doc in snapshots) {
                            batch.delete(doc.reference)
                        }
                        batch.commit().addOnSuccessListener {
                            if (isAdded) Toast.makeText(requireContext(), "Notifiche eliminate", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            .setNegativeButton("ANNULLA", null)
            .show()
    }

    private fun loadNotifications(uid: String, tvEmpty: TextView) {
        db.collection("notifications")
            .whereEqualTo("targetUid", uid)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    tvEmpty.visibility = View.VISIBLE
                    return@addSnapshotListener
                }

                if (snapshots != null && isAdded) {
                    val notifications = snapshots.toObjects(NotificationItem::class.java)
                        .sortedByDescending { it.timestamp }
                    adapter.updateNotifications(notifications)
                    tvEmpty.visibility = if (notifications.isEmpty()) View.VISIBLE else View.GONE
                }
            }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}