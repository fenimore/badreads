package com.timenotclocks.bookcase

import android.util.Log
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.InputStream
import java.text.SimpleDateFormat
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

            val title = row.get("Title") ?: continue

            val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.US)

            val book = Book(
                    bookId = 0,
                    title = title,
                    coverUrl = null,
                    isbn = when(!row["ISBN"].isNullOrEmpty()) {
                        true -> row["ISBN"]!!.drop(2).dropLast(1).toIntOrNull()
                        false -> null
                    },
                    isbn13 = when(row["ISBN13"] != null) {
                        true -> row["ISBN13"]!!.drop(2).dropLast(1).toLongOrNull()
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
                    dateAdded = when(!row["Date Added"].isNullOrEmpty()) {
                        true -> formatter.parse(row["Date Added"]!!)
                        false -> null

                    },
                    dateRead = when(!row["Date Read"].isNullOrBlank()) {
                        true -> formatter.parse(row["Date Read"]!!)
                        false -> null
                    },
            )
            imports.add(book)
        }

        return imports
    }

}