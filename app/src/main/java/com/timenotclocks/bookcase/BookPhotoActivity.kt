package com.timenotclocks.bookcase


import android.R.attr
import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.squareup.picasso.Picasso
import com.timenotclocks.bookcase.database.Book
import com.timenotclocks.bookcase.database.BookViewModel
import com.timenotclocks.bookcase.database.BookViewModelFactory
import com.timenotclocks.bookcase.database.BooksApplication
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


const val LOG_BOOK_PHOTO_VIEW = "BookPhoto"

class BookPhotoActivity : AppCompatActivity() {

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.book_edit_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    private val bookViewModel: BookViewModel by viewModels {
        BookViewModelFactory((application as BooksApplication).repository)
    }

    private var ocrText: String = ""
    //Our variables
    private var mImageView: ImageView? = null
    private var mUri: Uri? = null
    //Our widgets
    private lateinit var btnCapture: Button
    private lateinit var btnChoose : Button
    //Our constants
    private val OPERATION_CAPTURE_PHOTO = 1
    private val OPERATION_CHOOSE_PHOTO = 2

    private var book: Book? = null

    private fun initializeWidgets() {
        mImageView = findViewById(R.id.mImageView)
        btnCapture = findViewById(R.id.book_btn_capture)
        btnChoose = findViewById(R.id.book_btn_choose)
    }

