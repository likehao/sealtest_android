package com.example.bluetoothscantest;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class BluetoothScanActivity extends Activity implements View.OnClickListener {
    private final int REQUEST_ENABLE_BT = 0xa01;
    private final int PERMISSION_REQUEST_COARSE_LOCATION = 0xb01;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> arrList = new ArrayList<String>();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothManager bluetoothManager;
    // 广播接收发现蓝牙设备
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                showToast("开始扫描...");
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device != null) {
                            // 添加到ListView的Adapter。
                            String deviceName = device.getName() == null ? "Unknown device" : device.getName();
                            String deviceMac = device.getAddress();
                            String itemName = deviceName + "->" + deviceMac;
                            if (!arrList.contains(itemName) && deviceName.contains("BLE-")) {
                                arrList.add(itemName);
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });

            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                showToast("扫描结束.");
            }
        }
    };

    private OnItemClickListener lvItemClick = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapter, View v, int itemIndex,
                                long arg3) {
            //首先停止扫描
            stopDiscovery();
            //获取选中项的mac地址
            String deviceName = arrList.get(itemIndex);
            String deviceAddr = deviceName.split("->")[1];
            String name = deviceName.split("->")[0];
            //打开印章操作的页面，并把mac地址及名字传递过去
            Intent intent = new Intent(BluetoothScanActivity.this, BluetoothOperateActivity.class);
            intent.putExtra("mac", deviceAddr);
            intent.putExtra("name", name);
            startActivity(intent);
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetoothscan);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }
        // 注册广播接收器。
        // 接收蓝牙发现
        IntentFilter filterFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filterFound);
        IntentFilter filterStart = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(mReceiver, filterStart);
        IntentFilter filterFinish = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filterFinish);
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrList);
        ((ListView) findViewById(R.id.listView)).setAdapter(mAdapter);
        ((ListView) findViewById(R.id.listView)).setOnItemClickListener(lvItemClick);

        findViewById(R.id.init).setOnClickListener(this);
        findViewById(R.id.discovery).setOnClickListener(this);
        findViewById(R.id.stop_discovery).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.init:
                init();
                break;
            case R.id.discovery:
                discovery();
                break;
            case R.id.stop_discovery:
                stopDiscovery();
                break;

        }
    }

    // 初始化蓝牙设备
    private void init() {
       /* bluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();*/
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 检查设备是否支持蓝牙设备
        if (mBluetoothAdapter == null) {
            showToast("设备不支持蓝牙");
            // 不支持蓝牙，退出。
            return;
        }
        // 如果用户的设备没有开启蓝牙，则弹出开启蓝牙设备的对话框，让用户开启蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            showToast("请求用户打开蓝牙");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            // 接下去，在onActivityResult回调判断
        }
    }

    // 启动蓝牙发现...
    private void discovery() {
        if (mBluetoothAdapter == null) {
            init();
        }
//        mBluetoothAdapter.startDiscovery();   //经典扫描方式
       bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
       bluetoothLeScanner.startScan(scanCallback);
 //       mBluetoothAdapter.startLeScan(callback);  //扫描ble,5.0开始弃用
    }

    /**
     * 5.0之后新加入的扫描
     */
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice bluetoothDevice = result.getDevice();
            if (bluetoothDevice != null) {
                // 添加到ListView的Adapter。
                String deviceName = bluetoothDevice.getName() == null ? "Unknown device" : bluetoothDevice.getName();
                String deviceMac = bluetoothDevice.getAddress();
                String itemName = deviceName + "->" + deviceMac;
                if (!arrList.contains(itemName) && deviceName.contains("BLE-")) {
                    arrList.add(itemName);
                    mAdapter.notifyDataSetChanged();
                }
            }
   /*         byte[] scanData = result.getScanRecord().getBytes();
            //把byte数组转成16进制字符串
            int i = result.getRssi();
            Log.e("TAG","onScanResult :"+DataTrans.bytesHexString(scanData));
            Log.e("TAG","onScanResult :"+result.getScanRecord().toString());*/
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    final BluetoothAdapter.LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            if (bluetoothDevice != null) {
                // 添加到ListView的Adapter。
                String deviceName = bluetoothDevice.getName() == null ? "Unknown device" : bluetoothDevice.getName();
                String deviceMac = bluetoothDevice.getAddress();
                String itemName = deviceName + "->" + deviceMac;
                if (!arrList.contains(itemName) && deviceName.contains("BLE-")) {
                    arrList.add(itemName);
                    mAdapter.notifyDataSetChanged();
                }
            }
            String s =  bluetoothDevice.getName();
           String str = bluetoothDevice.getAddress();
        }
    };

    //停止扫描
    private void stopDiscovery() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                showToast("打开蓝牙成功！");
            }
            if (resultCode == RESULT_CANCELED) {
                showToast("放弃打开蓝牙！");
            }
        } else {
            showToast("蓝牙异常！");
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showToast("已经授权定位权限!");
                }
                break;
        }
    }

    // 弹出提示框
    public void showToast(String str) {
        Toast.makeText(BluetoothScanActivity.this, str, Toast.LENGTH_SHORT).show();
    }
}
