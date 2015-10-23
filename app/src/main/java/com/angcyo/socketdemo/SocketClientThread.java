package com.angcyo.socketdemo;

import android.text.TextUtils;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by angcyo on 15-10-20-020.
 */
public class SocketClientThread extends Thread {
    public volatile static String MSG;
    public volatile static boolean haveMsg = false;
    public volatile static boolean isConnected = false;
    int TIME_OUT = 8 * 1000;
    int READ_TIME = 3 * 1000;
    private Socket mSocket;
    private InputStream mDataRead;
    private OutputStream mDataWrite;
    private String mSvrIp;
    private int mSvrPort = 8300;
    private volatile boolean isIpChange = false;
    private volatile boolean isExit = false;

    public SocketClientThread(String mSvrIp) {
        this.mSvrIp = mSvrIp;
    }

    @Override
    public void run() {
        while (!isExit) {
            if (TextUtils.isEmpty(mSvrIp)) {
                disconnectSocket();
                continue;
            }

            if (isIpChange) {
                disconnectSocket();
            }

            if (mSocket == null) {
                try {
                    connectSocket(mSvrIp, mSvrPort);
                    isIpChange = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    mSocket = null;
                    isConnected = false;
                }
            } else if (mSocket.isClosed() || !mSocket.isConnected()) {
                disconnectSocket();
                continue;
            }

            if (mSocket != null && mSocket.isConnected()) {
                try {
                    read();
                } catch (Exception e) {
                    e.printStackTrace();
//                    disconnectSocket();
//                    break;
                }
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.e("SocketClientThread", "退出");
        disconnectSocket();
    }

    /**
     * 连接socket
     */
    public synchronized void connectSocket(String ip, int port) throws IOException {
//        if (mSocket == null) {
        mSocket = new Socket();
        mSocket.setSoTimeout(READ_TIME);
//        mSocket.setSoLinger(READ_TIME);
//        mSocket.setKeepAlive(false);
//        mSocket.setTcpNoDelay(true);
//        }
        SocketAddress socketAddress = new InetSocketAddress(ip, port);
        try {
            mSocket.connect(socketAddress, TIME_OUT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mSocket != null && !mSocket.isClosed() && mSocket.isConnected()) {// mSocket.isConnected()
            mDataRead = mSocket.getInputStream();
            mDataWrite = mSocket.getOutputStream();
            isConnected = true;
        } else {
            isConnected = false;
        }
    }

    /**
     * 断开socket
     */
    public synchronized void disconnectSocket() {
        try {
            if (mSocket != null) {
                if (!mSocket.isInputShutdown()) {
                    mSocket.shutdownInput();
                }
                if (!mSocket.isOutputShutdown()) {
                    mSocket.shutdownOutput();
                }

                if (mDataRead != null) {
                    mDataRead.close();
                }
                if (mDataWrite != null) {
                    mDataWrite.close();
                }
                mSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mDataRead = null;
            mDataWrite = null;
            mSocket = null;
        }
        isConnected = false;
    }

    /**
     * 写入数据
     */
    private synchronized void write(byte[] data) throws IOException {
        if (mDataWrite != null) {
            mDataWrite.write(data);
            mDataWrite.flush();
        }
    }

    /**
     * 读取数据
     */
    private synchronized void read() throws Exception {
        if (mDataRead != null) {
//            String line = null;
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(mDataRead));
//
//            try {
//                line = bufferedReader.readLine();//阻塞方法
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            if (!TextUtils.isEmpty(line)) {
//                postMsg(line);//读取到数据
//                haveMsg = true;
//            } else {
//                haveMsg = false;
//            }

            int len;
            byte[] bytes = new byte[1024];
            DataInputStream dataInputStream = new DataInputStream(mDataRead);
            if ((  len = dataInputStream.read(bytes)) > -1) {
                byte[] temp = new byte[len];
                System.arraycopy(bytes, 0, temp, 0, len);
                postMsg(new String(temp));//读取到数据
                haveMsg = true;
            } else {
//                disconnectSocket();
                haveMsg = false;
            }
        }
    }

    public void postMsg(byte[] bytes) {
        MSG = new String(bytes);
    }

    public void postMsg(String msg) {
        MSG = msg.trim();
    }

    public void setSvrIp(String ip, int port) {
        if (TextUtils.isEmpty(mSvrIp) || !mSvrIp.equalsIgnoreCase(ip) || port != mSvrPort) {
            mSvrIp = ip;
            mSvrPort = port;
            isIpChange = true;
        }
    }

    public void exit() {
        isExit = true;
//        try {
//            mDataRead.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
