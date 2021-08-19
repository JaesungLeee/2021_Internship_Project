# Sendbird - Android 개발 문서

## Index
### 1. [Requirements](#requirements)
### 2. [Libraries](#libraries)
### 3. [Permissions](#permissions)
### 4. [Main Function](#main-function)

<br>


## Requirements
<br>

| Requirements | Version |
| ----------- | ----------- |
| Android Studio | `4.2.2` |
| Language | `Kotlin (1.5.10)` |
| Gradle Version | `6.7.1` | 
| JDK Version | `1.8 (JDK_1_8)` |
| Android Emulator | `30.7.4` |

| SDK | Version |
| --- | -------- |
| minSDK | `23 (Android 6.0)` |
| targetSDK | `30 (Android 11)` |
| Android SDK Build-Tools | `31-rc5` |
| Android SDK Platform | `Android 11.0(R), API Level 30` |
| Android SDK Platform-Tools | `31.0.2` |

<br>


## Libraries
<br>

| Libraries | Implementation | Version |
| --------- | ------- | ----------- |
| Sendbird SDK | com.sendbird.sdk:sendbird-android-sdk:`{VERSION}` | `3.0.166` |
| Retrofit | com.squareup.retrofit2:retrofit:`{VERSION}` | `2.9.0` |
| | com.squareup.retrofit2:converter-gson:`{VERSION}` | `2.9.0` | 
| | com.google.code.gson:gson:`{VERSION}` | `2.8.7` |
| Glide | com.github.bumptech.glide:glide:`{VERSION}` | `4.12.0` | 
| | kapt "android.arch.lifecycle:compiler:`{VERSION}` | `1.0.0` | 
| | kapt 'com.github.bumptech.glide:compiler:`{VERSION}` | `4.12.0` | 
| Google Social Login | com.google.android.gms:play-services-auth:`{VERSION}` | `19.2.0` |
| Facebook Social Login | com.facebook.android:facebook-login:`{VERSION}` | `8.2.0` |
| Kakao Social Login | implementation group: 'com.kakao.sdk', name: 'usermgmt', version:`{VERSION}` | `1.30.0` |
| | com.kakao.sdk:v2-user:`{VERSION}` | `2.5.0` |
| Naver Social Login | com.naver.nid:naveridlogin-android-sdk:`{VERSION}` | `4.2.6` |
| FCM Push Notification | com.google.firebase:firebase-core:`{VERSION}` | `19.0.0` |
| | com.google.firebase:firebase-messaging:`{VERSION}` | `22.0.0` |
| Progressbar | com.dinuscxj:circleprogressbar:`{VERSION}` | `1.3.6` |


<br>

## Permissions

1. Internet
2. Vibrate
3. External Storage (Read, Write)

<br>

### Implementation
```
// AndroidManifest.xml

<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

<br>

## Main Function
https://crawling-house-807.notion.site/Sendbird-Android-53f92d2ee0ec41f287e0e55eecdea13c