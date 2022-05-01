package com.android.sleipnir.ui.show_register

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.android.sleipnir.DetailedRegisterActivity
import com.android.sleipnir.FillRouteInfoActivity
import com.android.sleipnir.R
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject


class ShowRegister : Fragment() {

    private lateinit var list: ListView

    private lateinit var recList: JSONArray



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list = requireActivity().findViewById(R.id.record_list)
        list.setOnItemClickListener {parent, _, position, _ ->
            val intnt = Intent(requireContext(), DetailedRegisterActivity::class.java)
            intnt.putExtra("recordId", recList.getJSONObject(position).getInt("id"))
            startActivity(intnt)
        }

        val sharedPref : SharedPreferences = requireActivity().getSharedPreferences("userPreference",
            AppCompatActivity.MODE_PRIVATE
        )

        val token = sharedPref.getString("token", "")

        val queue = Volley.newRequestQueue(requireContext())

        val url = "http://10.0.2.2:8000/route/rider_records/".plus(sharedPref.getInt("userId", -1).toString())

        val jsonObjectRequest = object: JsonArrayRequest(
            Method.GET, url, null,
            { response ->
                recList = response

                val recordList = arrayOfNulls<String>(response.length())
                var record: JSONObject

                for (i in 0 until response.length()) {
                    record = response.getJSONObject(i)
                    recordList[i] = record.getString("record_name")
                        .plus(" (")
                        .plus(record.getString("date").replace('T', ' '))
                        .plus(")")
                }

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, recordList)
                list.adapter = adapter
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_show_register, container, false)
    }

    companion object {
        fun newInstance() =
            ShowRegister().apply {

            }
    }
}