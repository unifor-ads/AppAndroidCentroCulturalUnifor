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
import androidx.recyclerview.widget.RecyclerView
import com.example.centroculturalunifor.R
import com.example.centroculturalunifor.view.EditObras.EditObras
import com.example.centroculturalunifor.view.FeedObras.FeedObras
import com.example.centroculturalunifor.view.obraViewFromQR.ObraViewFromQR
import com.google.firebase.storage.FirebaseStorage
import java.io.File


class CustomAdapter(private var dataSet: Map<String, Map<String, Any>>, val isGerenciamento: Boolean = false) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dbs = FirebaseStorage.getInstance()


    class ViewHolderCommon(view: View) : RecyclerView.ViewHolder(view) {

        val layout : LinearLayout = view.findViewById(R.id.obra_item_vert_layout)
        // Define click listener for the ViewHolder's View
        val textTitulo: TextView = view.findViewById(R.id.textTitulo)
        val textDescricao: TextView = view.findViewById(R.id.textDescricao)
        val imgObra: ImageView = view.findViewById(R.id.imgObra)


    }

    class ViewHolderGer(view: View) : RecyclerView.ViewHolder(view) {
        val layout : LinearLayout = view.findViewById(R.id.obra_item_horiz_layout)
        // Define click listener for the ViewHolder's View
        val textTituloGer: TextView = view.findViewById(R.id.textTituloGer)
        val textDescricaoGer: TextView = view.findViewById(R.id.textDescricaoGer)
        val imgObraGer: ImageView = view.findViewById(R.id.imgObraGer)
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == 1) {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.obra_item_horiz, viewGroup, false)
            return ViewHolderGer(view)
        } else {

            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.obra_item_vert, viewGroup, false)
            return ViewHolderCommon(view)
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val id = dataSet.keys.toList()[position]
        val item: Map<String, Any>? = dataSet[id]


        Log.d("type", viewHolder.itemViewType.toString())

        if(viewHolder.itemViewType == 1) {
            val viewHolderGer: ViewHolderGer = viewHolder as ViewHolderGer
            viewHolderGer.layout.setOnClickListener {
                val intent = Intent(
                    viewHolder.itemView.context,
                    EditObras::class.java,
                )
                intent.putExtra("intentDataGer", id)

                viewHolder.itemView.context.startActivity(intent)
            }
            viewHolderGer.textTituloGer.text = (item?.get("title") ?: "") as String
            viewHolderGer.textDescricaoGer.text = (item?.get("description") ?: "") as String
            setObraImageToImageView(id, viewHolderGer.imgObraGer)
        } else {
            val viewHolderCommon: ViewHolderCommon = viewHolder as ViewHolderCommon
            viewHolderCommon.layout.setOnClickListener {
                val intent = Intent(
                   viewHolder.itemView.context,
                    ObraViewFromQR::class.java,
                )
                intent.putExtra("intentData", id)

                viewHolder.itemView.context.startActivity(intent)
            }
            viewHolderCommon.textTitulo.text = (item?.get("title") ?: "") as String
            viewHolderCommon.textDescricao.text = (item?.get("description") ?: "") as String
            setObraImageToImageView(id, viewHolderCommon.imgObra)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    override fun getItemViewType(position: Int): Int {
        return if (isGerenciamento)  1 else 0
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(data: Map<String, Map<String, Any>>) {
        dataSet = data
        notifyDataSetChanged()
    }

    private fun setObraImageToImageView(id: String, imageView: ImageView){
        val imageReference = dbs.getReference(id)
        val localFile : File = File.createTempFile("tempfile",".jpg")
        imageReference.getFile(localFile).addOnSuccessListener {
            val bitmap: Bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            imageView.setImageBitmap(bitmap)
        }
    }

}
