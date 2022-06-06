package com.android.sleipnir.ui.friend

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.sleipnir.R
import com.android.sleipnir.ui.util.ObserverItemAdapter
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject


class ShowFriendsFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_show_friends, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val queue = Volley.newRequestQueue(requireContext())
        val sharedPref : SharedPreferences = requireContext().getSharedPreferences("userPreference",
            AppCompatActivity.MODE_PRIVATE
        )
        val userId = sharedPref.getInt("userId", -1)
        val token = sharedPref.getString("token", "")
        var sureToken: String = ""

        if (token != null)
            sureToken = token


        val friends: ArrayList<JSONObject> = ArrayList()
        val adapter = ObserverItemAdapter(requireContext(), userId, sureToken, friends, true)
        val list: ListView = requireActivity().findViewById(R.id.observer_container)
        list.adapter = adapter

        val addBtn: Button = requireActivity().findViewById(R.id.observer_save_btn)
        addBtn.setOnClickListener {

            val observerInput: EditText = requireActivity().findViewById(R.id.observer_input)

            val url = "http://10.0.2.2:8000/user/".plus(userId)
                .plus("/add_friend")

            val jsonBody = JSONObject()
            jsonBody.put("username", observerInput.text)

            val jsonObjectRequest = object: JsonObjectRequest(
                Method.POST, url, jsonBody,
                { response ->

                    friends.add(response)
                    adapter.notifyDataSetChanged()

                },
                { error ->
                    Log.d("error", error.toString())
                    Toast.makeText(requireActivity(), requireActivity().getString(R.string.error_add_friend), Toast.LENGTH_SHORT).show()
                }
            )
            {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    if (token != null)
                        headers["Authorization"] = "Token $token"
                    return headers
                }
            }

            queue.add(jsonObjectRequest)

            observerInput.text.clear()

        }





        val url = "http://10.0.2.2:8000/user/".plus(userId)
            .plus("/friends")
        val jsonObjectRequest = object: JsonArrayRequest(
            Method.GET, url, null,
            { response ->

                for (i in 0 until response.length()) {
                    val observer = response.getJSONObject(i)

                    friends.add(observer)
                }

                adapter.notifyDataSetChanged()
            },
            { error ->
                Log.d("error", error.toString())
                Toast.makeText(requireActivity(), requireActivity().getString(R.string.error_get_friends), Toast.LENGTH_SHORT).show()
            }
        )
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                if (token != null)
                    headers["Authorization"] = "Token $token"
                return headers
            }
        }

        queue.add(jsonObjectRequest)

    }


}