package com.example.ble.exception;

import android.bluetooth.BluetoothGatt;

import com.example.ble.common.IBLEExceptionCode;

/**
 * 连接异常
 */
public class IConnectException extends IBLEException {
    private BluetoothGatt bluetoothGatt;
    private int gattStatus;

    public IConnectException(BluetoothGatt bluetoothGatt, int gattStatus) {
        super(IBLEExceptionCode.CONNECT_ERR, "Connect Exception Occurred! ");
        this.bluetoothGatt = bluetoothGatt;
        this.gattStatus = gattStatus;
    }

    public int getGattStatus() {
        return gattStatus;
    }

    public IConnectException setGattStatus(int gattStatus) {
        this.gattStatus = gattStatus;
        return this;
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    public IConnectException setBluetoothGatt(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
        return this;
    }

    @Override
    public String toString() {
        return "ConnectException{" +
                "gattStatus=" + gattStatus +
                ", bluetoothGatt=" + bluetoothGatt +
                "} " + super.toString();
    }
}
