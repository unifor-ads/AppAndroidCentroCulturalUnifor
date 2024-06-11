
package com.example.centroculturalunifor.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.centroculturalunifor.R
import com.example.centroculturalunifor.view.FeedObras.FeedObras
import com.example.centroculturalunifor.view.obraViewFromQR.ObraViewFromQR
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.UUID
import kotlin.properties.Delegates


class ComentarioAdapter(val obraID: String, private var dataSet: Map<String, Map<String, Any>>) :
    RecyclerView.Adapter<ComentarioAdapter.ViewHolder>() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val layout : LinearLayout = view.findViewById(R.id.comentario_layout)

        //val imgAvatar = view.findViewById<ImageView>(R.id.imgAvatar)
        val textUsername = view.findViewById<TextView>(R.id.textUsername)
        val textComentario = view.findViewById<TextView>(R.id.textComentario)

    }



    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {

            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.comentario_item, viewGroup, false)
            return ViewHolder(view)

    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val id = dataSet.keys.toList()[position]
        val item: Map<String, Any>? = dataSet[id]

        viewHolder.textUsername.text = (item?.get("userName") ?: "") as String
        viewHolder.textComentario.text = (item?.get("comment") ?: "") as String
        viewHolder.layout.setOnLongClickListener {

            if (isAdminLoggedIn()){
                val builder: AlertDialog.Builder = AlertDialog.Builder(viewHolder.itemView.context)
                builder
                    .setMessage("Deseja ocultar esse comentário")
                    .setTitle("Ocultar comentário")
                    .setPositiveButton("Apagar") {dialog, which ->
                        db.collection("Obras").document(obraID)
                            .collection("comments").document(id)
                            .update("isVisible", false).addOnSuccessListener {
                                Log.d("isNotVisible", "set $id as not visible")
                            }
                    }
                    .setNegativeButton("Manter") {dialog, which ->
                        dialog.dismiss()
                    }

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
            true
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(data: Map<String, Map<String, Any>>) {
        dataSet = data
        notifyDataSetChanged()
    }

    private fun getUserEmail(callback: (String) -> Unit){
        val user = auth.currentUser
        callback(user?.email ?: "")
    }
    private fun isAdminLoggedIn():Boolean{
        val emailAdmin = "admin@unifor.com.br"
        var test by Delegates.notNull<Boolean>()
        getUserEmail { userEmail ->
            if (userEmail != emailAdmin) {
                test = false
            } else {
                test = true
            }
        }
        return test
    }

}
