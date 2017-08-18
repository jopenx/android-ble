package com.example.ble.utils;

import android.graphics.Color;

public class InstructionsUtils {
    String speedInstructions = "6807000d00000000010100010100000000000016";
    String lightInstructions = "68090004ff000001000016";
    static InstructionsUtils instance;

    private InstructionsUtils() {
    }

    /**
     * 获取句柄
     *
     * @return InstructionsUtils对象
     */
    public static InstructionsUtils getInstance() {
        if (instance == null) instance = new InstructionsUtils();
        return instance;
    }

    /**
     * 获取注册指令(没有使用)
     *
     * @return 注册指令
     */
    public String getRegisterInstructions() {
        return replaceValidateCode("680100080100000000000000000016");
    }

    /**
     * 获取最大速度指令
     *
     * @return 最大速度指令
     */
    public String getMaxSpeedInstructions() {
        return replaceValidateCode(speedInstructions);
    }

    /**
     * 设置最大速度指令
     *
     * @param speedStyle
     */
    public void setMaxSpeedInstructions(int speedStyle) {
        if (speedStyle < 0 || speedStyle >= 4) return;

        StringBuilder sb = new StringBuilder();
        String hs = Integer.toHexString(speedStyle);
        for (int i = 0; i < (2 - hs.length()); i++)
            sb.append(0);
        sb.append(hs);

        StringBuilder sb2 = new StringBuilder(speedInstructions);
        sb2.replace(24, 26, sb.toString());
        speedInstructions = sb2.toString();
    }

    /**
     * 获取最大速度为20的指令
     *
     * @return
     */
    public String getMaxSpeed20() {
        return replaceValidateCode("6807000d00000000010100010400000014000016");
    }

    /**
     * 获取灯光指令
     *
     * @return
     */
    public String getLightInstructions() {
        return replaceValidateCode(lightInstructions);
    }

    /**
     * 设置灯光指令
     *
     * @int clr 灯光
     */
    public void setLightInstructions(int clr) {
        StringBuilder sb2 = new StringBuilder(lightInstructions);
        int r = Color.red(clr);
        int g = Color.green(clr);
        int b = Color.blue(clr);
        int c[] = {r, g, b};

        for (int i = 0; i < 3; i++) {
            StringBuilder sb = new StringBuilder();
            String hs = Integer.toHexString(c[i]);
            for (int j = 0; j < (2 - hs.length()); j++)
                sb.append(0);
            sb.append(hs);
            sb2.replace(8 + 2 * i, 10 + 2 * i, sb.toString());
        }
        lightInstructions = sb2.toString();

    }

    /**
     * 解码注册指令回码
     *
     * @param str
     * @return
     */

    public boolean DecodeRegisterInstructionsBack(String str) {
        if (str == null || str.equals("") || !validateInstruction(str)) {
            return false;
        }
        String instructionsStyle = str.substring(2, 4);
        if (Integer.valueOf(instructionsStyle) == ConstantUtils.INSTRUCTIONS_BLE_REGISTER_BACK_ZHANGLIN) {
            return true;
        }
        return false;
    }

    /**
     * 解码主动上报指令
     *
     * @param str 主动上报指令
     */
    public void DecodeAutoReportInstructions(String str) {
        if (str == null || str.equals("") || !validateInstruction(str)) {
            return;
        }
        String instructionsStyle = str.substring(2, 4);
        if (Integer.valueOf(instructionsStyle) == ConstantUtils.INSTRUCTIONS_BLE_AUTO_REPORT) {
            DataUtils.getInstance().setCurrentSpeed(
                    (float) ((float) ConvertUtils.getInstance().byteToInt(
                            ConvertUtils.getInstance().hexStringToBytes(str.substring(44, 52))) * 0.0132));

            DataUtils.getInstance().setCurrentPower(
                    DataUtils.getInstance().getPowerValueByVoltage(
                    ConvertUtils.getInstance().byteToInt(
                            ConvertUtils.getInstance().hexStringToBytes(str.substring(64, 72)))));
        }
    }

    /**
     * 替换校验码
     *
     * @param str 校验前的指令
     * @return 修改校验码后的指令
     */
    public String replaceValidateCode(String str) {
        if (str == null || str.equals("")) {
            return "";
        }
        str = str.toUpperCase();
        int length = str.length() / 2;
        char[] hexChars = str.toCharArray();
        short validate = 0;
        for (int i = 1; i < length - 3; i++) {
            int pos = i * 2;
            byte b = (byte) (ConvertUtils.getInstance().charToByte(hexChars[pos]) << 4 | ConvertUtils.getInstance().charToByte(hexChars[pos + 1]));
            validate = (short) (validate + (b < 0 ? b + 256 : b));
        }
        StringBuilder sb = new StringBuilder();
        String hs = Integer.toHexString(validate);
        for (int i = 0; i < (4 - hs.length()); i++)
            sb.append(0);
        sb.append(hs);

        StringBuilder sb2 = new StringBuilder(str);
        sb2.replace(sb2.length() - 6, sb2.length() - 2, sb.toString());
        return sb2.toString();
    }

    /**
     * 校验校验码
     */
    public boolean validateInstruction(String str) {
        if (str == null || str.equals("")) {
            return false;
        }
        String objectStr = str.substring(0, str.length() - 6);
        String currentCode = str.substring(str.length() - 6, str.length() - 2);


        objectStr = objectStr.toUpperCase();
        int length = objectStr.length() / 2;
        char[] hexChars = objectStr.toCharArray();
        short validate = 0;
        for (int i = 1; i < length - 3; i++) {
            int pos = i * 2;
            byte b = (byte) (ConvertUtils.getInstance().charToByte(hexChars[pos]) << 4 | ConvertUtils.getInstance().charToByte(hexChars[pos + 1]));
            validate = (short) (validate + (b < 0 ? b + 256 : b));
        }
        StringBuilder sb = new StringBuilder();
        String hs = Integer.toHexString(validate);
        for (int i = 0; i < (4 - hs.length()); i++)
            sb.append(0);
        sb.append(hs);

        String caluateCode = sb.toString();
        return caluateCode.equalsIgnoreCase(currentCode);
    }

}
