package com.example.centroculturalunifor.view.gerenciamentoObras

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.centroculturalunifor.R
import com.example.centroculturalunifor.adapter.CustomAdapter
import com.example.centroculturalunifor.databinding.ActivityFormObrasBinding
import com.example.centroculturalunifor.databinding.ActivityGerenciamentoObrasBinding
import com.example.centroculturalunifor.view.formObras.FormObras
import com.example.centroculturalunifor.view.obraViewFromQR.ObraViewFromQR
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class GerenciamentoObras : AppCompatActivity() {
    private lateinit var binding: ActivityGerenciamentoObrasBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val dbs = FirebaseStorage.getInstance()

    private var customAdapter: CustomAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGerenciamentoObrasBinding.inflate(layoutInflater)
        setContentView(binding.root)



        customAdapter = CustomAdapter(emptyMap(), isGerenciamento = true)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewGerenciamento)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = customAdapter


        binding.goToFormObras.setOnClickListener{view->
            val intent = Intent(this@GerenciamentoObras, FormObras::class.java)
            startActivity(intent)
        }

        getObrasData{obrasData->
            customAdapter?.updateData(obrasData)
        }
    }

    override fun onResume() {
        super.onResume()
        getObrasData{obrasData->
            customAdapter?.updateData(obrasData)
        }
    }

    private fun getObrasData(callback: (MutableMap<String,Map<String, Any>>) -> Unit){
        val documentsMap = mutableMapOf<String,Map<String, Any>>()

        db.collection("Obras").get().addOnSuccessListener{documents ->
            for (document in documents){
                documentsMap[document.id] = document.data
            }
            val obrasData = documentsMap
            callback(obrasData)
        }
    }
}