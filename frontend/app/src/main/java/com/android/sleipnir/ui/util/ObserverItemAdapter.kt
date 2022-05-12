package com.android.sleipnir.ui.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.sleipnir.R
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ObserverItemAdapter(context: Context, userId: Int, token: String, data: ArrayList<JSONObject>): BaseAdapter() {


    var context: Context? = context
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
        return 4
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val observer: JSONObject = data[position]
        val id = observer.getInt("id")
        val user = observer.getString("telegram_user")


        var vi: View? = convertView
        if (vi == null) vi = getInflatedLayout()
        if (vi != null) {
            val userText = vi.findViewById(R.id.observer_user) as TextView
            userText.text = user

            val deleteBtn: Button = vi.findViewById(R.id.delete_btn) as Button
            deleteBtn.setOnClickListener {


                val queue = Volley.newRequestQueue(context)
                val url = "http://10.0.2.2:8000/user/".plus(userId)
                    .plus("/delete_observer/")
                    .plus(id.toString())

                val jsonObjectRequest = object: JsonObjectRequest(
                    Method.POST, url, null,
                    { response ->

                        data.removeAt(position)
                        this.notifyDataSetChanged()

                    },
                    { error ->
                        Log.d("error", error.toString())
                    }
                )
                {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                            headers["Authorization"] = "Token $token"
                        return headers
                    }
                }

                queue.add(jsonObjectRequest)

            }

        }

        return vi
    }

    private fun getInflatedLayout(): View? {
        return LayoutInflater.from(context).inflate(
            R.layout.observer_row, null)

    }


}