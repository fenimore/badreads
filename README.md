# Badreads

Badreads is a book tracking Android application for logging books
you're reading, read, and want to read. Your library "shelves" (reading, read, and to read)
are stored locally on your phone. Use the search or barcode reader to look up
new book on [OpenLibrary](https://openlibary.org) and add it to your library. You can
import and export your library from or to Open Library and Goodreads.

Badreads is very unstable. I recommend backing up with an export before installing updates or changes.

## License

GPLv3
Copyright 2021 Fenimore Love

## Bug:

1. Deleting data directly after exporting, and then importing that same csv will break the CSV.
There seems to be an issue with the first line of the "rows" that are written (after the headers).

## TODO:

1. Move export and import to settings
2. Fix settings action bar color
2. Manual New Book Entry
4. Custom Icons
5. Multiple Authors/DB Relation
8. Add tag/star to books
9. OpenLibrary book details button?
10. Search openlibrary within app
11. Fetch description
12. Add setting for default tab
13. Empty library prompt

## TODO: sort
1. Sort a - z
2. Sort z - a
3. Sort page numbers
4. Sort rating