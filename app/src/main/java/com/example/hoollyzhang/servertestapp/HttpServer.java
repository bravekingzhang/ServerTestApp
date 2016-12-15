package com.example.hoollyzhang.servertestapp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hoollyzhang on 16/12/15.
 */
public class HttpServer {

    private AtomicInteger clientCount = new AtomicInteger();
    private ExecutorService executorService = Executors.newFixedThreadPool(8);

    private Thread waitConnectionThread;

    public int startServer() {
        int port = 0;
        try {
            InetAddress inetAddress = InetAddress.getByName("127.0.0.1");
            ServerSocket serverSocket = new ServerSocket(0, 8, inetAddress);
            port = serverSocket.getLocalPort();
            System.out.println("server start at http:/" + serverSocket.getLocalSocketAddress());
            CountDownLatch startSignal = new CountDownLatch(1);
            waitConnectionThread = new Thread(new AccecpTread(serverSocket, startSignal));
            waitConnectionThread.start();
            startSignal.await();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return port;
    }

    private void acceptRequest(ServerSocket serverSocket) throws IOException {
        while (true) {
            executorService.submit(new RequestThread(serverSocket.accept()));
            System.out.println("当前客户端数量" + clientCount.incrementAndGet());
        }
    }

    class AccecpTread implements Runnable {

        private final ServerSocket serverSocket;
        private final CountDownLatch signal;

        AccecpTread(ServerSocket serverSocket, CountDownLatch signal) {
            this.serverSocket = serverSocket;
            this.signal = signal;
        }

        @Override
        public void run() {
            signal.countDown();
            try {
                acceptRequest(serverSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class RequestThread implements Runnable {
        private final Socket socket;

        public RequestThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                OutputStream out = socket.getOutputStream();
                System.out.println("向客户端发送一些字节");
                out.write("HTTP/1.1 200 OK\n\n".getBytes());
                out.write("Welcome to nginx;\n".getBytes());
                closeSocket(socket);
                System.out.println("当前客户端数量" + clientCount.decrementAndGet());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void closeSocket(Socket socket) throws IOException {
            if (!socket.isInputShutdown()) {
                socket.shutdownInput();
            }
            if (!socket.isOutputShutdown()) {
                socket.shutdownOutput();
            }
            if (!socket.isClosed()) {
                socket.close();
            }
        }
    }
}
