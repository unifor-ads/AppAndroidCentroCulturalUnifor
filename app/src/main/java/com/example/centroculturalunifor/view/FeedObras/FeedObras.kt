package com.example.centroculturalunifor.view.FeedObras

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.centroculturalunifor.R
import com.example.centroculturalunifor.ScannedBarcodeActivity
import com.example.centroculturalunifor.adapter.CustomAdapter
import com.example.centroculturalunifor.databinding.ActivityFeedObrasBinding
import com.example.centroculturalunifor.view.EditUserInfo.EditUserInfo
import com.example.centroculturalunifor.view.formLogin.FormLogin
import com.example.centroculturalunifor.view.formObras.FormObras
import com.example.centroculturalunifor.view.gerenciamentoObras.GerenciamentoObras
import com.example.centroculturalunifor.view.obraViewFromQR.ObraViewFromQR
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FeedObras : AppCompatActivity() {
    private lateinit var binding: ActivityFeedObrasBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var customAdapter: CustomAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFeedObrasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setBtnGoToObrasManagerVisibleState()
        setUserNameOnEditText()
        setObrasDataOnTextView()

        customAdapter = CustomAdapter(emptyMap())

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = customAdapter


        binding.btnGoToObrasManager.setOnClickListener{view->
            val intent = Intent(this@FeedObras, GerenciamentoObras::class.java)
            startActivity(intent)
        }

        val bottomNavigationFeed = binding.bottomNavigationFeed.root
        bottomNavigationFeed.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.item_1 -> {
                    /*
                    val i = Intent(this, FeedObras::class.java )
                    startActivity(i)
                    finish()
                    */
                }
                R.id.item_2 -> {
                    val i = Intent(this, ScannedBarcodeActivity::class.java )
                    startActivity(i)
                    finish()
                }
                R.id.item_3 -> {
                    val i = Intent(this, EditUserInfo::class.java )
                    startActivity(i)
                    finish()
                }
            }
            true
        }
        bottomNavigationFeed.selectedItemId = R.id.item_1
    }

    override fun onResume() {
        super.onResume()
        setBtnGoToObrasManagerVisibleState()
        setUserNameOnEditText()
        setObrasDataOnTextView()
    }
    private fun getObrasData(callback: (MutableMap<String,Map<String, Any>>) -> Unit){
        val documentsMap = mutableMapOf<String,Map<String, Any>>()

        db.collection("Obras").whereEqualTo("isVisible", true).get().addOnSuccessListener{documents ->
            for (document in documents){
                documentsMap[document.id] = document.data
            }
            val obrasData = documentsMap
            callback(obrasData)
        }
    }

    private fun setObrasDataOnTextView(){
        getObrasData{obrasData->
            customAdapter?.updateData(obrasData)
        }
    }

    private fun getUserID(): String {
        val user = auth.currentUser
        return user?.uid.toString()
    }

    private fun getUserEmail(callback: (String) -> Unit){
        val user = auth.currentUser
        callback(user?.email ?: "")
    }

    private fun getUserName(callback: (String) -> Unit) {
        val userID = getUserID()

        db.collection("Users").document(userID)
            .addSnapshotListener { document, error ->
                if (document != null) {
                    val userName = document.getString("name").toString()
                    callback(userName)
                }
            }
    }

    private fun setUserNameOnEditText() {
        if (getUserID() != "null") {
            getUserName { userName ->
                binding.autenticatedUser.text = "Bem-vindo, " + (userName.split(" "))[0] + "!"
            }
        }
    }

    private fun setBtnGoToObrasManagerVisibleState(){
        val emailAdmin = "admin@unifor.com.br"
        getUserEmail { userEmail ->
            if (userEmail != emailAdmin) {
                binding.btnGoToObrasManager.visibility = View.GONE
            } else {
                binding.btnGoToObrasManager.visibility = View.VISIBLE
            }
        }
    }


}