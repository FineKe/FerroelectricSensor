package com.litesky.ferroelectricsensor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.litesky.ferroelectricsensor.adapter.DeviceAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.internal.schedulers.ExecutorScheduler;
import io.reactivex.internal.schedulers.SchedulerPoolFactory;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE=1;
    private static final String TAG = "MainActivity";
    private static final long SCAN_TIME_OUT = 5000;

    private BluetoothManager bluetoothManager=null;
    private BluetoothAdapter bluetoothAdapter=null;
    private BluetoothLeScanner bluetoothLeScanner=null;
    private List<BluetoothDevice> bluetoothDevices=null;

    private boolean isScaning=false;

    private ListView lv_ble_devices;
    private TextView startScan;
    private DeviceAdapter deviceAdapter;


    private BluetoothAdapter.LeScanCallback leScanCallback=null;

    private Handler handler=null;
    private Runnable scanRunable=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
        initEvents();
    }

    private void initEvents() {
        startScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isScaning)
                {
                    isScaning=true;
                    startScan.setText(getResources().getString(R.string.scaning));
                    startScan.setEnabled(false);
                    startScan();
                }
            }
        });

        lv_ble_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent deviceDetailIntent=new Intent(MainActivity.this,DeviceDetailActivity.class);
                deviceDetailIntent.putExtra("device",deviceAdapter.getDevice(i));
                startActivity(deviceDetailIntent);
            }
        });
    }

    private void initData() {
        bluetoothManager= (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter=bluetoothManager.getAdapter();
        bluetoothDevices=new ArrayList<>();

        //如果没有开启蓝牙，通过对话框提示用户开启蓝牙
        if (bluetoothAdapter==null||!bluetoothAdapter.isEnabled())
        {
            Intent enableBLEIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBLEIntent,REQUEST_ENABLE);
        }
        deviceAdapter=new DeviceAdapter(this,bluetoothDevices);

        //创建扫描监听回调
        leScanCallback=new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                Log.d(TAG, "onLeScan: 信号强度："+i);
                Log.d(TAG, "onLeScan: 广告数据："+new String(bytes));
                if (bluetoothDevice!=null&&bluetoothDevice.getName()!=null)
                {
                    bluetoothDevices.add(bluetoothDevice);
                }
            }
        };

        handler=new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what==0)
                {
                    startScan.setText(getResources().getString(R.string.startScan));
                    startScan.setEnabled(true);
                    deviceAdapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, "扫描完毕", Toast.LENGTH_SHORT).show();
                }
            }
        };

        scanRunable=new Runnable() {
            @Override
            public void run() {
                bluetoothAdapter.stopLeScan(leScanCallback);
                isScaning=false;
                Message message=new Message();
                message.what=0;
                handler.sendMessage(message);
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println(requestCode);
        System.out.println(resultCode);
    }

    private void initView() {
        lv_ble_devices = ((ListView) findViewById(R.id.lv_ble_devices));
        startScan = ((TextView) findViewById(R.id.tv_start_scan));
        lv_ble_devices.setAdapter(deviceAdapter);
    }

    private void startScan()
    {
        bluetoothDevices.clear();
        deviceAdapter.notifyDataSetChanged();
        handler.postDelayed(scanRunable,SCAN_TIME_OUT);
        bluetoothAdapter.startLeScan(leScanCallback);
    }
}
