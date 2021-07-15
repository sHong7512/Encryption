# Encryption (with MVVM_AAC)
# Requirements
- Kotlin 1.5.10
- Gradle 4.2.1
- Android min SDK 28
- Android target SDK 30
- etc

# Installation
- gradle(:project)
```
buildscript {
    …
    dependencies {
        …
        classpath 'com.google.gms:google-services:4.3.8'
    }
}
```


- gradle(:app)
```
apply plugin: 'com.google.gms.google-services'

dependencies {
   	…
    // For Identity Credential APIs
    implementation("androidx.security:security-identity-credential:1.0.0-alpha02")

    // For App Authentication APIs
    implementation("androidx.security:security-app-authenticator:1.0.0-alpha02")
}
```

- Manifest
```
<application
        android:allowBackup="false"
        ...
    </application>
```



# Description

1. SharedPreference를 사용한다면 Context.MODE_PRIVATE로 설정
 - androidx에 EncryptedSharedPreferences가 있어서 간단하게 Encryption 적용시 이걸로 적용해보자


2. AndroidManifest.xml 에서 저장소를 내부 저장소로만 고정 및 백업을 허용하지 않기
```
 android: installLocation = “internalOnly”    //최근은 바뀐듯
 
 android: allowBackup = “false”
```


3. AES 암호화를 사용한 사용자 데이터 보호 방법
- PBKD 구조
- 암호(User Passwor)를 랜덤데이터(salt)와 함께 여러번 해싱하여 AES Key를 만듬 
- 고유한 키를 가지게 되므로, 키가 해킹되더라도 같은 암호를 가진 암호들이더라도 노출되지 않음


4. Encryption

 - 난수 생성
```
val random = SecureRandom()
val salt = ByteArray(256)
random.nextBytes(salt)
```
 - key 생성
```
val pbKeySpec = PBEKeySpec(password, salt, 1324, 256)
val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded 
val keySpec = SecretKeySpec(keyBytes, "AES") 
```

 - Initalization Vector 생성(동일한 데이터를 암호화할때 첫블록이 같으므로 이 부분을 해결하기 위해)
```
val ivRandom = SecureRandom()   //기존변수(random)이 아닌 새로운 SecureRandom 변수를 만들어서 사용해야함
val iv = ByteArray(16)
ivRandom.nextBytes(iv)
val ivSpec = IvParameterSpec(iv)
```
 - Encrypt (AES의 모드 중 표준모드인 CBC(cipher block chaining)사용)
```
val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")     //AES모드 선택 및 PKCS7패딩(모든 데이터가 블록 크기에 맞는게 아니므로 남는 공간에 패딩) 선택
cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
val encrypted = cipher.doFinal(dataToEncrypt)
```
 - 값들 hashmap에 넣음
```
map["salt"] = salt
map["iv"] = iv
map["encrypted"] = encrypted
```

5. Decrypt

 - 값 변수 대입
```
val salt = map["salt"]
val iv = map["iv"]
val encrypted = map["encrypted"]
```
- regenerate key from password
```
val pbKeySpec = PBEKeySpec(password, salt, 1324, 256)
val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
val keySpec = SecretKeySpec(keyBytes, "AES")
```
- Decrypt
```
val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
val ivSpec = IvParameterSpec(iv)
cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
decrypted = cipher.doFinal(encrypted)
```
=> 대칭 암호화 알고리즘을 사용하기 때문에 역 추적하면 됨


6. SharedPreferences 암호화
 - 데이터를 CharArray로 받아옴
```
val password = CharArray(login_password.length())
login_password.text.getChars(0, login_password.length(), password, 0)
```
 - DateFormat은 Base64형식임
```
val currentDateTimeString = DateFormat.getDateTimeInstance().format(Date())
 - 데이터 Encrypt (문자열 형식을 고려해야함) 
val map = Encryption().encrypt(currentDateTimeString.toByteArray(Charsets.UTF_8), password)
```
 - Convert Base64 -> String
```
val valueBase64String = Base64.encodeToString(map["encrypted"], Base64.NO_WRAP)
val saltBase64String = Base64.encodeToString(map["salt"], Base64.NO_WRAP)
val ivBase64String = Base64.encodeToString(map["iv"], Base64.NO_WRAP)
```
 - Save to shared prefs
```
val editor = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit()
editor.putString("l", valueBase64String)
editor.putString("lsalt", saltBase64String)
editor.putString("liv", ivBase64String)
editor.apply()
```

 7. SharedPreferences 복호화 
 - Get password
```
val password = CharArray(login_password.length())
login_password.text.getChars(0, login_password.length(), password, 0)
```
 - shared prefs data 에서 가져옴
```
val preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
val base64Encrypted = preferences.getString("l", "")
val base64Salt = preferences.getString("lsalt", "")
val base64Iv = preferences.getString("liv", "")
```
 - Base64형식으로 디코딩
```
val encrypted = Base64.decode(base64Encrypted, Base64.NO_WRAP)
val iv = Base64.decode(base64Iv, Base64.NO_WRAP)
val salt = Base64.decode(base64Salt, Base64.NO_WRAP)
```
 - 복호화
```
val decrypted = Encryption().decrypt(
    hashMapOf("iv" to iv, "salt" to salt, "encrypted" to encrypted), password)

var lastLoggedIn: String? = null
decrypted?.let {
    lastLoggedIn = String(it, Charsets.UTF_8)
}
```

8. Generating new random key(keystore) 
```
val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
val keyGenParameterSpec = KeyGenParameterSpec.Builder("MyKeyAlias",
    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
// 2 requires lock screen, invalidated if lock screen is disabled
    //.setUserAuthenticationRequired(true) 
// 3 only available x seconds from password authentication. -1 requires finger print - every time
    //.setUserAuthenticationValidityDurationSeconds(120) 
// 4 different ciphertext for same plaintext on each call    
    .setRandomizedEncryptionRequired(true) 
    .build()
keyGenerator.init(keyGenParameterSpec)
keyGenerator.generateKey()
```

9. Encrypting keystore data

```
val keyStore = KeyStore.getInstance("AndroidKeyStore")
keyStore.load(null)

val secretKeyEntry = keyStore.getEntry("MyKeyAlias", null) as KeyStore.SecretKeyEntry
val secretKey = secretKeyEntry.secretKey

//Encrypt data
val cipher = Cipher.getInstance("AES/GCM/NoPadding")
cipher.init(Cipher.ENCRYPT_MODE, secretKey)
val ivBytes = cipher.iv
val encryptedBytes = cipher.doFinal(dataToEncrypt)

map["iv"] = ivBytes
map["encrypted"] = encryptedBytes
```

10. Decrypting keystore data

```
val keyStore = KeyStore.getInstance("AndroidKeyStore")
keyStore.load(null)

val secretKeyEntry = keyStore.getEntry("MyKeyAlias", null) as KeyStore.SecretKeyEntry
val secretKey = secretKeyEntry.secretKey

//Extract info from map
val encryptedBytes = map["encrypted"]
val ivBytes = map["iv"]


//Decrypt data
val cipher = Cipher.getInstance("AES/GCM/NoPadding")
val spec = GCMParameterSpec(128, ivBytes)
cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
decrypted = cipher.doFinal(encryptedBytes)
```

참조 : https://www.raywenderlich.com/778533-encryption-tutorial-for-android-getting-started#toc-anchor-005


