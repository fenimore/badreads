package com.timenotclocks.bookcase.api

import android.util.Log
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.timenotclocks.bookcase.database.Book
import com.timenotclocks.bookcase.database.csvDateFormatter
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


const val LOG_GOOD_READ = "BookGoodRead"

class GoodReadImport {

    fun serialize(inputStream: InputStream): List<Book> {
        val rows = csvReader().readAllWithHeader(inputStream)
        var imports: MutableList<Book> = ArrayList()

        for (row in rows) {
            val fullTitle = row.get("Title") ?: continue  // TODO Add subtitle
            var title: String = fullTitle
            var subtitle: String? = null
            if (title.contains(":")) {
                title = fullTitle.split(":").first().trim()
                subtitle = fullTitle.split(":")[1].trim()
            }
            val additionalAuthors = row.get("Additional Authors")
            val isbn10: String? = row["ISBN"]?.removeSurrounding("=\"", "\"")?.ifBlank { null }
            val isbn13: String? = row["ISBN13"]?.removeSurrounding("=\"", "\"")?.ifBlank { null }


            Log.i(LOG_EXP, "bkentries ${row.entries}")
            Log.i(LOG_EXP, "bknull ${row.getOrDefault("Date Started", null)}")
            Log.i(LOG_EXP, "bkstarted ${row}")
            Log.i(LOG_EXP, "bkarted ${row["Date Started"]}")
            Log.i(LOG_EXP, "bkstartedString ${row["Date Started"].toString()}")
            Log.i(LOG_EXP, "bkstarted ${row["Date Started"] == "null"}")
            Log.i(LOG_EXP, "blank ${row["Date Started"]?.isBlank()}")
            Log.i(LOG_EXP, "empty ${row["Date Started"]?.isEmpty()}")
            Log.i(LOG_EXP, "get ${row.get("Date Started")}")
            Log.i(LOG_EXP, "ifBlank ${row["Date Started"]?.ifBlank { "Is Blank" }}")
            val started = row["Date Started"].let {
                if(it.isNullOrBlank()) { null } else {
                    if (it == "null") { null } else {
                        LocalDate.parse(it, csvDateFormatter)
                    }
                }
            }
            val added: LocalDate? = row["Date Added"].let {
                if(it.isNullOrBlank()) { null } else {
                    if (it == "null") {
                        null
                    } else {
                        LocalDate.parse(it, csvDateFormatter)
                    }
                }
            }
            val read = row["Date Read"].let {
                if(it.isNullOrBlank()) { null } else {
                    if (it == "null") {
                        null
                    } else {
                        LocalDate.parse(it, csvDateFormatter)
                    }
                }
            }


            val book = Book(
                    bookId = 0,
                    title = title,
                    subtitle = subtitle,
                    isbn10 = isbn10,
                    isbn13 = isbn13,
                    author = row["Author"],
                    authorExtras = additionalAuthors,
                    publisher = row["Publisher"],
                    year = row["Year Published"]?.toIntOrNull(),
                    originalYear = row["Original Publication Year"]?.toIntOrNull(),
                    numberPages = row["Number of Pages"]?.toIntOrNull(),
                    rating = row["My Rating"]?.toIntOrNull(),
                    shelf = row.getOrDefault("Exclusive Shelf", "to-read"),
                    notes = row["My Review"],
                    dateAdded = added,
                    dateRead = read,
                    dateStarted = started,
            )
            imports.add(book)
        }

        return imports
    }

}