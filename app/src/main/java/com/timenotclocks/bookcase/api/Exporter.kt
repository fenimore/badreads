package com.timenotclocks.bookcase.api

import android.util.Log
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.timenotclocks.bookcase.database.Book
import java.io.OutputStream

const val LOG_EXP = "BookExport"

class Exporter {
    fun csv(output: OutputStream, books: List<Book>): OutputStream {
        val headers = listOf(
                "Title", "Author", "Additional Authors",
                "Exclusive Shelf",  "Number of Pages",
                "ISBN", "ISBN13", "Publisher",
                "My Rating", "Year Published", "Original Publication Year",
                "Date Read", "Date Added", "Date Started",
                "My Review",
        )  // add the started date
        val rows: List<List<String>> = books.map { book ->
            listOf(
                    book.title, book.author.orEmpty(), book.authorExtras.orEmpty(),
                    book.shelf, book.numberPages.toString(),
                    book.isbn10.orEmpty(), book.isbn13.orEmpty(), book.publisher.orEmpty(),
                    book.rating.toString().ifEmpty { "" }, book.year.toString().ifEmpty { "" }, book.originalYear.toString().ifEmpty { "" },
                    book.dateRead.toString(), book.dateAdded.toString(), book.dateStarted.toString(),
                    book.notes.toString(),
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