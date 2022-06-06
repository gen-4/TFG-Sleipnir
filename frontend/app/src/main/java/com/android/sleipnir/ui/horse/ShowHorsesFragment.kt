package com.android.sleipnir.ui.horse

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.sleipnir.AddHorseActivity
import com.android.sleipnir.DetailedRegisterActivity
import com.android.sleipnir.R
import com.android.sleipnir.ShowDetailedHorse
import com.android.sleipnir.ui.util.HorseItemAdapter
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ShowHorsesFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_show_horses, container, false)
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


        val horses: ArrayList<JSONObject> = ArrayList()
        val adapter = HorseItemAdapter(requireActivity(), userId, sureToken, horses)
        val list: ListView = requireActivity().findViewById(R.id.horse_container)
        list.setOnItemClickListener { _, _, position, _ ->
            val intnt = Intent(requireActivity(), ShowDetailedHorse::class.java)
            intnt.putExtra("name", horses[position].getString("name"))
            intnt.putExtra("height", horses[position].getDouble("height"))
            intnt.putExtra("weight", horses[position].getDouble("weight"))
            intnt.putExtra("age", horses[position].getInt("age"))
            intnt.putExtra("coat", horses[position].getInt("coat"))
            intnt.putExtra("gender", horses[position].getInt("gender"))
            intnt.putExtra("breed", horses[position].getString("breed"))
            intnt.putExtra("image", horses[position].getString("image"))
            startActivity(intnt)
        }


        list.adapter = adapter

        val addBtn: Button = requireActivity().findViewById(R.id.add_horse_btn)
        addBtn.setOnClickListener {

            val intent = Intent(requireContext(), AddHorseActivity::class.java)
            startActivity(intent)

            val url = "http://10.0.2.2:8000/user/".plus(userId)
                .plus("/horses")
            val jsonObjectRequest = object: JsonArrayRequest(
                Method.GET, url, null,
                { response ->

                    horses.clear()
                    for (i in 0 until response.length()) {
                        val horse = response.getJSONObject(i)

                        horses.add(horse)
                    }

                    adapter.notifyDataSetChanged()
                },
                { error ->
                    Log.d("error", error.toString())
                    Toast.makeText(requireActivity(), requireActivity().getString(R.string.error_get_horses), Toast.LENGTH_SHORT).show()
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



        val url = "http://10.0.2.2:8000/user/".plus(userId)
            .plus("/horses")
        val jsonObjectRequest = object: JsonArrayRequest(
            Method.GET, url, null,
            { response ->

                for (i in 0 until response.length()) {
                    val horse = response.getJSONObject(i)

                    horses.add(horse)
                }

                adapter.notifyDataSetChanged()
            },
            { error ->
                Log.d("error", error.toString())
                Toast.makeText(requireActivity(), requireActivity().getString(R.string.error_get_observers), Toast.LENGTH_SHORT).show()
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