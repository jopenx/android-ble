package com.example.ble.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.example.ble.application.App;
import com.example.ble.common.IState;
import com.example.ble.utils.BroadcastUtils;
import com.example.ble.utils.ConstantUtils;
import com.example.ble.utils.ConvertUtils;
import com.example.ble.utils.InstructionsUtils;
import com.example.ble.utils.LogUtils;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * 蓝牙控制器，在进行蓝牙通信时通信双方分为外围和中央，外围用于提供数据，中央用于接收和处理数据
 */
public class BluetoothController {
    /**
     * 单例模式(静态内部类的方式)
     */
    private BluetoothController() {
    }
    public static BluetoothController getInstance() {
        return SingletonHolder.instance;
    }
    private static class SingletonHolder {
        private static final BluetoothController instance = new BluetoothController();
    }

    private Context context;//上下文
    private BluetoothAdapter bluetoothAdapter;//蓝牙适配器
    private BluetoothGatt bluetoothGatt;//蓝牙GATT
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;//GATT特征值
    private IState state = IState.DISCONNECT;//设备状态描述
    private String deviceAddress;//当前蓝牙地址
    private String deviceName;//蓝牙名
    private String autoReportData = "";//主动上报数据
    private int autoReportCnt = 0;//主动上报次数

    private static String arrayOfString= "";//接收到蓝牙发送过来的byte[]数据

