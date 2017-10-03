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