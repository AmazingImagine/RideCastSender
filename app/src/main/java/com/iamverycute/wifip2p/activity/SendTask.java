package com.iamverycute.wifip2p.activity;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.iamverycute.wifip2p.socket.SendSocket;

import java.io.File;

/**
 * date：2018/2/27 on 16:51
 * description: 客户端发送文件详情
 */
public class SendTask extends AsyncTask<String, Integer, Void> implements SendSocket.ProgressSendListener {

    private static final String TAG = "SendTask";
    private Context mContext;
    private SendSocket mSendSocket;

    public SendTask(Context ctx) {
        mContext = ctx;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(String... strings) {
        mSendSocket = new SendSocket(strings[0], this);
        mSendSocket.createSendSocket();
        return null;
    }


    @Override
    public void onProgressChanged(File file, int progress) {
        Log.e(TAG, "发送进度：" + progress);
    }

    @Override
    public void onFinished(File file) {
        Log.e(TAG, "发送完成");
        Toast.makeText(mContext, file.getName() + "发送完毕！", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFaliure(File file) {
        Log.e(TAG, "发送失败");
        Toast.makeText(mContext, "发送失败，请重试！", Toast.LENGTH_SHORT).show();
    }
}
