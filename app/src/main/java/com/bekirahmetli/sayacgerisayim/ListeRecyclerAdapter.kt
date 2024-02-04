package com.bekirahmetli.sayacgerisayim



import android.graphics.Bitmap
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bekirahmetli.sayacgerisayim.databinding.RecyclerRowBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ListeRecyclerAdapter(val sayacListesi:ArrayList<String>,val idListesi:ArrayList<Int>,val sayacadapterTakvimSaatListesi:ArrayList<String>,val sayacGorselListesi: ArrayList<Bitmap>) : RecyclerView.Adapter<ListeRecyclerAdapter.SayacHolder>()  {
    class SayacHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {
        var timer: CountDownTimer? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SayacHolder {
        return SayacHolder(
            RecyclerRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return sayacListesi.size
    }


    override fun onBindViewHolder(holder: SayacHolder, position: Int) {
        val sortedIndices = sayacadapterTakvimSaatListesi.indices.sortedBy { parseDateTime(sayacadapterTakvimSaatListesi[it]).time }
        val sortedSayacListesi = ArrayList<String>()
        val sortedIdListesi = ArrayList<Int>()
        val sortedTakvimSaatListesi = ArrayList<String>()
        val sortedGorselListesi = ArrayList<Bitmap>()

        for (index in sortedIndices) {
            sortedSayacListesi.add(sayacListesi[index])
            sortedIdListesi.add(idListesi[index])
            sortedTakvimSaatListesi.add(sayacadapterTakvimSaatListesi[index])
            sortedGorselListesi.add(sayacGorselListesi[index])
        }

        holder.binding.recyclerisimtext.text = sortedSayacListesi[position]
        holder.binding.recyclertakvimsaatText.text = sortedTakvimSaatListesi[position]
        holder.binding.recyclerView.setImageBitmap(sortedGorselListesi[position])

        val selectedDateTime = parseDateTime(sortedTakvimSaatListesi[position])
        val currentTime = Calendar.getInstance().time
        val timeDiffInMillis = selectedDateTime.time - currentTime.time

        holder.timer?.cancel()

        if (timeDiffInMillis > 0) {
            holder.timer = object : CountDownTimer(timeDiffInMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val remainingTime = calculateRemainingTime(millisUntilFinished)
                    holder.binding.recyclertakvimsaatText.text = remainingTime
                }

                override fun onFinish() {
                    holder.binding.recyclertakvimsaatText.text = "Süre Bitti"
                }
            }
            holder.timer?.start()
        } else {
            holder.binding.recyclertakvimsaatText.text = "Süre Bitti"
        }

        holder.itemView.setOnClickListener {
            holder.timer?.cancel()

            val action = ListeFragmentDirections.actionListeFragmentToEkleFragment(
                "recyclerdangeldim",
                sortedIdListesi[position]
            )
            Navigation.findNavController(it).navigate(action)
        }
    }


    private fun calculateRemainingTime(millisUntilFinished: Long): String {
        val seconds = Math.abs(millisUntilFinished / 1000) % 60
        val minutes = Math.abs(millisUntilFinished / (1000 * 60)) % 60
        val hours = Math.abs(millisUntilFinished / (1000 * 60 * 60)) % 24
        val days = Math.abs(millisUntilFinished / (1000 * 60 * 60 * 24))

        return "$days gün $hours saat $minutes dakika $seconds saniye"
    }

    private fun parseDateTime(dateTimeString: String): Date {
        val format = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        return try {
            format.parse(dateTimeString)
        } catch (e: ParseException) {
            e.printStackTrace()
            Date()
        }
    }

}



