package com.timenotclocks.bookcase.api

import android.util.Log
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.timenotclocks.bookcase.database.Book
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

            val isbn10: String? = row["ISBN"]?.removeSurrounding("=\"", "\"")?.ifBlank { null }
            val isbn13: String? = row["ISBN13"]?.removeSurrounding("=\"", "\"")?.ifBlank { null }

            var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

            val book = Book(
                    bookId = 0,
                    title = title,
                    subtitle = subtitle,
                    isbn10 = isbn10,
                    isbn13 = isbn13,
                    author = row["Author"],
                    authorExtras = row["Additional Authors"],
                    publisher = row["Publisher"],
                    year = row["Year Published"]?.toIntOrNull(),
                    originalYear = row["Original Publication Year"]?.toIntOrNull(),
                    numberPages = row["Number of Pages"]?.toIntOrNull(),
                    rating = row["My Rating"]?.toIntOrNull(),
                    shelf =row.getOrDefault("Exclusive Shelf", "to-read"),
                    notes = row["My Review"],
                    dateAdded = when(!row["Date Added"].isNullOrEmpty()) {
                        true -> LocalDate.parse(row["Date Added"], formatter)
                        false -> LocalDate.now()

                    },
                    dateRead = when(!row["Date Read"].isNullOrBlank()) {
                        true -> LocalDate.parse(row["Date Read"], formatter)
                        false -> null
                    },
                    dateStarted= when(!row["Date Started"].isNullOrBlank()) {
                        // Custom column not included by goodreads
                        true -> LocalDate.parse(row["Date Started"], formatter)
                        false -> null
                    },
            )
            imports.add(book)
        }

        return imports
    }

}