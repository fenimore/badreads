/*
  Fenimore Love | 2021

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/


package com.timenotclocks.bookcase.database

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Size
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.room.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * TODO: too dumb to figure out how many to many works with Room
 */

// TODO: Add converter to Room data class
enum class ShelfType(val shelf: String) {
    CurrentShelf("currently-reading"), ReadShelf("read"), ToReadShelf("to-read")
}

enum class SortColumn(val column: String) {
    DateAdded("dateAdded"), DateRead("dateRead"), DateStarted("dateStarted"),
    Title("title"), Author("author"), Year("year")
}



@Entity(
        tableName = "books",
        indices = arrayOf(Index(value = ["title", "author"], unique = true)),
)
data class Book(
    @PrimaryKey(autoGenerate = true) var bookId: Long,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "subtitle") var subtitle: String?,
    @ColumnInfo(name = "cover") var cover: String?,
    @ColumnInfo(name = "isbn10") var isbn10: String?,
    @ColumnInfo(name = "isbn13") var isbn13: String?,
    @ColumnInfo(name = "selfLink") var selfLink: String?,
    @ColumnInfo(name = "author") var author: String?,
    @ColumnInfo(name = "authorExtras") var authorExtras: String?,
    @ColumnInfo(name = "publisher") var publisher: String?,
    @ColumnInfo(name = "year") var year: Int?,
    @ColumnInfo(name = "originalYear") var originalYear: Int?,
    @ColumnInfo(name = "numberPages") var numberPages: Int?,
    @ColumnInfo(name = "progress") var progress: Int?,
    @ColumnInfo(name = "series") var series: String?,
    @ColumnInfo(name = "language") var language: String?,
    @ColumnInfo(name = "dateAdded") var dateAdded: Long?,
    @ColumnInfo(name = "dateStarted") var dateStarted: Long?,
    @ColumnInfo(name = "dateRead") var dateRead: Long?,
    // create column for every shelf added
    @ColumnInfo(name = "rating") var rating: Int?,
    @ColumnInfo(name = "shelf") var shelf: String,
    @ColumnInfo(name = "description") var description: String?,
    @ColumnInfo(name = "notes") var notes: String?
) {
    fun cover(size: String = "thumbnail"): String? {
////        val isbn = isbn13 ?: isbn10
//        if (size == "thumbnail") {
//            // Load thumbnail of a specific media item.
//            val coverUri = cover?.toUri()
//            val thumbnail = getContentResolver().loadThumbnail(coverUri, Size(480, 480), null)
//            return thumbnail
//        }
        if (cover.isNullOrBlank()) run {
            cover = "content://com.timenotclocks.bookcase.fileprovider/book_cover_images/Android/data/com.timenotclocks.bookcase/files/Pictures/BookCoverPhoto-20210913_220545.jpg"
//            cover = ""
        }
        return cover
//        return "content://com.timenotclocks.bookcase.fileprovider/my_images/Android/data/com.timenotclocks.bookcase/cache/My_Captured_Photo.jpg"
//        return "https://covers.openlibrary.org/b/isbn/$isbn-$size.jpg?default=false"
    }

    fun titleString(): String {
        return subtitle?.let {
            sub -> "$title: $sub"
        } ?: title
    }
    fun authorString(): String {
        if (author != null && !authorExtras.isNullOrEmpty()) {
            return "$author, $authorExtras"
        }

        return author ?: ""
    }
    fun yearString(): String? {
        if (year != null && originalYear != null) {
            if (year == originalYear) {
                return year.toString()
            } else {
                return "$year ($originalYear)"
            }
        }

        if (year == null && originalYear != null) {
            return "$originalYear"
        }

        (year ?: originalYear)?.let {
            return it.toString()
        }

        return null
    }

    fun shelfString(): String {
        return when (shelf) {
            ShelfType.ReadShelf.shelf -> {
                "Read"
            }
            ShelfType.CurrentShelf.shelf -> {
                "Reading"
            }
            ShelfType.ToReadShelf.shelf -> {
                "To Read"
            }
            else -> {
                "Unshelved"
            }
        }
    }

    fun shelve(target: ShelfType, button: Button?, bookViewModel: BookViewModel?): Boolean {
        if (shelf != target.shelf) {
            shelf = target.shelf
            when (target) {
                ShelfType.ReadShelf -> {
                    dateRead = LocalDate.now().toEpochDay()
                }
                ShelfType.CurrentShelf -> {
                    dateStarted = LocalDate.now().toEpochDay()
                }
                else -> {}
            }
            if (dateAdded == null) {
                dateAdded = LocalDate.now().toEpochDay()
            }
            button?.text = shelf

            bookViewModel?.update(this)
        }
        return true
    }
}



var csvDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
var viewDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("LLL d, uuu")

fun fakeBook(
        id: Long = 0, title: String = "Dark Money",
        author: String? = "Jane Mayer",
        authorExtras: String? = "edit. Someoneelse",
        isbn13: String? = "978-0-385-53559-5",
        year: Int? = null,
        publisher: String? = null,
): Book {
    return Book(
        bookId = id,
        title = title,
        subtitle = "How Subs Change Books",
        cover = "content://com.timenotclocks.bookcase.fileprovider/my_images/Android/data/com.timenotclocks.bookcase/cache/My_Captured_Photo.jpg",
        isbn10 = "0123456789",
        isbn13 = isbn13,
        selfLink = "https://www.googleapis.com/books/v1/volumes/IAmWzgEACAAJ",
        author = author,
        authorExtras = authorExtras,
        publisher = publisher,
        year = year,
        originalYear = 2010,
        numberPages = 290,
        progress = null,
        series = null,
        language = null,
        rating = null,
        shelf = "currently-reading",
        description = null,
        notes = null,
        dateAdded = LocalDate.now().toEpochDay(),
        dateStarted = null,
        dateRead = null,
    )
}

fun emptyBook(
        fullTitle: String,
        author: String?,
        isbn13: String?,
        isbn10: String?,
): Book {
    var title: String = fullTitle
    var subtitle: String? = null
    if (fullTitle.contains(":")) {
        title = fullTitle.split(":").first().trim()
        subtitle = fullTitle.split(":")[1].trim()
    }
    return Book(
        bookId = 0,
        title = title,
        subtitle = subtitle,
        cover = "",
        isbn10 = isbn10,
        isbn13 = isbn13,
        selfLink = null,
        author = author,
        authorExtras = null,
        publisher = null,
        year = null,
        originalYear = null,
        numberPages = null,
        progress = null,
        series = null,
        language = null,
        rating = null,
        shelf = "to-read",
        description = null,
        notes = null,
        dateAdded = LocalDate.now().toEpochDay(),
        dateStarted = null,
        dateRead = null,
    )
}


// Full Text Search Table
@Fts4(contentEntity = Book::class)
@Entity(tableName = "books_fts")
class BooksFts(val bookId: Long, val title: String, val subtitle: String?, val author: String?, val shelf: String)
