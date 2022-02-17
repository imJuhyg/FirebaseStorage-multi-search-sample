package com.example.storagemultisearch

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storagemultisearch.recyclerview.IconRecyclerViewAdapter
import java.util.ArrayList

/** SubActivity:
 * 검색 결과를 RecyclerView에 표시합니다.
 */
class SubActivity : AppCompatActivity() {
    private var iconRecyclerViewAdapter: IconRecyclerViewAdapter? = null
    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recycler_view) }
    private lateinit var uriList: ArrayList<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub)

        uriList = intent.getParcelableArrayListExtra("URI_LIST") ?: ArrayList()

        iconRecyclerViewAdapter = IconRecyclerViewAdapter(this)

        // 리사이클러뷰에 3열의 GridLayoutManager 를 적용합니다.
        val gridLayoutManager = GridLayoutManager(this, 3)
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.adapter = iconRecyclerViewAdapter

        for(uri in uriList) {
            iconRecyclerViewAdapter!!.addItem(uri)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        recyclerView.adapter = null
        iconRecyclerViewAdapter = null
    }
}