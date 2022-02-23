package com.example.storagemultisearch

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.example.storagemultisearch.util.getDeviceDpi
import com.example.storagemultisearch.util.getNetworkState
import com.example.storagemultisearch.util.registerNetworkCallback
import com.example.storagemultisearch.util.unregisterNetworkCallback
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.*

/**
 * MainActivity:
 * 네트워크 상태를 실시간으로 체크합니다.
 * 불러올 수 있는 모든 Url에 대하여 StorageReference 리스트를 생성합니다.
 * StorageReference 리스트를 참조하여 사용자가 검색한 파일 이름에 대한 Url을 다운로드합니다.
 * 다운로드 성공 시 Url리스트를 SubActivity로 넘겨줍니다.
 */

class MainActivity : AppCompatActivity() {
    private val inputMethodManager by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }
    private val editText: EditText by lazy { findViewById(R.id.edit_text) }
    private val progressBar: ProgressBar by lazy { findViewById(R.id.progress_bar) }
    private lateinit var directoryPath: String
    private val storage by lazy { FirebaseStorage.getInstance() }
    private lateinit var storageAllReference: StorageReference
    private lateinit var imageFileReferences: List<StorageReference>

    /* 네트워크 상태가 변동될 때 Callback 됩니다. */
    private val networkCallback = object: ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            /* 네트워크가 활성화 된 경우 */
            Handler(Looper.getMainLooper()).post {
                /* UI를 변경하려는 경우 */
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            /* 네트워크가 비활성화되거나 끊어진 경우 */
            Handler(Looper.getMainLooper()).post {
                /* UI를 변경하려는 경우 */
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(!getNetworkState()) {
            /* 순간적인 네트워크 상태를 알 수 있습니다 */
            Toast.makeText(this, "연결된 네트워크가 없습니다.", Toast.LENGTH_SHORT).show()

        } else {
            // directory path ex) https://firebase-storage/google_icons/drawable-xxhdpi/
            directoryPath = "google_icons/drawable-${getDeviceDpi()}/"
            storageAllReference = storage.reference.child(directoryPath)

            val listAllTask: Task<ListResult> = storageAllReference.listAll()
            listAllTask.addOnFailureListener {
                /* 리스트를 불러오는 데에 실패한 경우입니다. */

            }.addOnCompleteListener {
                if(it.isSuccessful) {
                    /* 성공적으로 결과를 불러왔을 경우입니다 */
                    imageFileReferences = it.result!!.items
                    editText.isEnabled = true
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        registerNetworkCallback(networkCallback)

        editText.setOnEditorActionListener { textView, actionId, keyEvent ->
            if(textView.text.length < 2) { // 2자 미만을 검색했을 때 작업시간이 오래 걸릴 수 있습니다.
                Toast.makeText(this, "두 자 이상 입력해야 합니다.", Toast.LENGTH_SHORT).show()

            } else if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                val uriList = ArrayList<Uri>()
                val fileName = textView.text.toString()
                var itemCount = 0

                progressBar.visibility = View.VISIBLE
                inputMethodManager.hideSoftInputFromWindow(editText.windowToken, 0)
                editText.setText("")
                editText.isEnabled = false

                for(reference in imageFileReferences) {
                    if(reference.name.contains(fileName)) {
                        ++itemCount // 불러올 파일 개수를 확인
                    }
                }

                if(itemCount == 0) { // 해당하는 항목이 없을경우
                    progressBar.visibility = View.GONE
                    editText.isEnabled = true
                    Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
                    true
                }

                for(reference in imageFileReferences) {
                    if(reference.name.contains(fileName)) {
                        reference.downloadUrl.addOnSuccessListener { uri ->
                            uriList.add(uri)
                            if(uriList.size == itemCount) { // 검색 완료
                                val intent = Intent(this@MainActivity, SubActivity::class.java)
                                intent.putExtra("URI_LIST", uriList)
                                startActivity(intent)

                                progressBar.visibility = View.GONE
                                editText.isEnabled = true
                            }

                        }.addOnFailureListener {
                            /* 다운로드에 실패한 경우입니다 */
                            progressBar.visibility = View.GONE
                            editText.isEnabled = true
                        }
                    }
                }
            }
            true
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterNetworkCallback(networkCallback)
    }
}