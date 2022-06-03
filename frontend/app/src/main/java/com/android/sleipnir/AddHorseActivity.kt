package com.android.sleipnir

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.sleipnir.ui.horse.ShowHorsesFragment
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
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


    @SuppressLint("WrongThread")
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

            val bitmap = (imageContainer.getDrawable() as BitmapDrawable).getBitmap()
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
            val image = stream.toByteArray()

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
            jsonObj.put("image", userId.toString().plus("horse").plus("/base64/")
                .plus(Base64.encodeToString(image, Base64.DEFAULT)))


            val jsonObjectRequest = object: JsonObjectRequest(
                Method.POST, url, jsonObj,
                { response ->
                    horseId = response.getInt("id")
                    val intnt = Intent(this, DrawerActivity::class.java)
                    startActivity(intnt)
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

        }

    }

}