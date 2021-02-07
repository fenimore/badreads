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
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.Buffer


class MainActivity : AppCompatActivity() {

    private val newWordActivityRequestCode = 1
    private val goodReadsImport = 2

    private val bookViewModel: BookViewModel by viewModels {
        BookViewModelFactory((application as BooksApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = BookListAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        bookViewModel.allBooks.observe(owner = this) { books ->
            // Update the cached copy of the books in the adapter.
            books.let { adapter.submitList(it) }
        }

        val fab0: FloatingActionButton = findViewById<FloatingActionButton>(R.id.fab0)
        val fab1: FloatingActionButton = findViewById<FloatingActionButton>(R.id.fab1)
        val fab2: FloatingActionButton = findViewById<FloatingActionButton>(R.id.fab2)
        var isFABOpen = false
        fab0.setOnClickListener {
            if (!isFABOpen) {
                isFABOpen = true
                fab1.animate().translationY(-resources.getDimension(R.dimen.standard_75))
                fab2.animate().translationY(-resources.getDimension(R.dimen.standard_155))  // next step is 155
            } else {
                isFABOpen = false

                fab1.animate().translationY(0F)
                fab2.animate().translationY(0F)
            }
        }
        fab1.setOnClickListener {
            val intent = Intent(this@MainActivity, NewBookActivity::class.java)
            startActivityForResult(intent, newWordActivityRequestCode)
        }
        fab2.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            intent.type = "*/*"

            startActivityForResult(intent, goodReadsImport)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)
        if (requestCode == goodReadsImport && resultCode == Activity.RESULT_OK) {

            if (intentData?.data == null) {
                Log.i("BK", "Nothing to report")
                return
            }

            val uri: Uri? = intentData?.data
            if (uri != null) {
                val inputStream = contentResolver.openInputStream(uri)
                if (inputStream != null){
                    val books = GoodReadImport().serialize(inputStream)
                    books.forEach {
                        Log.i("BK", it.toString())
                        bookViewModel.insert(it)
                    }
                }
            }
        }

        if (requestCode == newWordActivityRequestCode && resultCode == Activity.RESULT_OK) {
            intentData?.getStringExtra(NewBookActivity.EXTRA_REPLY)?.let { reply ->
                //val book = Book(reply)
                //bookViewModel.insert(book)
            }
        }

        Toast.makeText(
                applicationContext,
                R.string.empty_not_saved,
                Toast.LENGTH_LONG
        ).show()

    }
}


