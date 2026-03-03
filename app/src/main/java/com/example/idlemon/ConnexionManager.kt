package com.example.idlemon

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object ConnexionManager {
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    fun estConnecte(): Boolean = auth.currentUser != null
    
    fun getEmail(): String? = auth.currentUser?.email

    fun deconnexion(context: Context) {
        auth.signOut()
        SaveManager.resetToDefault(context)
    }

    fun inscription(email: String, pass: String, context: Context, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // save local vers cloud immédiat à l'inscription
                    SaveManager.sauvegarder(onSuccess, onFailure)
                } else {
                    onFailure(task.exception?.message ?: "Erreur d'inscription")
                }
            }
    }

    fun connexion(email: String, pass: String, context: Context, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    SaveManager.charger(context, onSuccess)
                } else {
                    onFailure(task.exception?.message ?: "Identifiants incorrects")
                }
            }
    }

    fun resetCompte(context: Context, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val user = auth.currentUser
        
        // 1. Clear local et cloud
        SaveManager.deleteLocal(context)
        SaveManager.resetToDefault(context)

        if (user != null) {
            db.collection("users").document(user.uid).delete()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it.message ?: "Erreur cloud") }
        } else {
            onSuccess()
        }
    }
}
