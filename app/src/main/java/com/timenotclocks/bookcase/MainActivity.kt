/*
 * Copyright (C) 2021 Fenimore Love
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

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.timenotclocks.bookcase.api.Exporter
import com.timenotclocks.bookcase.api.GoodReadImport
import com.timenotclocks.bookcase.database.BookViewModel
import com.timenotclocks.bookcase.database.BookViewModelFactory
import com.timenotclocks.bookcase.database.BooksApplication
import com.timenotclocks.bookcase.database.emptyBook
import com.timenotclocks.bookcase.ui.main.EXTRA_BOOK
import com.timenotclocks.bookcase.ui.main.SectionsPagerAdapter
import java.time.LocalDate
import java.time.format.DateTimeFormatter


const val LOG_TAG = "Bookshelf"
const val REQ_EXP = 9832
const val REQ_EXP_READ = 5833

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
        val landingTab = PreferenceManager.getDefaultSharedPreferences(
                this
        ).getString("landing_tab", "0")?.toInt() ?: 0
        viewPager.setCurrentItem(landingTab)


        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val darkModeValues = resources.getStringArray(R.array.dark_mode_values)
        val darkPref = prefs.getString(getString(R.string.dark_mode_prerence), darkModeValues[0])
        when (darkPref) {
            darkModeValues[0] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            darkModeValues[1] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            darkModeValues[2] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            darkModeValues[3] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        }

        Log.d(LOG_TAG, "Created Main Activity")

        //val intent = Intent(applicationContext, OpenLibrarySearchActivity::class.java)
        //startActivity(intent)
        /*
        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            val intent = Intent(applicationContext, OpenLibrarySearchActivity::class.java)
            startActivity(intent)
        }
                val intent = Intent(applicationContext, OpenLibrarySearchActivity::class.java).apply {
                    putExtra(EXTRA_SCAN, true)
                }

                startActivity(intent)
                return true*/

        /*
        val data = """{"author" : "Jane Mayer", "authorExtras" : "", "bookId" : 0, "dateAdded" : "2020-11-25", "dateStarted": null, "dateRead" : null, "isbn10" : "0307970655", "isbn13" : "9780385535595", "notes" : null, "numberPages" : null, "originalYear" : 2016, "publisher" : "Doubleday", "rating" : null, "shelf" : "to-read", "subtitle" : null, "title" : "Dark Money", "year" : 2016}"""
        val intent = Intent(applicationContext, NewBookActivity::class.java).apply {
            putExtra(EXTRA_BOOK, data)
        }
        startActivity(intent)

        var data = """{"author" : "Jane Mayer", "authorExtras" : "", "bookId" : 0, "dateAdded" : "2020-11-25", "dateStarted": null,
            | "dateRead" : null, "isbn10" : null, "isbn13" : "9780385535595", "notes" : null, "numberPages" : null,
            | "originalYear" : 2016, "publisher" : null, "rating" : null, "shelf" : "to-read", "subtitle" : null,
            |  "title" : "Dark Money", "year" : null}""".trimMargin()
        val intent = Intent(applicationContext, NewBookActivity::class.java).apply {
            putExtra(EXTRA_BOOK, data)
        }
        startActivity(intent)

        val data = """{"author" : "Jane Mayer", "authorExtras" : "", "bookId" : 0, "dateAdded" : "2020-11-25", "dateStarted": null, "dateRead" : null, "isbn10" : "0307970655", "isbn13" : "9780385535595", "notes" : null, "numberPages" : null, "originalYear" : 2016, "publisher" : "Doubleday", "rating" : null, "shelf" : "to-read", "subtitle" : null, "title" : "Dark Money", "year" : 2016}"""
        val intent = Intent(applicationContext, NewBookActivity::class.java).apply {
            putExtra(EXTRA_BOOK, data)
        }
        startActivity(intent)
        */
        /*val intent = Intent(applicationContext, BookViewActivity::class.java).apply {
            putExtra(EXTRA_ID, 710.toLong())
        }
        startActivity(intent)

        val intent = Intent(applicationContext, AboutActivity::class.java)
        startActivity(intent)
        */
        // Dark Money
        //val intent = Intent(applicationContext, BookViewActivity::class.java).apply {putExtra(EXTRA_ID, 262.toLong())}
        //val intent = Intent(applicationContext, SettingsActivity::class.java)
        // val intent = Intent(applicationContext, OpenLibrarySearchActivity::class.java)
        // val intent = Intent(applicationContext, ChartActivity::class.java)
        //startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_main_add_manual -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Manual Book Entry")
                builder.setMessage("Enter the new book title:")

                val input = EditText(this)
                input.inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
                builder.setView(input)
                builder.setPositiveButton("Ok") { dialog, id ->
                    input.text?.let { editable ->
                        val newBook = emptyBook(fullTitle = editable.toString(), author = null, isbn13 = null, isbn10 = null)
                        bookViewModel.insertSync(newBook).observe(this, { observed ->
                            if (observed > 0) {
                                Log.i(TAG_NEW, "Added Book Manually: $observed $editable")
                                val intent = Intent(this, BookEditActivity::class.java).apply {
                                    putExtra(EXTRA_ID, observed)
                                }
                                startActivity(intent)
                            } else {
                                Log.e(TAG_NEW, "Manual Book could not be added: $observed $editable")
                            }

                        })
                    }
                }
                builder.show()
                return true
            }
            R.id.menu_main_barcode -> {
                val intent = Intent(applicationContext, OpenLibrarySearchActivity::class.java).apply {
                    putExtra(EXTRA_SCAN, true)
                }

                startActivity(intent)
                return true
            }
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
            R.id.menu_about -> {
                val intent = Intent(applicationContext, AboutActivity::class.java)
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
                    val today = LocalDate.now()
                    var formatter = DateTimeFormatter.ofPattern("yy_MM_dd")
                    var formattedDate = today.format(formatter)
                    putExtra(Intent.EXTRA_TITLE, "badreads_export_${formattedDate}.csv")
                }
                startActivityForResult(createIntent, REQ_EXP)
                true
            }
            R.id.menu_charts -> {
                val chartIntent = Intent(this, ChartActivity::class.java)
                startActivity(chartIntent)
                true
            }
            R.id.menu_export_read -> {
                val createIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/csv"
                    val today = LocalDate.now()
                    var formatter = DateTimeFormatter.ofPattern("yy_MM_dd")
                    var formattedDate = today.format(formatter)
                    putExtra(Intent.EXTRA_TITLE, "badreads_read_export_${formattedDate}.csv")
                }
                startActivityForResult(createIntent, REQ_EXP_READ)
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

        if (requestCode == REQ_EXP_READ && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                bookViewModel.readShelf.observe(this, { observable ->
                    observable?.let { allBooks ->
                        contentResolver.openOutputStream(uri)?.let { outputStream ->
                            Exporter().csv(outputStream, allBooks)
                        }
                    }
                })
            }
        }

        if (requestCode == REQ_EXP && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                bookViewModel.allBooks.observe(this, { observable ->
                    observable?.let { allBooks ->
                        contentResolver.openOutputStream(uri)?.let { outputStream ->
                            Exporter().csv(outputStream, allBooks)
                        }
                    }
                })
            }
        }


        super.onActivityResult(requestCode, resultCode, data)
    }


}


