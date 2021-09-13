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
                val isbn13: String? =  isbn13Array?.firstOrNull()?.get("identifier") as String
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

    fun searchOpenLibrary(query: String) = viewModelScope.launch {
        Log.i(LOG_EDIT, "FFF searchOpenLibrary $query")
        Log.i(LOG_EDIT, "Searching with string $query ")
//        val url = "https://$base/search.json"
        val url = "https://$base/books/v1/volumes"
        val encodedQuery = encode(query, "utf-8")
        val urlQuery = "$url?q=isbn:$encodedQuery"
//        val urlQuery = "$url?q=isbn:$encodedQuery"
        Log.i(LOG_EDIT, "urlQuery $urlQuery ")
        val stringRequest = StringRequest(Request.Method.GET, urlQuery,
                { response ->
                    val entry = Klaxon().parseJsonObject(StringReader(response))
//                    val results: JsonArray<JsonObject>? = entry["docs"] as? JsonArray<JsonObject>
                    val results: JsonArray<JsonObject>? = entry["items"] as? JsonArray<JsonObject>
                    if (results != null && !results.isEmpty()) {
                        val books: List<Book> = serializeSearchResults(results)
                        Log.i(LOG_SEARCH, "Flattened books: $books")
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
        Log.i(LOG_EDIT, "FFF serializeSearchResults $results")
        println("results results results: ${results.toString()}")
        return results.map { result ->
            Log.i(LOG_EDIT, "FFF serializeSearchResults 2 result 1 $result")
            val selfLink: String = result["selfLink"] as String
            val result: JsonObject = result["volumeInfo"] as JsonObject
            Log.i(LOG_EDIT, "FFF serializeSearchResults 3 result 2 $result")
//            val author: String? = (result["author_name"] as? JsonArray<*>)?.removeFirst() as String?
//            val authorExtras: String? = (result["author_name"] as? JsonArray<*>)?.joinToString(", ")
            val author: String? = (result["authors"] as? JsonArray<*>)?.removeFirst() as String?
            val authorExtras: String? = (result["authors"] as? JsonArray<*>)?.joinToString(", ")
            Log.i(LOG_EDIT, "FFF serializeSearchResults  author $author")
            // val publishers: JsonArray<String>? = result["publisher"] as? JsonArray<String>
            // val years: List<Int>? = result["publish_year"] as? JsonArray<Int>
//            val originalYear: Int? = result["first_publish_year"].toString().toIntOrNull()
//                    ?: (result["publish_year"] as? JsonArray<Int>)?.minOrNull()
//            val isbnArray: JsonArray<String>? = result["isbn"] as? JsonArray<String>
            val industryIdentifiers: Any? = result["industryIdentifiers"]
            Log.i(LOG_EDIT, "FFF serializeSearchResults  industryIdentifiers $industryIdentifiers")
            val originalYear: Int? = result["publishedDate"].toString().take(4).toIntOrNull()
                ?: (result["publishedDate"] as? JsonArray<Int>)?.minOrNull()
            Log.i(LOG_EDIT, "FFF serializeSearchResults  originalYear $originalYear")

            val isbnArray: JsonArray<String>? = result["industryIdentifiers"] as? JsonArray<String>
            val isbnObject: JsonObject? = result["industryIdentifiers"] as? JsonObject
//            val isbnString: JsonArray<Long?>? = isbnArray?.long("identifier")
            val isbnArray2 = isbnArray?.get("identifier") as? JsonArray<String>
            println("isbnObject: ${isbnObject.toString()}")
            println("isbnArray: ${isbnArray?.toJsonString(true)}")
//            println("isbnString: ${isbnString.toString()}")
            Log.i(LOG_EDIT, "FFF serializeSearchResults  isbnString $isbnArray2")
            // NOTE: OpenLibrary Search API just returns a list of ISBNs
            // and so because I cannot match them one to the other, I'm
            // just going to ignore ISBN 10
            // Similarly publishers and publish year are in random order,
            // so those are left null.
            val bookList: List<Book>? = isbnArray2?.filter { it.length > 10 }?.let { isbn13s ->
                isbn13s.map { isbn13 ->
                    Book(
                        bookId = 0,
                        title = result["title"].toString(),
                        subtitle = result["subtitle"] as? String,
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

