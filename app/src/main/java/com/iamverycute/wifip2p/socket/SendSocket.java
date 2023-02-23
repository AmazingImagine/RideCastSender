package com.iamverycute.wifip2p.socket;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.iamverycute.rtsp_android.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * date：2018/2/24 on 18:10
 * description: 客户端发送的socket
 */

public class SendSocket {
    public static final String TAG = "SendSocket";
    public static final int PORT = 12349;
    private String mAddress;
    Socket mSocket;
    InputStream mInputStream;
    OutputStream mOutStream;
    private static SendSocket instance;
    public static SendSocket getInstance(){
        return instance;
    }

    public void OnStreamReady(String strUrl){
        if(null != mOutStream){
            String strMsg = "StreamOk:"+strUrl+";";
            try {
                mOutStream.write(strMsg.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 10:
                    int progress = (int) msg.obj;
                    if (mlistener != null) {
                      //  mlistener.onProgressChanged(mFile, progress);
                    }
                    break;
                case 20:
                    if (mlistener != null) {
                       // mlistener.onFinished(mFile);
                    }
                    break;
                case 30:
                    if (mlistener != null) {
                       // mlistener.onFaliure(mFile);
                    }
                    break;
            }
        }
    };

    public SendSocket(String address, ProgressSendListener listener) {
        instance = this;
        mAddress = address;
        mlistener = listener;
    }

    public void createSendSocket() {
        try {
            mSocket = new Socket();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(mAddress, PORT);
            mSocket.connect(inetSocketAddress);
            mInputStream = mSocket.getInputStream();
            mOutStream = mSocket.getOutputStream();

            byte bytes[] = new byte[1024];
            int len;
            while ((len = mInputStream.read(bytes)) == -1) {
                Thread.sleep(1000);
            }

            String strMsg = new String(bytes,0,len);
            if(strMsg.contains("SinkOk")){
                MainActivity.getInstance().StartCast();
            }

            Log.e(TAG, "文件发送成功");
        } catch (Exception e) {
            Log.e(TAG,e.toString());
            Log.e(TAG, "文件发送异常");
        }
    }

    public void destorySendSocket() {
        try {
            mOutStream.close();
            mInputStream.close();
            mSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mHandler.sendEmptyMessage(20);
    }


    /**
     * 监听发送进度
     */
    private ProgressSendListener mlistener;

    public interface ProgressSendListener {

        //当传输进度发生变化时
        void onProgressChanged(File file, int progress);

        //当传输结束时
        void onFinished(File file);

        //传输失败时
        void onFaliure(File file);
    }
}

