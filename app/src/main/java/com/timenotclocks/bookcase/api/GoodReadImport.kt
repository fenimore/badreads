package com.timenotclocks.bookcase.api

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.timenotclocks.bookcase.database.Book
import com.timenotclocks.bookcase.database.csvDateFormatter
import java.io.InputStream
import java.time.LocalDate
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
            var cover: String? = null
            if (title.contains(":")) {
                title = fullTitle.split(":").first().trim()
                subtitle = fullTitle.split(":")[1].trim()
            }
            val additionalAuthors = row.get("Additional Authors")?.ifBlank { null }
            val isbn10: String? = row["ISBN"]?.removeSurrounding("=\"", "\"")?.ifBlank { null }
            val isbn13: String? = row["ISBN13"]?.removeSurrounding("=\"", "\"")?.ifBlank { null }

            val startString: String? = row.get("Date Started")?.ifBlank { null }
            val started: Long? = startString?.let { LocalDate.parse(it, csvDateFormatter).toEpochDay() }
            val readString: String? = row.get("Date Read")?.ifBlank { null }
            val read: Long? = readString?.let { LocalDate.parse(it, csvDateFormatter).toEpochDay() }
            val addedString: String? = row.get("Date Added")?.ifBlank { null }
            val added: Long = addedString?.let { LocalDate.parse(it, csvDateFormatter).toEpochDay() }
                    ?: LocalDate.now().toEpochDay()
            val book = Book(
                bookId = 0,
                title = title,
                subtitle = subtitle,
                cover = "",
                isbn10 = isbn10,
                isbn13 = isbn13,
                selfLink = null,
                author = row["Author"]?.ifBlank { null },
                authorExtras = additionalAuthors,
                publisher = row["Publisher"]?.ifBlank { null },
                year = row["Year Published"]?.toIntOrNull(),
                originalYear = row["Original Publication Year"]?.toIntOrNull(),
                numberPages = row["Number of Pages"]?.toIntOrNull(),
                progress = row["Progress"]?.toIntOrNull(),
                series = row["Series"]?.ifBlank { null },
                language = row["Language"]?.ifBlank { null },
                rating = row["My Rating"]?.toIntOrNull(),
                shelf = row.getOrDefault("Exclusive Shelf", "to-read"),
                description = row["Description"]?.ifBlank { null },
                notes = row["My Review"]?.ifBlank { null },
                dateAdded = added,
                dateRead = read,
                dateStarted = started,
            )
            imports.add(book)
        }

        return imports
    }

}