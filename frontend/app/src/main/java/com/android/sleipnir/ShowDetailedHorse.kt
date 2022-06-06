package com.android.sleipnir

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.Volley

class ShowDetailedHorse : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_detailed_horse)



        val horseName: TextView = findViewById(R.id.horse_name)
        val horseImg: ImageView = findViewById(R.id.horse_image)
        val horseHeight: TextView = findViewById(R.id.horse_height)
        val horseWeight: TextView = findViewById(R.id.horse_weight)
        val horseAge: TextView = findViewById(R.id.horse_age)
        val horseCoat: TextView = findViewById(R.id.horse_coat)
        val horseGender: TextView = findViewById(R.id.horse_gender)
        val horseBreed: TextView = findViewById(R.id.horse_breed)
        var coat = ""
        var gender = ""

        val intCoat = intent.getIntExtra("coat", -1)
        when (intCoat) {
            0 -> coat = getString(R.string.white_coat)
            1 -> coat = getString(R.string.black_coat)
            2 -> coat = getString(R.string.brown_coat)
            3 -> coat = getString(R.string.sorrel_coat)
            4 -> coat = getString(R.string.thrush_coat)
            5 -> coat = getString(R.string.appaloosa_coat)
            6 -> coat = getString(R.string.overo_coat)
            7 -> coat = getString(R.string.roan_coat)
            8 -> coat = getString(R.string.bay_coat)
            9 -> coat = getString(R.string.elizabethan_coat)
        }

        val intGender = intent.getIntExtra("gender", -1)
        when (intGender) {
            0 -> gender = getString(R.string.stallion)
            1 -> gender = getString(R.string.gelding)
            2 -> gender = getString(R.string.mare)
        }

        val queue = Volley.newRequestQueue(this)

        horseName.text = intent.getStringExtra("name")
        horseHeight.text = intent.getDoubleExtra("height", 0.0).toString().plus(" cm")
        horseWeight.text = intent.getDoubleExtra("weight", 0.0).toString().plus(" kg")
        horseAge.text = intent.getIntExtra("age", 0).toString()
        horseCoat.text = coat
        horseGender.text = gender
        horseBreed.text = intent.getStringExtra("breed")

        val imageUrl = "http://10.0.2.2:8000/static/".plus(intent.getStringExtra("image"))
        val imageRequest = ImageRequest(
            imageUrl,
            {bitmap ->
                horseImg.setImageBitmap(bitmap)
            },
            0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.ARGB_8888,
            {error ->
                Log.d("error", error.toString())
                Toast.makeText(this, getString(R.string.error_get_horse_image), Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(imageRequest)

    }
}