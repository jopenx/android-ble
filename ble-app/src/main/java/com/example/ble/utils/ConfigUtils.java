package com.example.ble.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;

import com.example.ble.application.App;

/**
 * 本地保存的配置信息
 */
public class ConfigUtils {
    /**
     * 单例模式(静态内部类的方式)
     */
    private ConfigUtils() {
    }
    public static ConfigUtils getInstance() {
        return SingletonHolder.instance;
    }
    private static class SingletonHolder {
        private static final ConfigUtils instance = new ConfigUtils();
    }

    /**
     * 配置信息
     */
    private SharedPreferences sp;
    private String deviceAddress = "";//最后一次连接成功的蓝牙地址
    private String deviceName = "";//最后一次连接成功的蓝牙名
    private int clr;//灯光颜色
    private int speedMax;//最大速度

    public String getAddress() {
        return deviceAddress;
    }

    /**
     * 建造者模式
     */
    public ConfigUtils setAddress(String address) {
        this.deviceAddress = address;
        return this;
    }

    public String getName() {
        return deviceName;
    }

    public ConfigUtils setName(String name) {
        this.deviceName = name;
        return this;
    }

    public int getClr() {
        return clr;
    }

    public ConfigUtils setClr(int clr) {
        this.clr = clr;
        return this;
    }

    public int getSpeedMax() {
        return speedMax;
    }

    public ConfigUtils setSpeedMax(int speedMax) {
        this.speedMax = speedMax;
        return this;
    }
    /**
     * 读取配置文件
     */
    public void readConfig() {
        sp = App.getContext().getSharedPreferences("app_config", Context.MODE_PRIVATE);
        deviceAddress = sp.getString("address", "");
        deviceName = sp.getString("name", "");
        clr = sp.getInt("color", Color.RED);
        speedMax = sp.getInt("speed", 6);
    }
    /**
     * 保存配置文件
     */
    public void saveConfig() {
        sp = App.getContext().getSharedPreferences("app_config", Context.MODE_PRIVATE);
        Editor ed = sp.edit();
        ed.putString("address", deviceAddress);//蓝牙地址
        ed.putString("name", deviceName);//蓝牙地址
        ed.putInt("color", clr);//颜色
        ed.putInt("speed", speedMax);
        ed.apply();
    }

    @Override
    public String toString() {
        return "ConfigUtils{" +
                "sp=" + sp +
                ", deviceAddress='" + deviceAddress + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", clr=" + clr +
                ", speedMax=" + speedMax +
                '}';
    }
}