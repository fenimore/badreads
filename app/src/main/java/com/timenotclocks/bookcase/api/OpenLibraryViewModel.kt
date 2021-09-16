package com.timenotclocks.bookcase.api

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.timenotclocks.bookcase.LOG_EDIT
import com.timenotclocks.bookcase.LOG_SEARCH
import com.timenotclocks.bookcase.TAG_NEW
import com.timenotclocks.bookcase.database.Book
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.IOException
import java.io.StringReader
import java.lang.Integer.min
import java.net.URLEncoder.encode
import java.time.LocalDate



const val MAX_SEARCH_RESULTS = 100

const val LOG_LIB = "BookOpenLib"

data class BookDetails(
    val isbn13: String?,
    val isbn10: String?,
    val selfLink: String?,
    val publisher: String?,
    val publishYear: Int?,
    val numberPages: Int?,
    val description: String?,
    val series: String?,
    val language: String?,
)


internal class OpenLibraryViewModel(application: Application) : AndroidViewModel(application) {

//    private val base = "openlibrary.org"
    private val base = "www.googleapis.com"
//    https://www.googleapis.com/books/v1/volumes?q=isbn:0735619670

    private val searches: MutableLiveData<List<Book>> by lazy { MutableLiveData() }

    var numResults: MutableLiveData<Int> = MutableLiveData<Int>(0)
    var bookDetails: MutableLiveData<BookDetails> = MutableLiveData<BookDetails>()

    fun getSearches(): LiveData<List<Book>> { return searches }

