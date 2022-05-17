package com.android.sleipnir.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.android.sleipnir.R
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ParticipantItemAdapter(context: Context, userId: Int, token: String, genders: List<String>,data: ArrayList<JSONObject>): BaseAdapter() {

    var context: Context? = context
    val genders = genders
    val userId = userId
    val token = token
    var data: ArrayList<JSONObject> = data
    private var inflater: LayoutInflater? = context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?


    override fun getCount(): Int {

        return data.size
    }

    override fun getItem(position: Int): Any? {

        return data[position]
    }

    override fun getItemId(position: Int): Long {

        return position.toLong()
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val horse: JSONObject = data[position]
        val horseName = horse.getString("name")
        val imagePath = horse.getString("image")
        val horseGenderInt = horse.getInt("gender")
        val horseOwner = horse.getJSONObject("owner").getJSONObject("user").getString("username")
        val horseGender = genders[horseGenderInt]


        val queue = Volley.newRequestQueue(context)



        var vi: View? = convertView
        if (vi == null) vi = getInflatedLayout()
        if (vi != null) {
            val nameText = vi.findViewById(R.id.horse_name) as TextView
            nameText.text = horseName

            val genderText = vi.findViewById(R.id.horse_gender) as TextView
            genderText.text = horseGender

            val ownerText = vi.findViewById(R.id.horse_owner) as TextView
            ownerText.text = horseOwner

            val image: ImageView = vi.findViewById(R.id.horse_image) as ImageView

            val imageUrl = "http://10.0.2.2:8000/static/".plus(imagePath)
            val imageRequest = ImageRequest(
                imageUrl,
                {bitmap ->
                    image.setImageBitmap(bitmap)
                },
                0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.ARGB_8888,
                {error ->
                    Log.d("error", error.toString())
                }
            )
            queue.add(imageRequest)

        }

        return vi
    }

    private fun getInflatedLayout(): View? {
        return LayoutInflater.from(context).inflate(
            R.layout.participant_row, null)

    }
}