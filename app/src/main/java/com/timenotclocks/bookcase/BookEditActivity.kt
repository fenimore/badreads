package com.timenotclocks.bookcase


import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.widget.doAfterTextChanged
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.timenotclocks.bookcase.api.OpenLibraryViewModel
import com.timenotclocks.bookcase.database.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate


const val RESULT_DELETED = 140
const val EXTRA_SAVED_BOOK = "book_changes_saved"
const val LOG_EDIT = "BookEdit"

class BookEditActivity : AppCompatActivity() {

    private var book: Book? = null
    private val openLibraryViewModel: OpenLibraryViewModel by viewModels()
    private val bookViewModel: BookViewModel by viewModels {
        BookViewModelFactory((application as BooksApplication).repository)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.book_edit_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_edit)
//        val Rid = R.id
        val RidToolbar = R.id.toolbar
        Log.i(TAG_NEW, "FFF BookEditActiviti mUri: $RidToolbar")
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        intent.extras?.getLong(EXTRA_ID)?.let { bookId ->
            bookViewModel.getBook(bookId).observe(this, { observable ->
                observable?.let {
                    Log.i(LOG_EDIT, "Editing a book $it")
                    book = it
                    populateViews(it)
                }
            })
        }

        openLibraryViewModel.bookDetails.observe(this) { observable ->
            observable?.let { details ->
                if (
                        book?.isbn13 != details.isbn13 ?: book?.isbn13 ||
                        book?.isbn10 != details.isbn10 ?: book?.isbn10 ||
                        book?.publisher != details.publisher ?: book?.publisher ||
                        book?.year != details.publishYear ?: book?.year ||
                        book?.numberPages != details.numberPages ?: book?.numberPages ||
                        book?.description != details.description ?: book?.description ||
                        book?.series != details.series ?: book?.series ||
                        book?.language != details.language ?: book?.language
                        ) {
                    Snackbar.make(
                            findViewById(R.id.book_edit_activity),
                            "New details added, click Save (disk) to save.",
                            Snackbar.LENGTH_INDEFINITE
                    ).setAction("Ok", null).show()
                    book?.isbn13 = details.isbn13 ?: book?.isbn13
                    book?.isbn10 = details.isbn10 ?: book?.isbn10
                    book?.publisher = details.publisher ?: book?.publisher
                    book?.year = details.publishYear ?: book?.year
                    book?.numberPages = details.numberPages ?: book?.numberPages
                    book?.description = details.description ?: book?.description
                    book?.series = details.series ?: book?.series
                    book?.language = details.language ?: book?.language
                    book?.let { populateViews(it) }
                } else {
                    Snackbar.make(
                            findViewById(R.id.book_edit_activity),
                            "No new details found.",
                            Snackbar.LENGTH_LONG
                    ).setAction("Ok", null).show()
                }
            }
        }
    }

    private fun populateViews(current: Book) {
        val emptyCover = findViewById<TextView>(R.id.book_edit_empty_cover)
        emptyCover?.text = current.titleString() + "\n\n" + current.authorString()
        val coverView = findViewById<ImageView>(R.id.book_edit_cover_image)
        current.cover("L").let {
            Picasso.get().load(it).into(coverView, object : Callback {
                override fun onSuccess() {
                    emptyCover.visibility = View.INVISIBLE
                }
                override fun onError(e: Exception) {}
            })
        }
        coverView.drawable ?: run {
            Log.i(LOG_TAG, "Check is it nulll?")
            emptyCover.visibility = View.VISIBLE
        }

//        val scan = root.findViewById<MaterialButton>(R.id.fragment_scan_button)
//        scan.setOnClickListener {
//            val intent = Intent(context, OpenLibrarySearchActivity::class.java).apply {
//                putExtra(EXTRA_SCAN, true)
//            }
//            startActivity(intent)
//        }
        findViewById<Button>(R.id.book_edit_image_edit).setOnClickListener { view ->
//            val term = current.isbn13 ?: current.titleString()
//            val url = "https://openlibrary.org/search?q=$term"
//            val i = Intent(Intent.ACTION_VIEW)
//            i.data = Uri.parse(url)
//            startActivity(i)


//            Log.i(LOG_BOOK_VIEW, "Taking photo of this book")
//            val intent = Intent(applicationContext, BookPhotoActivity::class.java).apply {
//                putExtra(EXTRA_SCAN, true)
//            }
////            startActivity(intent)
//            startActivityForResult(intent, 100)

            current.let { it ->

                val intent = Intent(applicationContext, BookPhotoActivity::class.java).apply {
                    Log.i(LOG_BOOK_VIEW, "Taking photo of this book 2 $it")
                    putExtra(EXTRA_ID, it.bookId)
                }
//                startActivity(intent)
                startActivityForResult(intent, 100)
//
            }

        }

        findViewById<Button>(R.id.book_upload_book).setOnClickListener { view ->
            current.let { it ->
                Thread(Runnable {
                    postBlog(it)
                }).start()
            }

            println("sync book:  $book")
        }


        val titleEdit = findViewById<TextInputLayout>(R.id.book_edit_title_layout)?.editText
        titleEdit?.setText(current.title)
        titleEdit?.doAfterTextChanged { editable -> current.title = editable.toString() }

        val subTitleEdit = findViewById<TextInputLayout>(R.id.book_edit_subtitle).editText
        subTitleEdit?.doAfterTextChanged { editable -> current.subtitle = editable.toString().ifEmpty { null } }
        current.subtitle?.let { subTitleEdit?.setText(it) }

        val authorEdit = findViewById<TextInputLayout>(R.id.book_edit_author).editText
        authorEdit?.doAfterTextChanged { editable -> current.author = editable.toString().ifEmpty { null } }
        current.author?.let { authorEdit?.setText(it) }

        val extrasEdit = findViewById<TextInputLayout>(R.id.book_edit_author_extras).editText
        extrasEdit?.doAfterTextChanged { editable -> current.authorExtras = editable.toString().ifEmpty { null } }
        current.authorExtras?.let { extrasEdit?.setText(it) }

        val isbn10Edit = findViewById<TextInputLayout>(R.id.book_edit_isbn10).editText
        current.isbn10?.let { isbn10Edit?.setText(it) }
        isbn10Edit?.doAfterTextChanged { editable -> current.isbn10 = editable.toString().ifEmpty { null } }

        val isbn13Edit = findViewById<TextInputLayout>(R.id.book_edit_isbn13).editText
        current.isbn13?.let { isbn -> isbn13Edit?.setText(isbn) }
        isbn13Edit?.doAfterTextChanged { editable -> current.isbn13 = editable.toString().ifEmpty { null } }

        val publisherEdit = findViewById<TextInputLayout>(R.id.book_edit_publisher).editText
        current.publisher?.let { publisherEdit?.setText(it) }
        publisherEdit?.doAfterTextChanged { editable -> current.publisher = editable.toString().ifEmpty { null } }

        val yearEdit = findViewById<TextInputLayout>(R.id.book_edit_year).editText
        current.year?.let { yearEdit?.setText(it.toString()) }
        yearEdit?.doAfterTextChanged { editable -> editable.toString().toIntOrNull().let { year -> current.year = year } }

        val originalYearEdit = findViewById<TextInputLayout>(R.id.book_edit_original_year).editText
        current.originalYear?.let { originalYearEdit?.setText(it.toString()) }
        originalYearEdit?.doAfterTextChanged { editable ->
            editable.toString().toIntOrNull().let { year ->
                current.originalYear = year
            }
        }
        val pageNumbersEdit = findViewById<TextInputLayout>(R.id.book_edit_page_numbers).editText
        current.numberPages?.let { pageNumbersEdit?.setText(it.toString()) }
        pageNumbersEdit?.doAfterTextChanged { editable ->
            editable.toString().toIntOrNull().let { pages ->
                current.numberPages = pages
            }
        }

        val progressEdit = findViewById<TextInputLayout>(R.id.book_edit_progress).editText
        current.progress?.let { progressEdit?.setText(it.toString()) }
        progressEdit?.doAfterTextChanged { editable ->
            editable.toString().toIntOrNull().let { progress ->
                current.progress = progress
            }
        }

        val seriesEdit = findViewById<TextInputLayout>(R.id.book_edit_series).editText
        current.series?.let { seriesEdit?.setText(it) }
        seriesEdit?.doAfterTextChanged { editable -> current.series = editable.toString().ifEmpty { null } }

        val languageEdit = findViewById<TextInputLayout>(R.id.book_edit_language).editText
        current.language?.let { languageEdit?.setText(it) }
        languageEdit?.doAfterTextChanged { editable -> current.language = editable.toString().ifEmpty { null } }
        
        val dateAddedView = findViewById<DatePicker>(R.id.book_edit_date_added)
        val dateAdded = current.dateAdded?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.now()
        dateAddedView.init(dateAdded.year, dateAdded.monthValue - 1, dateAdded.dayOfMonth, object : DatePicker.OnDateChangedListener {
            override fun onDateChanged(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
                current.dateAdded = LocalDate.of(year, monthOfYear + 1, dayOfMonth).toEpochDay()
            }
        })
        val dateStartedView = findViewById<DatePicker>(R.id.book_edit_date_started)
        val dateStarted = current.dateStarted?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.now()
        dateStartedView.init(dateStarted.year, dateStarted.monthValue - 1, dateStarted.dayOfMonth, object : DatePicker.OnDateChangedListener {
            override fun onDateChanged(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
                current.dateStarted = LocalDate.of(year, monthOfYear + 1, dayOfMonth).toEpochDay()
            }
        })
        val dateReadView = findViewById<DatePicker>(R.id.book_edit_date_read)
        val dateRead = current.dateRead?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.now()
        dateReadView.init(dateRead.year, dateRead.monthValue - 1, dateRead.dayOfMonth, object : DatePicker.OnDateChangedListener {
            override fun onDateChanged(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
                current.dateRead = LocalDate.of(year, monthOfYear + 1, dayOfMonth).toEpochDay()
            }
        })

        val descEdit = findViewById<TextInputLayout>(R.id.book_edit_description)?.editText
        current.description?.let { descEdit?.setText(it) }
        descEdit?.doAfterTextChanged { editable -> current.description = editable?.toString()?.ifEmpty { null } }

        val notesEdit = findViewById<TextInputLayout>(R.id.book_edit_notes).editText
        current.notes?.let { notesEdit?.setText(it) }
        notesEdit?.doAfterTextChanged { editable -> current.notes = editable?.toString() }

        val shelfDropdown = findViewById<Button>(R.id.book_edit_shelf_dropdown)
        shelfDropdown.text = current.shelfString()
        shelfDropdown.setOnClickListener { view ->
            view?.let { v ->
                val popup = PopupMenu(applicationContext, v)
                popup.menuInflater.inflate(R.menu.shelf_menu, popup.menu)
                popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                    when (menuItem.itemId) {
                        R.id.shelf_to_read -> {
                            current.shelve(ShelfType.ToReadShelf, shelfDropdown, bookViewModel)
                        }
                        R.id.shelf_currently_reading -> {
                            current.shelve(ShelfType.CurrentShelf, shelfDropdown, bookViewModel)
                        }
                        R.id.shelf_read -> {
                            current.shelve(ShelfType.ReadShelf, shelfDropdown, bookViewModel)
                        }
                        else -> {
                            true
                        }
                    }
                }
                popup.show()
            }
        }
        val ratingBar = findViewById<RatingBar>(R.id.book_edit_rating_bar)
        current.rating?.let { ratingBar.setRating(it.toFloat()) }
        // TODO Doesn't work
        ratingBar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { bar, rating, fromUser ->
            current.rating = rating.toInt()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(applicationContext, BookViewActivity::class.java).apply {
                    book?.let {
                        putExtra(EXTRA_ID, it.bookId)
                    }
                }
                setResult(RESULT_CANCELED, intent)
                finish()
            }
            R.id.menu_delete -> {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.apply {
                    setPositiveButton("OK",
                            DialogInterface.OnClickListener { dialog, id ->
                                // User clicked OK button
                                book?.let { bookViewModel.delete(it) }
                                val intent = Intent(applicationContext, MainActivity::class.java)
                                setResult(RESULT_DELETED, intent)
                                finish();
                            })
                    setNegativeButton("CANCEL",
                            DialogInterface.OnClickListener { dialog, id ->
                                // User cancelled the dialog
                            })
                }
                builder.setMessage("Delete this book?")
                builder.create()
                builder.show()
            }
            R.id.menu_save -> {
                Log.i("BK", "Saving Book ${book?.title} ${book?.bookId}")
                book?.let { bookViewModel.update(it) }
                val intent = Intent(applicationContext, BookViewActivity::class.java).apply {
                    book?.let {
                        putExtra(EXTRA_ID, it.bookId)
                    }
                }
                setResult(RESULT_OK, intent)
                finish();
            }
            R.id.menu_meta -> {
                book?.isbn13?.let { openLibraryViewModel.getBookDetails(it) }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 100) {
            Log.i(TAG_NEW, "FFF BookEditActivity onActivityResult data: $data")
            Log.i(TAG_NEW, "FFF BookEditActivity onActivityResult data.data: $data.data")
//            doSomeOperations()
        }
    }

    /**
     * Post book to WebBookList
     *
     * @param book Book object of currently viewd book
     */
    fun postBlog(current: Book){


        val urlDev = "http://10.0.2.2:8000/api/blogpost"

        val url = (
                PreferenceManager.getDefaultSharedPreferences(this).getString(
                    "remote_url", urlDev)?.toUri() ?: urlDev
                ).toString()

        val picasoBitmap = Picasso.get().load(current.cover).get()
        val fileObject = bitmapToFile(picasoBitmap, "temp_file.jpg")!!

        val client = OkHttpClient()

        val JSONObjectString_2 = "{\"title\": \"${current.title}\", \"shortDescription\": \"${current.author}\", \"body\": \"${current.isbn13}\"}"
        val MEDIA_TYPE_JPG = "image/jpeg".toMediaType()
        val fileUri = Uri.parse(current.cover)
        val fileName = fileUri.lastPathSegment

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("metadata", JSONObjectString_2)
            .addFormDataPart("file", fileName, fileObject.asRequestBody(MEDIA_TYPE_JPG))
            .build()

        val request = Request.Builder()
            .header("X-AUTH-TOKEN", "marko1112@droopia.net")
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            println(response)
        }
    }

    /**
     * Convert Picasso loaded bitmap to File object
     * need File object to post it via .addFormDataPart on okhttp request
     * and this was most reliable way to get the image.
     *
     * @param bitmap Bitmap loaded with picasso
     * @param fileNameToSave String to save this temporary file
     */
    fun bitmapToFile(bitmap: Bitmap, fileNameToSave: String): File? { // File name like "image.png"
        //create a file to write bitmap data
        var file: File? = null
        return try {
            file = File(Environment.getExternalStorageDirectory().toString() + File.separator + fileNameToSave)
            file.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos) // YOU can also save it in JPEG
            val bitmapdata = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file // it will return null
        }
    }
}