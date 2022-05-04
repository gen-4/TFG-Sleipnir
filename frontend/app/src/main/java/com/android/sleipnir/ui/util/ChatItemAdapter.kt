package com.android.sleipnir.ui.util

import com.android.sleipnir.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import org.json.JSONObject


class ChatItemAdapter(context: Context, data: ArrayList<JSONObject>): BaseAdapter() {

    var context: Context? = context
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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val message: JSONObject = data[position]
        val type = message.getInt("type")
        var layoutId = 0
        var userName = message.getJSONObject("writer")
            .getJSONObject("user").getString("username")

        if (type == 0) {
            layoutId = R.layout.my_message_creator_item
            userName = context?.getString(R.string.you)
        } else if (type == 1) {
            layoutId = R.layout.my_message_item
            userName = context?.getString(R.string.you)
        } else if (type == 2) {
            layoutId = R.layout.partner_message_creator_item
        } else if (type == 3) {
            layoutId = R.layout.partner_message_item
        }



        var vi: View? = convertView
        if (vi == null) vi = inflater?.inflate(layoutId, null)
        val userNameText = vi?.findViewById(R.id.message_userName) as TextView
        userNameText.text = userName

        val messageText = vi.findViewById(R.id.message) as TextView
        messageText.text = message.getString("message")

        val dateText = vi.findViewById(R.id.message_date) as TextView
        dateText.text = message.getString("date").replace("T", " ")

        return vi
    }

}