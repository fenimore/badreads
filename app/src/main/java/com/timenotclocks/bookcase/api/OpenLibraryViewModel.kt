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
import org.jsoup.nodes.Element
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

/**
 * Main class for searching books
 * should be separated for openlibrary, google books and cobiss
 *
 *
 * @param application Application.
 * @property base Base url to search
 * @property searches search results to show
 * @property numResults number of search results to show
 * @property bookDetails book details to show in list
 * @constructor Creates an empty group.
 */
internal class OpenLibraryViewModel(application: Application) : AndroidViewModel(application) {

//    private val base = "openlibrary.org"
    private val base = "www.googleapis.com"
//    https://www.googleapis.com/books/v1/volumes?q=isbn:0735619670

    private val searches: MutableLiveData<List<Book>> by lazy { MutableLiveData() }

    var numResults: MutableLiveData<Int> = MutableLiveData<Int>(0)
    var bookDetails: MutableLiveData<BookDetails> = MutableLiveData<BookDetails>()

    /**
     * get search results live data list if Book.
     * @return return search results
     */
    fun getSearches(): LiveData<List<Book>> { return searches }

    /**
     * Search Google Books for data
     *
     * get search results live data list if Book.
     * @param selfLink permanent link to requested Book
     */
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

    /**
     * Search COBISS site for data on book
     * endpoint is https://plus.sr.cobiss.net/opac7/bib/risCit/$book_bib
     *
     * get search results live data list if Book.
     * @param selfLink permanent link to requested Book
     */
    fun getCobissBookDetails(selfLink: String) =  viewModelScope.launch {

        val url_book = selfLink
        val stringRequestBook = StringRequest(Request.Method.GET, url_book,
            { response2 ->
                val book_text = response2
                val book_object: Book = risToBook(book_text)
                println("getCobissBookDetails book_object: ${book_object}")
                val isbn13 = book_object.isbn13
                val isbn10 = book_object.isbn10
                val selfLink = book_object.selfLink
                val publisher = book_object.publisher
                val publishYear = book_object.year
                val numPages = book_object.numberPages
                val description = book_object.description
                val series = book_object.series
                val language = book_object.language

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
                println("volleyError! $it")
                Log.e("BK", "Volley Error $it")
            })

        RequestQueueSingleton.getInstance(getApplication()).addToRequestQueue(stringRequestBook)
    }

    /**
     * Search Google Books by ISBN for data on book
     * endpoint is https://plus.sr.cobiss.net/opac7/bib/risCit/$book_bib
     *
     * @param isbn scanned ISBN number to search
     */
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

    /**
     * Search Google Books by keyword or ISBN for list of books
     * endpoint is https://plus.sr.cobiss.net/opac7/bib/risCit/$book_bib
     *
     * @param query search terms
     * @param is_barcode if search should be just by ISBN
     */
    fun searchOpenLibrary(query: String, is_barcode: Boolean = false) = viewModelScope.launch {

        val url = "https://$base/books/v1/volumes"
        val barcode_code = if (is_barcode) "isbn:" else ""
        val encodedQuery = encode(query, "utf-8")
        val urlQuery = "$url?q=$barcode_code$encodedQuery"
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

    /**
     * Search COBISS by keyword or ISBN for list of books
     * endpoint is
     * ttps://plus.sr.cobiss.net/opac7/bib/search/expert?c=$barcode_code$query&db=cobib&mat=allmaterials&tyf=1_knj_tskknj
     *
     * @param query search terms
     * @param is_barcode if search should be just by ISBN
     */
    fun searchCobiss(query: String, is_barcode: Boolean = false) = viewModelScope.launch {

        val bookList = mutableListOf<Book>()
        val barcode_code = if (is_barcode) "bn%3D" else ""
        val url = "https://plus.sr.cobiss.net/opac7/bib/search/expert?c=$barcode_code$query&db=cobib&mat=allmaterials&tyf=1_knj_tskknj"
//        val url_book = "https://plus.sr.cobiss.net/opac7/bib/risCit"

        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->

                val doc: Document = Jsoup.parse(response)
                val links: Elements = doc.select("tr[data-cobiss-id]")

                if (!links.isEmpty()) {
                    for (link in links) {
                        val book_object: Book = tableDataToBook(link)
                        bookList.add(book_object)
                    }

                    searches.setValue(bookList.subList(0, min(MAX_SEARCH_RESULTS, bookList.size)))
                    numResults.value = bookList.size
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

    /**
     * Parse line of RIS output (like bibtex) from COBISS
     *
     * RIS example:
     *   TY  - BOOK
     *   ID  - 120688908
     *   TI  - Ljudsko, suviše ljudsko : knjiga za slobodne duhove
     *   AU  - Nietzsche, Friedrich
     *   AU  - Ниче, Фридрих
     *   PY  - 2005
     *   SP  - 554
     *   CY  - Beograd
     *   PB  - Dereta
     *   SN  - 86-7346-451-X
     *
     * @param regex_string regex string for wanted field
     * @param ris_string RIS from COBISS
     */
    private fun parseRisField(regex_string: String, ris_string: String): String {
        val regex = "$regex_string".toRegex()
        try {
            val matchResults = regex.find(ris_string)!!
            val (field) = matchResults.destructured
            println("parseRisField book field: $field ")
            return field
        } catch (e: NullPointerException) {
            println("parseRisField Error: ${e.message}")
            return ""
        }

        return ""
    }

    /**
     * Convert COBISS table row of list of results to Book
     *
     * @param link Element from jsoup
     * @return Book object
     */
    private fun tableDataToBook(link: Element): Book {

        val book_bib = link.attr("data-cobiss-id")
        val title = link.attr("data-title")
        val author = link.select("span.author").first().text()
        val year = link.select("span.publishDate-data").first().text()
        val selfLink = "https://plus.sr.cobiss.net/opac7/bib/risCit/$book_bib"

        val book: Book = Book(
            bookId = 0,
            title = title,
            subtitle = "",
            cover = "",
            isbn10 = null,
            isbn13 = null,
            selfLink = selfLink,
            author = author,
            authorExtras = null,
            publisher = null,
//            year = publishYear as? Int,
            year = year?.toInt(),
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

    /**
     * Convert RIS file from COBISS to Book
     *
     * @param book RIS string
     * @return Book object
     */
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

