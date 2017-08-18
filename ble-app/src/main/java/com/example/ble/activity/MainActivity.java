package com.example.ble.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ble.controller.BluetoothController;
import com.example.ble.service.BLEService;
import com.example.ble.utils.BroadcastUtils;
import com.example.ble.utils.ConfigUtils;
import com.example.ble.utils.ConstantUtils;
import com.example.ble.utils.ConvertUtils;
import com.example.ble.utils.InstructionsUtils;
import com.example.ble.R;
import com.example.ble.utils.LogUtils;
import com.example.ble.utils.PermissionUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private MsgReceiver msgReceiver = new MsgReceiver();
    private Button btnConnect, btnSetspeed10, btnSetspeed20;
    private TextView tvConfig, tvBattery, tvSpeed;
    private Switch btnSwitch;
    private SeekBar sbLight;
    private Intent intentService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        initView();
        initListener();
        initData();
        initBLE();
        initBroadcast();
        initLight();//初始化灯光模块
        initSpeed();//初始化速度模块
    }

    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    protected void initView() {
        btnConnect = (Button) findViewById(R.id.btn_connect);
        tvConfig = (TextView) findViewById(R.id.tv_config);
        tvBattery = (TextView) findViewById(R.id.tv_battery);
        tvSpeed = (TextView) findViewById(R.id.tv_speed);
        btnSetspeed10 = (Button) findViewById(R.id.btn_setspeed10);
        btnSetspeed20 = (Button) findViewById(R.id.btn_setspeed20);
        btnSwitch = (Switch) findViewById(R.id.btn_switch);
        sbLight = (SeekBar) findViewById(R.id.sb_light);
    }
    protected void initListener() {
        btnConnect.setOnClickListener(this);
        tvConfig.setOnClickListener(this);
        tvBattery.setOnClickListener(this);
        tvSpeed.setOnClickListener(this);
        btnSetspeed10.setOnClickListener(this);
        btnSetspeed20.setOnClickListener(this);
        btnSwitch.setOnClickListener(this);
        sbLight.setOnClickListener(this);
    }
    protected void initData(){
        if (ConfigUtils.getInstance().getName().isEmpty() || ConfigUtils.getInstance().getAddress().isEmpty()) {
            tvConfig.setText("手机本地没有蓝牙配置信息");
        } else {
            tvConfig.setText(ConfigUtils.getInstance().getName()+","+ConfigUtils.getInstance().getAddress()
                    +","+ConfigUtils.getInstance().getClr()+","+ConfigUtils.getInstance().getSpeedMax());
        }
    }
    protected void initBLE() {
        //初始化蓝牙，建议放在App第一个Activity中
        BluetoothController.getInstance().initBLE(this);
        if (!BluetoothController.getInstance().isBleOpen() && !ConfigUtils.getInstance().getAddress().isEmpty()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1);
        } else {
            //开始服务
            LogUtils.e("开始服务");
            intentService = new Intent(MainActivity.this, BLEService.class);
            startService(intentService);
        }
    }
    protected void initBroadcast() {
        String[] actionArray = {
                ConstantUtils.ACTION_CONECTED_STATE_CHANGE_SUCCESS,//蓝牙连接状态改变广播
                ConstantUtils.ACTION_CONECTED_STATE_CHANGE_FAILURE,
                ConstantUtils.ACTION_CONECTED_STATE_CHANGE_DISCONNECT,
                ConstantUtils.ACTION_UPDATE_POWER,//更新电量广播
                ConstantUtils.ACTION_UPDATE_SPEED//更新速度广播
        };
        BroadcastUtils.getInstance().registerSystemBroadcast(msgReceiver, actionArray);//注册广播
    }

    /** =============================== 灯光模块 =============================== */
    private int colorsff[] = new int[]{
            Color.argb(255, 255, 0, 0),//纯红
            Color.argb(255, 255, 22, 145),//桃红
            Color.argb(255, 255, 0, 255),//纯紫
            Color.argb(255, 207, 24, 255),//深紫
            Color.argb(255, 0, 0, 255),//纯蓝
            Color.argb(255, 18, 180, 255),//淡蓝
            Color.argb(255, 6, 255, 246),//青色
            Color.argb(255, 5, 255, 176),//淡绿
            Color.argb(255, 0, 255, 0),//纯绿
            Color.argb(255, 0, 255, 0),//纯绿
            Color.argb(255, 255, 255, 0),//纯黄
            Color.argb(255, 25, 127, 0),//淡橙
            Color.argb(255, 255, 76, 0)//橙色
    };
    private boolean on = false;//安装时默认是关灯状态，以后打开记录上次状态
    private int preIndex = 1;//拖动过程中如果还是同一种颜色就不再发指令，初始化值不能是滑块初始化的位置
    private int index;

    private void initLight() {
        //恢复
        index = ConfigUtils.getInstance().getClr();
        if (index >= 0 && index < colorsff.length) {
            Message msg = new Message();
            msg.what = 1;
            msg.arg1 = index;
            handlerSelf.sendMessage(msg);
        } else if (index == 13) {
            on = false;
        }
        if (BluetoothController.getInstance().isConnected())
            sendColorInstruction(index);
        //灯光开关
        btnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    on = false;//开启状态
                    if (BluetoothController.getInstance().isConnected()) {
                        sendColorInstruction(preIndex);
                        saveColorConfig(preIndex);
                    }
                } else {
                    on = true;//关闭状态
                    if (BluetoothController.getInstance().isConnected()) {
                        sendColorInstruction(13);
                        saveColorConfig(13);
                    }
                }
            }
        });
        //设置灯光
        sbLight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                index = i / 8;
                if (BluetoothController.getInstance().isConnected() && index != preIndex && !on) {
                    sendColorInstruction(index);
                }
                saveColorConfig(index);
                preIndex = index;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /**
     * 发送颜色指令
     */
    private void sendColor(int index) {
        InstructionsUtils.getInstance().setLightInstructions(index);//发送消息
        BluetoothController.getInstance().write(ConvertUtils.getInstance().hexStringToBytes(InstructionsUtils.getInstance().getLightInstructions()));
    }
    private void sendColorInstruction(int index) {
        if (index >= 0 && index < colorsff.length) {
            sendColor(colorsff[index]);
        } else if (index == 13) {
            sendColor(Color.BLACK);
        }
    }

    private void saveColorConfig(int index) {
        if (index >= 0 && index <= 13) {
            ConfigUtils.getInstance().setClr(index);
            ConfigUtils.getInstance().saveConfig();
        }
    }

    public Handler handlerSelf = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case -1:
                    BluetoothController.getInstance().disconnect();
                    btnConnect.setText(getResources().getString(R.string.search_ble));
                    Toast.makeText(MainActivity.this, "蓝牙已断开连接.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /** =============================== 速度模块 =============================== */

    private int preSpeed = 6;//原先的速度

    /**
     * 初始化速度模块
     */
    private void initSpeed() {
        sendSpeed(ConfigUtils.getInstance().getSpeedMax());
    }

    /**
     * 发送速度指令
     */
    private void sendSpeed(int speed) {
        if (speed > 0) {
            preSpeed = speed;
            if (BluetoothController.getInstance().isConnected()) {//如果连接着串口发送指定
                //发送指令
                BluetoothController sender = BluetoothController.getInstance();
                ConvertUtils converter = ConvertUtils.getInstance();
                InstructionsUtils instruction = InstructionsUtils.getInstance();
                switch (speed) {
                    case 6://6km/h
                        instruction.setMaxSpeedInstructions(1);
                        sender.write(converter.hexStringToBytes(instruction.getMaxSpeedInstructions()));//发指令给串口
                        break;
                    case 10://10km/h
                        instruction.setMaxSpeedInstructions(2);
                        sender.write(converter.hexStringToBytes(instruction.getMaxSpeedInstructions()));
                        break;
                    case 15://15km/h
                        instruction.setMaxSpeedInstructions(3);
                        sender.write(converter.hexStringToBytes(instruction.getMaxSpeedInstructions()));
                        break;
                    case 20://20km/h
                        sender.write(converter.hexStringToBytes(instruction.getMaxSpeed20()));
                        break;
                    default:
                        break;
                }
            } else {
                Log.e("JAVA", "蓝牙未连接");
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //查找设备
            case R.id.btn_connect:
                //安卓6.0以上需要申请蓝牙权限
                PermissionUtils.getInstance().requestPermissions(MainActivity.this, 101,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        new PermissionUtils.OnPermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                if (!BluetoothController.getInstance().isConnected()) {//如果还未连接
                                    if (!BluetoothController.getInstance().isBleOpen()) {
                                        Toast.makeText(getApplicationContext(), R.string.bluetooth_close, Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    startActivity(new Intent(MainActivity.this, BLEActivity.class));
                                } else {
                                    //确定断开设备 提示框
                                    new AlertDialog.Builder(MainActivity.this).setTitle("")
                                            .setMessage(R.string.confirm_to_disconnect)
                                            .setNegativeButton("取消", null)
                                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (BluetoothController.getInstance().isConnected()) {
                                                        ConfigUtils.getInstance().setName("");
                                                        ConfigUtils.getInstance().setAddress("");
                                                        ConfigUtils.getInstance().saveConfig();
                                                        Message msg = handlerSelf.obtainMessage();//同 new Message();
                                                        msg.what = -1;
                                                        handlerSelf.sendMessage(msg);
                                                    }
                                                }
                                            }).show();
                                }
                            }
                            @Override
                            public void onPermissionDenied(String[] deniedPermissions) {
                            }
                        });
                break;
            //设置速度为10
            case R.id.btn_setspeed10:
                ConfigUtils.getInstance().setSpeedMax(10).saveConfig();
                sendSpeed(6);
                break;
            //设置速度为20
            case R.id.btn_setspeed20:
                ConfigUtils.getInstance().setSpeedMax(20).saveConfig();
                sendSpeed(20);
                break;
            default:
                break;
        }
    }

    /**
     * 广播接收器
     */
    public class MsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                //更新电量广播
                case ConstantUtils.ACTION_UPDATE_POWER:
                    int power = intent.getIntExtra("power", 100);
                    tvBattery.setText("设备电量：" + power + "%");
                    break;
                //更新充电中广播
                case ConstantUtils.ACTION_CHARGE_POWER:
                    tvBattery.setText("设备电量：充电中");
                    break;
                //更新速度广播
                case ConstantUtils.ACTION_UPDATE_SPEED:
                    double speed = intent.getDoubleExtra("speed", 0);
                    Log.e("JAVA", "speed："+speed);
                    tvSpeed.setText("设备速度：" + speed);
                    break;
                /*蓝牙连接状态改变广播*/
                //连接成功
                case ConstantUtils.ACTION_CONECTED_STATE_CHANGE_SUCCESS:
                    ConfigUtils.getInstance()
                            .setName(intent.getStringExtra("name"))
                            .setAddress(intent.getStringExtra("address"))
                            .saveConfig();
                    //发送注册信息
                    BluetoothController.getInstance().write(ConvertUtils.getInstance()
                            .hexStringToBytes(InstructionsUtils.getInstance().getRegisterInstructions()));
                    if (BluetoothController.getInstance().isConnected()) {//已连接
                        btnConnect.setText(getResources().getString(R.string.connected));
                        initData();
                    }
                    break;
                //连接失败或者异常断开
                case ConstantUtils.ACTION_CONECTED_STATE_CHANGE_FAILURE:
                    ConfigUtils.getInstance()
                            .setName(intent.getStringExtra("name"))
                            .setAddress(intent.getStringExtra("address"))
                            .saveConfig();
                    LogUtils.e("蓝牙"+intent.getStringExtra("address")+","
                            +intent.getStringExtra("name")+","+"连接失败或者异常断开");
                    intent.getStringExtra("name");
                    //搜索蓝牙
                    BLEService.setStartService();
                    startService(intentService);
                    btnConnect.setText(getResources().getString(R.string.search_ble));
                    initData();
                    break;
                //手动断开连接
                case ConstantUtils.ACTION_CONECTED_STATE_CHANGE_DISCONNECT:
                    ConfigUtils.getInstance()
                            .setName("")
                            .setAddress("")
                            .saveConfig();
                    LogUtils.e("hello"+intent.getStringExtra("address")+","+intent.getStringExtra("name"));
                    intent.getStringExtra("name");
                    btnConnect.setText(getResources().getString(R.string.search_ble));
                    initData();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionUtils.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        BroadcastUtils.getInstance().unRegisterSystemBroadcast(msgReceiver);//解绑广播
    }
}