package com.timenotclocks.bookcase

import android.content.Context
import android.net.Proxy
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.LinkHeader.Parameters.Type
import java.net.InetSocketAddress

class OpenLibraryApi {

    suspend fun isbn(isbn13: String): String {
        val client = HttpClient()  // TODO: make it take Android
        val host = "openlibrary.org"

        val urlString = URLBuilder(
                protocol = URLProtocol.HTTPS,
                host = host,
                encodedPath = "isbn/$isbn.json" // "/works/OL20800341W.json"  //
        ).buildString()
        val htmlContent = client.request<String> {
            url(urlString)  // I don't understand
            method = HttpMethod.Get
        }
        System.out.println(htmlContent)
        return htmlContent
    }
}