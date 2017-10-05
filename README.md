#  Reactive Downloader Library for Android

[![Build Status](https://api.travis-ci.org/oussaki/RxDownloader.svg?branch=master)](https://travis-ci.org/oussaki/RxDownloader)
[![API](https://img.shields.io/badge/API-14%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=14)

This library provides a simple api to download files and handle data stream in a reactive way 


if you liked this library you can buy me a coffe : 
<a href='https://ko-fi.com/A8134D6Z' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://az743702.vo.msecnd.net/cdn/kofi1.png?v=0' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>

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
                .subscribe(new BiConsumer<List<FileContainer>, Throwable>() {
                    @Override
                    public void accept(List<FileContainer> fileContainers, Throwable throwable) throws Exception {
                        // Do awesome things with your files

                    }
                });
```

# Handling Progress & Error Events

There is 5 Events you can use in this library:
  
      * `doOnStart` : This event will be called before start downloading files.
      * `doOnProgress` : This event will publish the current progress each time a file downloaded.
      * `doOnEachSingleError` : Event called each time a file failed to download.
      * `doOnCompleteWithError` : Event called when finish all the downloads and some of the files failed to download.
      * `doOnCompleteWithSuccess` : Event called when finish downloading all the files successfully.

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
                .doOnEachSingleError(throwable -> {
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

* Building list of urls methods:

	- `addFile(String url)`: add url directly to list of urls without renaming the downloaded file.
	- `addFile(String newName, String url)`: add url to list and renaming the file to newName, Ps: No need to write the file extension.
	- `addFiles(List<String> urls)`: add a bulk of URL's.



# Download Strategies
  
  	-MAX Strategy: will try to download all the files in case of errors it's will continue till the end.

```java
 	 new RxDownloader
                .Builder(context)
                .strategy(DownloadStrategy.MAX)
                

```

    
    -ALL Strategy: will try to download all the files but if it encountered an error it's will stop immediately.

```java
	new RxDownloader
                .Builder(context)
                .strategy(DownloadStrategy.ALL)
```

Sometimes you want your files to be in a certain order, for that you can achieve that by calling the `Order` Method in `Builder` this way:

  ```java 
  .Order(DownloadStrategy.FLAG_SEQUENTIAL)
  ```

    * By default the downloader is using `FLAG_PARALLEL` to achieve more speed and performance 

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

Add it in your root build.gradle at the end of repositories:

```groovy 
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

Add the dependency to your `build.gradle`:

```groovy
dependencies {
    compile 'com.github.oussaki:RxDownloader:0.3'
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