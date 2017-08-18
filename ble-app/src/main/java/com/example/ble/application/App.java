package com.example.ble.application;

import android.app.Application;
import android.content.Context;

import com.example.ble.utils.ConfigUtils;

public class App extends Application {
    private static Context mContext;
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
}