    private Handler serviceHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                //接收蓝牙主动上报消息
                case ConstantUtils.WM_RECEIVE_REPORT_MSG_FROM_BLE:
                    if (msg.obj == null) return;
                    InstructionsUtils.getInstance().decodeAutoReportInstructions(msg.obj.toString());
                    break;
                case ConstantUtils.WM_RECEIVE_REGISTOR_MSG_FROM_BLE:
                    if (msg.obj == null) return;
                    boolean b = InstructionsUtils.getInstance().decodeRegisterInstructionsBack(msg.obj.toString());
                    LogUtils.e("注册指令回码: " + b);
                    if (b) {
                        App.setRegistered(b);
                    } else {
                        LogUtils.e("发送注册指令:"+ InstructionsUtils.getInstance().getRegisterInstructions());
                        //发送注册指令
                        BluetoothController.getInstance().write(ConvertUtils.getInstance().hexStringToBytes(InstructionsUtils.getInstance().getRegisterInstructions()));
                    }
                default:
                    break;
            }
        }
    };

    /**
     * 蓝牙所有相关操作的核心回调类
     */
    private BluetoothGattCallback coreGattCallback = new BluetoothGattCallback() {
        /**
         * 连接状态改变，主要用来分析设备的连接与断开
         * @param gatt GATT
         * @param status 改变前状态
         * @param newState 改变后状态
         */
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {//连接
                gatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {//断开连接
                state = IState.DISCONNECT;
                close();
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        Bundle bundle = new Bundle();
                        if (status == 0) {
                            //手动断开连接
                            bundle.putString("address", "");
                            bundle.putString("name", "");
                            BroadcastUtils.getInstance().sendSystemBroadcast(
                                    ConstantUtils.ACTION_CONECTED_STATE_CHANGE_DISCONNECT, bundle);
                        } else {
                            //连接失败或者异常断开
                            bundle.putString("address", deviceAddress);
                            bundle.putString("name", deviceName);
                            BroadcastUtils.getInstance().sendSystemBroadcast(
                                    ConstantUtils.ACTION_CONECTED_STATE_CHANGE_FAILURE, bundle);
                        }
                    }
                });
            } else if (newState == BluetoothGatt.STATE_CONNECTING) {//连接中
                state = IState.CONNECT_PROCESS;//连接中
            }
        }
        /**
         * 发现服务，主要用来获取设备支持的服务列表
         * @param gatt GATT
         * @param status 当前状态
         */
        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            if (status == 0) {
                bluetoothGatt = gatt;
                state = IState.CONNECT_SUCCESS;
                findService(gatt.getServices());
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        Bundle bundle = new Bundle();
                        bundle.putString("address", deviceAddress);
                        bundle.putString("name", deviceName);
                        //连接成功
                        BroadcastUtils.getInstance().sendSystemBroadcast(
                                ConstantUtils.ACTION_CONECTED_STATE_CHANGE_SUCCESS, bundle);
                    }
                });
            } else {
                state = IState.CONNECT_FAILURE;
                close();
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        Bundle bundle = new Bundle();
                        bundle.putString("address", deviceAddress);
                        bundle.putString("name", deviceName);
                        //连接失败或者异常断开
                        BroadcastUtils.getInstance().sendSystemBroadcast(
                                ConstantUtils.ACTION_CONECTED_STATE_CHANGE_FAILURE, bundle);
                    }
                });
            }
        }
        /**
         * 特征值改变，主要用来接收设备返回的数据信息（依照 实际的 蓝牙自定义协议 进行数据处理）
         * BLE传过来的数据存在一次发送大小 Android BLE中传输数据的最大长度
         * @param gatt GATT
         * @param characteristic 特征值
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] arrayOfByte = characteristic.getValue();
            state = IState.CONNECT_SUCCESS;

            if ((arrayOfByte != null) && (arrayOfByte.length > 2)) {
                String newArrayOfString = ConvertUtils.getInstance().bytesToHexString(arrayOfByte);
                if (newArrayOfString.equals(arrayOfString)){
                    return;
                }
                arrayOfString = newArrayOfString;
                LogUtils.i("接收到的数据:" + arrayOfString);

                //注册指令主板回码
                if ((arrayOfByte[0] == ConstantUtils.INSTRUCTIONS_APP_START//指令帧头
                        && arrayOfByte[1] == ConstantUtils.INSTRUCTIONS_BLE_REGISTER_BACK_ZHANGLIN)//注册指令主板回码
                        && autoReportCnt == 0) {//注册回码
                    LogUtils.i("注册指令主板回码:" + arrayOfString);
                    if (serviceHandler != null) {
                        Message msg = new Message();
                        msg.what = ConstantUtils.WM_RECEIVE_REGISTOR_MSG_FROM_BLE;//接收蓝牙注册回码消息
                        msg.obj = arrayOfString.substring(0, 32);//byte数组转十六进制字符串
                        serviceHandler.sendMessage(msg);
                    }
                    //主板主动上报指令
                } else if ((arrayOfByte[0] == ConstantUtils.INSTRUCTIONS_APP_START//指令帧头
                        && arrayOfByte[1] == ConstantUtils.INSTRUCTIONS_BLE_AUTO_REPORT)//主板主动上报指令
                        || autoReportCnt > 0) {//主动上报
                    autoReportCnt++;
                    if (autoReportCnt <= 3) {
                        String temp = arrayOfString;
                        if (autoReportCnt == 3)
                            temp = temp.substring(0, 18);
                        autoReportData = autoReportData + temp;
                    }
                    if (serviceHandler != null && autoReportCnt == 3) {
                        int length = autoReportData.length();
                        if (length != 98) {
                            autoReportCnt = 0;
                            autoReportData = "";
                            return;
                        }
                        String resultValue = autoReportData.substring(96, 98);
                        if (!resultValue.equalsIgnoreCase("16")) {
                            autoReportCnt = 0;
                            autoReportData = "";
                            return;
                        }
                        Message msg = new Message();
                        msg.what = ConstantUtils.WM_RECEIVE_REPORT_MSG_FROM_BLE;//接收蓝牙主动上报消息
                        msg.obj = autoReportData;
                        serviceHandler.sendMessage(msg);
                        autoReportCnt = 0;
                        autoReportData = "";
                    }
                }
            }
        }
        /**
         * 读取特征值，主要用来读取该特征值包含的可读信息
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == 0) {
                LogUtils.i("onCharacteristicChanged received: ok");
                return;
            }
            LogUtils.i("onCharacteristicRead received: " + status);
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor characteristic, int status) {
            LogUtils.i("onDescriptorRead received: ok");
        }
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor characteristic, int status) {
            LogUtils.i("onDescriptorRead received: ok");
        }
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int paramAnonymousInt1, int status) {
            LogUtils.i("onReadRemoteRssi received: ok");
        }
        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            LogUtils.i("onReliableWriteCompleted received: ok");
        }
    };

    /**
     * 自定义处理接收到的数据
     * @param arrayOfByte
     */
    private void processData(byte[] arrayOfByte){

    }

    /**
     * 初始化蓝牙
     */
    public boolean initBLE(Context context) {
        if (this.context == null) {
            this.context = context.getApplicationContext();
            //检查当前手机是否支持BLE蓝牙,如果不支持返回false
            if (!this.context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                return false;
            }
            //初始化蓝牙适配器, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
            bluetoothAdapter = ((BluetoothManager)this.context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
            //检查设备上是否支持蓝牙
            if (bluetoothAdapter == null) return false;
            else return true;
        }
        return false;
    }

    /**
     * 搜索服务
     * @param paramList
     */
    public void findService(List<BluetoothGattService> paramList) {
        Iterator localIterator1 = paramList.iterator();
        while (localIterator1.hasNext()) {
            BluetoothGattService localBluetoothGattService = (BluetoothGattService) localIterator1.next();
            if (localBluetoothGattService.getUuid().toString().equalsIgnoreCase(ConstantUtils.UUID_SERVER)) {
                List localList = localBluetoothGattService.getCharacteristics();
                Iterator localIterator2 = localList.iterator();
                while (localIterator2.hasNext()) {
                    BluetoothGattCharacteristic localBluetoothGattCharacteristic = (BluetoothGattCharacteristic) localIterator2.next();
                    if (localBluetoothGattCharacteristic.getUuid().toString().equalsIgnoreCase(ConstantUtils.UUID_NOTIFY)) {
                        bluetoothGattCharacteristic = localBluetoothGattCharacteristic;
                        break;
                    }
                }
                break;
            }
        }
        bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);
    }

    /**
     * 开始扫描
     * @param leScanCallback
     */
    public void startLeScan(BluetoothAdapter.LeScanCallback leScanCallback) {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.startLeScan(leScanCallback);
            state = IState.SCAN_PROCESS;
        }
    }
    /**
     * 停止扫描
     * @param leScanCallback
     */
    public void stopLeScan(BluetoothAdapter.LeScanCallback leScanCallback) {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }
    /**
     * 连接设备
     * @param deviceAddress 设备信息
     */
    public synchronized void connect(String deviceAddress, String deviceName) {
        this.deviceAddress = deviceAddress;
        this.deviceName = deviceName;
        if (deviceAddress != null && deviceName != null) {
            state = IState.CONNECT_PROCESS;
            if ((bluetoothAdapter.getRemoteDevice(deviceAddress) != null) && (bluetoothAdapter != null)) {
                bluetoothAdapter.getRemoteDevice(deviceAddress).connectGatt(this.context, false, coreGattCallback);
            }
        }
    }

    /**
     * 蓝牙是否打开
     */
    public boolean isBleOpen() {
        return bluetoothAdapter.isEnabled();
    }

    /**
     * 静默打开,不做任何提示
     */
    public boolean openBleSwitchSilent() {
        return bluetoothAdapter.enable();
    }

    /**
     * 传输数据
     * @param byteArray
     */
    public boolean write(byte byteArray[]) {
        if (bluetoothGattCharacteristic == null) return false;
        if (bluetoothGatt == null) return false;
        bluetoothGattCharacteristic.setValue(byteArray);
        return bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
    }

    /**
     * 是否是主线程
     */
    private boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    private Handler handler = new Handler(Looper.getMainLooper());
    /**
     * 切换到主线程
     * @param runnable
     */
    private void runOnMainThread(Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            if (handler != null) {
                handler.post(runnable);
            }
        }
    }
    /**
     * 设备是否连接
     * @return 返回设备是否连接
     */
    public boolean isConnected() {
        if (state == IState.CONNECT_SUCCESS) {
            return true;
        } else {
            return false;
        }
    }
    /**
     * 主动断开设备连接
     */
    public synchronized void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
    }
    /**
     * 刷新设备缓存
     * @return 返回是否刷新成功
     */
    private synchronized boolean refreshDeviceCache() {
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null && bluetoothGatt != null) {
                final boolean success = (Boolean) refresh.invoke(bluetoothGatt);
                LogUtils.i("Refreshing result: " + success);
                return success;
            }
        } catch (Exception e) {
            LogUtils.e("An exception occured while refreshing device" + e);
        }
        return false;
    }
    /**
     * 关闭GATT
     */
    public synchronized void close() {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
    }
    /**
     * 清除设备的相关信息，一般是在不使用该设备时调用
     */
    public synchronized void clear() {
        disconnect();
        refreshDeviceCache();
        close();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}