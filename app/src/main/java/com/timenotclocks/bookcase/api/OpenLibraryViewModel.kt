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
import com.timenotclocks.bookcase.api.RequestQueueSingleton
import com.timenotclocks.bookcase.database.Book
import kotlinx.coroutines.launch
import java.io.StringReader
import java.lang.Integer.min
import java.net.URLEncoder.encode
import java.time.LocalDate


const val MAX_SEARCH_RESULTS = 50
internal class OpenLibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val searches: MutableLiveData<List<Book>> by lazy {
        MutableLiveData()
    }

    fun getSearches(): LiveData<List<Book>> {
        return searches
    }

    var numResults: MutableLiveData<Int> = MutableLiveData<Int>(0)

    fun searchOpenLibrary(query: String) = viewModelScope.launch {
        val base = "openlibrary.org"
        val url = "https://$base/search.json"
        val encodedQuery = encode(query, "utf-8")
        val urlQuery = "$url?q=$encodedQuery"
        val stringRequest = StringRequest(Request.Method.GET, urlQuery,
                { response ->
                    val entry = Klaxon().parseJsonObject(StringReader(response))
                    entry["numFound"]?.toString()?.toIntOrNull()?.let {
                        numResults.setValue(it)
                    }
                    val results: JsonArray<JsonObject>? = entry["docs"] as? JsonArray<JsonObject>
                    if (results != null && !results.isEmpty()) {
                        val books: List<Book> = serializeSearchResults(results)
                        searches.setValue(books.subList(0, min(MAX_SEARCH_RESULTS, books.size)))
                    } else {
                        numResults.value = 0
                        searches.setValue(emptyList<Book>())
                        Log.e("BK", "No results :(")
                    }
                },
                {
                    Log.e("BK", "Volley Error $it")
                })
        RequestQueueSingleton.getInstance(getApplication()).addToRequestQueue(stringRequest)
    }

    private fun serializeSearchResults(results: JsonArray<JsonObject>): List<Book> {
        return results.map { result ->
            val author: String? = (result["author_name"] as? JsonArray<*>)?.removeFirst() as String?
            val authorExtras: String? = (result["author_name"] as? JsonArray<*>)?.joinToString(", ")
            val publisher: String? = (result["publisher"] as? JsonArray<*>)?.removeFirst() as String?
            val year: Int? = (result["publish_year"] as? JsonArray<Int>)?.removeFirst()
            val originalYear: Int? = result["first_publish_year"].toString().toIntOrNull()
                    ?: (result["publish_year"] as? JsonArray<Int>)?.minOrNull()
            val isbnArray: JsonArray<String>? = result["isbn"] as? JsonArray<String>
            val isbn10 = isbnArray?.firstOrNull { it.length <= 10 }?.replace("-", "")
            val isbn13 = isbnArray?.firstOrNull { it.length == 13 }?.replace("-", "")
            val secondIsbn13 = isbnArray?.getOrNull(1)
            Book(
                    bookId = 0,
                    title = result["title"].toString(),
                    subtitle = result["subtitle"] as? String,
                    isbn10 = isbn10,
                    isbn13 = isbn13,
                    author = author,
                    authorExtras = authorExtras,
                    publisher = publisher,
                    year = year,
                    originalYear = originalYear,
                    numberPages = null,
                    rating = null,
                    shelf = "to-read",
                    notes = secondIsbn13,
                    dateAdded = LocalDate.now(),
                    dateRead = null,
            )
        }
    }

}

