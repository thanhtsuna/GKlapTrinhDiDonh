
package com.example.gklaptrinhdidonh

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AuthScreen(
    onAuthSuccess: (FirebaseUser) -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var isRegister by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isRegister) "Đăng ký tài khoản"
            else "Đăng nhập"
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    message = "Vui lòng nhập email và mật khẩu"
                    return@Button
                }

                loading = true
                message = ""

                if (isRegister) {
                    auth.createUserWithEmailAndPassword(
                        email.trim(),
                        password
                    ).addOnSuccessListener { result ->
                        val user = result.user

                        if (user == null) {
                            loading = false
                            message = "Không lấy được tài khoản"
                        } else {
                            val userData = mapOf(
                                "email" to user.email,
                                "role" to "user"
                            )

                            db.collection("users")
                                .document(user.uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    loading = false
                                    onAuthSuccess(user)
                                }
                                .addOnFailureListener { error ->
                                    loading = false
                                    message = error.message
                                        ?: "Không lưu được thông tin người dùng"
                                }
                        }
                    }.addOnFailureListener { error ->
                        loading = false
                        message = error.message ?: "Đăng ký thất bại"
                    }
                } else {
                    auth.signInWithEmailAndPassword(
                        email.trim(),
                        password
                    ).addOnSuccessListener { result ->
                        loading = false
                        result.user?.let(onAuthSuccess)
                    }.addOnFailureListener { error ->
                        loading = false
                        message = error.message ?: "Đăng nhập thất bại"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text(
                if (loading) "Đang xử lý..."
                else if (isRegister) "Đăng ký"
                else "Đăng nhập"
            )
        }

        TextButton(
            onClick = {
                isRegister = !isRegister
                message = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (isRegister) "Đã có tài khoản? Đăng nhập"
                else "Chưa có tài khoản? Đăng ký"
            )
        }

        if (message.isNotBlank()) {
            Text(
                text = message,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
