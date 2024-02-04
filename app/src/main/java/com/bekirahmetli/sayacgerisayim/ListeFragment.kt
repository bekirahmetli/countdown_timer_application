package com.bekirahmetli.sayacgerisayim

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bekirahmetli.sayacgerisayim.databinding.FragmentListeBinding
import java.sql.Blob

class ListeFragment : Fragment() {

    var sayacIsmiListesi = ArrayList<String>()
    var sayacTakvimSaatListesi  = ArrayList<String>()
    var sayacIdListesi = ArrayList<Int>()
    var sayacGorselListesi = ArrayList<Bitmap>()

    private lateinit var listeAdapter : ListeRecyclerAdapter

    private lateinit var binding: FragmentListeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.show()

        binding = FragmentListeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listeAdapter = ListeRecyclerAdapter(sayacIsmiListesi,sayacIdListesi,sayacTakvimSaatListesi, sayacGorselListesi)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = listeAdapter

        sqlVeriAlma()
    }

    fun sqlVeriAlma(){
        try {
            activity?.let {
                val database = it.openOrCreateDatabase("Sayaclar",Context.MODE_PRIVATE,null)
                val cursor = database.rawQuery("SELECT * FROM sayaclar",null)
                val sayacIsmiIndex = cursor.getColumnIndex("sayacismi")
                val sayacTakvimSaatIndex = cursor.getColumnIndex("sayactakvimsaat")
                val sayacIdIndex = cursor.getColumnIndex("id")
                val sayacGorselIndex = cursor.getColumnIndex("gorsel")

                sayacIsmiListesi.clear()
                sayacIdListesi.clear()
                sayacTakvimSaatListesi.clear()
                sayacGorselListesi.clear()

                while (cursor.moveToNext()){
                    sayacIsmiListesi.add(cursor.getString(sayacIsmiIndex))
                    sayacIdListesi.add(cursor.getInt(sayacIdIndex))
                    sayacTakvimSaatListesi.add(cursor.getString(sayacTakvimSaatIndex))


                    val blob = cursor.getBlob(sayacGorselIndex)
                    val bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.size)
                    sayacGorselListesi.add(bitmap)

                }
                listeAdapter.notifyDataSetChanged()
                cursor.close()
            }
        }catch (e:Exception){

        }
    }

}