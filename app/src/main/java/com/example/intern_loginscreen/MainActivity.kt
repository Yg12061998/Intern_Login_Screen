package com.example.intern_loginscreen

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.testapp.util.ConnectionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    lateinit var etEmail: TextInputEditText
    lateinit var etPass: TextInputEditText
    lateinit var btnLogin: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etEmail = findViewById(R.id.etEmail)
        etPass = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        lateinit var email: String
        lateinit var pass: String

        btnLogin.setOnClickListener {

            email = etEmail.text.toString()
            pass = etPass.text.toString()

            if (ConnectionManager().checkConnection(this)) {           /// Internet Check - Is Internet available or NOT

                try {
                    volleyRequest(email, pass)                                /// Volley Request is call by user Defined volleyRequest(email : String , pass:String) --> present in next ScreenShot

                } catch (e: JSONException) {
                    Toast.makeText(applicationContext, "JSON Error occured", Toast.LENGTH_SHORT)
                        .show()
                }

            } else {
                val dialog = AlertDialog.Builder(this)                 /// if there is no internet access then it sends to settings to turn on the internet
                dialog.setTitle("Failed!")
                dialog.setMessage("Network is not connected")
                dialog.setPositiveButton("Open Settings") { text, listener ->
                    val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                    startActivity(settingsIntent)
                    finish()

                }
                dialog.setNegativeButton("Exit App") { text, listener ->
                    finishAffinity()
                }

                dialog.create()
                dialog.show()

            }
        }

    }

    fun volleyRequest(email: String, pass: String) {                                                           // logic to fetch API call

        val queue = Volley.newRequestQueue(this)
        val url = "https://api.cinquex.com/api/internshala/login"

        val jsonParams = JSONObject()
        jsonParams.put("email", email)
        jsonParams.put("password", pass)
        var msg : String = ""

        val jsonObjectRequest =
            object : JsonObjectRequest(Request.Method.POST, url, jsonParams,
                Response.Listener {

                    msg = it.getString("message")                                                         // msg store the result for SUCCESS LOGIN
                    createDialogCall(msg, 1)                                                              // Now, we have the message, So we can create Dialog Box - Mode: 1 means Dialog box contain "Log out" button

                }, Response.ErrorListener {
                    val str = String(it.networkResponse.data, Charsets.UTF_8)                                   // parsing JSON Object for FAILED LOGIN
                    val data = JSONObject(str)                                                                  // data contain JSON Object (which contains message)

                    if (it.networkResponse.statusCode == 403) {                                                 // Error Code : 403 - Wrong Credentials
                        msg = data.getString("message").toString()                                                  // msg store message if Error-403 happened
                    }else{                                                                                      // Error Code : 400 - Validation Error
                        msg = data.getJSONObject("message").getJSONArray("email")[0].toString()              // msg store message if Error-400 happened --- here JSON Object has JSON Array (So it takes 2 calls)
                    }
                    createDialogCall(msg, 2)                                                              // Now, we have the message, So we can create Dialog Box - Mode: 2 means Dialog box contain "Try Again" button
                }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    var headers = HashMap<String, String>()
                    headers["Body-type"] = "JSON"
                    return headers
                }

            }
        queue.add(jsonObjectRequest)
    }


    fun createDialogCall(msg : String, mode : Int){                                                 // Dialog Box creation function

        // Mode:
        // 1 -> Log out
        // 2 -> Try Again

        val dialog = AlertDialog.Builder(this)

        dialog.setTitle("Status")
        dialog.setMessage(msg)

        dialog.setNegativeButton("Exit App") { text, listener ->
            finishAffinity()
        }

        if(mode == 1)
            dialog.setPositiveButton("Log out") { text, listener ->
                etEmail.setText("")
                etPass.setText("")
            }
        else
            dialog.setPositiveButton("Try Again") { text, listener ->
                etEmail.setText("")
                etPass.setText("")
            }

        dialog.create()
        dialog.show()
    }


}