    private fun show(message: String) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }
    private fun capturePhoto(){

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        val capturedImage = File( getExternalFilesDir(Environment.DIRECTORY_PICTURES)  , "BookCoverPhoto-$timeStamp.jpg")
        if(capturedImage.exists()) {
            capturedImage.delete()
        }
        capturedImage.createNewFile()

        mUri = if(Build.VERSION.SDK_INT >= 24){
            FileProvider.getUriForFile(this, "com.timenotclocks.bookcase.fileprovider", capturedImage)
        } else {
            Uri.fromFile(capturedImage)
        }

        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
        startActivityForResult(intent, OPERATION_CAPTURE_PHOTO)
    }


    private fun openGallery(){

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//        val intent = Intent("android.intent.action.ACTION_OPEN_DOCUMENT")
        intent.type = "image/*"
//        mUri =
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
        startActivityForResult(intent, OPERATION_CHOOSE_PHOTO)



    }
    private fun renderImage(imagePath: String?){
        if (imagePath != null) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            mImageView?.setImageBitmap(bitmap)
        }
        else {
            show("ImagePath is null")
        }
    }

    private fun getImagePath(uri: Uri, selection: String?): String {
        var path: String? = null
        val cursor = contentResolver.query(uri, null, selection, null, null )
        if (cursor != null){
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path!!
    }
    @TargetApi(19)
    private fun handleImageOnKitkat(data: Intent?) {

        var imagePath: String? = null
        val uri = data!!.data as Uri
        //DocumentsContract defines the contract between a documents provider and the platform.
        if (DocumentsContract.isDocumentUri(this, uri)){
            val docId = DocumentsContract.getDocumentId(uri)
            if ("com.android.providers.media.documents" == uri?.authority){
                val id = docId.split(":")[1]
                val selsetion = MediaStore.Images.Media._ID + "=" + id
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    selsetion)
            }
            else if ("com.android.providers.downloads.documents" == uri.authority){
                val contentUri = ContentUris.withAppendedId(Uri.parse(
                    "content://downloads/public_downloads"), java.lang.Long.valueOf(docId))
                imagePath = getImagePath(contentUri, null)
            }
        }
        else if ("content".equals(uri?.scheme, ignoreCase = true)){
            imagePath = getImagePath(uri, null)
        }
        else if ("file".equals(uri?.scheme, ignoreCase = true)){
            imagePath = uri?.path
        }
        renderImage(imagePath)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantedResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantedResults)
        when(requestCode){
            1 ->
                if (grantedResults.isNotEmpty() && grantedResults.get(0) ==
                    PackageManager.PERMISSION_GRANTED){
                    openGallery()
                }else {
                    show("Unfortunately You are Denied Permission to Perform this Operataion.")
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG_NEW, "FFF onActivityResult, requestCode - data: $requestCode $data")
        when(requestCode){
            OPERATION_CAPTURE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG_NEW, "FFF onActivityResult OPERATION_CAPTURE_PHOTO")
                    Log.i(TAG_NEW, "FFF onActivityResult mUri: $mUri")
                    val mmUri = mUri as Uri
                    val bitmap = BitmapFactory.decodeStream(
                        getContentResolver().openInputStream(mmUri))
                    mImageView!!.setImageBitmap(bitmap)
                }
            OPERATION_CHOOSE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {
                    val data_test = data
                    mUri = data?.data
                    val mmUri = mUri as Uri
                    val bitmap = BitmapFactory.decodeStream(
                        getContentResolver().openInputStream(mmUri))
                    mImageView!!.setImageBitmap(bitmap)
                    Log.i(TAG_NEW, "FFF onActivityResult OPERATION_CHOOSE_PHOTO")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        getContentResolver().takePersistableUriPermission(
                            mmUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    }

                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageOnKitkat(data)
                    }
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_photo)
        val RidToolbar = R.id.toolbar
        Log.i(TAG_NEW, "FFF BookPhotoActiviti mUri: $RidToolbar")
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        intent.extras?.getLong(EXTRA_ID)?.let { bookId ->
            bookViewModel.getBook(bookId).observe(this, { observable ->
                observable?.let {
                    Log.i(LOG_EDIT, "Editing a book $it")
                    book = it
//                    populateViews(it)
                }
            })
        }

        initializeWidgets()

        btnCapture.setOnClickListener{capturePhoto()}
        btnChoose.setOnClickListener{
            //check permission at runtime
            val checkSelfPermission = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED){
                //Requests permissions to be granted to this application at runtime
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
            else{
                openGallery()
            }
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
            R.id.menu_save -> {
                Log.i("BookPhoto", "Saving Photo ${book?.title} ${book?.bookId}")

                    Thread(Runnable {
                        val picasso_bitmap = Picasso.get().load(mUri).get()
                        val image = InputImage.fromBitmap(picasso_bitmap, 0)
                        ocrText = this.recognizeText(image, book?.bookId!!)
                        Log.i("MLkit", "ocr succes ocrText Thread $ocrText")
                    }).start()

                Log.i("MLkit", "ocr succes ocrText After Thread $ocrText")

                book?.description = ocrText

                book?.cover = mUri.toString()
                book?.let { bookViewModel.update(it) }
                val intent = Intent(applicationContext, BookViewActivity::class.java).apply {
                    book?.let {
                        putExtra(EXTRA_ID, it.bookId)
                    }
                }
                Log.i("BookPhoto", "Saving Photo ${book}")
                setResult(RESULT_OK, intent)
                finish();
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun getRealPathFromURI(contentURI: Uri?, context: Activity): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.managedQuery(
            contentURI, projection, null,
            null, null
        ) ?: return null
        val column_index = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        return if (cursor.moveToFirst()) {
            // cursor.close();
            cursor.getString(column_index)
        } else null
        // cursor.close();
    }


    /**
     * OCR book cover using MLkit
     *
     * @param image Bitmap image to give to recognizer
     * @param bookId ID of the book which cover we are ocring
     */
    private fun recognizeText(image: InputImage, bookId: Long): String {

        // [START get_detector_default]
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        // [END get_detector_default]

        // [START run_detector]
        val result = recognizer.process(image)
            .addOnSuccessListener { visionText ->
                Log.i("MLkit", "ocr succes $visionText")
                // Task completed successfully
                // [START_EXCLUDE]
                // [START get_text]
                for (block in visionText.textBlocks) {
                    val boundingBox = block.boundingBox
                    val cornerPoints = block.cornerPoints
                    val text = block.text
                    ocrText  = ocrText + text + "\n\r"
                    Log.i("MLkit", "ocr succes text $text")
                    Log.i("MLkit", "ocr succes ocrText LOOP $ocrText")

                    for (line in block.lines) {
                        // ...
                        for (element in line.elements) {
                            // ...
                            Log.i("MLkit", "ocr succes element $element")
                        }
                    }

                    bookViewModel.getBook(bookId)
                    book?.description = ocrText
                    book?.let { bookViewModel.update(it) }
                    Log.i("MLkit", "ocr succes ocrText for END $ocrText")
                }
                // [END get_text]
                // [END_EXCLUDE]
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                // ...
                Log.i("MLkit", "ocr fail $e")
            }
        // [END run_detector]

        Log.i("MLkit", "ocr succes ocrText END $ocrText")
        return ocrText
    }

}