    fun getGoogleBookDetails(selfLink: String) =  viewModelScope.launch {
        Log.i(LOG_EDIT, "FFF getBookDetails $selfLink")
        //val url = "https://$base/isbn/$isbn.json"
//        val url = "https://$base/api/books?bibkeys=ISBN:$isbn&jscmd=details&format=json"
//        val url = "https://$base/books/v1/volumes?q=isbn:$isbn"

        val url = selfLink

        Log.i(LOG_EDIT, "Searching with API $url")

        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                Log.i(TAG_NEW, response.toString())
                val result = Klaxon().parseJsonObject(StringReader(response))
                Log.i(TAG_NEW, "Found some JSON ${result.keys.firstOrNull()}")
                Log.i(TAG_NEW, result.toJsonString())

//                val test1: String? = result.keys.firstOrNull()
//                println("test1 test1 test1: ${test1.toString()}")
                val details: JsonObject? = result?.getOrDefault("volumeInfo", JsonObject()) as JsonObject
//                println("test2 test2 test2: ${test2.toString()}")

//                val details2: JsonObject? = result.keys.firstOrNull()?.let { key ->
//                    result.getOrDefault(key, JsonObject()) as JsonObject
//                }?.getOrDefault("volumeInfo", JsonObject()) as JsonObject
//
//                val details: JsonObject? = result.keys.firstOrNull()?.let { key ->
//                    result.getOrDefault(key, JsonObject()) as JsonObject
//                }?.getOrDefault("volumeInfo", JsonObject()) as JsonObject
//                val publisher: String? =
//                    (details?.get("publisher") as JsonArray<String>)?.firstOrNull()
//                val publisher2: Char? = (details?.get("publisher") as String)?.firstOrNull()
                val publisher = details?.get("publisher").toString()

                val industryIdentifiers: JsonArray<JsonObject>? = details?.get("industryIdentifiers") as JsonArray<JsonObject>?
                val isbn13Array = industryIdentifiers?.filter{
                    it.get("type") == "ISBN_13"
                }
                println("isbn13Array  isbn13Array: $isbn13Array")
                val isbn13: String? = isbn13Array?.let { it.firstOrNull()?.get("identifier") as String }
//                val isbn13: String? =  isbn13Array?.firstOrNull()?.get("identifier") as String
//                val isbn13: String? =
//                    (details?.get("industryIdentifiers") as JsonArray<String>?)?.firstOrNull()
                val isbn10 = null
//                val isbn10: String? =
//                    (details?.get("industryIdentifiers") as JsonArray<String>?)?.firstOrNull()
                val description: String? = (details?.get("description")) as? String
                val numPages: Int? = details?.get("pageCount") as? Int
                val publishYear: Int? = (details?.get("publishedDate") as? String)?.let {
                    Regex("""\b\d{4}\b""").find(it)
                }?.value?.toIntOrNull()
                val series: String? =
                    (details?.get("series") as JsonArray<String>?)?.firstOrNull()
                val language: String? =  (details?.get("language")) as? String
//                val language: String? = (details?.get("language") as JsonArray<JsonObject>?)?.firstOrNull()?.get("key") as? String
                // TODO: make human friendly https://openlibrary.org/languages
                Log.i(LOG_LIB, "Okay so I've $publisher $isbn10 $isbn13 $numPages $publishYear")
                Log.i(LOG_LIB, "Description: $description")
                bookDetails.value = BookDetails(
                    isbn13,
                    isbn10,
                    selfLink,
                    publisher,
                    publishYear,
                    numPages,
                    description,
                    series,
                    language?.replace("/languages/", "")?.capitalize(),
                )
            },
            {
                Log.e("BK", "Volley Error $it")
            })
        RequestQueueSingleton.getInstance(getApplication()).addToRequestQueue(stringRequest)
    }

    fun getBookDetails(isbn: String) = viewModelScope.launch {
        Log.i(LOG_EDIT, "FFF getBookDetails $isbn")
        //val url = "https://$base/isbn/$isbn.json"
//        val url = "https://$base/api/books?bibkeys=ISBN:$isbn&jscmd=details&format=json"
        val url = "https://$base/books/v1/volumes?q=isbn:$isbn"
        val selfLink = null
        Log.i(LOG_EDIT, "Searching with API $url")
        val stringRequest = StringRequest(Request.Method.GET, url,
                { response ->
                    Log.i(TAG_NEW, response.toString())
                    val result = Klaxon().parseJsonObject(StringReader(response))
                    Log.i(TAG_NEW, "Found some JSON ${result.keys.firstOrNull()}")
                    Log.i(TAG_NEW, result.toJsonString())

                    val details: JsonObject? = result.keys.firstOrNull()?.let { key ->
                        result.getOrDefault(key, JsonObject()) as JsonObject
                    }?.getOrDefault("volumeInfo", JsonObject()) as JsonObject
                    val publisher: String? =
                        (details?.get("publisher") as JsonArray<String>?)?.firstOrNull()

                    val isbn13: String? =
                        (details?.get("industryIdentifiers") as JsonArray<String>?)?.firstOrNull()
                    val isbn10: String? =
                        (details?.get("isbn_10") as JsonArray<String>?)?.firstOrNull()
                    val description: String? = (details?.get("description")) as? String
                    val numPages: Int? = details?.get("pageCount") as? Int
                    val publishYear: Int? = (details?.get("publishedDate") as? String)?.let {
                        Regex("""\b\d{4}\b""").find(it)
                    }?.value?.toIntOrNull()
                    val series: String? =
                        (details?.get("series") as JsonArray<String>?)?.firstOrNull()
                    val language: String? = (details?.get("language") as JsonArray<JsonObject>?)?.firstOrNull()?.get("key") as? String
                    // TODO: make human friendly https://openlibrary.org/languages
                    Log.i(LOG_LIB, "Okay so I've $publisher $isbn10 $isbn13 $numPages $publishYear")
                    Log.i(LOG_LIB, "Description: $description")
                    bookDetails.value = BookDetails(
                        isbn13,
                        isbn10,
                        selfLink,
                        publisher,
                        publishYear,
                        numPages,
                        description,
                        series,
                        language?.replace("/languages/", "")?.capitalize(),
                    )
                },
                {
                    Log.e("BK", "Volley Error $it")
                })
        RequestQueueSingleton.getInstance(getApplication()).addToRequestQueue(stringRequest)
    }


    fun searchOpenLibrary(query: String, is_barcode: Boolean = false) = viewModelScope.launch {


        val url = "https://$base/books/v1/volumes"
        val barcode_code = if (is_barcode) "isbn:" else ""
        val encodedQuery = encode(query, "utf-8")
        val urlQuery = "$url?q=$barcode_code$encodedQuery"
        Log.i(LOG_EDIT, "urlQuery $urlQuery ")
        val stringRequest = StringRequest(Request.Method.GET, urlQuery,
                { response ->
                    val bookListCobbiss: List<Book> = getBookFromCobiss(query,is_barcode)
                    val entry = Klaxon().parseJsonObject(StringReader(response))
                    val results: JsonArray<JsonObject>? = entry["items"] as? JsonArray<JsonObject>
                    if (results != null && !results.isEmpty()) {
                        val books_from_google: List<Book> = serializeSearchResults(results)
//                        val books = bookListCobbiss.plus(books_from_google)
//                        val books = bookListCobbiss
                        val tmp_searches = searches.getValue()
                        val books = bookListCobbiss + books_from_google
                        searches.setValue(books.subList(0, min(MAX_SEARCH_RESULTS, books.size)))
                        numResults.value = books.size
                    } else {
                        numResults.value = 0
                        searches.setValue(emptyList<Book>())
                        Log.e("BK", "No results :(")
                    }
                },
                {
                    numResults.value = 0
                    searches.setValue(emptyList<Book>())
                    Log.e("BK", "Volley Error $it")
                })
        RequestQueueSingleton.getInstance(getApplication()).addToRequestQueue(stringRequest)
    }

    private fun serializeSearchResults(results: JsonArray<JsonObject>): List<Book> {

        return results.map { result ->
            val selfLink: String = result["selfLink"] as String
            val result: JsonObject = result["volumeInfo"] as JsonObject

            val author: String? = (result["authors"] as? JsonArray<*>)?.removeFirst() as String?
            val authorExtras: String? = (result["authors"] as? JsonArray<*>)?.joinToString(", ")

            val industryIdentifiers: Any? = result["industryIdentifiers"]
            val originalYear: Int? = result["publishedDate"].toString().take(4).toIntOrNull()
                ?: (result["publishedDate"] as? JsonArray<Int>)?.minOrNull()

            val isbnArray: JsonArray<String>? = result["industryIdentifiers"] as? JsonArray<String>
            val isbnObject: JsonObject? = result["industryIdentifiers"] as? JsonObject

            val isbnArray2 = isbnArray?.get("identifier") as? JsonArray<String>

            val bookList: List<Book>? = isbnArray2?.filter { it.length > 10 }?.let { isbn13s ->
                isbn13s.map { isbn13 ->
                    Book(
                        bookId = 0,
                        title = result["title"].toString(),
                        subtitle = result["subtitle"] as? String,
                        cover = "",
                        isbn10 = null,
                        isbn13 = isbn13,
                        selfLink = selfLink,
                        author = author,
                        authorExtras = authorExtras,
                        publisher = null,
                        year = null,
                        originalYear = originalYear,
                        numberPages = null,
                        progress = null,
                        series = null,
                        language = null,
                        rating = null,
                        shelf = "to-read",
                        notes = null,
                        description = null,
                        dateAdded = LocalDate.now().toEpochDay(),
                        dateRead = null,
                        dateStarted = null,
                    )
                }
            }
            bookList ?: emptyList<Book>()
        }.flatten()
    }


    private fun getBookFromCobiss(query: String, is_barcode: Boolean): List<Book> {

        val bookList = mutableListOf<Book>()


        Thread(Runnable {
            val stringBuilder = StringBuilder()
            try {
                val barcode_code = if (is_barcode) "bn%3D" else ""
                val url = "https://plus.sr.cobiss.net/opac7/bib/search/expert?c=$barcode_code$query&db=cobib&mat=allmaterials"
                val url_book = "https://plus.sr.cobiss.net/opac7/bib/risCit"

                val doc: Document = Jsoup.connect(url).get()
                val title: String = doc.title()
                val links: Elements = doc.select("tr[data-cobiss-id]")
                stringBuilder.append(title).append("\n")
                for (link in links) {
                    val book_bib = link.attr("data-cobiss-id")
                    println("getBookFromCobiss link: ${book_bib}")
                    val url_book = "https://plus.sr.cobiss.net/opac7/bib/risCit/$book_bib"

                    try {
                        val book = with(Jsoup.connect(url_book)) {
                            header("Content-Type", "text/plain")
                            get()
                        }

                        val book_text = book.wholeText()
                        val book_object: Book = risToBook(book_text)

                        bookList.add(book_object)

                        println("getBookFromCobiss book_object: ${book_object}")

                    } catch (e: IOException) {
                        println("getBookFromCobiss Error: ${e.message}")
                        stringBuilder.append("Error : ").append(e.message).append("\n")
                    }
//                    stringBuilder.append("\n").append("Link :").append(link.attr("href")).append("\n").append("Text : ").append(link.text())
                }
            } catch (e: IOException) {
                stringBuilder.append("Error : ").append(e.message).append("\n")
            }
//            runOnUiThread { textView.text = stringBuilder.toString() }
        }).start()

//        val tmp_searches = searches.getValue()
//        bookList.plus(tmp_searches)
//        searches.setValue(bookList.subList(0, min(MAX_SEARCH_RESULTS, bookList.size)))
//        numResults.value = bookList.size

        return bookList
    }

    private fun parseRisField(regex_string: String, ris_string: String): String {
        val regex = "$regex_string".toRegex()
        try {
            val matchResults = regex.find(ris_string)!!
            val (field) = matchResults.destructured
            println("parseRisField book field: $field ")
            return field
        } catch (e: NullPointerException) {
            println("parseRisField Error: ${e.message}")
        }

        return ""
    }

    private fun risToBook(book: String): Book {

        val title       = parseRisField("(?:.*TI.*)-\\s(.*)", book)
        val author      = parseRisField("(?:.*AU.*)-\\s(.*)", book)
        val publishYear = parseRisField("(?:.*PY.*)-\\s(.*)", book)
        val publisher   = parseRisField("(?:.*PB.*)-\\s(.*)", book)
        val isbn10      = parseRisField("(?:.*SN.*)-\\s(.*)", book).replace("-","").let{if (it.length == 10) it else null }
        val isbn13      = parseRisField("(?:.*SN.*)-\\s(.*)", book).replace("-","").let{if (it.length == 13) it else null }
        val numberPages = parseRisField("(?:.*SP.*)-\\s(.*)", book)

        val book: Book = Book(
            bookId = 0,
            title = title,
            subtitle = "",
            cover = "",
            isbn10 = isbn10,
            isbn13 = isbn13,
            selfLink = "",
            author = author,
            authorExtras = null,
            publisher = publisher,
//            year = publishYear as? Int,
            year = 1990,
            originalYear = null,
//            numberPages = numberPages as? Int,
            numberPages = 111,
            progress = null,
            series = null,
            language = null,
            rating = null,
            shelf = "to-read",
            notes = null,
            description = null,
            dateAdded = LocalDate.now().toEpochDay(),
            dateRead = null,
            dateStarted = null,
        )

        return book
    }

}

