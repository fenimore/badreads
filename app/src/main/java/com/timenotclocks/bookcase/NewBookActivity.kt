/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.timenotclocks.bookcase

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import java.io.StringReader

/**
 * Activity for entering a word.
 */




class NewBookActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_book)
        //unregisterForContextMenu()

        val base = "openlibrary.org"
        val url = "https://$base/isbn/"

        val editTitleView = findViewById<EditText>(R.id.edit_title)
        val coverUrlView = findViewById<EditText>(R.id.cover_url)
        val isbnView = findViewById<EditText>(R.id.isbn)
        val isbn13View = findViewById<EditText>(R.id.isbn13)
        val authorNameView = findViewById<EditText>(R.id.author_name)

        val searchView = findViewById<SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // 9781250134769
                //"978-3-16-148410-0

                if (query?.replace("-", "")?.toLongOrNull() == null) {
                    return false
                }
                val isbn = query.replace("-", "").toLong()

                val stringRequest = StringRequest(Request.Method.GET, url + "$isbn.json",
                        { response ->
                            // Display the first 500 characters of the response string.
                            Log.i("BK", response)
                            val entry = Klaxon().parseJsonObject(StringReader(response))
                            Log.i("BK", entry.toString())
                            //isbnView?.setText(entry["isbn_10"])
                            //if (isbnView != null) isbnView.setText(entry["isbn_10"])
                        },
                        {
                            Log.i("BK","DOESNT WORK")
                        })
                RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(stringRequest)

                Log.i("BK","Llego al querysubmit $query?")
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Log.i("BK","Llego al $newText?")
                return true
            }
        })

        val button = findViewById<Button>(R.id.button_save)
        button.setOnClickListener {
            val replyIntent = Intent()
            if (TextUtils.isEmpty(editTitleView.text)) {
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                val word = editTitleView.text.toString()
                replyIntent.putExtra(EXTRA_REPLY, word)
                setResult(Activity.RESULT_OK, replyIntent)
            }
            finish()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {

        } else {
        }
    }

    companion object {
        const val EXTRA_REPLY = "com.timenotclocks.android.wordlistsql.REPLY"
    }

    fun searchIsbn(ctx: Context, isbn: Long) {

    }
}

