package com.oussaki.rxfilesdownloader;

/**
 * Created by oussama abdallah , AKA oussaki on 9/13/2017 , 3:04 PM.
 */

public class DownloadStrategy {
    /*
    * Strategies to download files
    * 1 - Try to download the maximum you can
    * 2 - Try to download all of them
    *
    * */

    /*
    * MAX: Flag means that the downloader will try to download all the files
    * in case of errors it's will continue till the end
    * */
    public static int MAX = 1;
    /*
    * ALL: Flag means that the downloader will try to download all the files
    * but if it encountered an error it's will stop immediately
    * */
    public static int ALL = 2;

    /*
    * Definition: <p>Default Flag </p>
    * */
    public static int DEFAULT = MAX;
}
