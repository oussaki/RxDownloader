#  Reactive Downloader Library for Android

[![Build Status](https://api.travis-ci.org/oussaki/RxDownloader.svg?branch=master)](https://travis-ci.org/oussaki/RxDownloader)
[![API](https://img.shields.io/badge/API-14%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=14)

This library provides a simple api to download files and handle data stream in a reactive way 

# Usage
	

Example:

```java
// Create one instance 
 RxDownloader rxDownloader = new RxDownloader
                .Builder(context)
                .addFile("http://reactivex.io/assets/Rx_Logo_S.png") // you can add more files
                .build();

// Subscribe to start downloading files 
       rxDownloader.asList()
                .subscribeOn(Schedulers.computation())
                .subscribe(new BiConsumer<List<FileContainer>, Throwable>() {
                    @Override
                    public void accept(List<FileContainer> fileContainers, Throwable throwable) throws Exception {
                        // Do awesome things with your files

                    }
                });
```

# Hanling Progress & Error Events

There is 5 Events you can use in this library:
  
      * `doOnStart` : This event will be called before start downloading files
      * `doOnProgress` : This event will publish the current progress each time a file downloaded
      * `doOnSingleError` : Event called each time a file failed to download
      * `doOnCompleteWithError` : Event called when finish all the downloads and some of the files failed to download
      * `doOnCompleteWithSuccess` : Event called when finish downloading all the files successfully

* Example:

```java
 RxDownloader rxDownloader = new RxDownloader
                .Builder(context)
                .addFile("http://reactivex.io/assets/Rx_Logo_S.png")
                .build()
                .doOnStart(() -> {
                    progressBar.setProgress(0);
                    multiline.setText("");
                    txtProgress.setText("About to start downloading");
                })
                .doOnProgress(progress -> {
                    progressBar.setProgress(progress);
                    multiline.append("\n Progress " + progress);
                    txtProgress.setText("Progress: " + progress + "%");
                })
                .doOnSingleError(throwable -> {
                    multiline.append("\n " + throwable.getMessage());
                })
                .doOnCompleteWithError(() -> {
                    txtProgress.setText("Download finished with error");
                })
                .doOnCompleteWithSuccess(() -> {
                    txtProgress.setText("Download finished successfully");
                });
```

# Options :
* Customize OkHttpClient  
```java
	 OkHttpClient ok = new OkHttpClient.Builder().connectTimeout(6,TimeUnit.SECONDS).build();
	 new RxDownloader
                .Builder(context)
                .client(ok)
                .build();
```
* Customize Directory to save your files
By default files will be saved in cache directory.
You can just customize the directory by calling : `storage(File storagePath)`

* Download Strategy
  
1- MAX Strategy: will try to download all the files in case of errors it's will continue till the end.

```java
 	 new RxDownloader
                .Builder(context)
                .strategy(DownloadStrategy.MAX)
                

```

2- ALL Strategy: will try to download all the files but if it encountered an error it's will stop immediately.

```java
	new RxDownloader
                .Builder(context)
                .strategy(DownloadStrategy.ALL)
```

# Testing
  
You can find the tests inside the sample project in RxTest Class, Here is some examples (Using Robolectric).

* Testing downloading with Max Strategy:

```java
	rxDownloader = builder
                .strategy(DownloadStrategy.MAX)
                .addFile("http://fakeURL.com/error-file.jpg")  // this will trigger an error
                .build();
        TestObserver<List<FileContainer>> testObserver = rxDownloader.asList().test();
        testObserver.awaitTerminalEvent();
        testObserver
                .assertNoErrors()
                .assertValue(l -> l.size() == 3);

```
	
* Second test, Testing with ALL Strategy:

```java
	 rxDownloader = builder
                .strategy(DownloadStrategy.ALL)
                .addFile("http://fakeURL.com/error-file2.jpg") // this will trigger an error
                .addFile("http://reactivex.io/assets/Rx_Logo_S.png")
                .build();

        TestObserver<List<FileContainer>> testObserver = rxDownloader.asList().test();
        testObserver.awaitTerminalEvent();
        testObserver
                .assertNoErrors()
                .assertValueCount(1);
```

# Setup

The lib is available on jCenter. Add the following to your `build.gradle`:

```groovy
dependencies {
    compile ''
}
```

# License

	Copyright 2016 Oussama Abdallah

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	    http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.