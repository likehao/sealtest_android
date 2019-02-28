package com.example.bluetoothscantest;

public class DataTrans {

    /**
     * byte转字符串
     */
    public static String bytesHexString(byte[] bytes) {
        String str = "";
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            str += hex.toUpperCase()+" ";
        }

        return (str);
    }

    /**
     * 将16进制字符串转换为byte
     */
    public static byte[] toBytes(String str) {
        if (str == null || str.trim().equals("")) {
            return new byte[0];
        }
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }
        return bytes;
    }

    /**
     * 二进制转16
     */
    public static String hexString2binaryString(String bString) {
        if (bString == null || bString.equals("") || bString.length() % 8 != 0)
            return null;
        StringBuffer tmp = new StringBuffer();
        int iTmp = 0;
        for (int i = 0; i < bString.length(); i += 4) {

            iTmp = 0;
            for (int j = 0; j < 4; j++) {
                iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << (4 - j - 1);
            }
            tmp.append(Integer.toHexString(iTmp));

        }
        return tmp.toString().toUpperCase();
    }

    public static String intToHex(int i) {
        return String.format("%02x", i);
    }

    public static byte[] intToByteArray(Integer integer, Boolean bigEndian) {
        int byteNum = (40 - Integer.numberOfLeadingZeros(integer < 0 ? ~integer : integer)) / 8;
        byte[] byteArray = new byte[4];

        for (int n = 0; n < byteNum; n++) {
            if (bigEndian)
                byteArray[3 - n] = (byte) (integer >>> (n * 8));
            else
                byteArray[n] = (byte) (integer >>> (n * 8));
        }

        return byteArray;

    }

    public static byte[] shortToByteArray(Short s, Boolean bigEndian) {
        Integer integer = (int) s;
        int byteNum = (40 - Integer.numberOfLeadingZeros(integer < 0 ? ~integer : integer)) / 8;
        byte[] byteArray = new byte[4];

        for (int n = 0; n < byteNum; n++) {
            if (bigEndian)
                byteArray[3 - n] = (byte) (integer >>> (n * 8));
            else
                byteArray[n] = (byte) (integer >>> (n * 8));
        }

        byte[] newByteArray = new byte[2];
        if (bigEndian) {
            newByteArray[0] = byteArray[2];
            newByteArray[1] = byteArray[3];
        } else {
            newByteArray[0] = byteArray[0];
            newByteArray[1] = byteArray[1];
        }

        return newByteArray;

    }

    /**
     * 数组截取,b原数组,off索引,length截取长度
     */
    public static byte[] subByte(byte[] b,int off,int length){
        byte[] b1 = new byte[length];
        System.arraycopy(b,off,b1,0,length); //源数组,源数组复制起始位,目的数组,目的数组起始位,复制长度
        return b1;
    }

    /**
     * 数组合并
     */
    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2){
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    /**
     * 将字符串转换成int
     */
    public static Integer integer(String string){
        Integer integer = Integer.parseInt(string,16);
        return integer;
    }

    public static long parseLong(String string){
        Long l = Long.parseLong(string,16);
        return l;
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序
     */
    public static int bytesToInt(byte[] src, int offset) {
        int value = 0;
        for (int i = 0; i < src.length; i++) {
            value |= ((src[offset + i] & 0xFF) << i * 8);
        }
        return value;
    }
}
