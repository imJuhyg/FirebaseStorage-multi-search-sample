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

   Google material icons는 구글에서 해상도별로 제공하는 아이콘 디자인 모음입니다. 따라서 멀티서치에 적용하기에도 적합합니다. 예제를 수월하게 따라하실 수 있도록 이름과 폴더를 깔끔하게 정리하여 깃 저장소에 올려두었습니다.

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