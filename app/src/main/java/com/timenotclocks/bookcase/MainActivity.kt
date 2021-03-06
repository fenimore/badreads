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

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.timenotclocks.bookcase.api.Exporter
import com.timenotclocks.bookcase.api.GoodReadImport
import com.timenotclocks.bookcase.api.LOG_EXP
import com.timenotclocks.bookcase.database.BookViewModel
import com.timenotclocks.bookcase.database.BookViewModelFactory
import com.timenotclocks.bookcase.database.BooksApplication
import com.timenotclocks.bookcase.ui.main.SectionsPagerAdapter

const val LOG_TAG = "Bookshelf"
const val EXTRA_SCAN = "scan_please"
const val REQ_EXP = 9832

class MainActivity : AppCompatActivity()  {

    private val goodReadsImport = 20
    private val bookViewModel: BookViewModel by viewModels {
        BookViewModelFactory((application as BooksApplication).repository)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        Log.d(LOG_TAG, "Created Main Activity")


/*        val data = """{"author" : "Jane Mayer", "authorExtras" : "", "bookId" : 0, "dateAdded" : "2020-11-25", "dateStarted": null, "dateRead" : null, "isbn10" : "0307970655", "isbn13" : "9780385535595", "notes" : null, "numberPages" : null, "originalYear" : 2016, "publisher" : "Doubleday", "rating" : null, "shelf" : "to-read", "subtitle" : null, "title" : "Dark Money", "year" : 2016}"""
        val intent = Intent(applicationContext, NewBookActivity::class.java).apply {
            putExtra(EXTRA_BOOK, data)
        }
        startActivity(intent)*/

/*        val intent = Intent(applicationContext, BookEditActivity::class.java).apply {
            putExtra(EXTRA_ID, 11.toLong())
        }
        startActivity(intent)*/
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_main_open_library_search -> {
                val intent = Intent(applicationContext, OpenLibrarySearchActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.menu_settings -> {
                val intent = Intent(applicationContext, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.menu_import -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                intent.type = "text/*"

                // TODO: add a confirmation
                startActivityForResult(intent, goodReadsImport)
                return true
            }
            R.id.menu_export -> {
                val createIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/csv"
                    putExtra(Intent.EXTRA_TITLE, "export.csv")
                }
                startActivityForResult(createIntent, REQ_EXP)
                true
            }
            R.id.menu_delete -> {
                Log.i(LOG_TAG, "Request deleting all data")
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
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
                builder.setTitle("Delete")
                builder.setMessage("Delete all your data? (This cannot be undone)")
                builder.create()
                builder.show()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // TODO: save from new book activity here
        if (requestCode == goodReadsImport && resultCode == RESULT_OK) {
            val uri: Uri? = data?.data
            uri?.let {
                contentResolver.openInputStream(it)?.let { inputStream ->
                    val imports = GoodReadImport().serialize(inputStream)
                    // TODO: add batch import
                    imports.map { book -> bookViewModel.insert(book)}
                    Snackbar.make(
                            findViewById(R.id.activity_main_layout),
                            "You've added ${imports.size} books",
                            Snackbar.LENGTH_LONG
                    ).setAction("Action", null).show()
                }
            }
        }


        if (requestCode == REQ_EXP && resultCode == RESULT_OK) {
            Log.i(LOG_EXP, "Huh So this is working")

            data?.data?.let { uri ->
                bookViewModel.allBooks.observe(this, { observable ->
                    observable?.let { allBooks ->
                        contentResolver.openOutputStream(uri)?.let { outputStream ->
                            Exporter().csv(outputStream, allBooks)
                        }
                        val sendIntent = Intent(Intent.ACTION_SEND)
                        sendIntent.type = "text/plain"
                        sendIntent.putExtra(Intent.EXTRA_STREAM, uri)
                        // Share the file after saving
                        startActivity(sendIntent)
                    }
                })
            }
        }




        super.onActivityResult(requestCode, resultCode, data)
    }


}


