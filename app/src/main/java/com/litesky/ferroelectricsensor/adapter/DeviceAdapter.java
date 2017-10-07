package com.litesky.ferroelectricsensor.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.litesky.ferroelectricsensor.R;

import java.util.List;

/**
 * Created by finefine.com on 2017/10/7.
 */

public class DeviceAdapter extends BaseAdapter {
    private List<BluetoothDevice> devices;
    private Context mContext;
    private LayoutInflater mInflater;

    public DeviceAdapter(Context mContext,List<BluetoothDevice> devices) {
        this.devices = devices;
        this.mContext = mContext;
        mInflater=LayoutInflater.from(mContext);
    }

    public BluetoothDevice getDevice(int position)
    {
        return devices.get(position);
    }

    @Override
    public int getCount() {
        if (devices == null) {
            return 0;
        }
        else
        {
            return devices.size();
        }
    }



    @Override
    public Object getItem(int i) {
        return devices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        DeviceViewHolder deviceViewHolder=null;
        BluetoothDevice bluetoothDevice=devices.get(i);

        if (view==null)
        {
            view=mInflater.inflate(R.layout.item_device,null);
            deviceViewHolder=new DeviceViewHolder(view);
            view.setTag(deviceViewHolder);
        }else
        {
            deviceViewHolder= (DeviceViewHolder) view.getTag();
        }
        deviceViewHolder.tv_name.setText("设备名："+bluetoothDevice.getName());
        if (bluetoothDevice.getUuids()!=null&&bluetoothDevice.getUuids().length>0)
        deviceViewHolder.tv_uuid.setText("uuid："+(bluetoothDevice.getUuids())[0].getUuid());
        deviceViewHolder.tv_address.setText("MAC地址："+bluetoothDevice.getAddress());
        return view;
    }

    class DeviceViewHolder
    {
        private View itemView;
        TextView tv_name;
        TextView tv_uuid;
        TextView tv_address;

        public DeviceViewHolder(View itemView) {
            this.itemView = itemView;
            tv_name=itemView.findViewById(R.id.tv_name_item_device);
            tv_uuid=itemView.findViewById(R.id.tv_uuid_item_device);
            tv_address=itemView.findViewById(R.id.tv_address_item_device);
        }
    }

}
