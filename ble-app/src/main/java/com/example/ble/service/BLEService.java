package com.example.ble.service;

import android.app.Service;

import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import com.example.ble.controller.BluetoothController;
import com.example.ble.utils.ConfigUtils;
import com.example.ble.utils.LogUtils;

/**
 * 蓝牙服务
 */
public class BLEService extends Service {
    private static boolean isStart = true;
    private Handler mHandler;
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("MyHandlerThread");
        thread.start();//创建一个HandlerThread并启动它
        mHandler = new Handler(thread.getLooper());//使用HandlerThread的looper对象创建Handler，如果使用默认的构造方法，很有可能阻塞UI线程
    }
    public static void setStopService() {
        isStart = false;
    }

    public static void setStartService() {
        isStart = true;
    }

    //每次服务启动时回调
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.post(mBackgroundRunnable);//将线程post到Handler中
        return super.onStartCommand(intent, flags, startId);
    }

    //实现耗时操作的线程
    Runnable mBackgroundRunnable = new Runnable() {
        @Override
        public void run() {
            while (isStart && (!ConfigUtils.getInstance().getAddress().isEmpty())) {
                if (BluetoothController.getInstance().isConnected()) {//已经连接
                    return;
                }
                //未连接
                LogUtils.e("未连接:" + ConfigUtils.getInstance().getAddress() + ", 自动重连");
                BluetoothController.getInstance().disconnect();
                BluetoothController.getInstance().connect(
                        ConfigUtils.getInstance().getAddress(), ConfigUtils.getInstance().getName());
                try {
                    Thread.sleep(60000);//延迟1分钟重新请求
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        isStart = false;
        //销毁线程
        mHandler.removeCallbacks(mBackgroundRunnable);
    }
}