package com.fiqsky.voneapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fiqsky.voneapp.ui.theme.VoneAppTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : ComponentActivity() {
    private val dataList = mutableStateListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VoneAppTheme {
                Column {
                    ReadDataFromFirebase(dataList)
                    DataListView(dataList) { item ->
                        // Hapus item dari Firebase saat tombol delete ditekan
                        deleteItemFromFirebase(item)
                    }
                }
            }
        }
    }

    private fun deleteItemFromFirebase(nama: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("pengguna")

        // Cari item yang memiliki parameter "nama" sesuai dengan nama yang ingin dihapus
        val query = databaseReference.orderByChild("nama").equalTo(nama)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    // Hapus item yang memiliki parameter "nama" sesuai dengan nama yang ingin dihapus
                    childSnapshot.ref.removeValue()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle kesalahan yang terjadi saat menghapus item
            }
        })
    }
}

@Composable
fun ReadDataFromFirebase(dataList: MutableList<String>) {
    val databaseReference = FirebaseDatabase.getInstance().reference.child("pengguna")

    val valueEventListener = remember {
        object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newDataList = mutableListOf<String>()
                for (childSnapshot in dataSnapshot.children) {
                    val nama = childSnapshot.child("nama").getValue(String::class.java)
                    nama?.let {
                        newDataList.add(it)
                    }
                }
                dataList.clear()
                dataList.addAll(newDataList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle kesalahan yang terjadi saat membaca data
            }
        }
    }

    DisposableEffect(databaseReference) {
        databaseReference.addValueEventListener(valueEventListener)
        onDispose {
            databaseReference.removeEventListener(valueEventListener)
        }
    }
}

@Composable
fun DataListView(dataList: List<String>, onDeleteItem: (String) -> Unit) {
    Text(
        text = "Data Vone.fun:",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(16.dp)
    )

    // State untuk menampilkan atau menyembunyikan dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(dataList) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable {
                        itemToDelete = item
                        showDeleteDialog = true
                    },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.delete_outline),
                        contentDescription = "Delete",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Red // Ganti warna sesuai kebutuhan
                    )
                }
            }
        }
    }

    // Membuat dialog konfirmasi di sini
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                // Tutup dialog jika pengguna menekan di luar dialog
                showDeleteDialog = false
            },
            title = {
                Text("Konfirmasi Penghapusan")
            },
            text = {
                Text("Apakah Anda yakin ingin menghapus item ini?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Hapus item dan tutup dialog
                        onDeleteItem(itemToDelete)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Ya, Hapus")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        // Tutup dialog tanpa menghapus item
                        showDeleteDialog = false
                    }
                ) {
                    Text("Batal")
                }
            }
        )
    }
}
