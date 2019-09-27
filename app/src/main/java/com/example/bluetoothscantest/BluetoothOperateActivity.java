package com.example.bluetoothscantest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothOperateActivity extends Activity implements View.OnClickListener {
    //蓝牙MAC地址
    private String mac = null;
    private String name = null;
    //蓝牙管理器
    private BluetoothManager manager = null;
    //蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter = null;// BluetoothAdapter.getDefaultAdapter();
    //连接的蓝牙设备
    private BluetoothDevice device = null;
    //蓝牙设备协定对象
    private BluetoothGatt blueGatt = null;
    //蓝牙协定服务对象
    private BluetoothGattService service = null;
    //蓝牙协定服务写入对象
    private BluetoothGattCharacteristic wrt_char = null;
    //蓝牙协定服务通知对象
    private BluetoothGattCharacteristic ntf_char = null;
    //显示盖章次数的TextView控件
    private TextView tv = null;
    //盖章总次数
    private int stampCount = 0;
    private Button btnHand, btnStart, btnSuperUser, btnUpdatePwd, btnUpdateKeyPwd;
    private Button btnReset, btnClose, btnElectric, lock_seal, delete_fingerprint, set_fingerprint, select_fingerprint;
    private Button clear_bt, press_time, press_password, select_press_time, change_press_power, delete_press_pwd;
    private EditText showET;
    private Button set_seal_delay_bt,select_seal_delay_bt;
    byte[] bytes;
    private String str = "";
    //上传历史数据域集合
    List<byte[]> historyList = new ArrayList<>();
    //总的剩余次数
    int restCount = 0;
    //每次固定上传次数
    int num = 1;
    //记录序号
    int orderNum;
    int maxTime = 10000; //最大时间
    int needGetTime;  //需要发送的记录次数
    int currentCountDown = 0;

    boolean isStpoped;
    Timer timer;
    TimerTask task;
    private Button recording_fingerprint;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetoothoperate);

        initView();
        setListener();
        //获取传递过来的MAC地址
        Intent intent = this.getIntent();
        this.mac = intent.getStringExtra("mac");
        this.name = intent.getStringExtra("name");
        if (this.mac != null) {
            manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            mBluetoothAdapter = manager.getAdapter();
            //获取连接的蓝牙对象
            this.device = this.mBluetoothAdapter.getRemoteDevice(this.mac);
            //连接蓝牙
            connect(this.device);
        }

    }

    private void initView() {
        //初始化组件
        stampCount = 0;
        tv = (TextView) findViewById(R.id.txtCount);
        tv.setText(stampCount + "次");
        btnHand = (Button) findViewById(R.id.btnHand);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnSuperUser = (Button) findViewById(R.id.btnSuperUser);
        btnUpdatePwd = (Button) findViewById(R.id.btnUpdatePwd);
        btnUpdateKeyPwd = (Button) findViewById(R.id.btnUpdateKeyPwd);
        btnReset = (Button) findViewById(R.id.btnReset);
        btnClose = (Button) findViewById(R.id.btnClose);
        btnElectric = (Button) findViewById(R.id.btnElectric);
        lock_seal = (Button) findViewById(R.id.lock_seal);
        delete_fingerprint = (Button) findViewById(R.id.delete_fingerprint);
        set_fingerprint = (Button) findViewById(R.id.set_fingerprint);
        select_fingerprint = (Button) findViewById(R.id.select_fingerprint);
        showET = (EditText) findViewById(R.id.showET);
        clear_bt = (Button) findViewById(R.id.clear_bt);
        press_time = (Button) findViewById(R.id.press_time);
        press_password = (Button) findViewById(R.id.press_password);
        select_press_time = (Button) findViewById(R.id.select_press_time);
        change_press_power = (Button) findViewById(R.id.change_press_power);
        delete_press_pwd = (Button) findViewById(R.id.delete_press_pwd);
        set_seal_delay_bt = (Button) findViewById(R.id.set_seal_delay_bt);
        select_seal_delay_bt = (Button) findViewById(R.id.select_seal_delay_bt);
        recording_fingerprint = (Button) findViewById(R.id.recording_fingerprint);
    }

    public void setListener() {
        btnHand.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        btnSuperUser.setOnClickListener(this);
        btnUpdatePwd.setOnClickListener(this);
        btnUpdateKeyPwd.setOnClickListener(this);
        btnReset.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnElectric.setOnClickListener(this);
        lock_seal.setOnClickListener(this);
        delete_fingerprint.setOnClickListener(this);
        set_fingerprint.setOnClickListener(this);
        select_fingerprint.setOnClickListener(this);
        clear_bt.setOnClickListener(this);
        press_time.setOnClickListener(this);
        press_password.setOnClickListener(this);
        select_press_time.setOnClickListener(this);
        delete_press_pwd.setOnClickListener(this);
        change_press_power.setOnClickListener(this);
        set_seal_delay_bt.setOnClickListener(this);
        select_seal_delay_bt.setOnClickListener(this);
        recording_fingerprint.setOnClickListener(this);
    }

    //按钮点击
    @Override
    public void onClick(View view) {
        if (name.equals("BLE-baihe")) {
            switch (view.getId()) {
                case R.id.btnHand:
                    sendDataToBlue("HandShake/2yK39b");
                    showData("HandShake/2yK39b");
                    break;
                case R.id.btnStart:
                    sendDataToBlue("UPASSWD=123456");
                    showData("UPASSWD=123456");
                    break;
                case R.id.btnSuperUser:
                    sendDataToBlue("ADMIN1");
                    showData("ADMIN1");
                    break;
                case R.id.btnUpdatePwd:
                    sendDataToBlue("SPASSWD=123456333666");
                    showData("SPASSWD=123456333666");
                    break;
                case R.id.btnUpdateKeyPwd:
                    sendDataToBlue("KPASSWD=123456333666");
                    showData("KPASSWD=123456333666");
                    break;
                case R.id.btnReset:   //重置
                    sendDataToBlue("RESET");
                    showData("RESET");
                    break;
                case R.id.btnClose:
                    disConnect();
                    break;
                case R.id.clear_bt:
                    showET.getText().clear();
                    str = "";
                    break;
            }
        } else {
            switch (view.getId()) {
                case R.id.btnHand:
                    //获取时间字节数组
                    byte[] byteTime = CommonUtil.getDateTime();
                    //发送给蓝牙
                    sendDataToBlue(new DataProtocol(CommonUtil.HANDSHAKE, byteTime));
                    showData(bytes);
                    break;
                case R.id.btnStart:
                    //启动
                    byte[] startAllByte = CommonUtil.startData();
                    sendDataToBlue(new DataProtocol(CommonUtil.START, startAllByte));
                    showData(bytes);
                    break;
                case R.id.btnReset:   //重置
                    String reStr = DataTrans.hexString2binaryString("11100000");
                    byte[] resByte = DataTrans.toBytes(reStr);
                    sendDataToBlue(new DataProtocol(CommonUtil.RESET, resByte));
                    showData(bytes);
                    break;
                case R.id.press_time:  //长按时间
                    byte[] press_time = new byte[]{5};
                    sendDataToBlue(new DataProtocol(CommonUtil.PRESSTIME, press_time));
                    showData(bytes);
                    break;
                case R.id.select_press_time: //查询长按时间
                    byte[] select_press_time = new byte[]{0};
                    sendDataToBlue(new DataProtocol(CommonUtil.SELECTPRESSTIME, select_press_time));
                    showData(bytes);
                    break;
                case R.id.btnClose:
                    disConnect();
                    break;
                case R.id.btnElectric:   //电量查询
                    byte[] eleByte = new byte[]{0};
                    sendDataToBlue(new DataProtocol(CommonUtil.ElECTRIC, eleByte));
                    showData(bytes);
                    break;
                case R.id.lock_seal:      //锁定印章
                    byte[] lock = new byte[]{0};
                    sendDataToBlue(new DataProtocol(CommonUtil.LOCK, lock));
                    showData(bytes);
                    break;
                case R.id.recording_fingerprint:      //录制指纹
                    byte[] fingerprint = CommonUtil.startData();
                    sendDataToBlue(new DataProtocol(CommonUtil.RECORDING, fingerprint));
                    showData(bytes);
                    break;
                case R.id.delete_fingerprint:   //删除指纹
                    showDia(1);
                    break;
                case R.id.set_fingerprint:  //设置指纹权限
                    showDia(2);
                    break;
                case R.id.select_fingerprint:  //查询指纹权限
                    long select = DataTrans.parseLong("EB");
                    int selectInt = DataTrans.integer("25");
                    byte[] selectB = new byte[]{(byte) select, (byte) selectInt};
                    sendDataToBlue(new DataProtocol(CommonUtil.SELECT, selectB));
                    showData(bytes);
                    break;
                case R.id.clear_bt:
                    showET.getText().clear();
                    str = "";
                    break;
                case R.id.press_password:    //添加按键密码和权限
                    sendDataToBlue(new DataProtocol(CommonUtil.ADDPRESSPWD, CommonUtil.addPressPwd()));
                    showData(bytes);
                    break;
                case R.id.change_press_power:  //修改按键密码权限
                    byte[] changeByte = getShre();
                    if (changeByte.length != 0) {
                        byte[] changePwdPre = CommonUtil.changePwdPower(changeByte);
                        sendDataToBlue(new DataProtocol(CommonUtil.CHANGEPWDPOWER, changePwdPre));
                    }
                    showData(bytes);
                    break;
                case R.id.btnUpdateKeyPwd:   //修改按键密码
                    byte[] b = getShre();
                    if (b.length != 0) {
                        byte[] pwdCode = CommonUtil.changePwd(b);
                        sendDataToBlue(new DataProtocol(CommonUtil.UPDATEKEPWD, pwdCode));
                    }
                    showData(bytes);
                    break;
                case R.id.delete_press_pwd:  //删除按键密码
                    byte[] deleteByte = getShre();
                    if (deleteByte.length != 0) {
                        byte[] deletePwdCode = CommonUtil.deletePressPwd(deleteByte);
                        sendDataToBlue(new DataProtocol(CommonUtil.DELETEPRESSPWD, deletePwdCode));
                    }
                    showData(bytes);
                    break;
                case R.id.set_seal_delay_bt:  //设置盖章延时时间
                    byte[] set_seal_delay = new byte[]{1};
                    sendDataToBlue(new DataProtocol(CommonUtil.SETSEALDELAY, set_seal_delay));
                    showData(bytes);
                    break;
                case R.id.select_seal_delay_bt:  //查询盖章延时时间
                    byte[] select_seal_delay = new byte[]{0};
                    sendDataToBlue(new DataProtocol(CommonUtil.SELECTSEALDELAY, select_seal_delay));
                    showData(bytes);
                    break;
            }
        }

    }

    private void showDia(final int code){
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.dialog));
        View view = View.inflate(this,R.layout.dialog_layout,null);
        final AlertDialog dialog = builder.create();
        dialog.setView(view,0,0,0,0);

        final EditText text = (EditText) view.findViewById(R.id.et);
        Button button = (Button) view.findViewById(R.id.sure_bt);  //确定
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (code == 1){
                    //删除指纹
                    byte[] delete = new byte[]{Byte.parseByte(text.getText().toString().trim())};
                    sendDataToBlue(new DataProtocol(CommonUtil.DELETE, delete));
                    showData(bytes);
                }else if (code == 2){
                    //设置指纹权限
                    sendDataToBlue(new DataProtocol(CommonUtil.SET, CommonUtil.setFingerprint(Integer.parseInt(text.getText().toString().trim()))));
                    showData(bytes);
                }

            }
        });
        dialog.show();
    }
    //蓝牙设备回调对象
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status,
                                            final int newState) {
            blueGatt = gatt;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        // 已连接状态
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            gatt.discoverServices();
                            Log.e("ATG", "成功连接发现服务。。。。。。。。。。");
                        }
                        // 已断开状态
                        else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            gatt.close();
                        }
                    } else {
                        gatt.close();
                    }
                }
            });
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //如果成功发现服务
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //根据服务UUID获取印章服务对象
                service = gatt.getService(UUID.fromString(BluetoothUUID.SERVICE_UUID));
                //获取印章服务对象的NOTIFY特征
                ntf_char = service.getCharacteristic(UUID.fromString(BluetoothUUID.NOTIFY_UUID));

                if ((ntf_char.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    boolean flag = gatt.setCharacteristicNotification(ntf_char, true);
                    //导致连上服务立马断开
                    List<BluetoothGattDescriptor> descriptorList = ntf_char.getDescriptors();
                    if (flag && descriptorList != null && descriptorList.size() > 0) {
                        for (BluetoothGattDescriptor descriptor : descriptorList) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                    }
                }
                //获取印章服务对象的写入特性
                wrt_char = service.getCharacteristic(UUID.fromString(BluetoothUUID.WRITE_UUID));

            }
        }

        @Override
        public void onCharacteristicChanged(final BluetoothGatt gatt,
                                            final BluetoothGattCharacteristic characteristic) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //收到数据
                    Log.e("TAG","成功接收返回数据");
                    byte[] buffer = characteristic.getValue();
                    if (buffer != null && buffer.length > 2) {
                        if (!check(buffer)) {
                            return;
                        }
                        Integer integer = DataTrans.integer("FF");
                        byte type = buffer[2];  //类型值
                        switch (type) {
                            case (byte) 0xA0:
                                if (buffer[3] == 0) {
                                    receiveData(buffer);
                                    setUploadHistory(buffer);//设置上传历史
                                } else if (buffer[3] == integer) {
                                    receiveData(buffer);
                                }
                                break;
                            case (byte) 0xA1:
               /*                 if (buffer.length == 6) {
                                    sendCutPackageData(totalByte);
                                    //发送结束包
                                    byte[] endByte = new byte[]{00, 00};
                                    sendDataToBlue(new DataProtocol(CommonUtil.START, endByte));
                                }*/
                                if (buffer[3] == 0) {
                                    receiveData(buffer);
                                } else if (buffer[3] == integer) {
                                    receiveData(buffer);
                                }
                                break;
                            case (byte) 0xA2:
                                receiveData(buffer);
                                sendSealHistory(); //发送盖章记录上传
                                break;
                            case (byte) 0xA3:  //通知印章上传盖章历史
                                if (isStpoped) {
                                    return;
                                }
                                receiveData(buffer);
                                getReceive(buffer);
                                break;
                            case (byte) 0xA4:
                                if (buffer[3] == 0) {
                                    receiveData(buffer);
                                    sharedPre(buffer);  //存储byte
                                } else if (buffer[3] == integer) {
                                    receiveData(buffer);
                                }
                                break;
                            case (byte) 0xB0:    //修改按键密码
                                receiveData(buffer);
                                break;
                            case (byte) 0xB1:    //修改按键密码权限
                                receiveData(buffer);
                                break;
                            case (byte) 0xB2:    //删除按键密码
                                receiveData(buffer);
                                break;
                            case (byte) 0xB3:   //查询盖章延时
                                receiveData(buffer);
                                break;
                            case (byte) 0xB4:   //设置盖章延时
                                receiveData(buffer);
                                break;
                            case (byte) 0xA5:
                                if (buffer[3] == 0) {
                                    receiveData(buffer);
                                } else if (buffer[3] == integer) {
                                    receiveData(buffer);
                                }
                                break;
                            case (byte) 0xA6:
                                if (buffer[3] == 0) {
                                    receiveData(buffer);
                                } else if (buffer[3] == integer) {
                                    receiveData(buffer);
                                }
                                break;
                            case (byte) 0xA7:
                            /*    if (buffer[3] == 0) {
                                    receiveData(buffer);
                                } else if (buffer[3] == DataTrans.integer("80")) {
                                    receiveData(buffer);
                                }*/
                                receiveData(buffer);
                                break;
                            case (byte) 0xA8:
                                receiveData(buffer);
                                break;
                            case (byte) 0xA9:
                                if (buffer[3] == 0) {
                                    receiveData(buffer);
                                } else if (buffer[3] == integer) {
                                    receiveData(buffer);
                                }
                                break;
                            case (byte) 0xAA:
                                if (buffer[4] == 0){
                                    receiveData(buffer);
                                }else if (buffer[4] == integer){
                                    receiveData(buffer);
                                }
                                break;
                            case (byte) 0xAB:
                                if (buffer[3] == 0) {
                                    receiveData(buffer);
                                } else if (buffer[3] == integer) {
                                    receiveData(buffer);
                                }
                                break;
                            case (byte) 0xAC:
                                if (buffer[3] == 0) {
                                    receiveData(buffer);
                                } else if (buffer[3] == integer) {
                                    receiveData(buffer);
                                }
                                break;
                            case (byte) 0xAD:
                                receiveData(buffer);
                                break;
                            case (byte) 0xAE:
                     /*         //判断如果数据长度大于6,则为数据包,否则继续判断
                                if (buffer.length > 6) {
                                    //截取数据域,存放byte数组到集合
                                    byte[] historyByte = DataTrans.subByte(buffer, 5, buffer.length - 6);
                                    historyList.add(historyByte);
                                }
                                //长度小于6,如果为0则为结束包,否则为总包数
                                else if (buffer[4] == 0) {
                                    //发送结束包
                                    byte[] historyEnd = new byte[]{00};
                                    sendDataToBlue(new DataProtocol(CommonUtil.UPLOAD, historyEnd));
                                    //收到结束包之后解析获取的数据域数据
                                    for (int i = 0; i < historyList.size(); i++) {
                                        byte[] startId = DataTrans.subByte(historyList.get(i), 0, 16);
                                        byte[] code = DataTrans.subByte(historyList.get(i), 16, 2);
                                        byte[] sealTime = DataTrans.subByte(historyList.get(i), 18, 7);
                                    }

                                } else {
                                    //收到总包再响应给印章
                                    sendDataToBlue(buffer);
                                }*/
                                receiveData(buffer);
                                needGetTime = restCount - num > num ? num : restCount - num;
                                if (restCount - num > 0) {
                                    restCount = restCount - num;
                                    startGetHistory();
                                } else {
                                    // 表示已经全部获取完成，清空集合，停止倒计时，抛出获取成功的回调
                                    onStop();
                                }
                                break;
                            case (byte) 0xAF:
                                receiveData(buffer);
                                break;
                        }
                    } else {
                        String s = new String(buffer);
                        //盖章次数
                        if (s.equals("C1")) {
                            stampCount += 1;
                            SetStampCount();
                            receiveData("C1");
                        } else if (s.equals("H1")) {
                            showToast("握手成功！");
                            receiveData("H1");
                        } else if (s.equals("H0")) {
                            showToast("握手失败！");
                            receiveData("H0");
                        } else if (s.equals("S1")) {
                            showToast("更改盖章密码成功！");
                            receiveData("S1");
                        } else if (s.equals("S0")) {
                            showToast("更改盖章密码失败！");
                            receiveData("S0");
                        } else if (s.equals("R1")) {
                            showToast("设备重置成功！");
                            receiveData("R1");
                        } else if (s.equals("R0")) {
                            showToast("设备重置失败！");
                            receiveData("R0");
                        } else if (s.equals("U1")) {
                            showToast("印章启动成功！");
                            receiveData("U1");
                        } else if (s.equals("U0")) {
                            showToast("印章启动失败！");
                            receiveData("U0");
                        } else if (s.equals("K1")) {
                            showToast("更改按键密码成功！");
                            receiveData("K1");
                        } else if (s.equals("K0")) {
                            showToast("更改按键密码失败！");
                            receiveData("K1");
                        } else if (s.equals("A1")) {
                            showToast("已识别为超管!");
                            receiveData("A1");
                        } else if (s.equals("A0")) {
                            showToast("已识别为普通用户!");
                            receiveData("A0");
                        }
                    }
                }
            });

        }
    };

    /**
     * 截取密码代码并存储byte数组
     * @param b
     */
    private void sharedPre(byte[] b) {
        byte[] shaB = DataTrans.subByte(b, 4, 4);
        SharedPreferences sharedPreferences = getSharedPreferences("demo", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String s = new String(Base64.encode(shaB, Base64.DEFAULT));
        editor.putString("content", s);
        editor.commit();
    }

    /**
     * 取存储byte[]值
     * @return
     */
    private byte[] getShre() {
        SharedPreferences sharedPreferences = getSharedPreferences("demo", Activity.MODE_PRIVATE);
        String string = sharedPreferences.getString("content", "");
        byte[] b = Base64.decode(string.getBytes(), Base64.DEFAULT);
        return b;
    }

    //连接蓝牙
    private void connect(BluetoothDevice device) {
        if (device != null) {
            device.connectGatt(this.getApplicationContext(), false,
                    gattCallback);
        }
    }

    //断开连接
    private void disConnect() {
        if (blueGatt != null) {
            blueGatt.disconnect();
            blueGatt.close();
        }
    }

    //给蓝牙发送数据
    private void sendDataToBlue(String strData) {
        if (wrt_char != null && blueGatt != null) {
            wrt_char.setValue(0x00, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            wrt_char.setValue(strData.getBytes());
            blueGatt.writeCharacteristic(wrt_char);
        }
    }

    private void sendDataToBlue(byte[] bytes) {
        if (wrt_char != null && blueGatt != null) {
            wrt_char.setValue(0x00, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            wrt_char.setValue(bytes);
            blueGatt.writeCharacteristic(wrt_char);
        }
    }

    private void sendDataToBlue(DataProtocol dataProtocol) {
        if (dataProtocol != null) {
            bytes = dataProtocol.getBytes();
            if (bytes != null) {
                //判断是否分包
                sendDataToBlue(bytes);
            /*    if (bytes.length <= 20) {
                }
                //要分包发送
                else {
                    //取出数据域数据
                    byte[] byteCS = DataTrans.subByte(bytes, 3, 34);
                    //判断数据域能否被14整除,否则包数加1
                    int num = byteCS.length / 14;
                    if (byteCS != null && byteCS.length % 14 != 0) {
                        //总包数
                        num = num + 1;
                    }
                    //发送总包数
                    byte[] allByte = DataTrans.shortToByteArray((short) num, true);
                    sendDataToBlue(new DataProtocol(CommonUtil.START, allByte));
                    // 将data保存起来，当发送总包数之后收到设备的响应之后，再来截取逐个发送
                    totalByte = byteCS;
                }*/
            }
        }
    }

    /**
     * 截取分包发送
     */
    public void sendCutPackageData(byte[] bytes) {
        int forCount = bytes.length / 14;
        int y = bytes.length % 14;
        for (int i = 0; i < forCount; i++) {
            //截取
            byte[] bytes1 = DataTrans.subByte(bytes, i * 14, 14);
            // 拼接第几包，再发送
            byte[] packNum = DataTrans.shortToByteArray((short) i, true);
            byte[] sendByte = DataTrans.byteMerger(packNum, bytes1);
            sendDataToBlue(new DataProtocol(CommonUtil.START, sendByte));
        }
        // 判断是否能整除，不能整除说明有多余的，需要单独来截取发送
        if (y != 0) {
            //截取最后一包
            byte[] b = DataTrans.subByte(bytes, forCount * 14, y);
            byte[] packNum = DataTrans.shortToByteArray((short) (forCount + 1), true);
            //拼接最后一包
            byte[] sendByte = DataTrans.byteMerger(packNum, b);
            sendDataToBlue(new DataProtocol(CommonUtil.START, sendByte));
        }
    }

    /**
     * 盖章记录上传
     */
    private void sendSealHistory() {
        byte[] sendSealHistory = new byte[]{0};
        sendDataToBlue(new DataProtocol(CommonUtil.SEALHISTORYUPLOAD, sendSealHistory));
        showData(bytes);
    }

    /**
     * 上传盖章历史记录
     *
     * @param bytes
     * @return
     */
    private void setUploadHistory(byte[] bytes) {
        //截取剩余次数
        byte[] restTime = DataTrans.subByte(bytes, 6, 2);
        restCount = DataTrans.bytesToInt(restTime, 0);

        //判断有无剩余次数
        if (bytes != null && restCount > 0) {
            showUploadDialog();
        }
    }

    /**
     * 是否需要上传剩余次数dialog
     */
    private void showUploadDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(BluetoothOperateActivity.this);
        builder.setTitle("提示")
                .setMessage("剩余" + restCount + "次盖章记录,是否需要上传")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        needGetTime = restCount > num ? num : restCount;
                        //开始获取并发送
                        startGetHistory();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlertDialog dialog = builder.show();
                        dialog.dismiss();
                    }
                });
        builder.show();

    }

    /**
     * 开启计时器获取数据
     */
    private void startGetHistory() {
        //判断总的剩余次数与上传固定次数大小，发送获取历史记录的指令，同时开启倒计时
        isStpoped = false;
        openCountDown();
        sendDataToBlue(needGetTime);
    }

    /**
     * 发送盖章历史记录
     *
     * @param i
     */
    private void sendDataToBlue(int i) {
        byte[] bytes = new byte[]{(byte) i};
        sendDataToBlue(new DataProtocol(CommonUtil.NOTIFYHISTORYUPLOAD, bytes));
    }

    /**
     * 收到盖章历史记录数据
     *
     * @param bytes
     * @return
     */
    private void getReceive(byte[] bytes) {
        historyList.add(bytes);  //返回的每条数据添加到集合里
        orderNum = bytes[3]; //记录序号
        if (orderNum == needGetTime - 1) {  //判断记录号是否为最后一条去结束
            if (historyList.size() == needGetTime) {
                List<Map<String, String>> mapList = new ArrayList<>();
                for (int i = 0; i < historyList.size(); i++) {
                    byte[] subByte = historyList.get(i);
                    // 表示数据获取完整，没有丢包，先上传到服务器，上传成功之后清空集合，然后获取下一个needGetTime
                    byte[] startByte = DataTrans.subByte(subByte, 4, 4); //获取启动序号
                    byte[] sealByte = DataTrans.subByte(subByte, 8, 2); //获取盖章序号
                    byte[] timeByte = DataTrans.subByte(subByte, 10, 6); //获取盖章时间

                    Map<String, String> map = new HashMap<>();
                    map.put("startByte", String.valueOf(DataTrans.bytesToInt(startByte, 0)));
                    map.put("sealByte", String.valueOf(DataTrans.bytesToInt(sealByte, 0)));
                    map.put("timeByte", bytesHexString(timeByte));
                    mapList.add(map);
                }
                dataGet(mapList);

            } else {
                // 中间有丢包，此次传输失败，直接停止获取，并清空数组
                onStop();
            }
        } else {
            // 将倒计时时间归零，重新开始倒计时
            currentCountDown = 0;
        }
    }

    //byte转string
    public static String bytesHexString(byte[] bytes) {
        String str = "";
        for (int i = 0; i < bytes.length; i++) {
            int a = bytes[i];
            str += a + " ";
        }
        return (str);
    }

    /**
     * 开启计时器
     */
    private void openCountDown() {
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                currentCountDown += 1;
            }
        };
        timer.schedule(task, 0, 1000);
        if (currentCountDown == maxTime) {
            // 表示已经超时，清空集合，停止获取数据
            //销毁
            onStop();
        }
    }

    @Override
    protected void onDestroy() {
        disConnect();
        Log.e("TAG", "断开连接。。。。。。。。。。");
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        isStpoped = true;
        super.onStop();
        historyList.clear();
        //销毁定时器
        if (timer != null) {
            timer.cancel();
        }
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * 发送数据get请求
     */
    public void dataGet(List<Map<String, String>> list) {
        uploadSuccess();
       /* runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //创建OkHttpClient对象
                OkHttpClient okHttpClient = new OkHttpClient();
                //创建请求
                Request request = new Request.Builder()
                        .url(url+ 启动序号+盖章序号+盖章时间)
                        .get()
                        .build();
                //设置回调
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        //失败
                        uploadFail();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        //成功之后发送AE
                        uploadSuccess();
                    }
                });
            }
        });*/
    }

    // 上传到服务器成功
    private void uploadSuccess() {
        onStop();
        // 发送AE给印章
        byte[] uploadTrue = new byte[]{(byte) needGetTime};
        sendDataToBlue(new DataProtocol(CommonUtil.UPLOAD, uploadTrue));
        showData(bytes);
        // 在接收处接到AE就获取下一个5条

    }

    //上传到服务器失败
    private void uploadFail() {
        onStop();
    }

    //验证接收到的校验和
    private boolean check(byte[] bytes) {
        int cs = 0;
        for (int i = 0; i < bytes.length - 1; i++) {
            cs += bytes[i];
        }
        cs = (byte) cs;
        if (cs == bytes[(bytes.length - 1)]) {
            return true;
        }
        return false;
    }

    //设置盖章次数
    private void SetStampCount() {
        if (this.tv != null) {
            this.tv.setText(this.stampCount + "次");
        }
    }

    // 弹出提示框
    public void showToast(String str) {
        Toast.makeText(BluetoothOperateActivity.this, str, Toast.LENGTH_LONG).show();
    }

    /**
     * 显示发送接收数据
     *
     * @param b
     */
    public void showData(byte[] b) {
        str += "发送: " + DataTrans.bytesHexString(b) + "\n";
        showET.setText(str);
    }

    public void receiveData(byte[] b) {
        str += "接收: " + DataTrans.bytesHexString(b) + "\n";
        showET.setText(str);
    }

    public void showData(String st) {
        str += "发送: " + st + "\n";
        showET.setText(str);
    }

    public void receiveData(String st) {
        str += "接收: " + st + "\n";
        showET.setText(str);
    }

}
