package com.example.ble.utils;

import android.os.Bundle;

import com.example.ble.application.App;

import java.util.ArrayList;
import java.util.List;

/**
 * 蓝牙数据处理类
 */
public class DataUtils {
    /**
     * 单例模式
     */
    static DataUtils instance;// 句柄

    private DataUtils() {
    }

    public static DataUtils getInstance() {
        if (instance == null)
            instance = new DataUtils();
        return instance;
    }

    /**
     * 数据
     */
    double currentSpeed = 0, preSpeed = 0;// 主动上报的速度
    int currentPower = 100, prePower = 100;// 主动上报电量
    public List<Double> arraySpeed = new ArrayList<Double>();// 速度数组

    public void setCurrentPower(int currentPower) {
        this.currentPower = currentPower;
        //发送更新电量广播
        if (Math.abs(this.currentPower - prePower) >= 0
                || this.currentPower == 0) {
            prePower = this.currentPower;
            Bundle bundle = new Bundle();
            bundle.putInt("power", this.currentPower);
            BroadcastUtils.getInstance().sendSystemBroadcast(ConstantUtils.ACTION_UPDATE_POWER, bundle);//更新电量广播

            if (this.currentPower - prePower > 6) {//电量突然增大
                BroadcastUtils.getInstance().sendSystemBroadcast(ConstantUtils.ACTION_CHARGE_POWER, bundle);//充电中
            }
        }
    }

    public void setCurrentSpeed(double currentSpeed) {
        currentSpeed = currentSpeed - 1;
        if (currentSpeed < 0) currentSpeed = 0;
        this.currentSpeed = currentSpeed > ConfigUtils.getInstance().getSpeedMax() ? ConfigUtils.getInstance().getSpeedMax() : currentSpeed;
        preSpeed = this.currentSpeed;

        //发送更新速度广播
        Bundle bundle = new Bundle();
        bundle.putDouble("speed", this.currentSpeed);
        BroadcastUtils.getInstance().sendSystemBroadcast(ConstantUtils.ACTION_UPDATE_SPEED, bundle);
        //保存速度数组
        //if (App.app.isStartMileage()) {
            addData(this.currentSpeed);
       // }
    }

    void addData(double speed) {
        arraySpeed.add(speed);
    }

    /**
     * 获取最大速度
     */
    public double getMaxSpeed() {
        if (arraySpeed.size() == 0)
            return 0;
        double speed = 0;
        for (int i = 0; i < arraySpeed.size(); i++) {
            double value = (double) arraySpeed.get(i);
            if (value >= speed) {
                speed = value;
            }
        }
        return speed > 20 ? 20.00 : speed;
    }

    /**
     * 获取电量值 100% 24V 90% 23.2V 80% 22.7V 70% 22.4V 60% 22.1V 50% 21.8V 40%
     * 21.6V 30% 21.5V 20% 21.3V 10% 21.0V 5% 19.7V 0% 17.0V
     */
    public int getPowerValueByVoltage(int voltageRealValue) {
        float voltageValue = (float) (voltageRealValue / 100.0);

        if (voltageValue >= 28.5) {
            return 100;
        } else if (voltageValue >= 28.15 && voltageValue < 28.40) {
            int zeng = (int) ((voltageValue - 28.15) * (10.0 / (28.40 - 28.15)));
            return 90 + zeng;
        } else if (voltageValue >= 27.80 && voltageValue < 28.15) {
            int zeng = (int) ((voltageValue - 27.80) * (10.0 / (28.15 - 27.80)));
            return 80 + zeng;
        } else if (voltageValue >= 27.45 && voltageValue < 27.80) {
            int zeng = (int) ((voltageValue - 27.45) * (10.0 / (27.80 - 27.45)));
            return 70 + zeng;
        } else if (voltageValue >= 27.1 && voltageValue < 27.45) {
            int zeng = (int) ((voltageValue - 27.1) * (10.0 / (27.45 - 27.1)));
            return 60 + zeng;
        } else if (voltageValue >= 26.75 && voltageValue < 27.1) {
            int zeng = (int) ((voltageValue - 26.75) * (10.0 / (27.1 - 26.75)));
            return 50 + zeng;
        } else if (voltageValue >= 26.40 && voltageValue < 26.75) {
            int zeng = (int) ((voltageValue - 26.40) * (10.0 / (26.75 - 26.40)));
            return 40 + zeng;
        } else if (voltageValue >= 26.05 && voltageValue < 26.40) {
            int zeng = (int) ((voltageValue - 26.05) * (10.0 / (26.40 - 26.05)));
            return 30 + zeng;
        } else if (voltageValue >= 25.70 && voltageValue < 26.05) {
            int zeng = (int) ((voltageValue - 25.70) * (10.0 / (26.05 - 25.70)));
            return 20 + zeng;
        } else if (voltageValue >= 25.35 && voltageValue < 25.70) {
            int zeng = (int) ((voltageValue - 25.35) * (10.0 / (25.70 - 25.35)));
            return 10 + zeng;
        } else if (voltageValue >= 25.0 && voltageValue < 25.35) {
            int zeng = (int) ((voltageValue - 25.0) * (10.0 / (25.35 - 25.0)));
            return 0 + zeng;
        } else {
            return 0;
        }
    }

}
