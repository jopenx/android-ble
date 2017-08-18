package com.example.ble.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.ble.R;
import com.example.ble.adapter.BLEDeviceListAdapter;
import com.example.ble.controller.BluetoothController;
import com.example.ble.utils.BroadcastUtils;
import com.example.ble.utils.ConfigUtils;
import com.example.ble.utils.ConstantUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 搜寻并连接蓝牙
 */
public class BLEActivity extends AppCompatActivity {

    private ListView bleList;
    private ProgressBar progressBar;
    private SwipeRefreshLayout refreshLayout;
    private ArrayList<HashMap<String, Object>> listBLEArrayData = new ArrayList<>();
    private BLEDeviceListAdapter bleDeviceAdapter = null;
    private MsgReceiver msgReceiver = new MsgReceiver();
    private String name;
    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        initToolbar();
        initView();
        initListener();
        initData();
        initBroadcast();
    }

    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void initView() {
        bleList = (ListView) findViewById(R.id.list_ble);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeResources(R.color.swipe_color_1);//设置进度动画的颜色
        refreshLayout.setSize(SwipeRefreshLayout.DEFAULT);//设置进度圈的大小,只有两个值:DEFAULT、LARGE
        refreshLayout.setProgressViewEndTarget(true, 300);//true:下拉过程会自动缩放,200:下拉刷新的高度
    }
    private void initListener() {
        // 设置手势滑动监听
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 下拉刷新操作
                getData();
            }
        });
        // ListView条目点击事件
        bleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String deviceAddress = listBLEArrayData.get(position).get("device_address").toString();
                String deviceName = listBLEArrayData.get(position).get("device_name").toString();
                Log.i("JAVA","deviceAddress="+deviceAddress+",deviceName="+deviceName);

                progressBar.setVisibility(View.VISIBLE);//开始加载动画
                refreshLayout.setEnabled(false);//禁止下拉刷新
                bleList.setEnabled(false);//禁止点击
                BluetoothController.getInstance().connect(
                        (String) listBLEArrayData.get(position).get("device_address"),
                        (String) listBLEArrayData.get(position).get("device_name"));
            }
        });
    }
    private void initData() {
        // 进入页面就执行下拉动画
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                getData();
            }
        });
        bleDeviceAdapter = new BLEDeviceListAdapter(bleList, listBLEArrayData, R.layout.list_ble);
        bleList.setAdapter(bleDeviceAdapter);
    }
    private void initBroadcast() {
        //注册广播
        String[] actionArray = {ConstantUtils.ACTION_CONECTED_STATE_CHANGE_SUCCESS};
        BroadcastUtils.getInstance().registerSystemBroadcast(msgReceiver, actionArray);
        startSearchBLE();
    }
    private void getData() {
        new GetDataTask().execute();
    }

    /**
     * 开始扫描BLE
     */
    public void startSearchBLE(){
        BluetoothController.getInstance().startLeScan(new BluetoothAdapter.LeScanCallback(){
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if (device.getName() == null || device.getAddress() == null) {
                        return;
                    }
                    name = device.getName();
                    address = device.getAddress();
                    HashMap<String, Object> obj = new HashMap<>();
                    obj.put("device_name", name);
                    obj.put("device_address", address);

                    if (listBLEArrayData.contains(obj)) {
                        return;
                    }
                    listBLEArrayData.add(obj);
                    bleDeviceAdapter.notifyDataSetChanged();
                }
            });
    }

    /**
     * 停止扫描BLE
     */
    public void stopSearchBLE(){
        BluetoothController.getInstance().stopLeScan(new BluetoothAdapter.LeScanCallback(){
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            }
        });
    }

    /**
     * 广播接收器
     */
    public class MsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                //蓝牙连接状态改变广播, 连接成功
                case ConstantUtils.ACTION_CONECTED_STATE_CHANGE_SUCCESS:
                    if (BluetoothController.getInstance().isConnected()) {//已连接
                        ConfigUtils.getInstance().setName(name);
                        ConfigUtils.getInstance().setAddress(address);
                        ConfigUtils.getInstance().saveConfig();
                        finish();
                    }
                    break;
            }
        }
    }

    /**
     * 异步加载获取数据
     * 系统异步加载框架AsycnTask三个参数：提高兼容性
     * 参数1：子线程执行所需的参数
     * 参数2：显示当前的加载进度
     * 参数3：子线程执行的结果
     */
    private class GetDataTask extends AsyncTask<Void, Void, String[]> {
        //在子线程之前执行的操作
        @Override
        protected void onPreExecute() {
            stopSearchBLE();
            super.onPreExecute();
        }
        //在子线程之中执行的操作
        @Override
        protected String[] doInBackground(Void... params) {
            startSearchBLE();
            return null;
        }
        //在子线程之后执行的操作
        @Override
        protected void onPostExecute(String[] result) {
            // 请求完成结束刷新状态
            refreshLayout.setRefreshing(false);
            super.onPostExecute(result);
        }
        //显示当前加载进度
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSearchBLE();
        progressBar.setVisibility(View.GONE);//结束加载动画
        refreshLayout.setEnabled(true);//开启下拉刷新
        bleList.setEnabled(true);//开启点击
        BroadcastUtils.getInstance().unRegisterSystemBroadcast(msgReceiver);//解绑广播
    }
}