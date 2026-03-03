package com.example.idlemon

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ConnexionManager {
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private const val TAG = "ConnexionManager"

    fun estConnecte(): Boolean = auth.currentUser != null
    fun getEmail(): String? = auth.currentUser?.email

    fun deconnexion(context: Context) {
        auth.signOut()
        val credentialManager = CredentialManager.create(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing credentials", e)
            }
        }
        SaveManager.resetToDefault(context)
    }

    fun inscription(email: String, pass: String, context: Context, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
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
                    SaveManager.charger(context, onSuccess, onFailure = {
                        onSuccess()
                    })
                } else {
                    onFailure(task.exception?.message ?: "Identifiants incorrects")
                }
            }
    }

    //Google
    fun connexionGoogle(context: Context, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val credentialManager = CredentialManager.create(context)
        val webClientId = "250685621215-opqldocbb57comvogmj09eag78p7nncn.apps.googleusercontent.com" //clé webcli dispo sur FireBase

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false) 
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        CoroutineScope(Dispatchers.Main).launch {

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            //récup le token google
            val googleIdTokenCredential = when {
                credential is GoogleIdTokenCredential -> credential
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                    GoogleIdTokenCredential.createFrom(credential.data)
                }
                else -> null
            }

            if (googleIdTokenCredential != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        //succes
                        if (task.isSuccessful) {
                            SaveManager.charger(context,
                                onSuccess = { onSuccess() },
                                onFailure = {
                                    SaveManager.sauvegarder(onSuccess, onFailure)
                                }
                            )
                        } else {
                            //fail
                            onFailure("Erreur Firebase: ${task.exception?.message}")
                        }
                    }
            } else {
                Log.e(TAG, "Type reçu inattendu : ${credential.type}")
                onFailure("Erreur de format Google (${credential.type})")
            }
        }
    }

    fun resetCompte(context: Context, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val user = auth.currentUser
        //supp local et cloud
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
