package com.timenotclocks.bookcase.api

import android.content.Context
import android.util.Log
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.timenotclocks.bookcase.database.Book
import java.io.OutputStream
import java.io.OutputStreamWriter

const val LOG_EXP = "BookExport"

class Exporter {
    fun csv(output: OutputStream, books: List<Book>): OutputStream {
        val headers = listOf(
                "Title", "Author", "Additional Authors",
                "ISBN", "ISBN13", "Publisher",
                "My Rating", "Year Published", "Original Publication Year",
                "Date Read", "Date Added", "My Review",
                "Number of Pages"
        )
        val rows: List<List<String>> = books.map { book ->
            listOf(
                    book.title, book.author.orEmpty(), book.authorExtras.orEmpty(),
                    book.isbn10.orEmpty(), book.isbn13.orEmpty(), book.publisher.orEmpty(),
                    // book.rating, book.year, book.originalYear
                    // book.dateRead, book.dateAdded, book.notes,
                    // book.numberPages,
            )
        }
        csvWriter().open(output) {
            writeRow(headers)
            writeRows(rows)
        }
        Log.i(LOG_EXP, "Finish Writing csv with headers $headers")
        return output
    }
}