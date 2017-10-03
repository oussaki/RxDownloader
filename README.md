#  Reactive Downloader Library for Android

[![Build Status](https://api.travis-ci.org/oussaki/RxDownloader.svg?branch=master)](https://travis-ci.org/oussaki/RxDownloader)
[![API](https://img.shields.io/badge/API-16%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=16)

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
        rxDownloader.asList().subscribe((fileContainers, throwable) -> {
           // Do awesome things with your files
        });
```

# Testing
  
  You can find the tests inside the sample project in RxTest Class, Here is some examples (Using Robolectric) :
	Testing downloading with Max Strategy:

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
	
	Second test, Testing with ALL Strategy:
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