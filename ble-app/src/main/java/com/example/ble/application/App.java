package com.example.ble.application;

import android.app.Application;
import android.content.Context;

import com.example.ble.utils.ConfigUtils;

public class App extends Application {
    private static Context mContext;
    private static boolean mRegistered = false;//是否已经蓝牙注册成功
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        //读取配置
        ConfigUtils.getInstance().readConfig();
    }
    /**
     * @return 全局的上下文
     */
    public static Context getContext() {
        return mContext;
    }

    public static boolean getRegistered() {
        return mRegistered;
    }

    public static void setRegistered(boolean registered) {
        mRegistered = registered;
    }
}