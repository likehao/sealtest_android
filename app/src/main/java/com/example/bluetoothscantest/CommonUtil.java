package com.example.bluetoothscantest;

import java.util.Calendar;
import java.util.Date;

public class CommonUtil{

    /**
     * 握手帧类型
     */
    public static final byte HANDSHAKE = (byte) 0xA0;
    /**
     * 启动
     */
    public static final byte START = (byte) 0xA1;
    /**
     * 通知印章上传盖章历史记录
     */
    public static final byte NOTIFYHISTORYUPLOAD = (byte) 0xA3;
    /**
     * 修改按键密码
     */
    public static final byte UPDATEKEPWD = (byte) 0xA4;
    /**
     * 重置
     */
    public static final byte RESET = (byte) 0xA5;
    /**
     *设置长按时间
     */
    public static final byte PRESSTIME = (byte) 0xA6;
    /**
     * 查询长按时间
     */
    public static final byte SELECTPRESSTIME = (byte) 0xA7;
    /**
     * 电量查询
     */
    public static final byte ElECTRIC = (byte) 0xAF;
    /**
     * 锁定
     */
    public static final byte LOCK = (byte) 0xA9;
    /**
     * 录制指纹
     */
    public static final byte RECORDING = (byte) 0xAA;
    /**
     * 删除指纹
     */
    public static final byte DELETE = (byte) 0xAB;
    /**
     * 设置指纹权限
     */
    public static final byte SET = (byte) 0xAC;
    /**
     * 查询指纹
     */
    public static final byte SELECT = (byte) 0xAD;
    /**
     * 盖章历史记录上传
     */
    public static final byte UPLOAD = (byte) 0xAE;

    /**
     * 获取时间字节数据（见协议定义）
     *
     * @param date
     * @return
     */
    public static byte[] getDateTime(Date date) {
        byte[] bytes = new byte[]{};
        if (date != null) {
            //TODO
            //获取时间年月日
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DATE);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);
//            byte[] yearBytes = DataTrans.shortToByteArray((short) year, true);
            int y = year % 2000;
            byte yearB = (byte) y;
            bytes = new byte[]{yearB, (byte) month, (byte) day, (byte) hour, (byte) minute, (byte) second};

        }
        return bytes;
    }

    public static byte[] getDateTime() {
        return getDateTime(new Date());
    }

    /**
     * 启动数据
     */
    public static byte[] getStartData() {
        int startInt = Integer.parseInt("80");
        byte[] time = DataTrans.intToByteArray(3, true);
        byte[] startYear = DataTrans.shortToByteArray((short) 2019, true);
        byte[] startByte = new byte[]{(byte) startInt,
                1, 2, 3, 4, 5, 6,
                0, 0, 0, 26, 27, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 38,
                time[0], time[1], time[2], time[3],
                startYear[0], startYear[1], 9, 9, 19, 19, 19};
        return startByte;
    }

    /**
     * 启动
     */
    public static byte[] startData(){
        byte[] time = DataTrans.intToByteArray(5,true);  //可盖章次数
        int year = 2019 % 2000;
        byte failYear = (byte) year;
        byte[] startByte = new byte[]{ time[0], time[1],time[2],time[3],failYear,9, 9, 19, 19, 19};
        return startByte;

    }
    /**
     * 指纹权限
     * @return
     */
    public static byte[] setFingerprint() {
        long setLong = DataTrans.parseLong("EB");
        int setInt = DataTrans.integer("25");
        byte[] setBytes = DataTrans.intToByteArray(3, true);
        byte[] failYear = DataTrans.shortToByteArray((short) 2019, true);
        byte[] setFingerprint = new byte[]{(byte) setLong, (byte) setInt,
                setBytes[0], setBytes[1], setBytes[2], setBytes[3],
                failYear[0], failYear[1], 9, 9, 19, 19, 19};
        return setFingerprint;
    }

}
