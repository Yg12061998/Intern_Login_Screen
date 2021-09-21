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

            //Toast.makeText(applicationContext, "" + email+": "+pass , Toast.LENGTH_SHORT).show()


            if (ConnectionManager().checkConnection(this)) {

                try {
                    volleyRequest(email, pass)

                } catch (e: JSONException) {
                    Toast.makeText(applicationContext, "JSON Error occured", Toast.LENGTH_SHORT)
                        .show()
                }

            } else {
                val dialog = AlertDialog.Builder(this)
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

    fun volleyRequest(email: String, pass: String) {
        val queue = Volley.newRequestQueue(this)
        val url = "https://api.cinquex.com/api/internshala/login"

        val jsonParams = JSONObject()
        jsonParams.put("email", email)
        jsonParams.put("password", pass)
        var msg : String = ""

        val jsonObjectRequest =
            object : JsonObjectRequest(Request.Method.POST, url, jsonParams,
                Response.Listener {

                    msg = it.getString("message")
                    createDialogCall(msg, 1)

                }, Response.ErrorListener {
                    val str = String(it.networkResponse.data, Charsets.UTF_8)
                    val data = JSONObject(str)

                    if (it.networkResponse.statusCode == 403) {
                        msg = data.getString("message").toString()
                    }else{
                        msg = data.getJSONObject("message").getJSONArray("email")[0].toString()
                    }
                    createDialogCall(msg, 2)
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

    fun createDialogCall(msg : String, mode : Int){

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