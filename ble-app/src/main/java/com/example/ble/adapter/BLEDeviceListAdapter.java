package com.example.ble.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ble.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 蓝牙设备列表适配器
 */
public class BLEDeviceListAdapter extends BaseAdapter {
    private ArrayList<HashMap<String, Object>> dataList;
    private int layout_id=-1;
    private Context context;
    private LayoutInflater layoutInflater;
    private ViewHolder viewHolder;

    public BLEDeviceListAdapter(ListView listView, ArrayList<HashMap<String, Object>> dataList, int layout_id) {
        this.dataList=dataList;
        this.layout_id=layout_id;
        this.context=listView.getContext();
        this.layoutInflater=LayoutInflater.from(context);
        viewHolder = new ViewHolder();
    }

    @Override
    public int getCount() {
        return dataList==null?0:dataList.size();
    }

    @Override
    public HashMap<String, Object> getItem(int i) {
        if(i>(getCount()-1)||getCount()==0)
            return null;
        return dataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(null==view){
            view=layoutInflater.inflate(layout_id,null);
            onInitView(view, i,false);
        } else {
            onInitView(view, i,true);
        }
        return view;
    }

    public void onInitView(View view, int position, boolean isDataExit) {
        if (!isDataExit) {//试图已经存在
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
        }
        HashMap<String, Object> obj = getItem(position);
        viewHolder.deviceName.setText((String) obj.get("device_name"));
        viewHolder.deviceAddress.setText((String) obj.get("device_address"));
    }
    class ViewHolder {
        public TextView deviceName, deviceAddress;
    }

}
