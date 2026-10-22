
package com.example.gklaptrinhdidonh

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProductScreen(
    isAdmin: Boolean,
    onLogout: () -> Unit
) {
    val db = remember { FirebaseFirestore.getInstance() }

    var products by remember { mutableStateOf(emptyList<Product>()) }
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        val listener = db.collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    message = error.message ?: "Không tải được sản phẩm"
                    return@addSnapshotListener
                }

                products = snapshot?.documents?.map { doc ->
                    Product(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        type = doc.getString("type") ?: "",
                        price = doc.getLong("price") ?: 0L,
                        image = doc.getString("image") ?: ""
                    )
                } ?: emptyList()
            }

        onDispose {
            listener.remove()
        }
    }

    fun clearForm() {
        name = ""
        type = ""
        price = ""
        imageUrl = ""
        editingId = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Quản lý sản phẩm")

            TextButton(onClick = onLogout) {
                Text("Đăng xuất")
            }
        }

        if (isAdmin) {
            Text(if (editingId == null) "Thêm sản phẩm" else "Sửa sản phẩm")

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên sản phẩm") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = type,
                onValueChange = { type = it },
                label = { Text("Loại sản phẩm") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Giá sản phẩm") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Link hình ảnh") },
                placeholder = {
                    Text("https://example.com/image.jpg")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (imageUrl.isNotBlank()) {
                Spacer(Modifier.height(8.dp))

                AsyncImage(
                    model = imageUrl.trim(),
                    contentDescription = "Ảnh xem trước",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val parsedPrice = price.toLongOrNull()

                        if (
                            name.isBlank() ||
                            type.isBlank() ||
                            parsedPrice == null ||
                            parsedPrice < 0
                        ) {
                            message = "Vui lòng nhập tên, loại và giá hợp lệ"
                        } else {
                            val data = mapOf(
                                "name" to name.trim(),
                                "type" to type.trim(),
                                "price" to parsedPrice,
                                "image" to imageUrl.trim()
                            )

                            val request = if (editingId == null) {
                                db.collection("products").add(data)
                            } else {
                                db.collection("products")
                                    .document(editingId!!)
                                    .set(data)
                            }

                            request.addOnSuccessListener {
                                clearForm()
                                message = "Lưu sản phẩm thành công"
                            }.addOnFailureListener { error ->
                                message = error.message ?: "Lưu thất bại"
                            }
                        }
                    }
                ) {
                    Text(if (editingId == null) "Thêm" else "Cập nhật")
                }

                if (editingId != null) {
                    TextButton(onClick = { clearForm() }) {
                        Text("Hủy sửa")
                    }
                }
            }
        } else {
            Text("Danh sách sản phẩm")
        }

        if (message.isNotBlank()) {
            Text(
                text = message,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products, key = { it.id }) { product ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = product.image,
                            contentDescription = product.name,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        ) {
                            Text(product.name)
                            Text("Loại: ${product.type}")
                            Text("Giá: ${product.price}")
                        }

                        if (isAdmin) {
                            Column {
                                TextButton(
                                    onClick = {
                                        name = product.name
                                        type = product.type
                                        price = product.price.toString()
                                        imageUrl = product.image
                                        editingId = product.id
                                        message = ""
                                    }
                                ) {
                                    Text("Sửa")
                                }

                                TextButton(
                                    onClick = {
                                        db.collection("products")
                                            .document(product.id)
                                            .delete()
                                            .addOnSuccessListener {
                                                if (editingId == product.id) {
                                                    clearForm()
                                                }
                                                message = "Đã xóa sản phẩm"
                                            }
                                            .addOnFailureListener { error ->
                                                message = error.message
                                                    ?: "Xóa thất bại"
                                            }
                                    }
                                ) {
                                    Text("Xóa")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
