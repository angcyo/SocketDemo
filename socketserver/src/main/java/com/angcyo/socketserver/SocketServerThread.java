package com.angcyo.socketserver;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by angcyo on 15-10-20-020.
 */
public class SocketServerThread extends Thread {
    public volatile static String WRITE_MSG;
    public volatile static String clientIp;
    ServerSocket mServerSocket;
    Socket mClickSocket;
    private volatile boolean isExit = false;
    private int port = 8300;
    int TIME_OUT = 8 * 1000;

    @Override
    public void run() {
        while (!isExit) {
            if (MainActivity.PORT != port) {
                reset();
                port = MainActivity.PORT;
            }

            if (mServerSocket == null) {
                try {
                    mServerSocket = new ServerSocket(port);
                } catch (Exception e) {
                    e.printStackTrace();
                    mServerSocket = null;
                    continue;
                }
            }
            if (mClickSocket == null) {
                try {
                    mServerSocket.setSoTimeout(TIME_OUT);
                    mClickSocket = mServerSocket.accept();
                    clientIp = mClickSocket.getInetAddress().toString();
                } catch (IOException e) {
                    e.printStackTrace();
                    mClickSocket = null;
                    clientIp = null;
                    continue;
                }
            } else if (mClickSocket.isClosed() || !mClickSocket.isConnected()) {
                reset();
                mClickSocket = null;
                clientIp = null;
                continue;
            }

            if (!TextUtils.isEmpty(WRITE_MSG)) {
                if (mClickSocket != null) {
                    try {
                        String msg = WRITE_MSG;
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(mClickSocket.getOutputStream()));
                        bufferedWriter.write(msg);
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                        WRITE_MSG = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                        mClickSocket = null;
                        clientIp = null;
                    }
                }
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.e("SocketServerThread", "退出");
        reset();
    }

    private void reset() {
        if (mClickSocket != null) {
            try {
                mClickSocket.getOutputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mClickSocket.getInputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mClickSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mClickSocket = null;
        }
        if (mServerSocket != null) {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mServerSocket = null;
        }
        clientIp = null;
    }

    public synchronized void exit() {
        isExit = true;
    }
}
