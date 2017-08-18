package com.example.ble.exception;

import com.example.ble.common.IBLEExceptionCode;

import java.io.Serializable;

/**
 * BLE异常基类
 */
public class IBLEException implements Serializable {
    private IBLEExceptionCode code;
    private String description;

    public IBLEException(IBLEExceptionCode code, String description) {
        this.code = code;
        this.description = description;
    }

    public IBLEExceptionCode getCode() {
        return code;
    }

    public IBLEException setCode(IBLEExceptionCode code) {
        this.code = code;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public IBLEException setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return "BleException{" +
                "code=" + code +
                ", description='" + description + '\'' +
                '}';
    }
}
