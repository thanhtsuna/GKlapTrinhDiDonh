
package com.example.gklaptrinhdidonh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val auth = remember {
                    FirebaseAuth.getInstance()
                }

                val db = remember {
                    FirebaseFirestore.getInstance()
                }

                var currentUser by remember {
                    mutableStateOf<FirebaseUser?>(auth.currentUser)
                }

                var isAdmin by remember {
                    mutableStateOf(false)
                }

                var roleLoaded by remember {
                    mutableStateOf(false)
                }

                DisposableEffect(auth) {
                    val authListener =
                        FirebaseAuth.AuthStateListener { firebaseAuth ->
                            currentUser = firebaseAuth.currentUser
                        }

                    auth.addAuthStateListener(authListener)

                    onDispose {
                        auth.removeAuthStateListener(authListener)
                    }
                }

                LaunchedEffect(currentUser?.uid) {
                    roleLoaded = false
                    isAdmin = false

                    val user = currentUser

                    if (user != null) {
                        db.collection("users")
                            .document(user.uid)
                            .get()
                            .addOnSuccessListener { document ->
                                isAdmin =
                                    document.getString("role") == "admin"
                                roleLoaded = true
                            }
                            .addOnFailureListener {
                                isAdmin = false
                                roleLoaded = true
                            }
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    when {
                        currentUser == null -> {
                            AuthScreen(
                                onAuthSuccess = { user ->
                                    currentUser = user
                                }
                            )
                        }

                        !roleLoaded -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Đang tải tài khoản...")
                            }
                        }

                        else -> {
                            ProductScreen(
                                isAdmin = isAdmin,
                                onLogout = {
                                    auth.signOut()
                                    currentUser = null
                                    isAdmin = false
                                    roleLoaded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
