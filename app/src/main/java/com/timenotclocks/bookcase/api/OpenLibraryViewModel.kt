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

    private fun getHtmlFromWeb() {
        Thread(Runnable {
            val stringBuilder = StringBuilder()
            try {

                val url = "https://plus.sr.cobiss.net/opac7/bib/search/expert?c=bn%3D86-7346-451-X&db=cobib&mat=allmaterials"
                val url_book = "https://plus.sr.cobiss.net/opac7/bib/risCit"
                val doc: Document = Jsoup.connect(url).get()
                val title: String = doc.title()
                val links: Elements = doc.select("tr[data-cobiss-id]")
                stringBuilder.append(title).append("\n")
                for (link in links) {
                    val book_bib = link.attr("data-cobiss-id")
                    println("getHtmlFromWeb link: ${book_bib}")
                    val url_book = "https://plus.sr.cobiss.net/opac7/bib/risCit/$book_bib"

                    try {
//                        val book = Jsoup.connect(url_book).get()
                        val book = with(Jsoup.connect(url_book)) {
                            header("Content-Type", "text/plain")
//                            userAgent("Mozilla")
//                            method(Connection.Method.POST)
//                             'Content-type: application/json; charset=utf-8'
//                            data("[\"bib/120688908\"]")
//                            requestBody("[\"bib/120688908\"]")
//                            ignoreContentType(true)
//                            data("bib/120688908")
//                            post()
//                            execute()
                            get()
                        }
                        val book_text = book.wholeText()
                        risToBook(book_text)
                        val book_text_newline = book_text.replace("OK##", "")
//                        book.text()
                        println("getHtmlFromWeb book_text: ${book_text}")
                        println("getHtmlFromWeb book_text: ${book_text_newline}")

//                        println("getHtmlFromWeb book_text_newline: ${book_text_newline}")

                    } catch (e: IOException) {
                        println("getHtmlFromWeb Error: ${e.message}")
                            stringBuilder.append("Error : ").append(e.message).append("\n")
                        }
//                    stringBuilder.append("\n").append("Link :").append(link.attr("href")).append("\n").append("Text : ").append(link.text())
                }
            } catch (e: IOException) {
                stringBuilder.append("Error : ").append(e.message).append("\n")
            }
//            runOnUiThread { textView.text = stringBuilder.toString() }
        }).start()
    }

    fun searchOpenLibrary(query: String) = viewModelScope.launch {
        getHtmlFromWeb()

        val url = "https://$base/books/v1/volumes"
        val encodedQuery = encode(query, "utf-8")
        val urlQuery = "$url?q=$encodedQuery"
        Log.i(LOG_EDIT, "urlQuery $urlQuery ")
        val stringRequest = StringRequest(Request.Method.GET, urlQuery,
                { response ->
                    val entry = Klaxon().parseJsonObject(StringReader(response))
                    val results: JsonArray<JsonObject>? = entry["items"] as? JsonArray<JsonObject>
                    if (results != null && !results.isEmpty()) {
                        val books: List<Book> = serializeSearchResults(results)
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

    private fun parseRisField(regex_string: String, ris_string: String): String {
        val regex = "$regex_string".toRegex()
        val matchResults = regex.find(ris_string)!!
        val (field) = matchResults.groupValues
        println("risToBook book title: $field ")

        return field
    }

    private fun risToBook(book: String): Book {

        val title       = parseRisField("(?:.*TI.*)-\\s(.*)", book)
        val author      = parseRisField("(?:.*AU.*)-\\s(.*)", book)
        val publishYear = parseRisField("(?:.*PY.*)-\\s(.*)", book)
        val publisher   = parseRisField("(?:.*PB.*)-\\s(.*)", book)
        val isbn10      = parseRisField("(?:.*SN.*)-\\s(.*)", book)
        val numberPages = parseRisField("(?:.*SP.*)-\\s(.*)", book)

        val book: Book = Book(
            bookId = 0,
            title = title,
            subtitle = "",
            cover = "",
            isbn10 = isbn10,
            isbn13 = null,
            selfLink = "",
            author = author,
            authorExtras = null,
            publisher = publisher,
            year = publishYear.toInt(),
            originalYear = null,
            numberPages = numberPages.toInt(),
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
}

