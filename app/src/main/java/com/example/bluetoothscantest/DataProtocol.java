package com.example.bluetoothscantest;

import java.util.ArrayList;
import java.util.List;

public class DataProtocol {
    public DataProtocol(byte type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    /**
     * 帧头
     */
    private byte frameHead;
    /**
     * 长度
     */
    private byte len;
    /**
     * 类型
     */
    private byte type;
    /**
     * 数据域
     */
    private byte[] data;
    /**
     * 校验和
     */
    private byte cs;

    public byte getFrameHead() {
        return (byte) 0xFF;
    }

    private void setFrameHead(byte frameHead) {
        this.frameHead = frameHead;
    }

    public byte getLen() {
        return len;
    }

    private void setLen(byte len) {
        this.len = len;
    }

    public byte getType() {
        return type;
    }

    private void setType(byte type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    private void setData(byte[] data) {
        this.data = data;
    }

    public byte getCs() {
        return cs;
    }

    private void setCs(byte cs) {
        this.cs = cs;
    }

    /**
     * 获取协议数据帧
     *
     * @return
     */
    public byte[] getBytes() {
        byte[] bytes = null;
        if (this.data != null && this.data.length > 0) {
            //组装数据
            List<Byte> byteList = new ArrayList<Byte>();
            //帧头
            byteList.add(this.getFrameHead());
            //长度
            byteList.add((byte) this.data.length);
            //类型
            byteList.add(this.type);
            //数据域
            int cs = 0;
            for (int i = 0; i < this.data.length; i++) {
                byteList.add(this.data[i]);
            }

            //拿到完整的数据包循环赋给byte1
            byte[] bytes1 = new byte[byteList.size()];
            for (int i = 0; i < byteList.size(); i++) {
                bytes1[i] = byteList.get(i);
            }
            //循环byte1值的和
            for (int i = 0; i < bytes1.length; i++) {
                //累加
                cs += bytes1[i];
            }
            //校验和
            byteList.add((byte) cs );

            bytes = new byte[byteList.size()];
            for (int i = 0; i < byteList.size(); i++) {
                bytes[i] = byteList.get(i);
            }
        }
        return bytes;
    }
}
