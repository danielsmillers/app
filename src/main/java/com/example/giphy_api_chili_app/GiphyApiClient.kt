import android.util.Log
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder

class GiphyApiClient(private val apiKey: String) {

    companion object {
        private const val BASE_URL = "https://api.giphy.com/v1/gifs"
        private const val TAG = "GiphyApiClient"
    }

    private val client = OkHttpClient()

    fun searchGifs(query: String, limit: Int = 25, offset: Int = 0, callback: (List<String>?, String?) -> Unit) {
        val url = "$BASE_URL/search"
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val fullUrl = "$url?q=$encodedQuery&api_key=$apiKey&limit=$limit&offset=$offset"
        val request = buildRequest(fullUrl)

        Log.d(TAG, "API Request URL: $fullUrl")

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string()
                    Log.d(TAG, "API Response: $responseBody")
                    val gifUrls = parseGifUrls(responseBody)
                    callback(gifUrls, null)
                } catch (e: JSONException) {
                    Log.e(TAG, "JSON parsing error: ${e.message}")
                    callback(null, "JSON parsing error")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Network error: ${e.message}")
                callback(null, "Network error")
            }
        })
    }

    private fun buildRequest(url: String): Request {
        // Build an OkHttp request with the specified URL
        return Request.Builder()
            .url(url)
            .build()
    }

    private fun parseGifUrls(response: String?): List<String> {
        val gifUrls = mutableListOf<String>()
        response?.let {
            val jsonObject = JSONObject(it)
            val data = jsonObject.getJSONArray("data")

            for (i in 0 until data.length()) {
                val gifObject = data.getJSONObject(i)
                // Extract GIF URL from JSON response
                val gifUrl = gifObject.getJSONObject("images").getJSONObject("fixed_height").getString("url")
                gifUrls.add(gifUrl)
            }
        }
        return gifUrls
    }
}
