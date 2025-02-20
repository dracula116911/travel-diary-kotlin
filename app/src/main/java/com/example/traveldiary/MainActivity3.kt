package com.example.traveldiary

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.appcompat.widget.SearchView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity3 : AppCompatActivity(), onLocationCLickListener {

    private lateinit var db: FirebaseFirestore
    private lateinit var locationRecyclerView: RecyclerView
    private lateinit var addLocationFab: FloatingActionButton
    private lateinit var adapter: LocationAdapter
    private var lastAnimatedPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main3)
        setupWindowInsets()

        setSupportActionBar(findViewById(R.id.toolbar))

        db = FirebaseFirestore.getInstance()

        locationRecyclerView = findViewById(R.id.loc_recyclerview)
        addLocationFab = findViewById(R.id.addlocationFab)

        adapter = LocationAdapter(this)
        locationRecyclerView.layoutManager = LinearLayoutManager(this)
        locationRecyclerView.adapter = adapter
        locationRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0) { // Scrolling down
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                    for (i in firstVisibleItemPosition..lastVisibleItemPosition) {
                        if (i > lastAnimatedPosition) {
                            val viewHolder = recyclerView.findViewHolderForAdapterPosition(i) as? LocationViewHolder
                            viewHolder?.animation(viewHolder.itemView)
                            lastAnimatedPosition = i
                        }
                    }
                }
            }
        })

        setupSwipeRefreshLayout()
        setupAddLocationFabListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main3, menu)

        // Get the SearchView and set up the listener
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView
        searchView?.queryHint = "Search locations"
        val queryHintTextView: TextView = searchView?.findViewById(androidx.appcompat.R.id.search_src_text)!!
        queryHintTextView.setTextColor(Color.WHITE)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    performSearch(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    performSearch(newText)
                }
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                showLogoutConfirmationDialog()
                return true
            }
            R.id.about_us -> {

                showAboutUsDialog()
                return true
            }
            R.id.action_profile -> {

                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main3)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupSwipeRefreshLayout() {
        val swipeRefreshLayout: SwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            fetchLocationsFromFirestore()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setupAddLocationFabListener() {
        addLocationFab.setOnClickListener {
            val intent = Intent(this, MainActivity6::class.java)
            startActivity(intent)
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showAboutUsDialog() {
        AlertDialog.Builder(this)
            .setTitle("About Us")
            .setMessage("This is the Travel Diary App, where you can record your travel experiences.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performSearch(query: String) {
        val lowercaseQuery = query.lowercase()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).collection("locations")
                .get()
                .addOnSuccessListener { documents ->
                    val searchResults = documents.mapNotNull { document ->
                        Log.d("FirestoreData", "Document data: ${document.data}") // Log document data
                        val name = document.getString("name")
                        if (name?.lowercase()?.contains(lowercaseQuery) == true) {
                            val location = document.toObject(Locations::class.java)
                            Log.d("SearchResult", "Location:$location") // Log converted location object
                            location
                        } else {
                            null
                        }
                    }

                    // ... rest of the sorting and update logic ...
                    val (startsWithMatches, containsMatches) = searchResults.partition {
                        it.name?.lowercase()?.startsWith(lowercaseQuery) == true}
                    val sortedResults = startsWithMatches + containsMatches

                    adapter.updateData(sortedResults)
                }
                .addOnFailureListener { exception ->
                    Log.e("MainActivity3", "Error performing search", exception)
                }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchLocationsFromFirestore()
    }
    private fun fetchLocationsFromFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).collection("locations")
                .get()
                .addOnSuccessListener { documents ->
                    val locations = documents.map { document ->
                        Locations(
                            documentId = document.id,
                            name = document.getString("name") ?: "",
                            address = document.getString("address") ?: "",
                            date = document.getString("date") ?: "",
                            notes = document.getString("notes") ?: "",
                            imageUrl = document.getString("imageUrl") ?: "",
                            userId = document.getString("userId") ?: ""
                        )
                    }
                    adapter.submitList(locations) // Use submitList with DiffUtil
                    lastAnimatedPosition = -1
                }
                .addOnFailureListener {
                    Log.e("MainActivity3", "Error fetching locations from Firestore", it)
                }
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onLocationClick(locations: Locations) {
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("documentId", locations.documentId)
        intent.putExtra("name", locations.name)
        intent.putExtra("address", locations.address)
        intent.putExtra("date", locations.date)
        intent.putExtra("notes", locations.notes)
        intent.putExtra("userId", locations.userId)
        intent.putExtra("imageUrl", locations.imageUrl) // Pass imageUrl
        startActivity(intent)
        Log.d("MainActivity3", "Location clicked: ${locations.name}")
    }
}
