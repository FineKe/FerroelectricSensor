package com.litesky.ferroelectricsensor;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.litesky.ferroelectricsensor.adapter.PresenterDataActivity;
import com.litesky.ferroelectricsensor.adapter.ServiceAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeviceDetailActivity extends AppCompatActivity {

    private static final String TAG = "DeviceDetailActivity";
    private static final String SERVICE_UUID="";
    private static final String CHARACTERISTIC_UUID="";
    private BluetoothDevice bluetoothDevice=null;
    private BluetoothGattCallback bluetoothGattCallback=null;
    private BluetoothGatt bluetoothGatt=null;

    private BluetoothGattService gattService=null;
    private LineChart lineChart;
    private TextView startListen;


    private List<Entry> entries;
    private LineDataSet lineDataSet;
    private LineData lineData;

    private XAxis xAxis;
    private Runnable updateDataRunnable=null;
    private BluetoothGattCharacteristic dataCharacteristic=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);
        initData();
        initViews();
    }

    private void initViews() {
        lineChart = ((LineChart) findViewById(R.id.lineChart));
        startListen = ((TextView) findViewById(R.id.start_listen));
        startListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setListen();
            }
        });

        lineChart.setData(lineData);
        lineChart.setDragDecelerationEnabled(true);//设置触摸后继续滑动
        lineChart.setDragDecelerationFrictionCoef(0.999f);//滑动系数 ，0立即停止，0.999最快的
//        lineChart.getXAxis().setEnabled(false);//设置不显示轴
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);//设置x轴的位置
        lineChart.setAutoScaleMinMaxEnabled(false);
        xAxis=lineChart.getXAxis();
        YAxis l=lineChart.getAxisLeft();
        YAxis r=lineChart.getAxisRight();
        lineChart.setScrollContainer(true);
        lineChart.setPinchZoom(false);
        l.setStartAtZero(false);
        lineChart.setAutoScaleMinMaxEnabled(false);
        lineChart.setMaxVisibleValueCount(10);

        r.setEnabled(false);
        l.setDrawZeroLine(true);
        l.setZeroLineColor(Color.RED);
        l.setGranularityEnabled(true);

        xAxis.setGranularityEnabled(true);
//        xAxis.setGranularity(1);
        xAxis.setGranularity(0.1f);
        lineChart.zoom(2.5f,1f,0,0);
        xAxis.setLabelCount(100,true);
        xAxis.setEnabled(false);
    }

    private void initData() {
        bluetoothDevice=getIntent().getParcelableExtra("device");
        bluetoothGattCallback=new BluetoothGattCallback() {
            @Override
            public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                super.onPhyUpdate(gatt, txPhy, rxPhy, status);
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (status== BluetoothGatt.GATT_SUCCESS)
                {
                    Log.d(TAG, "onConnectionStateChange:连接成功");
                }
                if (newState== BluetoothProfile.STATE_CONNECTED)
                {
                    Log.d(TAG, "onConnectionStateChange: 设备已连接");

                    //扫描service
                    gatt.discoverServices();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if (status==BluetoothGatt.GATT_SUCCESS)
                {
                    Log.d(TAG, "onServicesDiscovered: 已发现服务");
                    gattService=gatt.getService(UUID.fromString(SERVICE_UUID));
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                if (status==BluetoothGatt.GATT_SUCCESS)
                {
                    Log.d(TAG, "onCharacteristicRead: 有新数据");
                    dataCharacteristic=characteristic;
                    handler.sendEmptyMessage(0);
                }

            }
        };

        bluetoothDevice.connectGatt(this,false,bluetoothGattCallback);
        entries=new ArrayList<>();
        lineDataSet=new LineDataSet(entries,"label");
        lineData=new LineData(lineDataSet);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //断开连接
        if (bluetoothGatt!=null)
        {
            bluetoothGatt.disconnect();
        }

    }

    public void setListen()
    {
        BluetoothGattCharacteristic characteristic=gattService.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID));
        bluetoothGatt.setCharacteristicNotification(characteristic,true);
        BluetoothGattDescriptor descriptor=characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
    }

    private Handler handler=new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case 0:up(lineData,xAxis,dataCharacteristic);
                    break;
            }
        }
    };

    public void up(LineData lineData,XAxis xAxis,BluetoothGattCharacteristic characteristic)
    {
        float x=characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT,2);
        Entry entry=new Entry(x*1.0f,(float)(Math.random()*10.0));
        x++;
        lineData.addEntry(entry,0);
        lineChart.notifyDataSetChanged();
        System.out.println(x);
//                lineChart.moveViewToX(lineData.getXMax()-1);
        lineChart.centerViewTo(1,0, YAxis.AxisDependency.RIGHT);
//        a++;
//        xAxis.setAxisMinValue(a);
    }

}
