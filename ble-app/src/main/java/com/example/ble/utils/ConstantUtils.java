package com.example.ble.utils;

public class ConstantUtils {

    //更新电量广播
    public final static String ACTION_UPDATE_POWER = "android.intent.action.update_power";
    //电量变化大，充电中
    public final static String ACTION_CHARGE_POWER = "android.intent.action.charge_power";
    //更新速度广播
    public final static String ACTION_UPDATE_SPEED = "android.intent.action.update_speed";
    //蓝牙连接状态改变广播
    public final static String ACTION_CONECTED_STATE_CHANGE_SUCCESS = "android.intent.action.connect_change_success";
    public final static String ACTION_CONECTED_STATE_CHANGE_FAILURE = "android.intent.action.connect_change_failure";
    public final static String ACTION_CONECTED_STATE_CHANGE_DISCONNECT = "android.intent.action.connect_change_disconnect";

    //指令帧头
    public final static int INSTRUCTIONS_APP_START = 104;
    //app注册指令
    public final static int INSTRUCTIONS_APP_REGISTER = 1;
    //注册指令主板回码
    public final static int INSTRUCTIONS_BLE_REGISTER_BACK_ZHANGLIN = 1;
    //主板主动上报指令
    public final static int INSTRUCTIONS_BLE_AUTO_REPORT = 5;
    //主板报警指令
    public final static int INSTRUCTIONS_BLE_WARNING = 3;
    //app报警指令回码
    public final static int INSTRUCTIONS_APP_WARNING_BACK = 3;
    //app 最大速度设置指令
    public final static int INSTRUCTIONS_APP_MAXSPEED_SETTING = 7;
    //app 灯光设置指令
    public final static int INSTRUCTIONS_APP_LIGHT_SETTING = 9;

    //接收蓝牙注册回码消息
    public final static int WM_RECEIVE_REGISTOR_MSG_FROM_BLE = 4;
    //接收蓝牙主动上报消息
    public final static int WM_RECEIVE_REPORT_MSG_FROM_BLE = 5;

    //UUID
    public final static String UUID_SERVER = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public final static String UUID_NOTIFY = "0000ffe1-0000-1000-8000-00805f9b34fb";
}
