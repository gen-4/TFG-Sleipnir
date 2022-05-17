package com.android.sleipnir

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream


class AddHorseActivity : AppCompatActivity() {

    private lateinit var imageContainer: ImageView
    private lateinit var imageUri: Uri

    companion object {
        private const val PERMISSION_CODE = 2
        private val IMAGE_PICK_CODE = 3
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    pickImageFromGallery()
                }
                else{
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            imageContainer.setImageURI(data?.data)

            val tmp = data?.data
            if (tmp != null)
                imageUri = tmp


        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_horse)


        val queue = Volley.newRequestQueue(this)
        val sharedPref : SharedPreferences = getSharedPreferences("userPreference",
            MODE_PRIVATE
        )
        val userId = sharedPref.getInt("userId", -1)
        val token = sharedPref.getString("token", "")
        var sureToken: String = ""

        if (token != null)
            sureToken = token


        imageContainer = findViewById(R.id.horse_image_picker)
        val imageBtn: Button = findViewById(R.id.horse_image_picker_btn)
        val nameInput: EditText = findViewById(R.id.horse_name_input)
        val heightInput: EditText = findViewById(R.id.horse_height_input)
        val weightInput: EditText = findViewById(R.id.horse_weight_input)
        val ageInput: EditText = findViewById(R.id.horse_age_input)
        var coat = 0
        var gender = 0
        var breed = ""



        val breedSpinner: Spinner = findViewById(R.id.horse_breed_input)
        val genderSpinner: Spinner = findViewById(R.id.horse_gender_input)
        val coatSpinner: Spinner = findViewById(R.id.horse_coat_input)

        val coatsAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf(
            getString(R.string.white_coat), getString(R.string.black_coat), getString(R.string.brown_coat),
            getString(R.string.sorrel_coat), getString(R.string.thrush_coat), getString(R.string.appaloosa_coat),
            getString(R.string.overo_coat), getString(R.string.roan_coat), getString(R.string.bay_coat), getString(R.string.elizabethan_coat)
        ))
        coatsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        coatSpinner.adapter = coatsAdapter

        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf(
            getString(R.string.stallion), getString(R.string.gelding), getString(R.string.mare)
        ))
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter

        val breedAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf(
        "Andaluz (PRE)", "Pura Raza Inglesa", "Pura Raza Galega", "Árabe", "Akhal-Teke",
        "Cuarto de milla", "Appaloosa", "Azteca", "Paso Peruano", "Painted Horse", "Paso Tennessee",
        "Mustang", "Shire", "Frisón", "Percherón", "Marwari", "Lusitano", "Bretón", "Bereber",
        "Criollo", "Gelder", "Hannoveriano", "Hispano-Árabe", "Kentucky Mountain", "Lipizzano",
        "Mongol", "Morgan", "Przewalski"
        ))
        breedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        breedSpinner.adapter = breedAdapter


        coatSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                coat = position
            }
        }

        genderSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                gender = position
            }
        }

        breedSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                val tmp = breedAdapter.getItem(position)
                if (tmp != null)
                    breed = tmp
            }
        }



        imageBtn.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED){
                    //permission denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                    //show popup to request runtime permission
                    requestPermissions(permissions, PERMISSION_CODE);
                }
                else{
                    //permission already granted
                    pickImageFromGallery();
                }
            }
            else{
                //system OS is < Marshmallow
                pickImageFromGallery();
            }
        }


        val btn: Button = findViewById(R.id.save_horse_btn)
        btn.setOnClickListener {

            var horseId = 0

            val url = "http://10.0.2.2:8000/user/".plus(userId)
                .plus("/add_horse")

            val jsonObj = JSONObject()
            jsonObj.put("name", nameInput.text)
            jsonObj.put("height", heightInput.text)
            jsonObj.put("weight", weightInput.text)
            jsonObj.put("age", ageInput.text)
            jsonObj.put("coat", coat)
            jsonObj.put("gender", gender)
            jsonObj.put("breed", breed)


            val jsonObjectRequest = object: JsonObjectRequest(
                Method.POST, url, jsonObj,
                { response ->
                    horseId = response.getInt("id")
                },
                { error ->
                    Log.d("error", error.toString())
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




            /*lateinit var imageData: ByteArray
            val inputStream = contentResolver.openInputStream(imageUri)
            inputStream?.buffered()?.use {
                imageData = it.readBytes()
            }


            val addImageUrl = "http://10.0.2.2:8000/user/".plus(userId)
                .plus("/horse/")
                .plus(horseId.toString())
                .plus("/add_image")

            val stringRequest = object : StringRequest(Method.POST, addImageUrl,
                Response.Listener<String> { response ->
                    try {
                        println("response")

                        Toast.makeText(applicationContext, response, Toast.LENGTH_LONG).show()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                object : Response.ErrorListener {
                    override fun onErrorResponse(volleyError: VolleyError) {
                        Toast.makeText(applicationContext, volleyError.message, Toast.LENGTH_LONG).show()
                    }
                })
            {
                @Throws(AuthFailureError::class)
                fun getByteData(): MutableMap<String, FileDataPart> {
                    var params = HashMap<String, FileDataPart>()

                    params.put("pic" , FileDataPart("image", imageData!!, "jpg"))

                    return params
                }
            }
            Volley.newRequestQueue(this).add(stringRequest)*/



        }

    }

}