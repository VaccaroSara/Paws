package com.example.paws.ui.screens.home

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.paws.R
import com.example.paws.ui.screens.auth.SignInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var ivAvatar: ImageView
    
    private lateinit var tvFullName: TextView
    private lateinit var tvCity: TextView
    private lateinit var tvProvince: TextView
    private lateinit var tvCap: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvAccountType: TextView

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            if (imageUri != null) {
                ProfileImageManager.saveProfileImageLocally(requireContext(), imageUri) {
                    ProfileImageManager.loadProfileImage(requireContext(), ivAvatar)
                }
            }
        }
    }

    private var userListener: com.google.firebase.firestore.ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ivAvatar = view.findViewById(R.id.ivAvatarMain)
        val btnLogout = view.findViewById<Button>(R.id.btnLogoutEdit)
        val btnDelete = view.findViewById<Button>(R.id.btnDeleteAccount)
        
        tvFullName = view.findViewById(R.id.tvFullNameEdit)
        tvCity = view.findViewById(R.id.tvCityEdit)
        tvProvince = view.findViewById(R.id.tvProvinceEdit)
        tvCap = view.findViewById(R.id.tvCapEdit)
        tvUsername = view.findViewById(R.id.tvUsernameEdit)
        tvPhone = view.findViewById(R.id.tvPhoneEdit)
        tvEmail = view.findViewById(R.id.tvEmailEdit)
        tvAccountType = view.findViewById(R.id.tvAccountTypeEdit)

        // Row containers
        val rowFullName = view.findViewById<View>(R.id.rowFullNameEdit)
        val rowAddress = view.findViewById<View>(R.id.rowAddressEdit)
        val rowUsername = view.findViewById<View>(R.id.rowUsernameEdit)
        val rowPhone = view.findViewById<View>(R.id.rowPhoneEdit)
        val rowEmail = view.findViewById<View>(R.id.rowEmailEdit)
        val rowPassword = view.findViewById<View>(R.id.rowPasswordEdit)
        val rowAccountType = view.findViewById<View>(R.id.rowAccountTypeEdit)

        val btnShare = view.findViewById<View>(R.id.btnShareMainProfile)

        btnLogout.setOnClickListener { logoutUser() }
        btnDelete.setOnClickListener { confirmDeleteAccount() }
        ivAvatar.setOnClickListener { openGallery() }
        btnShare.setOnClickListener { shareMyProfile() }

        // Edit listeners
        rowFullName.setOnClickListener { showFullNameEditDialog() }
        rowAddress.setOnClickListener { showAddressEditDialog() }
        rowUsername.setOnClickListener { showEditDialog("username", "Modifica Username", tvUsername) }
        rowPhone.setOnClickListener { showEditDialog("phone", "Modifica Telefono", tvPhone) }
        rowEmail.setOnClickListener { showEditDialog("email", "Modifica Email", tvEmail) }
        rowPassword.setOnClickListener { showPasswordResetDialog() }
        rowAccountType.setOnClickListener { showAccountTypeDialog() }

        // Hide keyboard when clicking background
        view.setOnClickListener { hideKeyboard() }

        loadUserData()

        return view
    }

    override fun onDestroyView() {
        userListener?.remove()
        super.onDestroyView()
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser ?: return
        userListener?.remove()
        userListener = db.collection("users").document(currentUser.uid)
            .addSnapshotListener { document, _ ->
                if (isAdded && document != null && document.exists()) {
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    tvFullName.text = "$firstName $lastName".trim()

                    tvCity.text = document.getString("city") ?: ""
                    tvProvince.text = document.getString("province") ?: ""
                    tvCap.text = document.getString("cap") ?: ""

                    tvUsername.text = document.getString("username") ?: "N/A"
                    tvEmail.text = document.getString("email") ?: currentUser.email
                    tvPhone.text = document.getString("phone") ?: "N/A"
                    tvAccountType.text = document.getString("accountType") ?: "Private"

                    // Aggiorna l'immagine profilo in tempo reale
                    ProfileImageManager.loadProfileImage(requireContext(), ivAvatar)
                }
            }
    }

    private fun showFullNameEditDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Modifica Nome e Cognome")

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val inputFirst = EditText(requireContext())
        inputFirst.hint = "Nome"
        layout.addView(inputFirst)

        val inputLast = EditText(requireContext())
        inputLast.hint = "Cognome"
        layout.addView(inputLast)

        builder.setView(layout)

        builder.setPositiveButton("Salva") { _, _ ->
            val first = inputFirst.text.toString()
            val last = inputLast.text.toString()
            val updates = hashMapOf<String, Any>("firstName" to first, "lastName" to last)
            updateUserFields(updates) {
                tvFullName.text = "$first $last".trim()
            }
        }
        builder.setNegativeButton("Annulla") { d, _ -> d.cancel() }
        builder.show()
    }

    private fun showAddressEditDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Modifica Indirizzo")

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val inputCity = EditText(requireContext())
        inputCity.hint = "Città"
        inputCity.setText(tvCity.text.toString())
        layout.addView(inputCity)

        val inputProv = EditText(requireContext())
        inputProv.hint = "Provincia"
        inputProv.setText(tvProvince.text.toString())
        layout.addView(inputProv)

        val inputCap = EditText(requireContext())
        inputCap.hint = "CAP"
        inputCap.setText(tvCap.text.toString())
        layout.addView(inputCap)

        builder.setView(layout)

        builder.setPositiveButton("Salva") { _, _ ->
            val city = inputCity.text.toString()
            val prov = inputProv.text.toString()
            val cap = inputCap.text.toString()
            val updates = hashMapOf<String, Any>("city" to city, "province" to prov, "cap" to cap)
            updateUserFields(updates) {
                tvCity.text = city
                tvProvince.text = prov
                tvCap.text = cap
            }
        }
        builder.setNegativeButton("Annulla") { d, _ -> d.cancel() }
        builder.show()
    }

    private fun showEditDialog(field: String, title: String, textView: TextView) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
        val input = EditText(requireContext())
        input.setText(textView.text.toString())
        builder.setView(input)
        builder.setPositiveButton("Salva") { _, _ ->
            val newValue = input.text.toString()
            updateUserFields(hashMapOf(field to newValue)) { textView.text = newValue }
        }
        builder.setNegativeButton("Annulla") { d, _ -> d.cancel() }
        builder.show()
    }

    private fun showPasswordResetDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Reimposta Password")
        builder.setMessage("Inviare un'email di reset a ${auth.currentUser?.email}?")
        builder.setPositiveButton("Invia") { _, _ ->
            auth.currentUser?.email?.let { email ->
                auth.sendPasswordResetEmail(email).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Email inviata!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton("Annulla") { d, _ -> d.cancel() }
        builder.show()
    }

    private fun showAccountTypeDialog() {
        val types = arrayOf("Private", "Animal Shelter")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Scegli Tipo Account")
        builder.setItems(types) { _, which ->
            updateUserFields(hashMapOf("accountType" to types[which])) { tvAccountType.text = types[which] }
        }
        builder.show()
    }

    private fun updateUserFields(updates: HashMap<String, Any>, onUpdateLocal: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Profilo aggiornato!", Toast.LENGTH_SHORT).show()
                    onUpdateLocal()
                }
            }
            .addOnFailureListener { e ->
                if (isAdded) Toast.makeText(requireContext(), "Errore: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun shareMyProfile() {
        val currentUser = auth.currentUser ?: return
        val username = tvUsername.text.toString()
        val fullName = tvFullName.text.toString()
        
        val shareText = "Check out my profile on Paws!\n\nUsername: $username\nName: $fullName"
        
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Paws Profile")
        intent.putExtra(android.content.Intent.EXTRA_TEXT, shareText)
        
        startActivity(android.content.Intent.createChooser(intent, "Share via"))
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(requireContext(), SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun confirmDeleteAccount() {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminare Account?")
            .setMessage("Questa azione eliminerà definitivamente il tuo profilo e tutti i tuoi post. Sei sicuro?")
            .setPositiveButton("ELIMINA DEFINITIVAMENTE") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("ANNULLA", null)
            .show()
    }

    private fun deleteAccount() {
        val user = auth.currentUser ?: return
        val uid = user.uid

        // 1. Delete user posts
        db.collection("posts")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { snapshots ->
                val postIds = snapshots.documents.map { it.id }
                val batch = db.batch()
                for (doc in snapshots) {
                    batch.delete(doc.reference)
                }
                
                // 2. Delete user document
                batch.delete(db.collection("users").document(uid))
                
                batch.commit().addOnSuccessListener {
                    // 3. Delete images from Supabase
                    ProfileImageManager.deleteAllUserDataFromSupabase(uid, postIds)

                    // 4. Delete from Auth
                    user.delete()
                        .addOnSuccessListener {
                            if (isAdded) {
                                Toast.makeText(requireContext(), "Account eliminato", Toast.LENGTH_SHORT).show()
                                logoutUser()
                            }
                        }
                        .addOnFailureListener { e ->
                            if (isAdded) {
                                if (e is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                                    Toast.makeText(requireContext(), "Riautenticazione necessaria. Effettua logout e login per eliminare.", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(requireContext(), "Errore Auth: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                }.addOnFailureListener { e ->
                    if (isAdded) Toast.makeText(requireContext(), "Errore Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                if (isAdded) Toast.makeText(requireContext(), "Errore recupero post: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}