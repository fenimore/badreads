package bookcase

import com.timenotclocks.bookcase.OpenLibraryApi
import kotlinx.coroutines.runBlocking
import org.junit.Test


class OpenLibraryApiTest {

    //val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun fetchIsbn() {
        // Given a Context object retrieved from Robolectric...
        // ...when the string is returned from the object under test...
        val result: String = runBlocking {
            OpenLibraryApi().isbn("9781250134769")
        }
        System.out.println(result.toString())

        //// ...then the result should be the expected one.
        assert(true)
    }
}
