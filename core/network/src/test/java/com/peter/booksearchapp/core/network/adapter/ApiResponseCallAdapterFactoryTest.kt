package com.peter.booksearchapp.core.network.adapter

import com.peter.booksearchapp.core.network.ApiResponse
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

class ApiResponseCallAdapterFactoryTest {
    private val server = MockWebServer()
    private lateinit var api: TestApi

    @Before
    fun setUp() {
        server.start()
        val json = Json { ignoreUnknownKeys = true }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().readTimeout(1, TimeUnit.SECONDS).build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .addCallAdapterFactory(ApiResponseCallAdapterFactory(json))
            .build()
            .create(TestApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `2xx 응답은 Success로 감싼다`() = runTest {
        server.enqueue(MockResponse().setBody("""{"name":"코틀린"}"""))

        val response = api.get()

        assertEquals(ApiResponse.Success(TestDto("코틀린")), response)
    }

    @Test
    fun `2xx가 아닌 응답은 HttpError로 감싸고 서버가 준 에러 메시지를 담는다`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"errorType":"AccessDeniedError","message":"wrong appKey(invalid) format"}"""),
        )

        val response = api.get() as ApiResponse.Failure.HttpError

        assertEquals(401, response.code)
        assertEquals("wrong appKey(invalid) format", response.errorMessage)
        assertEquals(401, response.exception.code())
    }

    @Test
    fun `서버가 에러 메시지를 주지 않으면 errorMessage는 null이다`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("<html>Internal Server Error</html>"))

        val response = api.get() as ApiResponse.Failure.HttpError

        assertEquals(500, response.code)
        assertNull(response.errorMessage)
    }

    @Test
    fun `응답을 받지 못하면 NetworkError로 감싼다`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        val response = api.get()

        assertTrue(response is ApiResponse.Failure.NetworkError)
    }

    @Test
    fun `응답을 파싱할 수 없으면 UnknownError로 감싼다`() = runTest {
        server.enqueue(MockResponse().setBody("""{"name":"""))

        val response = api.get()

        assertTrue(response is ApiResponse.Failure.UnknownError)
    }

    private interface TestApi {
        @GET("test")
        suspend fun get(): ApiResponse<TestDto>
    }

    @Serializable
    private data class TestDto(val name: String)
}
