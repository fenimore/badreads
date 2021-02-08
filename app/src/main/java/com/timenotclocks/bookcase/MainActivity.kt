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
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.timenotclocks.bookcase.ui.main.SectionsPagerAdapter
import kotlinx.coroutines.NonCancellable.cancel
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
        application

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        val fab0: FloatingActionButton = findViewById<FloatingActionButton>(R.id.fab0)
        val fab1: FloatingActionButton = findViewById<FloatingActionButton>(R.id.fab1)
        val fab2: FloatingActionButton = findViewById<FloatingActionButton>(R.id.fab2)
        val fab3: FloatingActionButton = findViewById<FloatingActionButton>(R.id.fab3)
        var isFABOpen = false
        fab0.setOnClickListener {
            if (!isFABOpen) {
                isFABOpen = true
                fab1.animate().translationY(-resources.getDimension(R.dimen.standard_75))
                fab2.animate().translationY(-resources.getDimension(R.dimen.standard_155))
                fab3.animate().translationY(-resources.getDimension(R.dimen.standard_230))
            } else {
                isFABOpen = false

                fab1.animate().translationY(0F)
                fab2.animate().translationY(0F)
                fab3.animate().translationY(0F)
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
            intent.type = "text/*"

            startActivityForResult(intent, goodReadsImport)
        }
        fab3.setOnClickListener {
            val alertDialog: AlertDialog? = this?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setPositiveButton("OK",
                            DialogInterface.OnClickListener { dialog, id ->
                                // User clicked OK button
                                bookViewModel.deleteAll()
                            })
                    setNegativeButton("CANCEL",
                            DialogInterface.OnClickListener { dialog, id ->
                                // User cancelled the dialog
                            })
                }
                builder.setTitle("Delete All Your Data!?")
                builder.setMessage("This cannot be undone.")
                builder.create()
            }
            alertDialog?.show()
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

        // TODO: update this
        Toast.makeText(
                applicationContext,
                R.string.empty_not_saved,
                Toast.LENGTH_LONG
        ).show()

    }
}


