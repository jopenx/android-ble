# Android BLE

Android蓝牙控制器(智能硬件)，包含监控电池电量、设备速度、设备灯光等智能硬件交互场景，交互数据格式为hex码（16进制的ASCII码）。BLE模块我们选用了CC2541低功率蓝牙模块4.0 BLE 串口 带底板插针 无线蓝牙模块。

![](https://raw.githubusercontent.com/smartbetter/android-ble/master/website/static/cc2541.jpg)

# Explain

手机手动连接到蓝牙设备后，设备就会主动上报速度，电量等数据；手机主动注册蓝牙后，主动上报设置指令才能生效；主板每次断电，手机需要重新去发送注册指令。

BluetoothController: 蓝牙核心控制器模块。
BLEService: 蓝牙服务，开启服务的目的是实现打开app自动连接上一次保存的蓝牙芯片。

# Sample usage

A sample project which provides runnable code examples that demonstrate uses of the classes in this project is available in the ble-app/ folder.

# Preview

![](https://raw.githubusercontent.com/smartbetter/android-ble/master/website/static/screenshot.png)
