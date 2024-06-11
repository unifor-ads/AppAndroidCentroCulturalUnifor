package com.example.centroculturalunifor.view.obraViewFromQR

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.centroculturalunifor.R
import com.example.centroculturalunifor.ScannedBarcodeActivity
import com.example.centroculturalunifor.adapter.ComentarioAdapter
import com.example.centroculturalunifor.databinding.ActivityObraViewFromQrBinding
import com.example.centroculturalunifor.view.EditUserInfo.EditUserInfo
import com.example.centroculturalunifor.view.FeedObras.FeedObras
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.UUID

class ObraViewFromQR : AppCompatActivity() {
    private lateinit var binding: ActivityObraViewFromQrBinding
    private val db = FirebaseFirestore.getInstance()
    private val dbs = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    lateinit var obraID:String
    lateinit var obraTitle:String
    lateinit var obraDescription:String
    lateinit var obraImageBitmap: Bitmap
    val maxCharObraDescription = 150
    val textEndObraDescription = " ..."

    private var comentarioAdapter: ComentarioAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityObraViewFromQrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent != null && intent.hasExtra("intentData")) {
            val intentData = intent.getStringExtra("intentData")
            obraID = (intentData.toString())
            setObraDataOnTextView()
            setObraImageToImageView()
        } else {
//            QRCodeInit()
        }

        comentarioAdapter = ComentarioAdapter( obraID, emptyMap())

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewComentarios)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = comentarioAdapter

        getComentarios()

        binding.textViewDescription.setOnClickListener{view ->
            formatTextViewDescription()
        }

        binding.imageViewArtwork.setOnClickListener {view ->
            adjustImageViewLayout(binding.imageViewArtwork,obraImageBitmap)
        }

        binding.btnSendComment.setOnClickListener{view ->
            getCurrentUser { userId ->
                if ((userId != "null")){
                    if (binding.editTextComment.text.toString() != ""){
                        sendComment()
                    }
                } else{
                    Toast.makeText(applicationContext,"Para comentar é necessário estar logado.",Toast.LENGTH_LONG).show()
                }
            }
        }

        val bottomNavigationQr = binding.bottomNavigationQr.root
        bottomNavigationQr.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.item_1 -> {
                    val i = Intent(this, FeedObras::class.java )
                    startActivity(i)
                    finish()
                }
                R.id.item_2 -> {
                    /*
                    val i = Intent(this, ObraViewFromQR::class.java )
                    startActivity(i)
                    finish()
                     */
                }
                R.id.item_3 -> {
                    val i = Intent(this, EditUserInfo::class.java )
                    startActivity(i)
                    finish()
                }
            }
            true
        }
        bottomNavigationQr.selectedItemId = R.id.item_2

    }

    fun sendComment() {
        buildCommentMap { commentMap ->
            val commentID = UUID.randomUUID().toString()
            db.collection("Obras").document(obraID)
                .collection("comments").document(commentID)
                .set(commentMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext, "Comentário enviado com sucesso!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(applicationContext, "Falha ao enviar comentário!", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnSuccessListener {
                    binding.editTextComment.setText("")
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(applicationContext, "Erro: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        }
    }


    fun buildCommentMap(callback: (HashMap<String, Any>) -> Unit) {
        getUserName{currentUserName ->
            val comment = binding.editTextComment.text.toString()
            val createAt = FieldValue.serverTimestamp()
            val isVisible = true
            val userName = currentUserName
            callback( hashMapOf(
                "comment" to comment,
                "createAt" to createAt,
                "isVisible" to isVisible,
                "userName" to userName
            ))
        }
    }


    private fun getCurrentUser(callback: (String) -> Unit){
        val userId = auth.currentUser?.uid
        callback(userId.toString())
    }

    private fun getUserName(callback: (String) -> Unit) {
        getCurrentUser{userID ->
                db.collection("Users").document(userID)
                    .addSnapshotListener { document, error ->
                        if (document != null) {
                            val userName = document.getString("name").toString()
                            callback(userName)
                        }
                    }
        }
    }



    fun formatTextViewDescription(){
        if (obraDescription.length > maxCharObraDescription) {
            if (binding.textViewDescription.text.length <= (maxCharObraDescription + textEndObraDescription.length)){
                binding.textViewDescription.text = obraDescription
            } else {
                binding.textViewDescription.text = obraDescription.substring(0, maxCharObraDescription).trim() + textEndObraDescription
            }
        }
    }

//    fun QRCodeInit(){
//        val intent = Intent(
//            this@ObraViewFromQR,
//            ScannedBarcodeActivity::class.java
//        )
//        scanBarcodeLauncher.launch(intent)
//    }
//
//    private val scanBarcodeLauncher = registerForActivityResult<Intent, ActivityResult>(
//        ActivityResultContracts.StartActivityForResult()
//    ) { result: ActivityResult ->
//        if (result.resultCode == RESULT_OK) {
//            val data = result.data
//            if (data != null && data.hasExtra("intentData")) {
//                val intentData = data.getStringExtra("intentData")
//                obraID = (intentData.toString())
//                setObraDataOnTextView()
//                setObraImageToImageView()
//            }
//        }
//    }
    private fun getObraIDfun():String{
        return obraID
    }
    private fun getObraData(callback: (MutableMap<String,Any>?) -> Unit) {

        db.collection("Obras").document(getObraIDfun())
            .addSnapshotListener { document, error ->
                if (error != null){
                    Toast.makeText(applicationContext, "Erro ao obter dados da obra: ${error.message}", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@ObraViewFromQR, FeedObras::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
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
            binding.textViewTitle.text = obraTitle
            binding.textViewDescription.text = obraDescription
            formatTextViewDescription()
        }
    }


    private fun setObraImageToImageView(){

        val imageReference = dbs.getReference(getObraIDfun())
        val localFile : File = File.createTempFile("tempfile",".jpg")
        imageReference.getFile(localFile).addOnSuccessListener {
            val bitmap: Bitmap = BitmapFactory.decodeFile(localFile.absolutePath)

            obraImageBitmap = bitmap
            binding.imageViewArtwork.setImageBitmap(obraImageBitmap)

        }
    }


    private fun adjustImageViewLayout(imageView: ImageView, bitmap: Bitmap) {
        obraImageBitmap = rotateBitmap(bitmap)
        binding.imageViewArtwork.setImageBitmap(obraImageBitmap)
    }
    fun rotateBitmap(bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(90f) // Rotaciona a imagem em 90 graus no sentido horário
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun getComentarios() {
        db.collection("Obras").document(getObraIDfun()).collection("comments").whereEqualTo("isVisible",true).addSnapshotListener { documents, error ->
            if(documents != null) {

                Log.d("documents obra", documents.documents.toString())


                val documentsMap = mutableMapOf<String,Map<String, Any>>()
                for (document in documents) {
                    documentsMap[document.id] = document.data
                }
                comentarioAdapter?.updateData(documentsMap)
            }
        }
    }
}