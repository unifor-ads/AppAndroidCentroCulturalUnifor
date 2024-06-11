package com.example.centroculturalunifor.view.EditUserInfo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.centroculturalunifor.R
import com.example.centroculturalunifor.ScannedBarcodeActivity
import com.example.centroculturalunifor.databinding.ActivityEditUserInfoBinding
import com.example.centroculturalunifor.view.FeedObras.FeedObras
import com.example.centroculturalunifor.view.formLogin.FormLogin
import com.example.centroculturalunifor.view.obraViewFromQR.ObraViewFromQR
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditUserInfo : AppCompatActivity() {
    private lateinit var binding: ActivityEditUserInfoBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!isUserLoggedIn()) {
            val intent = Intent(this@EditUserInfo, FormLogin::class.java)
            startActivity(intent)
            this.finish()
        }

        setUserNameOnEditText()
        binding.editTextEmail.setText(getUserEmail())

        binding.buttonLogout.setOnClickListener{view->
            auth.signOut()
            val snackbar = Snackbar.make(view,"Usuário deslogado com sucesso!", Snackbar.LENGTH_SHORT)
            snackbar.setBackgroundTint(Color.BLUE)
            snackbar.show()
            navToMainScreen()
        }
        binding.buttonSave.setOnClickListener{view ->
            setUserNameOnDB(binding.editTextName.text.toString())
            val snackbar = Snackbar.make(view,"Informações atualizadas com sucesso!", Snackbar.LENGTH_SHORT)
            snackbar.setBackgroundTint(Color.GREEN)
            snackbar.show()
        }

        val bottomNavigationUser = binding.bottomNavigationUser.root
        bottomNavigationUser.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.item_1 -> {
                    val i = Intent(this, FeedObras::class.java )
                    startActivity(i)
                    finish()
                }
                R.id.item_2 -> {
                    val i = Intent(this, ScannedBarcodeActivity::class.java )
                    startActivity(i)
                    finish()
                }
                R.id.item_3 -> {
                    /*
                    val i = Intent(this, EditUserInfo::class.java )
                    startActivity(i)
                    finish()
                     */
                }
            }
            true
        }
        bottomNavigationUser.selectedItemId = R.id.item_3


    }
    private fun navToMainScreen(){
        val intent = Intent(this, FeedObras::class.java)
        this.finish()
        startActivity(intent)
    }
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
    private fun getUserID(): String {
        val user = auth.currentUser
        return user?.uid.toString()
    }

    private fun getUserEmail():String{
        val user = auth.currentUser
        return user?.email.toString()
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
        getUserName { userName ->
            binding.editTextName.setText(userName)
        }
    }

    private fun setUserNameOnDB(newName:String) {
        db.collection("Users").document(getUserID()).update("name",newName)
    }

}