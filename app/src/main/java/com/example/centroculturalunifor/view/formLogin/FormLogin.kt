package com.example.centroculturalunifor.view.formLogin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.centroculturalunifor.databinding.ActivityFormLoginBinding
import com.example.centroculturalunifor.view.FeedObras.FeedObras
import com.example.centroculturalunifor.view.formCadastro.FormCadastro
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException


class FormLogin : AppCompatActivity() {
    private lateinit var binding: ActivityFormLoginBinding
    private val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFormLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textViewGoToRegister.setOnClickListener{
            val intent = Intent(this,FormCadastro::class.java)
            startActivity(intent)
        }

        binding.buttonLogin.setOnClickListener{view ->
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (email.isEmpty()||password.isEmpty()){
                val snackbar = Snackbar.make(view,"Preencha todos os campos!", Snackbar.LENGTH_SHORT)
                snackbar.setBackgroundTint(Color.RED)
                snackbar.show()
            } else {
                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener{authResult->
                    if (authResult.isSuccessful){
                        binding.editTextEmail.setText("")
                        binding.editTextPassword.setText("")
                        val snackbar = Snackbar.make(view,"Login efetuado com sucesso!",Snackbar.LENGTH_SHORT)
                        snackbar.setBackgroundTint(Color.BLUE)
                        snackbar.show()
                        navToMainScreen()
                    }
                }.addOnFailureListener{exception ->
                    val exceptionMsg = when(exception){
                        is FirebaseAuthInvalidUserException -> "E-mail e/ou Senha inválida!"
                        is FirebaseAuthInvalidCredentialsException -> "E-mail e/ou Senha inválida!"
                        is FirebaseNetworkException -> "Sem conexão com a Internet!"
                        else -> "Erro ao logar usuário!"
                    }
                    val snackbar = Snackbar.make(view,exceptionMsg,Snackbar.LENGTH_SHORT)
                    snackbar.setBackgroundTint(Color.RED)
                    snackbar.show()
                }
            }
        }


    }

    private fun navToMainScreen(){
        val intent = Intent(this,FeedObras::class.java)
        this.finish()
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()

        val usuarioAtual = FirebaseAuth.getInstance().currentUser

        if(usuarioAtual != null){
            navToMainScreen()
        }
    }

}