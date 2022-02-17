package com.example.storagemultisearch

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.example.storagemultisearch.util.getDeviceDpi
import com.example.storagemultisearch.util.getNetworkState
import com.example.storagemultisearch.util.registerNetworkCallback
import com.example.storagemultisearch.util.unregisterNetworkCallback
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.*
import kotlinx.coroutines.*

/**
 * MainActivity:
 * 네트워크 상태를 실시간으로 체크합니다.
 * 불러올 수 있는 모든 Url에 대하여 StorageReference 리스트를 생성합니다.
 * StorageReference 리스트를 참조하여 사용자가 검색한 파일 이름에 대한 Url을 다운로드합니다.
 * 다운로드 성공 시 Url리스트를 SubActivity로 넘겨줍니다.
 */

class MainActivity : AppCompatActivity() {
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
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            /* 네트워크가 비활성화되거나 끊어진 경우 */
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(!getNetworkState()) {
            /* 순간적인 네트워크 상태를 알 수 있습니다 */
            Toast.makeText(this, "연결된 네트워크가 없습니다.", Toast.LENGTH_SHORT).show()
        }

        /**
         * getDeviceDpi():
         * 디바이스의 Dpi를 계산할 수 있습니다.
         * 디바이스는 개별적인 Dpi를 가지고 있으며 이미지도 해당 Dpi에 맞는 이미지를 가져와야 합니다.
         * 따라서 Firebase Storage에는 해상도별로 폴더가 구분되어 있어야 합니다.
         */
        // directory path ex) https://firebase-storage/google_icons/drawable-xxhdpi/
        directoryPath = "google_icons/drawable-${getDeviceDpi()}/"
        storageAllReference = storage.reference.child(directoryPath)

        val listAllTask: Task<ListResult> = storageAllReference.listAll()
        listAllTask.addOnFailureListener {
            /* 리스트를 불러오는 데에 실패한 경우입니다. */

        }.addOnCompleteListener {
            if (it.isSuccessful) {
                /* 성공적으로 결과를 불러왔을 경우입니다 */
                imageFileReferences = it.result!!.items
                /* 불러오기를 실패하거나 불러오는 중일 때 imageFileReferences 를 참조한다면 에러가 발생합니다. */
                editText.isEnabled = true
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
                progressBar.visibility = View.VISIBLE // 프로그레스 바를 표시합니다.

                CoroutineScope(Dispatchers.Main).launch {
                    val fileName = editText.text.toString()
                    var iconCount = 0
                    /**
                     * downloadUrl 작업은 개별적인 스레드를 실행합니다.
                     * 즉 for 문이 끝나도 여전히 다운로드 중일 수 있습니다.
                     * 이 경우 의도한 데이터와 실제 반환된 리스트에 있는 데이터가 일치하지 않을 수 있습니다.
                     * 따라서 다운로드가 끝나기 전에 값을 반환하지 않도록 해야합니다.
                     * iconCount는 이러한 역할을 하는 변수입니다.
                     */
                    for(reference in imageFileReferences) {
                        if(reference.name.contains(fileName)) { // 검색되는 파일 개수를 먼저 확인합니다.
                            ++iconCount
                        }
                    }

                    val downloadJob: Deferred<ArrayList<Uri>> = async(Dispatchers.IO) {
                        val result = ArrayList<Uri>()

                        for(reference in imageFileReferences) {
                            if(reference.name.contains(fileName)) { // 검색한 내용이 파일 이름에 포함되는 경우입니다.
                                reference.downloadUrl.addOnSuccessListener { uri ->
                                    /* Url 가져오기에 성공했을 경우 입니다 */
                                    result.add(uri)

                                }.addOnFailureListener {
                                    /* 실패한 경우 입니다 */
                                    ArrayList<Uri>()

                                }
                            }
                        }

                        /* 데이터가 모두 불러와졌는지 확인하고 반환합니다. */
                        while(true) {
                            if(result.size == iconCount) break
                        }

                        result
                    }
                    downloadJob.join() // downloadJob의 작업이 끝나면 다음 코드가 실행됩니다.

                    progressBar.visibility = View.GONE
                    val uriList: ArrayList<Uri> = downloadJob.await()
                    val intent = Intent(this@MainActivity, SubActivity::class.java)
                    intent.putExtra("URI_LIST", uriList)
                    startActivity(intent)
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