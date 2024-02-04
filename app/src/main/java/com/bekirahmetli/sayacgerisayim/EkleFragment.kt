package com.bekirahmetli.sayacgerisayim

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bekirahmetli.sayacgerisayim.databinding.FragmentEkleBinding
import android.net.Uri
import androidx.navigation.Navigation
import java.io.ByteArrayOutputStream
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*


class EkleFragment : Fragment() {

    var secilenGorsel : Uri ?= null
    var secilenBitmap : Bitmap ?= null



    private lateinit var binding: FragmentEkleBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.hide()

        binding = FragmentEkleBinding.inflate(inflater,container,false)
        binding.textViewDateTime.setOnClickListener {
            showDateTimePicker()
        }
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.eklebutton.setOnClickListener {
            eklekaydet(it)
        }
        binding.ekleimageView.setOnClickListener {
            gorselSec(it)
        }
        binding.silbutton.setOnClickListener {
            sil(it)
        }
        binding.okikonu.setOnClickListener {
            val action = EkleFragmentDirections.actionEkleFragmentToListeFragment()
            Navigation.findNavController(view).navigate(action)
        }

        arguments?.let {
            var gelenbilgi = EkleFragmentArgs.fromBundle(it).bilgi
            if (gelenbilgi == ("menudengeldim")) {
                binding.ekleisimtext.setText("")
                binding.textViewDateTime.setText("")
                binding.eklebutton.visibility = View.VISIBLE
                binding.silbutton.visibility = View.GONE

                val gorselSecmeArkaPlani =
                    BitmapFactory.decodeResource(context?.resources, R.drawable.img)
                binding.ekleimageView.setImageBitmap(gorselSecmeArkaPlani)
            } else {


                binding.eklebutton.visibility = View.VISIBLE
                val secilenId = EkleFragmentArgs.fromBundle(it).id
                context?.let {
                    try {
                        val db =
                            it.openOrCreateDatabase("Sayaclar", Context.MODE_PRIVATE, null)
                        val cursor = db.rawQuery(
                            "SELECT * FROM sayaclar WHERE id=?",
                            arrayOf(secilenId.toString())
                        )

                        val sayacIsmiIndex = cursor.getColumnIndex("sayacismi")
                        val sayacTarihSaatIndex = cursor.getColumnIndex("sayactakvimsaat")
                        val sayacGorseli = cursor.getColumnIndex("gorsel")

                        while (cursor.moveToNext()) {
                            binding.ekleisimtext.setText(cursor.getString(sayacIsmiIndex))
                            binding.textViewDateTime.setText(cursor.getString(sayacTarihSaatIndex))

                            val byteDizisi = cursor.getBlob(sayacGorseli)
                            val bitmap =
                                BitmapFactory.decodeByteArray(byteDizisi, 0, byteDizisi.size)
                            binding.ekleimageView.setImageBitmap(bitmap)
                        }
                        cursor.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }


    fun sil(view: View) {
        val mesaj2 = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        mesaj2.setTitle("Sayacı sil")
        mesaj2.setMessage("Sayacı silmek istediğinize emin misiniz?")
        mesaj2.setPositiveButton("Evet", DialogInterface.OnClickListener { dialogInterface, i ->
            val secilenId = EkleFragmentArgs.fromBundle(requireArguments()).id
            context?.let {
                try {
                    val db = it.openOrCreateDatabase("Sayaclar", Context.MODE_PRIVATE, null)
                    val sqlString = "DELETE FROM sayaclar WHERE id=?"
                    val statement = db.compileStatement(sqlString)
                    statement.bindLong(1, secilenId.toLong())
                    statement.executeUpdateDelete()


                    val action = EkleFragmentDirections.actionEkleFragmentToListeFragment()
                    Navigation.findNavController(view).navigate(action)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
        mesaj2.setNegativeButton("İptal", DialogInterface.OnClickListener { dialogInterface, i ->

            val action = EkleFragmentDirections.actionEkleFragmentToListeFragment()
            Navigation.findNavController(view).navigate(action)
        })
        mesaj2.show()
    }



    fun eklekaydet(view: View) {
        val sayacIsmi = binding.ekleisimtext.text.toString()
        val sayacTarihSaat = binding.textViewDateTime.text.toString()

        if (secilenBitmap != null) {
            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!, 300)
            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteDizisi = outputStream.toByteArray()
            try {
                context?.let {
                    val database = it.openOrCreateDatabase("Sayaclar", Context.MODE_PRIVATE, null)
                    arguments?.let { bundle ->
                        val gelenbilgi = EkleFragmentArgs.fromBundle(bundle).bilgi
                        if (gelenbilgi == "menudengeldim") {
                            database.execSQL("CREATE TABLE IF NOT EXISTS sayaclar(id INTEGER PRIMARY KEY, sayacismi VARCHAR, sayactakvimsaat DATETIME, gorsel BLOB)")

                            val sqlString = "INSERT INTO sayaclar(sayacismi, sayactakvimsaat, gorsel) VALUES (?, ?, ?)"
                            val statement = database.compileStatement(sqlString)
                            statement.bindString(1, sayacIsmi)
                            statement.bindString(2, sayacTarihSaat)
                            statement.bindBlob(3, byteDizisi)
                            statement.execute()
                        } else {

                            val secilenId = EkleFragmentArgs.fromBundle(bundle).id
                            val sqlString = "UPDATE sayaclar SET sayacismi = ?, sayactakvimsaat = ?, gorsel = ? WHERE id = ?"
                            val statement = database.compileStatement(sqlString)
                            statement.bindString(1, sayacIsmi)
                            statement.bindString(2, sayacTarihSaat)
                            statement.bindBlob(3, byteDizisi)
                            statement.bindLong(4, secilenId.toLong())
                            statement.execute()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


            val action = EkleFragmentDirections.actionEkleFragmentToListeFragment()
            Navigation.findNavController(view).navigate(action)

        }else {
            Toast.makeText(requireContext(), "Lütfen bir resim seçin", Toast.LENGTH_SHORT).show()
        }
    }



    fun gorselSec(view: View){
        activity?.let {
            if(ContextCompat.checkSelfPermission(it.applicationContext,android.Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
            }else{
                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == 1){
            if (grantResults.size > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){

                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 2 && resultCode==Activity.RESULT_OK && data!=null){
            secilenGorsel = data.data
            try {
                context?.let {
                    if (secilenGorsel != null){
                        if (Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(it.contentResolver,secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.ekleimageView.setImageBitmap(secilenBitmap)
                        }else{
                            secilenBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver,secilenGorsel)
                            binding.ekleimageView.setImageBitmap(secilenBitmap)
                        }
                    }
                }
            }catch (e : Exception){
                e.printStackTrace()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun kucukBitmapOlustur(kullanicininSectigiBitmap:Bitmap,maximumBoyut:Int):Bitmap{
        var width = kullanicininSectigiBitmap.width
        var height = kullanicininSectigiBitmap.height

        val bitmapOrani : Double = width.toDouble()/height.toDouble()

        if(bitmapOrani > 1){

            width = maximumBoyut
            val kisaltilmisHeight = width/bitmapOrani
            height = kisaltilmisHeight.toInt()
        }else{

            height = maximumBoyut
            val kisaltilmisWidth = height*bitmapOrani
            width = kisaltilmisWidth.toInt()


            val temp = width
            width = height
            height = temp
        }
        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap,width,height,true)
    }


    private fun showDateTimePicker() {
        val now = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, monthOfYear)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)


                if (selectedDate.timeInMillis >= now.timeInMillis) {
                    showTimePicker(selectedDate)
                } else {
                    Toast.makeText(requireContext(), "Geçmiş bir tarih seçemezsiniz.", Toast.LENGTH_SHORT).show()
                }
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showTimePicker(selectedDate: Calendar) {
        val now = Calendar.getInstance()
        val timePicker = TimePickerDialog(
            requireContext(),
            { _: TimePicker, hourOfDay: Int, minute: Int ->
                selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedDate.set(Calendar.MINUTE, minute)


                if (selectedDate.timeInMillis >= now.timeInMillis) {
                    val selectedDateTime = selectedDate.time
                    val formattedDateTime = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(selectedDateTime)
                    binding.textViewDateTime.text = formattedDateTime
                } else {
                    Toast.makeText(requireContext(), "Geçmiş bir saat seçemezsiniz.", Toast.LENGTH_SHORT).show()
                }
            },
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            true
        )
        timePicker.show()
    }



}