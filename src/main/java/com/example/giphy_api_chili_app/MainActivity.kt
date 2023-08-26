package com.example.giphy_api_chili_app

import GiphyApiClient
import android.content.ContentValues.TAG
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity() {

    private lateinit var giphyApiClient: GiphyApiClient
    private lateinit var gifAdapter: GifAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var loadingIndicator: ProgressBar
    private val searchDelayMillis = 1000L
    private val searchHandler = Handler(Looper.getMainLooper())
    private var lastSearchQuery = ""
    private var currentOffset = 0
    private var apiKey: String = BuildConfig.API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the Giphy API client with the API key from BuildConfig
        giphyApiClient = GiphyApiClient(apiKey)

        searchEditText = findViewById(R.id.searchEditText)
        recyclerView = findViewById(R.id.recyclerView)
        loadingIndicator = findViewById(R.id.loadingIndicator)

        gifAdapter = GifAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = gifAdapter

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchHandler.removeCallbacksAndMessages(null)
            }

            override fun afterTextChanged(s: Editable?) {
                searchHandler.postDelayed({
                    val query = s.toString()
                    if (query != lastSearchQuery) {
                        performSearch(query)
                        lastSearchQuery = query
                    }
                }, searchDelayMillis)
            }
        })

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // Respond to scroll events and load more GIFs when reaching the end
                // of the list and scrolling downwards
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItem == totalItemCount - 1 && dy > 0) {
                    // Only load more GIFs when not already loading
                    if (loadingIndicator.visibility == View.GONE) {
                        loadMoreGifs(lastSearchQuery)
                    }
                }
            }
        })
    }

    private fun loadMoreGifs(query: String) {
        loadingIndicator.visibility = View.VISIBLE

        // Load more GIFs using the Giphy API client
        giphyApiClient.searchGifs(query, limit = 25, offset = currentOffset) { gifUrls, error ->
            runOnUiThread {
                loadingIndicator.visibility = View.GONE

                if (error != null) {
                    // Display a Snackbar with the error message
                    showSnackbar("Error loading more GIFs: $error")
                    Log.e(TAG, "loadMoreGifs error")
                } else {
                    // Add the loaded GIFs to the adapter
                    gifUrls?.let {
                        gifAdapter.addGifs(it)
                        currentOffset += it.size
                    }
                }
            }
        }
    }

    private fun performSearch(query: String) {
        loadingIndicator.visibility = View.VISIBLE

        // Clear the existing GIFs before performing a new search
        gifAdapter.clearGifs()

        // Perform a search using the Giphy API client
        giphyApiClient.searchGifs(query) { gifUrls, error ->
            runOnUiThread {
                loadingIndicator.visibility = View.GONE

                if (error != null) {
                    // Display a Snackbar with the error message
                    showSnackbar("Error performing search: $error")
                    Log.e(TAG, "performSearch error")
                } else {
                    // Add the searched GIFs to the adapter
                    gifAdapter.addGifs(gifUrls ?: mutableListOf())
                    currentOffset = gifUrls?.size ?: 0
                }
            }
        }
    }

    private fun showSnackbar(message: String) {
        // Display a Snackbar at the bottom of the screen with the provided message
        Snackbar.make(recyclerView, message, Snackbar.LENGTH_LONG).show()
    }
}
