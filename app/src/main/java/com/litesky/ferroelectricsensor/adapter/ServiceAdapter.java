package com.litesky.ferroelectricsensor.adapter;

import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.TextView;

import com.litesky.ferroelectricsensor.R;

import java.util.List;

/**
 * Created by finefine.com on 2017/10/7.
 */

public class ServiceAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private List<BluetoothGattService> mList;
    private LayoutInflater mInflater;

    public ServiceAdapter(Context mContext, List<BluetoothGattService> mList) {
        this.mContext = mContext;
        this.mList = mList;
        mInflater=LayoutInflater.from(mContext);
    }

    @Override
    public int getGroupCount() {
        if (mList!=null)
        {
            System.out.println("mLsize"+mList.size());
            return mList.size();
        }else
        {
            return 0;
        }
    }

    @Override
    public int getChildrenCount(int i) {
        if (mList.get(i)!=null)
        {
            System.out.println(mList.get(i).getCharacteristics().size());
            return mList.get(i).getCharacteristics().size();
        }
        else
        {
            return 0;
        }
    }

    @Override
    public Object getGroup(int i) {
        return mList.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return mList.get(i).getCharacteristics().get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        view=mInflater.inflate(R.layout.item_devicedetail_service,null);
        TextView textView=view.findViewById(R.id.tv_uuid_item_device_service);
        textView.setText(mList.get(i).getUuid().toString());
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        view=mInflater.inflate(R.layout.item_devicedetail_characteristic,null);
        TextView textView=view.findViewById(R.id.tv_uuid_item_characteristic);
        textView.setText(mList.get(i).getCharacteristics().get(i1).getUuid().toString());
        view.setTag(mList.get(i).getIncludedServices().get(i1));
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
