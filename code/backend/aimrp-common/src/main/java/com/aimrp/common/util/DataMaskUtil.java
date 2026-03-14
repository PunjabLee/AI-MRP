package com.aimrp.common.util;

/**
 * 数据脱敏工具类
 */
public class DataMaskUtil {
    
    /**
     * 全局脱敏开关（默认关闭）
     * 可通过配置中心动态调整
     */
    private static volatile boolean ENABLED = false;
    
    /**
     * 开启脱敏
     */
    public static void enable() {
        ENABLED = true;
    }
    
    /**
     * 关闭脱敏
     */
    public static void disable() {
        ENABLED = false;
    }
    
    /**
     * 获取当前开关状态
     */
    public static boolean isEnabled() {
        return ENABLED;
    }
    
    /**
     * 设置开关状态
     */
    public static void setEnabled(boolean enabled) {
        ENABLED = enabled;
    }
    
    /**
     * 手机号脱敏 138****5678
     */
    public static String maskMobile(String mobile) {
        if (!ENABLED || mobile == null || mobile.length() < 11) {
            return mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(7);
    }
    
    /**
     * 邮箱脱敏  a***@example.com
     */
    public static String maskEmail(String email) {
        if (!ENABLED || email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        if (parts[0].length() <= 2) {
            return parts[0] + "***@" + parts[1];
        }
        return parts[0].substring(0, 2) + "***@" + parts[1];
    }
    
    /**
     * 身份证号脱敏 3201**********1234
     */
    public static String maskIdCard(String idCard) {
        if (!ENABLED || idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 4);
    }
    
    /**
     * 银行卡脱敏 6222 **** **** 1234
     */
    public static String maskBankCard(String bankCard) {
        if (!ENABLED || bankCard == null || bankCard.length() < 8) {
            return bankCard;
        }
        return bankCard.substring(0, 4) + " **** **** " + bankCard.substring(bankCard.length() - 4);
    }
    
    /**
     * 姓名脱敏 张*
     */
    public static String maskName(String name) {
        if (!ENABLED || name == null || name.length() < 2) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "**";
    }
    
    /**
     * 地址脱敏
     */
    public static String maskAddress(String address) {
        if (!ENABLED || address == null || address.length() < 8) {
            return address;
        }
        return address.substring(0, 6) + "***";
    }
    
    /**
     * 通用脱敏
     */
    public static String mask(String data, int prefixLen, int suffixLen) {
        if (!ENABLED || data == null || data.length() <= prefixLen + suffixLen) {
            return data;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(data.substring(0, prefixLen));
        for (int i = 0; i < data.length() - prefixLen - suffixLen; i++) {
            sb.append("*");
        }
        sb.append(data.substring(data.length() - suffixLen));
        return sb.toString();
    }
    
    /**
     * 统一入口：根据字段类型自动脱敏
     * @param value 原始值
     * @param fieldType 字段类型：mobile, email, idCard, bankCard, name, address
     */
    public static String autoMask(String value, String fieldType) {
        if (!ENABLED || value == null) {
            return value;
        }
        
        switch (fieldType.toLowerCase()) {
            case "mobile":
                return maskMobile(value);
            case "email":
                return maskEmail(value);
            case "idcard":
                return maskIdCard(value);
            case "bankcard":
                return maskBankCard(value);
            case "name":
                return maskName(value);
            case "address":
                return maskAddress(value);
            default:
                return value;
        }
    }
}
