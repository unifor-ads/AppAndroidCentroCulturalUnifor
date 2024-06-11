package com.example.centroculturalunifor.view.EditObras

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.centroculturalunifor.databinding.ActivityEditObrasBinding
import com.example.centroculturalunifor.view.gerenciamentoObras.GerenciamentoObras
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class EditObras : AppCompatActivity() {
    private lateinit var binding: ActivityEditObrasBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val dbs = FirebaseStorage.getInstance()
    private lateinit var uri_image: Uri
    lateinit var obraID:String
    lateinit var obraTitle:String
    lateinit var obraDescription:String
    var obraIsVisible:Boolean = true
    lateinit var obraImageBitmap: Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditObrasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent != null && intent.hasExtra("intentDataGer")) {
            val intentData = intent.getStringExtra("intentDataGer")
            obraID = (intentData.toString())
            setObraDataOnTextView()
            setObraImageToImageView()
        } else {
            goToGerenciamentoDeObras()
        }

        binding.buttonAddPhoto.setOnClickListener{view ->
            pickImageFromGallery()
        }

        binding.buttonSave.setOnClickListener{view ->
            EditObra(obraID)
        }

        binding.buttonDelete.setOnClickListener { view ->
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Excluir Obra")
            alertDialogBuilder.setMessage("Tem certeza de que deseja excluir esta obra?")
            alertDialogBuilder.setPositiveButton("Sim") { _, _ ->
                deleteObra(obraID)
            }
            alertDialogBuilder.setNegativeButton("Não") { dialog, _ ->
                dialog.dismiss()
            }

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }


    }

    private fun deleteObra(ObraID:String) {
        db.collection("Obras").document(obraID).delete().addOnSuccessListener {
                dbs.getReference(obraID).delete().addOnSuccessListener {
                    Toast.makeText(this, "Obra excluída com sucesso!", Toast.LENGTH_SHORT).show()
                    goToGerenciamentoDeObras()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao excluir obra: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun pickImageFromGallery(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)

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

    fun editImage(ObraID:String){
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
                Toast.makeText(applicationContext,"Erro ao salvar imagem", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun EditObra(obraID:String){
        val titulo = binding.editTextTitle.text.toString()
        val descricao = binding.editTextDescription.text.toString()
        val isVisible = binding.switchIsVisible.isChecked
        val obraMap = hashMapOf<String, Any>(
            "title" to titulo,
            "description" to descricao,
            "isVisible" to isVisible
        )

        db.collection("Obras").document(obraID).update(obraMap).addOnCompleteListener{
            if (::uri_image.isInitialized){
                editImage(obraID)
            }
            Toast.makeText(applicationContext,"Obra salva com sucesso!", Toast.LENGTH_LONG).show()
            goToGerenciamentoDeObras()
        }.addOnFailureListener{
            Toast.makeText(applicationContext,"Erro ao salvar Obra!", Toast.LENGTH_LONG).show()
        }
    }


    fun goToGerenciamentoDeObras(){
        val intent = Intent(this@EditObras, GerenciamentoObras::class.java)
        this.finish()
        startActivity(intent)
    }

    private fun getObraData(callback: (MutableMap<String,Any>?) -> Unit) {
        db.collection("Obras").document(obraID)
            .addSnapshotListener { document, error ->
                if (document != null) {
                    val obra = document.data
                    callback(obra)
                }
            }
    }

    private fun setObraDataOnTextView() {
        getObraData { ObraData ->
            obraTitle = (ObraData?.get("title") as String?).toString()
            obraDescription = (ObraData?.get("description") as String?).toString()
            obraIsVisible = (ObraData?.get("isVisible") as Boolean?) ?: true
            binding.editTextTitle.setText(obraTitle)
            binding.editTextDescription.setText(obraDescription)
            binding.switchIsVisible.isChecked = obraIsVisible
        }
    }

    private fun setObraImageToImageView(){
        val imageReference = dbs.getReference(obraID)
        val localFile : File = File.createTempFile("tempfile",".jpg")
        imageReference.getFile(localFile).addOnSuccessListener {
            val bitmap: Bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            obraImageBitmap = bitmap
            binding.imageViewObra.setImageBitmap(obraImageBitmap)
        }
    }
}