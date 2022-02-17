# FirebaseStorage multi search sample

### 개요

1. 이 예제는 Firebase Storage와 Glide를 사용하여 수 천개의 이미지 파일 중 사용자가 검색한 파일만을 가져오는 작업을 다룹니다.

2. Firebase Storage에서 멀티서치를 다루는 자료가 부족하여 직접 제작하게 되었습니다. 그러나 예제보다 더 효율적인 방법이 있을 수도 있습니다.

3. Firebase 라이브러리의 모든 부분을 다루지 않으며 꼭 필요한 부분만을 설명합니다.

   

---

### 개발환경

* Platform <b>Android</b>
* Language <b>Kotlin 1.5</b>
* Library <b>Firebase, Glide, Coroutine</b>



---

### 준비

1. Firebase Storage를 생성하고 앱 프로젝트에 적용해주세요. 자세한 절차는 [Firebase 문서](https://firebase.google.com/docs/android/setup?authuser=1) 를 참조하세요.

2. Firebase Storage의 Rules를 설정해주세요. 아래는 모든 사용자가 열람할 수 있는 Read only의 예시입니다.

   ```
   rules_version = '2';
   service firebase.storage {
     match /b/{bucket}/o {
       match /{allPaths=**} {
         allow read;
       }
     }
   }
   ```

3. Firebase Storage에 검색할 수 있는 이미지파일을 업로드하세요. 예제에서는 Google에서 제공하는 Material Icon을 사용합니다.

   Google material icons는 구글에서 해상도별로 제공하는 아이콘 디자인 모음입니다. 따라서 멀티서치에 사용하기에도 적합합니다. <b>예제를 수월하게 따라하실 수 있도록 이름과 폴더를 깔끔하게 정리하여 깃 저장소에 올려두었습니다.</b>

4. Firebase, Glide 라이브러리를 모듈수준 build.gradle에 삽입하세요.

   ```groovy
   // Firebase
   implementation platform('com.google.firebase:firebase-bom:29.1.0')
   implementation 'com.google.firebase:firebase-storage'
   
   // Glide
   implementation 'com.github.bumptech.glide:glide:4.13.0'
   annotationProcessor 'com.github.bumptech.glide:compiler:4.13.0'



---

### Firebase 개요

![firebase](./img/firebase.png)

구글이 소유한 모바일 애플리케이션 개발 플랫폼으로 Firestore Database, Realtime Database, Storage뿐만 아니라 호스팅 또는 출시 및 모니터링까지도 지원하고 있습니다. 예제는 이 중에서 Storage를 사용했습니다.



Firebase Storage는 애플리케이션이 이미지나 동영상 파일 등을 저장하고 공유할 수 있도록 해줍니다. 클라우드에 업로드되어있는 파일들을 내려받을 수도 있고 업로드할 수도 있습니다. 또한 네트워크 상태가 좋지 못할 때 자동으로 중단된 위치부터 작업을 재시도해주는 장점도 있습니다.





### Firebase Storage 사용방식

<b>우선 저장소에있는 개별 Url을 불러오는 방법입니다.</b>

1. 먼저 FirebaseStorage의 instance를 가져옵니다.

```kotlin
val storage = FirebaseStorage.getInstance()
```



2. 그런 다음 업로드 또는 다운로드할 파일의 경로를 참조해야합니다. 만약 단일 파일이라면 아래와같이 child() 메소드를 사용하면됩니다.

```kotlin

val storage = FirebaseStorage.getInstance()
val reference = storage.reference.child("google_icons/drawable-xhdpi/icon.png")

```

변수 reference는 StorageReference 타입입니다. 이 레퍼런스를 가지고 Url을 다운로드하거나 업로드 할 수 있으며 경로가 단일 파일이 아니라면 레퍼런스를 리스트로 구성할 수도 있습니다.



3. 레퍼런스로 해당 경로의 이미지 파일을 열람할 수 있는 Url을 얻습니다.

```kotlin
reference.downloadUrl.addOnSuccessListener { uri ->
	/* 다운로드에 성공한 경우입니다 */
  /* Uri값을 받습니다 */

}.addOnFailureListener { exception ->
	/* 다운로드에 실패한 경우입니다 */
}
```

downloadUrl는 비동기적으로 작업을 실행합니다. 개발자가 네트워킹 작업을 위해 스레드를 따로 생성할 필요가 없습니다.





<b>만약 단일 파일이 아니라 폴더 전체를 대상으로 가져와야한다면?</b>

예를 들어 경로가 단일 파일을 지정하는 것이 아닌 "/google_icons/drawable-xhdpi/" 처럼 상위 폴더만을 지정했을 때 하위 경로에 있는 모든 파일을 참조할 수 있는 방법도 있습니다. 이 때는 StorageReference의 list() 또는 listAll() 메소드를 사용합니다.

list()는 pageToken을 통해 파일들에 대한 StorageReference를 일정단위로 잘라서 가져올 수 있습니다. 일관된 페이지 수를 제공해야 하거나 추가 결과를 가져올 시기를 제어할 때 사용합니다.

listAll()은 하위 경로에 있는 모든 파일에 대한 StorageReference를 리스트형태로 반환 받을 수 있습니다.





1. list() - StorageReference를 100개 단위로 가져오는 예제입니다.

```kotlin
fun listAllPaginated(pageToken: String?) {
    val reference: StorageReference = storage.reference.child(
        "google_icons/drawable-xxhdpi/"
    ) // drawable-xxhdpi폴더의 하위 항목들에 대한 StorageReference를 생성합니다.

    val listPageTask: Task<ListResult> = if(pageToken != null) {
        reference.list(100, pageToken)
    } else {
        reference.list(100)
    }

    listPageTask.addOnSuccessListener { listResult ->
				// 100개 단위로 StorageReference를 받을 수 있습니다.
				// 마지막 토큰인 경우 List의 사이즈는 100이 아닐 수 있습니다.
        val referenceList: List<StorageReference> = listResult.items

        /* 다음 페이지로 진행할 수 있습니다. */
        listResult.pageToken?.let { pageToken -> listAllPaginated(pageToken) }

    }.addOnFailureListener {

    }
}

override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
  	setContentView(R.layout.activity_main)

  	listAllPaginated(null) // 첫 페이지 토큰을 null로 할당합니다.

  	// ... ///

```





2. listAll() - StorageReference를 한꺼번에 가져오는 예제입니다.

```kotlin
val reference: StorageReference = storage.reference.child(
    "google_icons/drawable-xxhdpi/"
) // drawable-xxhdpi폴더의 하위 항목들에 대한 StorageReference를 생성합니다.

val listAllTask: Task<ListResult> = reference.listAll()
listAllTask.addOnFailureListener {

}.addOnCompleteListener {
    if (it.isSuccessful) {
    	/* 성공적으로 결과를 불러왔을 경우입니다 */            
    	val referenceList: List<StorageReference> = it.result!!.items
      
      // ... //
}
```