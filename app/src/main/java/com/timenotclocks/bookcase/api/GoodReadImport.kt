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

            val startString: String? = row.get("Date Started")?.ifBlank { null }
            val started: LocalDate? = startString?.let { LocalDate.parse(it, csvDateFormatter) }
            val readString: String? = row.get("Date Read")?.ifBlank { null }
            val read: LocalDate? = readString?.let { LocalDate.parse(it, csvDateFormatter) }
            val addedString: String? = row.get("Date Added")?.ifBlank { null }
            val added: LocalDate = addedString?.let { LocalDate.parse(it, csvDateFormatter) }
                    ?: LocalDate.now()
/*
                if (it.isNotBlank()) {

                } else {
                    Log.i(LOG_EXP, "This shouldn't happen, strange $row")
                    null
                }
            }
*/


            Log.i(LOG_EXP, "Book: $started $read $added")
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