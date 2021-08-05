package com.timenotclocks.bookcase.api

import android.util.Log
import com.github.doyaaaaaken.kotlincsv.dsl.context.WriteQuoteMode
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.timenotclocks.bookcase.database.Book
import com.timenotclocks.bookcase.database.csvDateFormatter
import java.io.OutputStream
import java.time.LocalDate

const val LOG_EXP = "BookExport"

class Exporter {
    fun csv(output: OutputStream, books: List<Book>): OutputStream {
        val headers = listOf(
            // basic info
            "Title", "Author", "Additional Authors",
            // more info
            "ISBN", "ISBN13", "Publisher",
            "Number of Pages", "Year Published", "Original Publication Year",
            "Progress", "Series", "Language",
            // dates
            "Date Read", "Date Added", "Date Started",
            // Library status
            "My Rating", "My Review", "Exclusive Shelf"
        )  // add the started date
        val rows: List<List<String>> = books.map { book ->
            Log.i(LOG_EXP, "The book ${book.bookId}: ${book.titleString()}")
            listOf(
                // basic info
                book.titleString(),  // Goodreads uses colon separated subtitle
                book.author.orEmpty(), book.authorExtras.orEmpty(),
                // more info
                book.isbn10.orEmpty(), book.isbn13.orEmpty(), book.publisher.orEmpty(),
                book.numberPages?.toString().orEmpty(),
                book.year?.toString().orEmpty(),
                book.originalYear?.toString().orEmpty(),
                book.progress?.toString().orEmpty(),
                book.series.orEmpty(),
                book.language.orEmpty(),
                // dates
                book.dateRead?.let { LocalDate.ofEpochDay(it).format(csvDateFormatter) } ?: "",
                book.dateAdded?.let { LocalDate.ofEpochDay(it).format(csvDateFormatter) } ?: "",
                book.dateStarted?.let { LocalDate.ofEpochDay(it).format(csvDateFormatter) } ?: "",
                // Library status
                book.rating?.toString().orEmpty(),
                book.notes.orEmpty(),
                book.shelf,
                // NOTE: put shelf last because it's non null (no empty string as last value
            )
        }
        csvWriter {
            quote {
                mode = WriteQuoteMode.ALL
            }
        }.open(output) {
            // TODO: is there an issue here
            writeRow(headers)
            writeRows(rows)
        }
        Log.i(LOG_EXP, "Finish Writing csv with headers $headers")
        return output
    }
}