package com.timenotclocks.bookcase.api

import android.util.Log
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.timenotclocks.bookcase.database.Book
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class GoodReadImport {

    fun serialize(inputStream: InputStream): List<Book> {
        Log.i("BK", "Rows")
        val rows = csvReader().readAllWithHeader(inputStream)
        var imports: MutableList<Book> = ArrayList()

        for (row in rows) {
            Log.d("BK", row.toString())
            for (m in row.entries) {
                Log.i("BK", m.key + ": " + m.value)
            }

            val fullTitle = row.get("Title") ?: continue  // TODO Add subtitle
            var title: String = fullTitle
            var subtitle: String? = null

            if (title?.contains(":") == true) {
                title = fullTitle.split(":").first().trim()
                subtitle = fullTitle.split(":")[1].trim()
            }

            var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

            val book = Book(
                    bookId = 0,
                    title = title,
                    subtitle = subtitle,
                    isbn10 = when(!row["ISBN"].isNullOrEmpty()) {
                        // TODO: .... deal with swiggly line
                        true -> row["ISBN"]!!.drop(2).dropLast(1)  // removes =""
                        false -> null
                    },
                    isbn13 = when(row["ISBN13"] != null) {
                        true -> row["ISBN13"]!!.drop(2).dropLast(1)
                        false -> null
                    },
                    author = row["Author"],
                    authorExtras = row["Additional Authors"],
                    publisher = row["Publisher"],
                    year = row["Year Published"]?.toIntOrNull(),
                    originalYear = row["Original Publication Year"]?.toIntOrNull(),
                    numberPages = row["Number of Pages"]?.toIntOrNull(),
                    rating = row["My Rating"]?.toIntOrNull(),
                    shelf =row.getOrDefault("Exclusive Shelf", "to-read"),
                    notes = row["My Review"],
                    dateAdded = when(!row["Date Added"].isNullOrEmpty() && row["Date Added"] != "null") {
                        true -> LocalDate.parse(row["Date Added"], formatter)
                        false -> LocalDate.now()

                    },
                    dateRead = when(!row["Date Read"].isNullOrBlank() && row["Date Read"] != "null") {
                        true -> LocalDate.parse(row["Date Read"], formatter)
                        false -> null
                    },
            )
            imports.add(book)
        }

        return imports
    }

}