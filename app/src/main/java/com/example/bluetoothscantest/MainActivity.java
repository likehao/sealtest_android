package com.example.bluetoothscantest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.Manifest;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements View.OnClickListener {
    //蓝牙管理器
    private BluetoothManager manager = null;
    //蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter = null;
    //蓝牙扫描器
    private BluetoothLeScanner scanner = null;
    //listview绑定的数组
    private ArrayList<String> arrList = new ArrayList<String>();
    private ArrayAdapter<String> adapter = null;
    private ListView lv = null;
    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_ENABLE_BT1 = 2;
    private final int REQUEST_ENABLE_BT2 = 3;
    private static final int REQUEST_CODE_LOCATION_SETTINGS = 5; //用于Gps打开

    private Handler mHandler = new Handler();
    private static final long SCAN_PERIOD = 10000;
    private static final int LOCATION_PERMISSION_CODE = 0;

    private ScanCallback callBack = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStartBlue = (Button) findViewById(R.id.btnStartBlue);
        btnStartBlue.setOnClickListener(this);

        Button btnStopBlue = (Button) findViewById(R.id.btnStopBlue);
        btnStopBlue.setOnClickListener(this);

        Button btnScan = (Button) findViewById(R.id.btnScan);
        btnScan.setOnClickListener(this);

        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, arrList);
        lv = (ListView) findViewById(R.id.lvBluetoothList);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(lvItemClick);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    || (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}
                        , LOCATION_PERMISSION_CODE);
            }
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showToast("不支持蓝牙设备");
            finish();
        }

        manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();

    }

    //点击蓝牙列表连接蓝牙
    OnItemClickListener lvItemClick = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapter, View v, int itemIndex,
                                long arg3) {
            //首先停止扫描
            stopLeScan();
            //获取选中项的mac地址
            String deviceName = arrList.get(itemIndex);
            String deviceAddr = deviceName.split("-")[2];
            //打开印章操作的页面，并把mac地址传递过去
            Intent intent = new Intent(MainActivity.this, BluetoothOperateActivity.class);
            intent.putExtra("mac", deviceAddr);
            startActivity(intent);
        }
    };

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnStartBlue:
                startBlue();
                break;
            case R.id.btnStopBlue:
                stopBlue();
                break;
            case R.id.btnScan:
                startLeScan();
                break;
            case R.id.btnCancel:
                stopLeScan();
                break;

        }
    }

    //开启蓝牙
    private void startBlue() {
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT2);
        }
    }

    private void stopBlue() {
        if (mBluetoothAdapter != null)
            mBluetoothAdapter.disable();
    }

    //开始扫描蓝牙
    @SuppressLint("NewApi")
    private void startLeScan() {
        if (scanner != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (callBack == null) {
                        callBack = new ScanCallback() {
                            @Override
                            public void onScanResult(final int callbackType,
                                                     final ScanResult result) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //showToast("onScanResult" + callbackType);
                                        if (result != null) {
                                            addBluetoothToListView(result.getDevice());
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onBatchScanResults(final List<ScanResult> results) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showToast("onBatchScanResults" + results.size());
                                    }
                                });
                            }

                            @Override
                            public void onScanFailed(final int errorCode) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showToast("onScanFailed" + errorCode);
                                    }
                                });

                            }
                        };
                    }
                    scanner.startScan(callBack);
                    showToast("开启扫描成功！");
                }
            });
        } else {
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                scanner = mBluetoothAdapter.getBluetoothLeScanner();
                if (scanner != null) {
                    showToast("蓝牙扫描对象初始化成功！");
                    startLeScan();
                }
            }
        }
    }

    //停止扫描蓝牙
    @SuppressLint("NewApi")
    private void stopLeScan() {
        if (scanner != null) {
            scanner.stopScan(callBack);
            showToast("停止扫描成功！");
        }
    }

    //扫描到蓝牙添加到列表中
    private void addBluetoothToListView(BluetoothDevice device) {
        if (device != null) {
            //印章名称
            String deviceName = device.getName();
            //蓝牙MAC地址
            String deviceMac = device.getAddress();
            //showToast(deviceName + deviceMac);
            //过滤蓝牙名称为BLE-baihe
            if (deviceName != null && deviceName.equals("BLE-baihe")) {
                String deviceAddr = device.getAddress();
                String itemName = deviceName + "-" + deviceAddr;
                if (!arrList.contains(itemName)) {
                    arrList.add(itemName);
                    adapter.notifyDataSetChanged();
                }
            } else {
                String itemName = "Unknown-device-" + deviceMac;
                if (!arrList.contains(itemName)) {
                    arrList.add(itemName);
                    adapter.notifyDataSetChanged();
                }
                //showToast("蓝牙名称为空！");
            }
        } else {
            showToast("蓝牙对象为空！");
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, String permissions[], final int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("已授予网络权限！");
                        }
                    });
                    if (isLocationEnable(this)) {
                        //定位已打开的处理
                    } else {
                        //定位依然没有打开的处理
                        Toast.makeText(MainActivity.this, "请打开GPS", Toast.LENGTH_SHORT).show();
                        setLocationService();
                    }
                }
                break;
            case REQUEST_ENABLE_BT1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("已授予GPS权限！");
                        }
                    });
                }
                break;
            case REQUEST_ENABLE_BT2:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("已授予蓝牙权限！");
                        }
                    });
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean grantedLocation = true;
        if (requestCode == LOCATION_PERMISSION_CODE) {
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    grantedLocation = false;
                }
            }
        }
        if (!grantedLocation) {
            Toast.makeText(this, "error!!!!!!!!!!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    //检测定位是否打开
    public static final boolean isLocationEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean networkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean gpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (networkProvider || gpsProvider) return true;
        return false;
    }

    //如果没有就打开，进入定位设置界面，让用户自己选择是否打开定位。选择的结果获取：
    private void setLocationService() {
        Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        this.startActivityForResult(locationIntent, REQUEST_CODE_LOCATION_SETTINGS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //确保蓝牙可以使用，如果不可以使用一个弹窗
        if (!mBluetoothAdapter.enable()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT2);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //不同打开蓝牙
        if (requestCode == REQUEST_ENABLE_BT2 && resultCode == RESULT_CANCELED) {
            finish();
            return;
        } else {
            startLeScan();
        }
        //定位
        if (requestCode == REQUEST_CODE_LOCATION_SETTINGS) {
            if (isLocationEnable(this)) {
                //定位已打开的处理
            } else {
                //定位依然没有打开的处理
                Toast.makeText(MainActivity.this, "请打开GPS", Toast.LENGTH_SHORT).show();
                setLocationService();
            }
        } else super.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    // 弹出提示框
    public void showToast(String str) {
        Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
    }
}
