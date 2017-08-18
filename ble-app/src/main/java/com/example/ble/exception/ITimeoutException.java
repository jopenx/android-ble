package com.example.ble.exception;

import com.example.ble.common.IBLEExceptionCode;

/**
 * 超时异常
 */
public class ITimeoutException extends IBLEException {
    public ITimeoutException() {
        super(IBLEExceptionCode.TIMEOUT, "Timeout Exception Occurred! ");
    }
}
