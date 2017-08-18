package com.example.ble.common;

/**
 * 状态描述
 */
public enum IState {
    SCAN_PROCESS(0x01), //扫描中
    SCAN_SUCCESS(0x02), //扫描成功
    SCAN_TIMEOUT(0x03), //扫描超时
    CONNECT_PROCESS(0x04),  //连接中
    CONNECT_SUCCESS(0x05),  //连接成功
    CONNECT_FAILURE(0x06),  //连接失败
    CONNECT_TIMEOUT(0x07),  //连接超时
    DISCONNECT(0x08);   //连接断开

    private int code;

    IState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
