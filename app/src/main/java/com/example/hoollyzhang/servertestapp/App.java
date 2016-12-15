package com.example.hoollyzhang.servertestapp;

import android.app.Application;

/**
 * Created by hoollyzhang on 16/12/15.
 */

public class App extends Application {

    public static HttpServer httpServer;
    public static int port = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        getHttpServer();
    }

    private static void getHttpServer() {
        synchronized (HttpServer.class) {
            if (httpServer == null) {
                httpServer = new HttpServer();
                port = httpServer.startServer();
            }
        }
    }
}
