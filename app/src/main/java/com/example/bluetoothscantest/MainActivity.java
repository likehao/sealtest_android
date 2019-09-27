package com.example.bluetoothscantest;

import android.Manifest;
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
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    //当前安卓是否支持
    private static final int REQUEST_CODE_BLUETOOTH_ON = 1000;
    private static final int REQUEST_COARSE_LOCATION = 0;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> arrList = new ArrayList<String>();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private ListView listView;
    private RefreshLayout record_refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initCheck();
        initView();

    }

    private void initView() {
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(lvItemClick);
        //    record_refreshLayout = (RefreshLayout) findViewById(R.id.record_refreshLayout);
        // 注册广播接收器。
        // 接收蓝牙发现
        IntentFilter filterFound = new IntentFilter();
        filterFound.addAction(BluetoothDevice.ACTION_FOUND);
        filterFound.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filterFound.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filterFound);
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrList);
        listView.setAdapter(mAdapter);
        //   setSmartRefreshLayout();
    }

    /**
     * 初始化蓝牙权限
     */
    private void initCheck() {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                //判断是否需要 向用户解释，为什么要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
                    showToast("蓝牙6.0需要该权限");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);
                return;
            }
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //获取蓝牙适配器
        //请求打开蓝牙
        Intent requestBluetoothOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        //请求开启蓝牙
        this.startActivityForResult(requestBluetoothOn, REQUEST_CODE_BLUETOOTH_ON);
    }

    /**
     * 刷新加载
     */
    public void setSmartRefreshLayout() {
        record_refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                //请求数据
                listView.setAdapter(mAdapter);
                discovery();
                refreshLayout.autoRefresh(); //自动刷新
                refreshLayout.finishRefresh(); //刷新完成
            }
        });
        record_refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.finishLoadMore();  //加载完成
            }
        });
    }

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

    private AdapterView.OnItemClickListener lvItemClick = new AdapterView.OnItemClickListener() {
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
            Intent intent = new Intent(MainActivity.this, BluetoothOperateActivity.class);
            intent.putExtra("mac", deviceAddr);
            intent.putExtra("name", name);
            startActivity(intent);
            arrList.clear();
            Log.e("TAG","蓝牙列表清空。。。。。。。。。。。");
        }
    };

    /**
     * 蓝牙扫描
     */
    private void discovery() {
        if (mBluetoothAdapter != null) {
            //  mBluetoothAdapter.startDiscovery();   //经典扫描方式
            bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.startScan(scanCallback);
            }
            //       mBluetoothAdapter.startLeScan(callback);  //扫描ble,5.0开始弃用
        }
    }

    /**
     * 5.0之后新加入的扫描
     */
    String itemName, deviceName, deviceMac;
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice bluetoothDevice = result.getDevice();
            if (bluetoothDevice != null) {
                // 添加到ListView的Adapter。
                deviceName = bluetoothDevice.getName() == null ? "Unknown device" : bluetoothDevice.getName();
                deviceMac = bluetoothDevice.getAddress();
                itemName = deviceName + "->" + deviceMac;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!arrList.contains(itemName) && deviceName.contains("BLE-")) {
                            arrList.add(itemName);
                            Log.e("扫描到:",deviceMac+"");
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
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
            String s = bluetoothDevice.getName();
            String str = bluetoothDevice.getAddress();
        }
    };

    //停止扫描
    private void stopDiscovery() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
       //     bluetoothLeScanner.stopScan(scanCallback);
        }
        Log.e("TAG","停止扫描。。。。。。。。。");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_BLUETOOTH_ON) {
            if (resultCode == RESULT_OK) {
                showToast("打开蓝牙成功！");
                discovery();
            }
            if (resultCode == RESULT_CANCELED) {
                showToast("放弃打开蓝牙！");
            }
        } else {
            showToast("蓝牙异常！");
        }
    }

    protected void onDestroy() {
        stopDiscovery();
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        stopDiscovery();
        arrList.clear();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showToast("已经授权定位权限!");
                }
                break;
        }
    }

    // 弹出提示框
    public void showToast(String str) {
        Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
    }
}