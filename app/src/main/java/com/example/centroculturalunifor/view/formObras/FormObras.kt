package com.example.centroculturalunifor.view.formObras


import android.app.ProgressDialog
import android.content.Intent

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.centroculturalunifor.databinding.ActivityFormObrasBinding
import com.example.centroculturalunifor.view.gerenciamentoObras.GerenciamentoObras
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class FormObras : AppCompatActivity() {
    private lateinit var binding: ActivityFormObrasBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val dbs = FirebaseStorage.getInstance()

    private lateinit var uri_image: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFormObrasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonAddPhoto.setOnClickListener{view ->
            pickImageFromGallery()
        }

        binding.buttonSave.setOnClickListener{view ->
            if (::uri_image.isInitialized){
                uploadObra()
            } else {
                Toast.makeText(applicationContext,"Selecione uma imagem!",Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun pickImageFromGallery(){
        val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI)

        startActivityForResult(Intent.createChooser(intent,"Escolha uma Imagem"),0)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null) {
            if (requestCode == 0){
                uri_image = data.data!!
                try {
                    binding.imageViewObra.setImageURI(uri_image)
                }catch (e:Exception){
                    Log.e("Upload_Image", "Erro ao carregar imagem: ${e.message}", e)
                }
            }
        }
    }

    fun uploadImage(ObraID:String){
        if (::uri_image.isInitialized){
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Salvando informações da Obra...")
            progressDialog.setMessage("Processando...")
            progressDialog.show()

            val ref: StorageReference = dbs.getReference().child(ObraID)
            ref.putFile(uri_image!!).addOnSuccessListener {
                progressDialog.dismiss()

            }.addOnFailureListener{
                progressDialog.dismiss()
                Toast.makeText(applicationContext,"Erro ao salvar imagem",Toast.LENGTH_LONG).show()
            }
        }
    }

    fun uploadObra(){
        val titulo = binding.editTextTitle.text.toString()
        val descricao = binding.editTextDescription.text.toString()
        val isVisible = binding.switchIsVisible.isChecked
        val obraMap = hashMapOf(
            "title" to titulo,
            "description" to descricao,
            "isVisible" to isVisible
        )
        val obraID = UUID.randomUUID().toString()
        db.collection("Obras").document(obraID).set(obraMap).addOnCompleteListener{
            if (::uri_image.isInitialized){
                uploadImage(obraID)
            }
            Toast.makeText(applicationContext,"Obra salva com sucesso!",Toast.LENGTH_LONG).show()
            goToGerenciamentoDeObras()
        }.addOnFailureListener{
            Toast.makeText(applicationContext,"Erro ao salvar Obra!",Toast.LENGTH_LONG).show()
        }
    }

    fun goToGerenciamentoDeObras(){
        val intent = Intent(this@FormObras, GerenciamentoObras::class.java)
        this.finish()
        startActivity(intent)
    }

}