package com.witmoiton.example;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.witmoiton.example.pojo.BLEDevice;
import com.witmoiton.example.prog.OffLineStep;
import com.witmoiton.example.prog.Step;
import com.witmoiton.example.utils.BLEUtils;
import com.witmoiton.example.utils.HexUtil;
import com.witmoiton.example.utils.TypeConversion;
import com.witmoiton.example.permission.PermissionListener;
import com.witmoiton.example.permission.PermissionRequest;
import com.witmoiton.example.utils.ToastUtils;
import com.witmoiton.example.sensor.SensorThread;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;


/**
 * 作者：witmotion on 2021/08/6 17:47
 * 程序的主页面
 * 作用：连接传感器并且解析传感器的数据，解析后显示在首页面上
 * <p>
 * <p>
 * 实现连接蓝牙传感器的步骤
 * 1检查手机是否支持低功耗蓝牙
 * (0.0) 4.0或以上的蓝牙都是低功耗蓝牙，部分老安卓手机不支持低功耗蓝牙所以要检查
 * 2初始化蓝牙
 * (1)申请权限
 * (2)打开蓝牙
 * 3扫描蓝牙设备
 * (1)扫描时只找WT开头的蓝牙名称，因为所有的传感器开头都是 WT
 * 4连接蓝牙设备
 * (1)连接蓝牙时要注意蓝牙的UUID，UUID在下面的代码中
 * 5解析传感器数据
 * (1)解析代码也在这个文件中
 */
public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {


    public Handler mainHandler;
    //校准时间
    private Button mButton;
    //获取设备电量
    private Button mSendBtn;
    //断开设备
    private Button disconnectDevice;
    private TextView mTextView;
    private TextView viewByIdMy;
    private SensorThread thread;
    private boolean isSensorReady = false;
    private Map load_data = new HashMap();
    private String allText = "";
    private int countNum = 0;
    private String actName = "K17";
    private int injuredSegment = 2;
    private SensorThread sthread;


    /**
     * 页面创建时
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;

        //申请权限
        initPermissions();

        //初始化视图
        InitRecyclerView();

        //初始化蓝牙
        InitBle();

        //开始搜索事件
        Button button = findViewById(R.id.ScanBLE);
        button.setOnClickListener(StartScanClick);

        getBasedata();
        mButton = (Button) findViewById(R.id.button);
        mTextView = (TextView) findViewById(R.id.textview);
        viewByIdMy = findViewById(R.id.MainActivityMsgTextView);
        mSendBtn = findViewById(R.id.button1);

        //获取设备电量
        mSendBtn.setOnClickListener(v -> {
            if (gattGroup == null) return;
            boolean isWrite = bleUtils.writeRXCharacteristic(gattGroup, UUID_SERVICE, UUID_WRITE, ELECTRIC_QUANTITY);
            if (isWrite) {
                Log.i(TAG, "onServicesDiscovered: 数据写入成功");
            } else {
                Log.i(TAG, "onServicesDiscovered: 数据写入失败");
            }
        });

        //获取设备实时数据
        findViewById(R.id.button2).setOnClickListener(v -> {
            if (gattGroup == null) return;
            boolean isWrite = bleUtils.writeRXCharacteristic(gattGroup, UUID_SERVICE, UUID_WRITE, EQUIPMENT_STATUS);
            if (isWrite) {
                Log.i(TAG, "onServicesDiscovered: 获取实时数据====数据写入成功");
            } else {
                Log.i(TAG, "onServicesDiscovered: 获取实时数据====数据写入失败");
            }
        });

        //停止获取设备实时状态
        findViewById(R.id.button3).setOnClickListener(v -> {
            if (gattGroup == null) return;
            boolean isWrite = bleUtils.writeRXCharacteristic(gattGroup, UUID_SERVICE, UUID_WRITE, STOP_EQUIPMENT_STATUS);
            if (isWrite) {
                Log.i(TAG, "onServicesDiscovered: 停止获取实时数据====数据写入成功");
            } else {
                Log.i(TAG, "onServicesDiscovered: 停止获取实时数据====数据写入失败");
            }
        });
        //获取实时计步
        findViewById(R.id.button4).setOnClickListener(v -> {
            if (gattGroup == null) return;
            boolean isWrite = bleUtils.writeRXCharacteristic(gattGroup, UUID_SERVICE, UUID_WRITE, STEP_COUNTING);
            if (isWrite) {
                Log.i(TAG, "onServicesDiscovered: 获取实时计步====数据写入成功");
            } else {
                Log.i(TAG, "onServicesDiscovered: 获取实时计步====数据写入失败");
            }
        });

        //获取离线计步
        findViewById(R.id.button5).setOnClickListener(v -> {
            if (gattGroup == null) return;
            boolean isWrite = bleUtils.writeRXCharacteristic(gattGroup, UUID_SERVICE, UUID_WRITE, getOffLineStepCounting());
            Log.i(TAG, "数据写入：" + getOffLineStepCounting());
            if (isWrite) {
                offLineList.clear();
                Log.i(TAG, "onServicesDiscovered:获取离线计步==== 数据写入成功");
            } else {
                Log.i(TAG, "onServicesDiscovered:获取离线计步==== 数据写入失败");
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isWrite = bleUtils.writeRXCharacteristic(gattGroup, UUID_SERVICE, UUID_WRITE, getCalibrationTime());
                Log.i(TAG, "数据写入：" + getCalibrationTime());
                if (isWrite) {
                    Log.i(TAG, "onServicesDiscovered: 时间校准======数据写入成功");
                } else {
                    Log.i(TAG, "onServicesDiscovered: 时间校准======数据写入失败");
                }

//                String buttonText = mButton.getText().toString();
//
//                if (buttonText.contains("伸膝活动")) {
//                    Message msg1 = thread.sensorAIHandler.obtainMessage(1021);
//                    msg1.obj = actName;
//                    msg1.arg2 = injuredSegment;
//                    thread.sensorAIHandler.sendMessage(msg1);
//                    Log.d(TAG, " 伸膝活动文字:发送标题头");
//                    Message msg2 = thread.sensorAIHandler.obtainMessage(1022);
////                       JSONObject json = new JSONObject(load_data);
//                    msg2.obj = load_data;
////                       msg2.obj = json;
//                    thread.sensorAIHandler.sendMessage(msg2);
//                    mButton.setText("请先连接蓝牙设备");
//                } else if (buttonText.contains("抱腿打弯")) {
//                    Message msg1 = thread.sensorAIHandler.obtainMessage(1021);
//                    msg1.obj = actName;
//                    msg1.arg2 = injuredSegment;
//                    thread.sensorAIHandler.sendMessage(msg1);
//                    Log.d(TAG, " 抱腿打弯文字:发送标题头");
//                    Message msg2 = thread.sensorAIHandler.obtainMessage(1022);
////                       JSONObject json = new JSONObject(load_data);
//                    msg2.obj = load_data;
////                       msg2.obj = json;
////                       msg2.obj = load_data;
//                    thread.sensorAIHandler.sendMessage(msg2);
//                    mButton.setText("请先连接蓝牙设备");
//                } else if (buttonText.contains("开始训练")) {
//                    Message msg1 = thread.sensorAIHandler.obtainMessage(1024);
//                    thread.sensorAIHandler.sendMessage(msg1);
//                    mButton.setText("暂停训练");
//                } else if (buttonText.contains("暂停训练")) {
//                    Message msg1 = thread.sensorAIHandler.obtainMessage(1025);
//                    thread.sensorAIHandler.sendMessage(msg1);
//                    mButton.setText("继续训练");
//                } else if (buttonText.contains("继续训练")) {
//                    Message msg1 = thread.sensorAIHandler.obtainMessage(1026);
//                    thread.sensorAIHandler.sendMessage(msg1);
//                    mButton.setText("停止训练");
//                } else if (buttonText.contains("停止训练")) {
//                    Message msg1 = thread.sensorAIHandler.obtainMessage(1027);
//                    thread.sensorAIHandler.sendMessage(msg1);
//                    mButton.setText("开始训练");
//                }
            }
        });
        /**
         * 断开设备
         */
        disconnectDevice=findViewById(R.id.button6);
        disconnectDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gattGroup == null) return;
                boolean isWrite = bleUtils.writeRXCharacteristic(gattGroup, UUID_SERVICE, UUID_WRITE, disConnectDeviceCommand(connectedDevice).replace(":",""));
                if (isWrite) {
                    Log.i(TAG, "onServicesDiscovered: 断开设备==========数据写入成功");
                } else {
                    Log.i(TAG, "onServicesDiscovered: 断开设备==========数据写入失败");
                }
            }
        });
        mainHandler = new MainHandler();
        thread = new SensorThread(mainHandler);
        sthread = thread;
        thread.start();


    }


    //日志标签
    private static final String TAG = "MainActivity";
    //蓝牙设备
    private ArrayList<BLEDevice> bleDevices = new ArrayList<>();
    //设备地址列表
    private List<String> macList = new ArrayList<>();
    //蓝牙辅助类
    private BLEUtils bleUtils = new BLEUtils();
    //本页面
    private Context mContext;

    BluetoothGatt gattGroup;
    //当前连接的蓝牙传感器
//    private BluetoothGatt bluetoothGatt;


    // 声明一个集合，在后面的代码中用来存储用户拒绝授权的权限
    private List<String> deniedPermissionList = new ArrayList<>();
    //动态申请权限
    private String[] requestPermissionArray = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };


    /**
     * 这是传感器的服务UUID和读写UUID
     */
//    //蓝牙设备的服务的UUID
//    public final static UUID UUID_SERVICE = UUID.fromString("0000ffe5-0000-1000-8000-00805f9a34fb");
//    //蓝牙设备的读取数据的UUID
//    public final static UUID UUID_READ = UUID.fromString("0000ffe4-0000-1000-8000-00805f9a34fb");
//    //蓝牙设备的写入数据的UUID
//    public final static UUID UUID_WRITE = UUID.fromString("0000ffe9-0000-1000-8000-00805f9a34fb");
//    // 调整蓝牙传输速率为 20Hz
//    public final static String UUID_TRANSMISSION_RATE =  "ff aa 03 07 00";
//    public final static UUID UUID_SERVICE_NEW = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
//    //蓝牙设备的读取数据的UUID
//    public final static UUID UUID_READ_NEW = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");

    //蓝牙设备的服务的UUID
    public final static UUID UUID_SERVICE = UUID.fromString("00000001-0000-1000-8000-00805f9b34fb");
    //蓝牙设备的读取数据的UUID
    public final static UUID UUID_READ = UUID.fromString("00000003-0000-1000-8000-00805f9b34fb");
    //蓝牙设备的写入数据的UUID
    public final static UUID UUID_WRITE = UUID.fromString("00000002-0000-1000-8000-00805f9b34fb");

//    public final static UUID UUID_SERVICE_NEW = UUID.fromString("000000001-0000-1000-8000-00805f9b34fb");
//    //蓝牙设备的读取数据的UUID
//    public final static UUID UUID_READ_NEW = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");

    //获取当前年份
    private String getCurrentYear() {
        SimpleDateFormat sdf = new SimpleDateFormat("yy");
        Date date = new Date();
        return sdf.format(date);
    }

    //获取当前年月日份
    private String getCurrentMM() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM");
        Date date = new Date();
        return sdf.format(date);
    }

    private String getCurrentDD() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        Date date = new Date();
        return sdf.format(date);
    }

    private String getCurrentHH() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        Date date = new Date();
        return sdf.format(date);
    }

    private String getCurrentMinutes() {
        SimpleDateFormat sdf = new SimpleDateFormat("mm");
        Date date = new Date();
        return sdf.format(date);
    }

    private String getCurrentS() {
        SimpleDateFormat sdf = new SimpleDateFormat("ss");
        Date date = new Date();
        return sdf.format(date);
    }

    /**
     * 判断当前日期是星期几
     */
    int getWeekOfDate() {
        Date dt = new Date();
        int[] weekDays = new int[7];
        weekDays[0] = 7;
        weekDays[1] = 1;
        weekDays[2] = 2;
        weekDays[3] = 3;
        weekDays[4] = 4;
        weekDays[5] = 5;
        weekDays[6] = 6;
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return weekDays[w];
    }

    //获取校准时间指令
    String getCalibrationTime() {
        String data = "BCBD08A7" + HexUtil.hex10To16Dec(getCurrentYear()) + HexUtil.hex10To16Dec(getCurrentMM()) + HexUtil.hex10To16Dec(getCurrentDD())
                + HexUtil.hex10To16Dec(getCurrentHH()) + HexUtil.hex10To16Dec(getCurrentMinutes()) + HexUtil.hex10To16Dec(getCurrentS()) + HexUtil.hex10To16Dec(getWeekOfDate() + "")
                + "3010ACAD";
        return data;
    }

    /**
     * 获取离线计步指令
     *
     * @return
     */
    String getOffLineStepCounting() {
//        String data = "BCBD02A2" + HexUtil.hex10To16Dec(getCurrentYear()) + HexUtil.hex10To16Dec(getCurrentMM()) + HexUtil.hex10To16Dec(getCurrentDD())
//                +"0800ACAD";
        String data = "BCBD02A201ACAD";
        return data;
    }

    private String disConnectDeviceCommand(String mac){
        return "BCBD08A0"+mac+"00ACAD";
    }


    //获取电量
    public final static String ELECTRIC_QUANTITY = "BCBD06A400000001ACAD";

    //获取实时设备状态
    public final static String EQUIPMENT_STATUS = "BCBD06A600000001ACAD";

    //停止获取实时设备状态
    public final static String STOP_EQUIPMENT_STATUS = "BCBD06A600000000ACAD";

    //获取实时计步
    public final static String STEP_COUNTING = "BCBD06A500000001ACAD";

    //保存传感器的数据
    float[] floats = new float[12];


    /**
     * 初始化蓝牙
     */
    private void InitBle() {

        //初始化ble管理器
        if (!bleUtils.initBle(mContext)) {
            Log.d(TAG, "该设备不支持低功耗蓝牙");
            Toast.makeText(mContext, "该设备不支持低功耗蓝牙", Toast.LENGTH_SHORT).show();
        } else {
            if (!bleUtils.isEnable()) {
                //去打开蓝牙
                bleUtils.openBluetooth(mContext, false);
            }
        }


        if (bleUtils != null) {
            bleUtils.stopDiscoveryDevice();
        }
    }


    /**
     * 初始化权限
     */
    private void initPermissions() {
        //Android 6.0以上动态申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final PermissionRequest permissionRequest = new PermissionRequest();
            permissionRequest.requestRuntimePermission(MainActivity.this, requestPermissionArray, new PermissionListener() {
                @Override
                public void onGranted() {
                    Log.d(TAG, "所有权限已被授予");
                }

                //用户勾选“不再提醒”拒绝权限后，关闭程序再打开程序只进入该方法！
                @Override
                public void onDenied(List<String> deniedPermissions) {
                    deniedPermissionList = deniedPermissions;
                    for (String deniedPermission : deniedPermissionList) {
                        Log.e(TAG, "被拒绝权限：" + deniedPermission);
                    }
                }
            });
        }
    }


    /**
     * 初始化列表
     */
    private void InitRecyclerView() {
        RecyclerView mRv = findViewById(R.id.DeviceListRecyclerView);
        //线性布局
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRv.setLayoutManager(linearLayoutManager);
        RecyclerAdapter adapter = new RecyclerAdapter(this, bleDevices, this);
        mRv.setAdapter(adapter);
    }


    /**
     * 刷新列表
     */
    public void UpdateList() {
        InitRecyclerView();
    }


    /**
     * 刷新列表并且添加一条数据
     *
     * @param str
     */
    public void UpdateList(BLEDevice str) {
        bleDevices.add(str);
        InitRecyclerView();
        StringBuilder stringBuilder = new StringBuilder();
        for (BLEDevice item : bleDevices) {
            stringBuilder.append("\n" + item.getBluetoothDevice().getAddress());
        }

        Log.e("12314124234243", "list = " + stringBuilder.toString());
    }


    /**
     * 开始扫描实现类
     */
    private View.OnClickListener StartScanClick = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            if (bleUtils == null) {
                Log.d(TAG, "searchBtDevice()-->bleUtils == null");
                return;
            }

            if (bleUtils.isDiscovery()) { //当前正在搜索设备...
                bleUtils.stopDiscoveryDevice();
            }

//            //如果有当前连接就断开
//            if(bluetoothGatt != null){
//                bluetoothGatt.disconnect();
//            }

            //清空列表
            macList.clear();
            bleDevices.clear();
            UpdateList();


            //开始搜索
            bleUtils.startDiscoveryDevice(onDeviceSearchListener, 15000);
        }
    };


    /**
     * 扫描结果回调
     */
    private OnDeviceSearchListener onDeviceSearchListener = new OnDeviceSearchListener() {

        @Override
        public void onDeviceFound(BLEDevice bleDevice) {
            //如果找到了WT开头的蓝牙，并且没有添加到设备列表
            if (bleDevice.getBluetoothDevice().getName() != null && bleDevice.getBluetoothDevice().getName().contains("WY01")
                    && !macList.contains(bleDevice.getBluetoothDevice().getAddress())
                    && !bleDevice.getBluetoothDevice().getName().contains("WY01S")) {
//            if (bleDevice.getBluetoothDevice().getName() != null && bleDevice.getBluetoothDevice().getName().contains("WT")
//                    && !macList.contains(bleDevice.getBluetoothDevice().getAddress())){
                //加入Mac地址列表
                macList.add(bleDevice.getBluetoothDevice().getAddress());
                //更新到设备列表
                UpdateList(bleDevice);
                //提示信息
                ShowToast("一个设备被找到");
            }
            //如果找到了MD开头的蓝牙，并且没有添加到设备列表
//            if (bleDevice.getBluetoothDevice().getName() != null && bleDevice.getBluetoothDevice().getName().contains("MD") && !macList.contains(bleDevice.getBluetoothDevice().getAddress())) {
//                //加入Mac地址列表
//                macList.add(bleDevice.getBluetoothDevice().getAddress());
//                //更新到设备列表
//                UpdateList(bleDevice);
//                //提示信息
//                ShowToast("一个设备被找到");
//            }
        }


        @Override
        public void onDiscoveryOutTime() {


        }
    };


    /**
     * 打开蓝牙设备
     *
     * @param bleDevice
     */
    public int kg = 0;

    public void OpenBleDevive(BLEDevice bleDevice) {

        //如果有当前连接就断开
//        if(this.bluetoothGatt != null){
//            this.bluetoothGatt.disconnect();
//        }

        //增加传感器
        int sid;
        String bodySite;
        if (bleDevice.getBluetoothDevice().getAddress().contains("54:6C:0E:B9:1C:B4")) {
            sid = 1;
            if (actName.equals("K5") || actName.equals("K6")) {
                bodySite = "足";

            } else if (actName.equals("K11")) {
                bodySite = "小腿";
            } else {
                bodySite = "大腿";
            }
            kg = 1;
        } else if (bleDevice.getBluetoothDevice().getAddress().contains("54:6C:0E:B9:15:64")) {
            //CB:06:FD:DC:D3:41
            sid = 2;
            kg = 1;
            if (actName.equals("K5") || actName.equals("K6")) {
                bodySite = "小腿";
            } else if (actName.equals("K25") || actName.equals("K26") || actName.equals("K12") || actName.equals("K13")) {
                bodySite = "大腿";
            } else if (actName.equals("K11")) {
                bodySite = "小腿";
            } else if (actName.equals("K9")) {
                bodySite = "足";
            } else {
                bodySite = "小腿";
            }

        } else {
            sid = 0;
            bodySite = "";
        }
        if (sid != 0) {
            Message msg = thread.sensorAIHandler.obtainMessage(1011);
            msg.arg1 = sid;
            msg.obj = bodySite;
            thread.sensorAIHandler.sendMessage(msg);
        }


        //停止搜索
        bleUtils.stopDiscoveryDevice();
        SetMsg("开始连接设备");
        //连接蓝牙
        BluetoothGatt bluetoothGatt = bleDevice.getBluetoothDevice().connectGatt(this, false, bluetoothGattCallbackOne);
        bluetoothGatt.connect();
        bleDevice.setConnnected(true);

//        this.bluetoothGatt = bluetoothGatt;
    }

 private String connectedDevice;

    /**
     * 第一个设备  蓝牙返回数据函数
     */
    private BluetoothGattCallback bluetoothGattCallbackOne = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    connectedDevice=gatt.getDevice().getAddress();
                    Log.i("123214143142", "设备连接成功 ：" + gatt.getDevice().getAddress());
                    ShowToast("设备连接成功");
                    //搜索Service
                    gattGroup = gatt;
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    ShowToast("设备连接断开");
                }
            }
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            gattGroup = gatt;
            boolean isConnect;
            // 开启接收信息
            //if(kg == 0) {
            isConnect = bleUtils.enableNotification(gatt, UUID_SERVICE, UUID_READ);
//            }else{
//                isConnect = bleUtils.enableNotification(gatt, UUID_SERVICE_NEW, UUID_READ_NEW);
//            }
            if (isConnect) {
                Log.i("123214143142", "onServicesDiscovered ：" + gatt.getDevice().getAddress());
                Log.i(TAG, "onServicesDiscovered: 设备一连接notify成功");
                ParcelUuid[] uuids = gatt.getDevice().getUuids();
                gatt.getDevice().getUuids();
                //发送信号
                if (kg != 1) {
                    // boolean isWrite =   bleUtils.writeRXCharacteristic(gatt, UUID_SERVICE, UUID_WRITE, UUID_TRANSMISSION_RATE);
                    // Log.i(TAG, "onServicesDiscovered: 设备一连接notify失败");
                }
                Log.i(TAG, "onServicesDiscovered: 设备一连接notify成功 uuids = " + uuids + ", mac = " + gatt.getDevice().getAddress());

            } else {
                Log.i(TAG, "onServicesDiscovered: 设备一连接notify失败");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {//数据改变
            super.onCharacteristicChanged(gatt, characteristic);
            sendSensorData(gatt, characteristic);
        }
    };

    //用于存储离线计步数据
    List<BluetoothGattCharacteristic> offLineList = new ArrayList<BluetoothGattCharacteristic>();

    //更新传感器数据
    private void sendSensorData(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

        int sid;
        String str = HexUtil.formatHexString(characteristic.getValue(), true);
        Log.i("new  sensor  data", "onCharacteristicChanged ：" + str);


        if (str.contains("bc bd 0a a4")) {
            //电量
            byte[] stepByte = new byte[1];
            stepByte[0] = characteristic.getValue()[characteristic.getValue().length - 6];
            String aa =  "电量： " + HexUtil.formatHexString(stepByte);
//            String newStr = "电量： " + characteristic.getValue()[characteristic.getValue().length - 6];

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextView.setText(aa);
                    Toast.makeText(MainActivity.this,"电量"+aa,Toast.LENGTH_LONG).show();
                }
            });

        } else if (str.contains("bc bd 07 a5")) {
            byte[] stepByte = new byte[1];
            stepByte[0] = characteristic.getValue()[characteristic.getValue().length - 3];
            String aa = HexUtil.formatHexString(stepByte);
            //实时计步
            String newStr = "实时步数： " + HexUtil.hexToDec(aa);
            mTextView.setText(newStr);

        } else if (str.contains("bc bd 41 00 a2")) {
            //离线计步
            byte[] stepByte = new byte[64];
            offLineList.add(characteristic);
            byte[] charByte = characteristic.getValue();
            for (int i = 7; i < charByte.length - 2; i++) {
                stepByte[i - 7] = charByte[i];
            }
            //年月日时
            String year = HexUtil.hexToDec(HexUtil.formatByteHexString(stepByte[0]));
            String month = HexUtil.hexToDec(HexUtil.formatByteHexString(stepByte[1]));
            String day = HexUtil.hexToDec(HexUtil.formatByteHexString(stepByte[2]));
            String hour = HexUtil.hexToDec(HexUtil.formatByteHexString(stepByte[3]));

            OffLineStep offLineStep = new OffLineStep();
            offLineStep.setYear(year);
            offLineStep.setMonth(month);
            offLineStep.setDay(day);
            offLineStep.setHour(hour);
            //计步详情字节
            List<Step> stepList = new ArrayList<>();
            for (int i = 4; i < stepByte.length; i++) {
                Step step = new Step(i - 4, HexUtil.hexToDec(HexUtil.formatByteHexString(stepByte[i])));
                stepList.add(step);
            }
            offLineStep.setStepList(stepList);

           // Log.i("new  sensor  data", "offLineStep ：" + offLineStep.toString());


            if (offLineList.size() == 24) {

            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextView.setText(str);
                }
            });

        }


        if (gatt.getDevice().getAddress().contains("54:6C:0E:B9:1C:B4")) {
            sid = 1;
        } else if (gatt.getDevice().getAddress().contains("54:6C:0E:B9:15:64")) {
            sid = 2;
        } else {
            sid = 0;
        }
        ;
//        if(sid != 0){
//        if(sid == 2){
//            byte[] raw = characteristic.getValue();
//            Long timeStamp = (Long) new Date().getTime();
//            Map sensorData = new HashMap();
//            sensorData.put("timeStamp", timeStamp);
//            sensorData.put("raw", raw);
//            Message msg = thread.sensorAIHandler.obtainMessage(1013);
//            msg.arg1 = sid;
//            msg.obj = sensorData;
//            thread.sensorAIHandler.sendMessage(msg);
//        };
        if (sid != 0) {
            byte[] raw = characteristic.getValue();
            Long timeStamp = (Long) new Date().getTime();
            Map sensorData = new HashMap();
            sensorData.put("timeStamp", timeStamp);
            sensorData.put("raw", raw);
            Message msg = thread.sensorAIHandler.obtainMessage(1113);
            msg.arg1 = sid;
            msg.obj = sensorData;
            thread.sensorAIHandler.sendMessage(msg);
        }
        ;
    }

    ;


    /**
     * 首页文本提示信息
     *
     * @param str
     */
    private void SetMsg(String str) {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                TextView viewById = findViewById(R.id.MainActivityMsgTextView);
                viewById.setText(str);
            }
        });
    }

    private TextToSpeech tts;

    private void ttsServer(String text) {
        if (tts == null) {
            tts = new TextToSpeech(this, this);
            tts.setPitch(1.0f);
            tts.setSpeechRate(1.0f);
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
//                    Logger.d(TAG, "onStart : utteranceId = "+utteranceId);
                }

                @Override
                public void onDone(String utteranceId) {
//                    Logger.d(TAG, "onDone : utteranceId = "+utteranceId);
                }

                @Override
                public void onError(String utteranceId) {
//                    Logger.d(TAG, "onError : utteranceId = "+utteranceId);
                }
            });
        }
        //播放语音
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "name");
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.CHINESE);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language is not available.", Toast.LENGTH_SHORT).show();
                tts.setLanguage(Locale.US);
            }
        }
    }


    private void SetMsgMy(int count, int actionCount, String voice, int timer, int thighAngle, int shankAngle, int jointAngle, int code) {

        String str = "完成总数： " + actionCount + ";\n" + "完成标准： " + count + ";\n" + "语音： " + voice + ";\n" + "计时器: " + timer + ";\n" + "大腿角度: " + thighAngle + ";\n" + "小腿角度: " + shankAngle + ";\n" + "夹角角度: " + jointAngle + ";\n";

        mTextView.setText("历史数据：\n" + allText);
        viewByIdMy.setText(str);

        if (code == 2023) {
            ttsServer(voice);

            String addText = "语音： " + voice + ";\n";
            if (countNum < 5) {

                if (allText != "") {
                    String[] a = allText.split("[\n]");

                    if (!a[a.length - 1].contains(addText)) {
                        allText += addText;
                        countNum += 1;
                    }
                } else {
                    allText += addText;
                    countNum += 1;
                }


            } else {

                String[] a = allText.split("[\n]");
                String b = "";
                for (int i = 2; i < a.length; i++) {

                    b += a[i] + "\n";
                }
                allText = b;
                if (!a[a.length - 1].contains(addText)) {
                    allText += addText;
                    countNum = a.length - 1;
                } else {
                    countNum = a.length - 2;
                }


            }
        }

    }


    /**
     * 打开Toast提示
     *
     * @param str
     */
    private void ShowToast(String str) {
//        Log.i("MainActivity",str);
        ToastUtils.show(MainActivity.this, str);
    }
    //训练数据配置demo演示


    public void getBasedata() {

        load_data.put("RepNum", 5.0);

        List<List> excelData = new ArrayList<>();
        List row0 = new ArrayList<>();
        row0.add("初始状态");
        row0.add("计时器初始0秒");
        row0.add(1.0);
        row0.add("准备动作检查");
        row0.add(0.0);
        row0.add(1.0);
        row0.add(0.0);
        row0.add(0.0);
        row0.add(0.0);
        row0.add(0.0);
        String BeginToPrepare0 = "-#-#-#-#-";
        row0.add(BeginToPrepare0);
        excelData.add(row0);
        List row1 = new ArrayList<>();
        row1.add("准备动作检查");
        row1.add("大腿水平小腿垂直");
        row1.add(2.0);
        row1.add("动作开始");
        row1.add(0.0);
        row1.add(3.0);
        row1.add(0.0);
        row1.add(0.0);
        row1.add(0.0);
        row1.add(1.0);
        String BeginToPrepare1 = "动作开始，勾脚，尽量伸直膝盖，脚跟离地#开始，勾脚，伸膝#开始，勾脚，伸膝#开始，勾脚，伸膝#开始，勾脚，伸膝";
        row1.add(BeginToPrepare1);
        excelData.add(row1);
        List row2 = new ArrayList<>();
        row2.add("准备动作检查");
        row2.add("大腿水平小腿垂直1");
        row2.add(3.0);
        row2.add("动作开始");
        row2.add(0.0);
        row2.add(3.0);
        row2.add(0.0);
        row2.add(0.0);
        row2.add(0.0);
        row2.add(1.0);
        String BeginToPrepare2 = "动作开始，勾脚，尽量伸直膝盖，脚跟离地#开始，勾脚，伸膝#开始，勾脚，伸膝#开始，勾脚，伸膝#开始，勾脚，伸膝";
        row2.add(BeginToPrepare2);
        excelData.add(row2);
        List row3 = new ArrayList<>();
        row3.add("准备动作检查");
        row3.add("未大腿水平小腿垂直完成时间");
        row3.add(1.0);
        row3.add("准备动作失败");
        row3.add(0.0);
        row3.add(3.0);
        row3.add(0.0);
        row3.add(10.0);
        row3.add(0.0);
        row3.add(1.0);
        String BeginToPrepare3 = "保持坐姿，膝关节弯曲90度#保持坐姿，膝关节弯曲90度#保持坐姿，膝关节弯曲90度#保持坐姿，膝关节弯曲90度#保持坐姿，膝关节弯曲90度";
        row3.add(BeginToPrepare3);
        excelData.add(row3);
        List row4 = new ArrayList<>();
        row4.add("准备动作失败");
        row4.add("大腿水平小腿垂直");
        row4.add(1.0);
        row4.add("动作开始");
        row4.add(0.0);
        row4.add(3.0);
        row4.add(0.0);
        row4.add(0.0);
        row4.add(0.0);
        row4.add(1.0);
        String BeginToPrepare4 = "动作开始，勾脚，尽量伸直膝盖，脚跟离地#开始，勾脚，伸膝#开始，勾脚，伸膝#开始，勾脚，伸膝#开始，勾脚，伸膝";
        row4.add(BeginToPrepare4);
        excelData.add(row4);
        List row5 = new ArrayList<>();
        row5.add("准备动作失败");
        row5.add("大腿水平小腿垂直1");
        row5.add(2.0);
        row5.add("动作开始");
        row5.add(0.0);
        row5.add(3.0);
        row5.add(0.0);
        row5.add(0.0);
        row5.add(0.0);
        row5.add(1.0);
        String BeginToPrepare5 = "动作开始，勾脚，尽量伸直膝盖，脚跟离地#开始，勾脚，伸膝#开始，勾脚，伸膝#开始，勾脚，伸膝#开始，勾脚，伸膝";
        row5.add(BeginToPrepare5);
        excelData.add(row5);
        List row6 = new ArrayList<>();
        row6.add("动作开始");
        row6.add("标准和计数范围内一次静止");
        row6.add(5.0);
        row6.add("第一次静止");
        row6.add(0.0);
        row6.add(3.0);
        row6.add(0.0);
        row6.add(0.0);
        row6.add(0.0);
        row6.add(1.0);
        String BeginToPrepare6 = "再伸直一点#再伸直一点#再伸直一点#再伸直一点#再伸直一点";
        row6.add(BeginToPrepare6);
        excelData.add(row6);
        List row7 = new ArrayList<>();
        row7.add("动作开始");
        row7.add("达到不计数角度，一直静止");
        row7.add(3.0);
        row7.add("准备动作检查");
        row7.add(0.0);
        row7.add(3.0);
        row7.add(0.0);
        row7.add(0.0);
        row7.add(0.0);
        row7.add(1.0);
        String BeginToPrepare7 = "您好像未做动作#您好像未做动作#您好像未做动作#您好像未做动作#您好像未做动作";
        row7.add(BeginToPrepare7);
        excelData.add(row7);
        List row8 = new ArrayList<>();
        row8.add("动作开始");
        row8.add("达到标准角度，一直静止");
        row8.add(4.0);
        row8.add("标准");
        row8.add(0.0);
        row8.add(3.0);
        row8.add(0.0);
        row8.add(0.0);
        row8.add(0.0);
        row8.add(0.0);
        String BeginToPrepare8 = "很好，保持5秒#很好，保持5秒#很好，保持5秒#很好，保持5秒#很好，保持5秒";
        row8.add(BeginToPrepare8);
        excelData.add(row8);
        List row9 = new ArrayList<>();
        row9.add("动作开始");
        row9.add("大腿垂直");
        row9.add(1.0);
        row9.add("怀疑不配合");
        row9.add(0.0);
        row9.add(3.0);
        row9.add(0.0);
        row9.add(0.0);
        row9.add(1.0);
        row9.add(0.0);
        String BeginToPrepare9 = "请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿";
        row9.add(BeginToPrepare9);
        excelData.add(row9);
        List row10 = new ArrayList<>();
        row10.add("动作开始");
        row10.add("大腿垂直1");
        row10.add(2.0);
        row10.add("怀疑不配合");
        row10.add(0.0);
        row10.add(3.0);
        row10.add(0.0);
        row10.add(0.0);
        row10.add(1.0);
        row10.add(0.0);
        String BeginToPrepare10 = "请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿";
        row10.add(BeginToPrepare10);
        excelData.add(row10);
        List row11 = new ArrayList<>();
        row11.add("第一次静止");
        row11.add("二次静止");
        row11.add(3.0);
        row11.add("第二次静止");
        row11.add(0.0);
        row11.add(3.0);
        row11.add(0.0);
        row11.add(0.0);
        row11.add(0.0);
        row11.add(0.0);
        String BeginToPrepare11 = "保持5秒#保持5秒#保持5秒#保持5秒#保持5秒";
        row11.add(BeginToPrepare11);
        excelData.add(row11);
        List row12 = new ArrayList<>();
        row12.add("第一次静止");
        row12.add("大腿垂直");
        row12.add(1.0);
        row12.add("怀疑不配合");
        row12.add(0.0);
        row12.add(3.0);
        row12.add(0.0);
        row12.add(0.0);
        row12.add(1.0);
        row12.add(0.0);
        String BeginToPrepare12 = "请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿";
        row12.add(BeginToPrepare12);
        excelData.add(row12);
        List row13 = new ArrayList<>();
        row13.add("第一次静止");
        row13.add("大腿垂直1");
        row13.add(2.0);
        row13.add("怀疑不配合");
        row13.add(0.0);
        row13.add(3.0);
        row13.add(0.0);
        row13.add(0.0);
        row13.add(1.0);
        row13.add(0.0);
        String BeginToPrepare13 = "请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿";
        row13.add(BeginToPrepare13);
        excelData.add(row13);
        List row14 = new ArrayList<>();
        row14.add("第二次静止");
        row14.add("达到标准角度");
        row14.add(3.0);
        row14.add("标准");
        row14.add(0.0);
        row14.add(0.0);
        row14.add(0.0);
        row14.add(0.0);
        row14.add(0.0);
        row14.add(0.0);
        String BeginToPrepare14 = "很好，保持#很好，保持#很好，保持#很好，保持#很好，保持";
        row14.add(BeginToPrepare14);
        excelData.add(row14);
        List row15 = new ArrayList<>();
        row15.add("第二次静止");
        row15.add("达到计数角度");
        row15.add(4.0);
        row15.add("计数");
        row15.add(0.0);
        row15.add(0.0);
        row15.add(0.0);
        row15.add(0.0);
        row15.add(0.0);
        row15.add(0.0);
        String BeginToPrepare15 = "伸直膝盖#伸直膝盖#伸直膝盖#伸直膝盖#伸直膝盖";
        row15.add(BeginToPrepare15);
        excelData.add(row15);
        List row16 = new ArrayList<>();
        row16.add("第二次静止");
        row16.add("达到不计数角度");
        row16.add(5.0);
        row16.add("不计数");
        row16.add(0.0);
        row16.add(0.0);
        row16.add(0.0);
        row16.add(0.0);
        row16.add(0.0);
        row16.add(0.0);
        String BeginToPrepare16 = "伸直膝盖#伸直膝盖#伸直膝盖#伸直膝盖#伸直膝盖";
        row16.add(BeginToPrepare16);
        excelData.add(row16);
        List row17 = new ArrayList<>();
        row17.add("第二次静止");
        row17.add("大腿垂直");
        row17.add(1.0);
        row17.add("怀疑不配合");
        row17.add(0.0);
        row17.add(3.0);
        row17.add(0.0);
        row17.add(0.0);
        row17.add(1.0);
        row17.add(0.0);
        String BeginToPrepare17 = "请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿";
        row17.add(BeginToPrepare17);
        excelData.add(row17);
        List row18 = new ArrayList<>();
        row18.add("第二次静止");
        row18.add("大腿垂直1");
        row18.add(2.0);
        row18.add("怀疑不配合");
        row18.add(0.0);
        row18.add(3.0);
        row18.add(0.0);
        row18.add(0.0);
        row18.add(1.0);
        row18.add(0.0);
        String BeginToPrepare18 = "请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿";
        row18.add(BeginToPrepare18);
        excelData.add(row18);
        List row19 = new ArrayList<>();
        row19.add("标准");
        row19.add("达到计数角度");
        row19.add(4.0);
        row19.add("计数");
        row19.add(0.0);
        row19.add(0.0);
        row19.add(0.0);
        row19.add(0.0);
        row19.add(0.0);
        row19.add(0.0);
        String BeginToPrepare19 = "伸直膝盖#伸直膝盖#伸直膝盖#伸直膝盖#伸直膝盖";
        row19.add(BeginToPrepare19);
        excelData.add(row19);
        List row20 = new ArrayList<>();
        row20.add("标准");
        row20.add("达到不计数角度");
        row20.add(5.0);
        row20.add("不计数");
        row20.add(0.0);
        row20.add(0.0);
        row20.add(0.0);
        row20.add(0.0);
        row20.add(0.0);
        row20.add(0.0);
        String BeginToPrepare20 = "伸直膝盖#伸直膝盖#伸直膝盖#伸直膝盖#伸直膝盖";
        row20.add(BeginToPrepare20);
        excelData.add(row20);
        List row21 = new ArrayList<>();
        row21.add("标准");
        row21.add("大腿垂直");
        row21.add(1.0);
        row21.add("怀疑不配合");
        row21.add(0.0);
        row21.add(3.0);
        row21.add(0.0);
        row21.add(0.0);
        row21.add(1.0);
        row21.add(0.0);
        String BeginToPrepare21 = "请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿";
        row21.add(BeginToPrepare21);
        excelData.add(row21);
        List row22 = new ArrayList<>();
        row22.add("标准");
        row22.add("大腿垂直1");
        row22.add(2.0);
        row22.add("怀疑不配合");
        row22.add(0.0);
        row22.add(3.0);
        row22.add(0.0);
        row22.add(0.0);
        row22.add(1.0);
        row22.add(0.0);
        String BeginToPrepare22 = "请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿";
        row22.add(BeginToPrepare22);
        excelData.add(row22);
        List row23 = new ArrayList<>();
        row23.add("标准");
        row23.add("完成时间");
        row23.add(3.0);
        row23.add("休息监控");
        row23.add(3.0);
        row23.add(3.0);
        row23.add(1.0);
        row23.add(0.0);
        row23.add(0.0);
        row23.add(2.0);
        String BeginToPrepare23 = "很好，放下腿休息10秒#很好，放下腿休息10秒#很好，放下腿休息10秒#很好，放下腿休息10秒#很好，放下腿休息10秒";
        row23.add(BeginToPrepare23);
        excelData.add(row23);
        List row24 = new ArrayList<>();
        row24.add("计数");
        row24.add("完成时间");
        row24.add(3.0);
        row24.add("休息监控");
        row24.add(2.0);
        row24.add(3.0);
        row24.add(1.0);
        row24.add(0.0);
        row24.add(0.0);
        row24.add(2.0);
        String BeginToPrepare24 = "放下腿休息10秒，下次加油膝盖再伸直一点#放下腿休息10秒，下次加油膝盖再伸直一点#放下腿休息10秒，下次加油膝盖再伸直一点#放下腿休息10秒，下次加油膝盖再伸直一点#放下腿休息10秒，下次加油膝盖再伸直一点";
        row24.add(BeginToPrepare24);
        excelData.add(row24);
        List row25 = new ArrayList<>();
        row25.add("计数");
        row25.add("达到不计数角度");
        row25.add(4.0);
        row25.add("不计数");
        row25.add(0.0);
        row25.add(0.0);
        row25.add(0.0);
        row25.add(0.0);
        row25.add(0.0);
        row25.add(0.0);
        String BeginToPrepare25 = "伸直膝盖#伸直膝盖#伸直膝盖#伸直膝盖#伸直膝盖";
        row25.add(BeginToPrepare25);
        excelData.add(row25);
        List row26 = new ArrayList<>();
        row26.add("计数");
        row26.add("大腿垂直");
        row26.add(1.0);
        row26.add("怀疑不配合");
        row26.add(0.0);
        row26.add(3.0);
        row26.add(0.0);
        row26.add(0.0);
        row26.add(1.0);
        row26.add(0.0);
        String BeginToPrepare26 = "请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿";
        row26.add(BeginToPrepare26);
        excelData.add(row26);
        List row27 = new ArrayList<>();
        row27.add("计数");
        row27.add("大腿垂直1");
        row27.add(2.0);
        row27.add("怀疑不配合");
        row27.add(0.0);
        row27.add(3.0);
        row27.add(0.0);
        row27.add(0.0);
        row27.add(1.0);
        row27.add(0.0);
        String BeginToPrepare27 = "请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿";
        row27.add(BeginToPrepare27);
        excelData.add(row27);
        List row28 = new ArrayList<>();
        row28.add("不计数");
        row28.add("完成时间");
        row28.add(3.0);
        row28.add("休息监控");
        row28.add(0.0);
        row28.add(3.0);
        row28.add(1.0);
        row28.add(0.0);
        row28.add(0.0);
        row28.add(2.0);
        String BeginToPrepare28 = "放下腿休息10秒，膝盖伸直不够，稍后再试一次#放下腿休息10秒，膝盖伸直不够，稍后再试一次#放下腿休息10秒，膝盖伸直不够，稍后再试一次#放下腿休息10秒，膝盖伸直不够，稍后再试一次#放下腿休息10秒，膝盖伸直不够，稍后再试一次";
        row28.add(BeginToPrepare28);
        excelData.add(row28);
        List row29 = new ArrayList<>();
        row29.add("不计数");
        row29.add("大腿垂直");
        row29.add(1.0);
        row29.add("怀疑不配合");
        row29.add(0.0);
        row29.add(3.0);
        row29.add(0.0);
        row29.add(0.0);
        row29.add(1.0);
        row29.add(0.0);
        String BeginToPrepare29 = "请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿";
        row29.add(BeginToPrepare29);
        excelData.add(row29);
        List row30 = new ArrayList<>();
        row30.add("不计数");
        row30.add("大腿垂直1");
        row30.add(2.0);
        row30.add("怀疑不配合");
        row30.add(0.0);
        row30.add(3.0);
        row30.add(0.0);
        row30.add(0.0);
        row30.add(1.0);
        row30.add(0.0);
        String BeginToPrepare30 = "请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿#请回到坐姿";
        row30.add(BeginToPrepare30);
        excelData.add(row30);
        List row31 = new ArrayList<>();
        row31.add("休息监控");
        row31.add("大腿水平小腿垂直");
        row31.add(1.0);
        row31.add("结束");
        row31.add(0.0);
        row31.add(3.0);
        row31.add(0.0);
        row31.add(0.0);
        row31.add(0.0);
        row31.add(0.0);
        String BeginToPrepare31 = "-#-#-#-#-";
        row31.add(BeginToPrepare31);
        excelData.add(row31);
        List row32 = new ArrayList<>();
        row32.add("休息监控");
        row32.add("大腿水平小腿垂直1");
        row32.add(2.0);
        row32.add("结束");
        row32.add(0.0);
        row32.add(3.0);
        row32.add(0.0);
        row32.add(0.0);
        row32.add(0.0);
        row32.add(0.0);
        String BeginToPrepare32 = "-#-#-#-#-";
        row32.add(BeginToPrepare32);
        excelData.add(row32);
        List row33 = new ArrayList<>();
        row33.add("休息监控");
        row33.add("未大腿水平小腿垂直完成时间");
        row33.add(3.0);
        row33.add("未休息");
        row33.add(0.0);
        row33.add(3.0);
        row33.add(0.0);
        row33.add(5.0);
        row33.add(0.0);
        row33.add(0.0);
        String BeginToPrepare33 = "放下小腿休息#放下小腿休息#放下小腿休息#放下小腿休息#放下小腿休息";
        row33.add(BeginToPrepare33);
        excelData.add(row33);
        List row34 = new ArrayList<>();
        row34.add("未休息");
        row34.add("大腿水平小腿垂直");
        row34.add(1.0);
        row34.add("结束");
        row34.add(0.0);
        row34.add(3.0);
        row34.add(0.0);
        row34.add(0.0);
        row34.add(0.0);
        row34.add(0.0);
        String BeginToPrepare34 = "-#-#-#-#-";
        row34.add(BeginToPrepare34);
        excelData.add(row34);
        List row35 = new ArrayList<>();
        row35.add("未休息");
        row35.add("大腿水平小腿垂直1");
        row35.add(2.0);
        row35.add("结束");
        row35.add(0.0);
        row35.add(3.0);
        row35.add(0.0);
        row35.add(0.0);
        row35.add(0.0);
        row35.add(0.0);
        String BeginToPrepare35 = "-#-#-#-#-";
        row35.add(BeginToPrepare35);
        excelData.add(row35);
        List row36 = new ArrayList<>();
        row36.add("结束");
        row36.add("休息时间");
        row36.add(1.0);
        row36.add("准备动作检查");
        row36.add(0.0);
        row36.add(3.0);
        row36.add(0.0);
        row36.add(0.0);
        row36.add(0.0);
        row36.add(2.0);
        String BeginToPrepare36 = "-#-#-#-#-";
        row36.add(BeginToPrepare36);
        excelData.add(row36);
        List row37 = new ArrayList<>();
        row37.add("怀疑不配合");
        row37.add("大腿垂直10秒");
        row37.add(1.0);
        row37.add("不配合");
        row37.add(0.0);
        row37.add(3.0);
        row37.add(0.0);
        row37.add(10.0);
        row37.add(2.0);
        row37.add(0.0);
        String BeginToPrepare37 = "您好像未在训练状态，请回到坐姿#您好像未在训练状态，请回到坐姿#您好像未在训练状态，请回到坐姿#您好像未在训练状态，请回到坐姿#您好像未在训练状态，请回到坐姿";
        row37.add(BeginToPrepare37);
        excelData.add(row37);
        List row38 = new ArrayList<>();
        row38.add("怀疑不配合");
        row38.add("大腿垂直10秒1");
        row38.add(2.0);
        row38.add("不配合");
        row38.add(0.0);
        row38.add(3.0);
        row38.add(0.0);
        row38.add(10.0);
        row38.add(2.0);
        row38.add(0.0);
        String BeginToPrepare38 = "您好像未在训练状态，请回到坐姿#您好像未在训练状态，请回到坐姿#您好像未在训练状态，请回到坐姿#您好像未在训练状态，请回到坐姿#您好像未在训练状态，请回到坐姿";
        row38.add(BeginToPrepare38);
        excelData.add(row38);
        List row39 = new ArrayList<>();
        row39.add("怀疑不配合");
        row39.add("大腿水平");
        row39.add(3.0);
        row39.add("准备动作检查");
        row39.add(0.0);
        row39.add(3.0);
        row39.add(0.0);
        row39.add(0.0);
        row39.add(0.0);
        row39.add(2.0);
        String BeginToPrepare39 = "-#-#-#-#-";
        row39.add(BeginToPrepare39);
        excelData.add(row39);
        List row40 = new ArrayList<>();
        row40.add("不配合");
        row40.add("大腿垂直");
        row40.add(1.0);
        row40.add("不配合");
        row40.add(0.0);
        row40.add(0.0);
        row40.add(0.0);
        row40.add(0.0);
        row40.add(0.0);
        row40.add(0.0);
        String BeginToPrepare40 = "您好像未在训练状态，请回到坐姿#您好像未在训练状态，请回到坐姿#您好像未在训练状态，请回到坐姿#您好像未在训练状态，请回到坐姿#您好像未在训练状态，请回到坐姿";
        row40.add(BeginToPrepare40);
        excelData.add(row40);
        List row41 = new ArrayList<>();
        row41.add("不配合");
        row41.add("大腿垂直1");
        row41.add(2.0);
        row41.add("不配合");
        row41.add(0.0);
        row41.add(0.0);
        row41.add(0.0);
        row41.add(0.0);
        row41.add(0.0);
        row41.add(0.0);
        String BeginToPrepare41 = "您好像未在训练状态，请回到坐姿#您好像未在训练状态，请回到坐姿#您好像未在训练状态，请回到坐姿#您好像未在训练状态，请回到坐姿#您好像未在训练状态，请回到坐姿";
        row41.add(BeginToPrepare41);
        excelData.add(row41);
        List row42 = new ArrayList<>();
        row42.add("不配合");
        row42.add("大腿水平");
        row42.add(3.0);
        row42.add("准备动作检查");
        row42.add(0.0);
        row42.add(2.0);
        row42.add(0.0);
        row42.add(0.0);
        row42.add(0.0);
        row42.add(2.0);
        String BeginToPrepare42 = "-#-#-#-#-";
        row42.add(BeginToPrepare42);
        excelData.add(row42);

        load_data.put("state", excelData);

        List<List> rule = new ArrayList<>();
        List row200 = new ArrayList<>();
        row200.add("计时器初始0秒");
        row200.add("timerEqual");
        row200.add("0");
        rule.add(row200);
        List row201 = new ArrayList<>();
        row201.add("大腿水平小腿垂直");
        row201.add("segmentAnglesGreaterThan");
        row201.add("0");
        rule.add(row201);
        List row202 = new ArrayList<>();
        row202.add("大腿水平小腿垂直");
        row202.add("segmentAnglesLessThan");
        row202.add("50");
        rule.add(row202);
        List row203 = new ArrayList<>();
        row203.add("大腿水平小腿垂直");
        row203.add("bufferTimer");
        row203.add("0");
        rule.add(row203);
        List row204 = new ArrayList<>();
        row204.add("大腿水平小腿垂直");
        row204.add("otherSegmentAnglesGreaterThan");
        row204.add("40");
        rule.add(row204);
        List row205 = new ArrayList<>();
        row205.add("大腿水平小腿垂直");
        row205.add("otherSegmentAnglesLessThan");
        row205.add("140");
        rule.add(row205);
        List row206 = new ArrayList<>();
        row206.add("大腿水平小腿垂直1");
        row206.add("segmentAnglesGreaterThan");
        row206.add("310");
        rule.add(row206);
        List row207 = new ArrayList<>();
        row207.add("大腿水平小腿垂直1");
        row207.add("segmentAnglesLessThan");
        row207.add("360");
        rule.add(row207);
        List row208 = new ArrayList<>();
        row208.add("大腿水平小腿垂直1");
        row208.add("bufferTimer");
        row208.add("0");
        rule.add(row208);
        List row209 = new ArrayList<>();
        row209.add("大腿水平小腿垂直1");
        row209.add("otherSegmentAnglesGreaterThan");
        row209.add("40");
        rule.add(row209);
        List row210 = new ArrayList<>();
        row210.add("大腿水平小腿垂直1");
        row210.add("otherSegmentAnglesLessThan");
        row210.add("140");
        rule.add(row210);
        List row211 = new ArrayList<>();
        row211.add("未大腿水平小腿垂直完成时间");
        row211.add("timerGreaterThan");
        row211.add("5");
        rule.add(row211);
        List row212 = new ArrayList<>();
        row212.add("未大腿水平小腿垂直完成时间");
        row212.add("bufferTimer");
        row212.add("0");
        rule.add(row212);
        List row213 = new ArrayList<>();
        row213.add("大腿水平");
        row213.add("otherSegmentAnglesGreaterThan");
        row213.add("40");
        rule.add(row213);
        List row214 = new ArrayList<>();
        row214.add("大腿水平");
        row214.add("otherSegmentAnglesLessThan");
        row214.add("140");
        rule.add(row214);
        List row215 = new ArrayList<>();
        row215.add("大腿水平");
        row215.add("bufferTimer");
        row215.add("0");
        rule.add(row215);
        List row216 = new ArrayList<>();
        row216.add("一次静止");
        row216.add("patientStatus");
        row216.add("1");
        rule.add(row216);
        List row217 = new ArrayList<>();
        row217.add("一次静止");
        row217.add("patientTimerGreaterThan");
        row217.add("1");
        rule.add(row217);
        List row218 = new ArrayList<>();
        row218.add("一次静止");
        row218.add("bufferTimer");
        row218.add("0");
        rule.add(row218);
        List row219 = new ArrayList<>();
        row219.add("二次静止");
        row219.add("patientStatus");
        row219.add("1");
        rule.add(row219);
        List row220 = new ArrayList<>();
        row220.add("二次静止");
        row220.add("patientTimerGreaterThan");
        row220.add("1");
        rule.add(row220);
        List row221 = new ArrayList<>();
        row221.add("二次静止");
        row221.add("bufferTimer");
        row221.add("0");
        rule.add(row221);
        List row222 = new ArrayList<>();
        row222.add("达到标准角度");
        row222.add("jointAnglesLessThan");
        row222.add("200");
        rule.add(row222);
        List row223 = new ArrayList<>();
        row223.add("达到标准角度");
        row223.add("jointAnglesGreaterThan");
        row223.add("170");
        rule.add(row223);
        List row224 = new ArrayList<>();
        row224.add("达到标准角度");
        row224.add("bufferTimer");
        row224.add("0");
        rule.add(row224);
        List row225 = new ArrayList<>();
        row225.add("达到标准角度，一直静止");
        row225.add("jointAnglesLessThan");
        row225.add("200");
        rule.add(row225);
        List row226 = new ArrayList<>();
        row226.add("达到标准角度，一直静止");
        row226.add("jointAnglesGreaterThan");
        row226.add("170");
        rule.add(row226);
        List row227 = new ArrayList<>();
        row227.add("达到标准角度，一直静止");
        row227.add("bufferTimer");
        row227.add("0");
        rule.add(row227);
        List row228 = new ArrayList<>();
        row228.add("达到标准角度，一直静止");
        row228.add("timerGreaterThan");
        row228.add("2");
        rule.add(row228);
        List row229 = new ArrayList<>();
        row229.add("达到计数角度");
        row229.add("jointAnglesGreaterThan");
        row229.add("120");
        rule.add(row229);
        List row230 = new ArrayList<>();
        row230.add("达到计数角度");
        row230.add("jointAnglesLessThan");
        row230.add("170");
        rule.add(row230);
        List row231 = new ArrayList<>();
        row231.add("达到计数角度");
        row231.add("bufferTimer");
        row231.add("0");
        rule.add(row231);
        List row232 = new ArrayList<>();
        row232.add("达到不计数角度");
        row232.add("jointAnglesLessThan");
        row232.add("120");
        rule.add(row232);
        List row233 = new ArrayList<>();
        row233.add("达到不计数角度");
        row233.add("bufferTimer");
        row233.add("0");
        rule.add(row233);
        List row234 = new ArrayList<>();
        row234.add("大腿垂直");
        row234.add("otherSegmentAnglesGreaterThan");
        row234.add("0");
        rule.add(row234);
        List row235 = new ArrayList<>();
        row235.add("大腿垂直");
        row235.add("otherSegmentAnglesLessThan");
        row235.add("40");
        rule.add(row235);
        List row236 = new ArrayList<>();
        row236.add("大腿垂直");
        row236.add("bufferTimer");
        row236.add("0");
        rule.add(row236);
        List row237 = new ArrayList<>();
        row237.add("大腿垂直1");
        row237.add("otherSegmentAnglesGreaterThan");
        row237.add("140");
        rule.add(row237);
        List row238 = new ArrayList<>();
        row238.add("大腿垂直1");
        row238.add("otherSegmentAnglesLessThan");
        row238.add("360");
        rule.add(row238);
        List row239 = new ArrayList<>();
        row239.add("大腿垂直1");
        row239.add("bufferTimer");
        row239.add("0");
        rule.add(row239);
        List row240 = new ArrayList<>();
        row240.add("完成时间");
        row240.add("timerGreaterThan");
        row240.add("5");
        rule.add(row240);
        List row241 = new ArrayList<>();
        row241.add("完成时间");
        row241.add("bufferTimer");
        row241.add("0");
        rule.add(row241);
        List row242 = new ArrayList<>();
        row242.add("标准和计数范围内一次静止");
        row242.add("patientStatus");
        row242.add("1");
        rule.add(row242);
        List row243 = new ArrayList<>();
        row243.add("标准和计数范围内一次静止");
        row243.add("patientTimerGreaterThan");
        row243.add("1");
        rule.add(row243);
        List row244 = new ArrayList<>();
        row244.add("标准和计数范围内一次静止");
        row244.add("bufferTimer");
        row244.add("0");
        rule.add(row244);
        List row245 = new ArrayList<>();
        row245.add("标准和计数范围内一次静止");
        row245.add("jointAnglesGreaterThan");
        row245.add("120");
        rule.add(row245);
        List row246 = new ArrayList<>();
        row246.add("休息时间");
        row246.add("timerGreaterThan");
        row246.add("10");
        rule.add(row246);
        List row247 = new ArrayList<>();
        row247.add("休息时间");
        row247.add("bufferTimer");
        row247.add("0");
        rule.add(row247);
        List row248 = new ArrayList<>();
        row248.add("大腿垂直10秒");
        row248.add("otherSegmentAnglesGreaterThan");
        row248.add("0");
        rule.add(row248);
        List row249 = new ArrayList<>();
        row249.add("大腿垂直10秒");
        row249.add("otherSegmentAnglesLessThan");
        row249.add("40");
        rule.add(row249);
        List row250 = new ArrayList<>();
        row250.add("大腿垂直10秒");
        row250.add("exceptionGreaterThan");
        row250.add("10");
        rule.add(row250);
        List row251 = new ArrayList<>();
        row251.add("大腿垂直10秒");
        row251.add("bufferTimer");
        row251.add("0");
        rule.add(row251);
        List row252 = new ArrayList<>();
        row252.add("大腿垂直10秒1");
        row252.add("otherSegmentAnglesGreaterThan");
        row252.add("140");
        rule.add(row252);
        List row253 = new ArrayList<>();
        row253.add("大腿垂直10秒1");
        row253.add("otherSegmentAnglesLessThan");
        row253.add("360");
        rule.add(row253);
        List row254 = new ArrayList<>();
        row254.add("大腿垂直10秒1");
        row254.add("exceptionGreaterThan");
        row254.add("10");
        rule.add(row254);
        List row255 = new ArrayList<>();
        row255.add("大腿垂直10秒1");
        row255.add("bufferTimer");
        row255.add("0");
        rule.add(row255);
        List row256 = new ArrayList<>();
        row256.add("达到不计数角度，一直静止");
        row256.add("jointAnglesLessThan");
        row256.add("120");
        rule.add(row256);
        List row257 = new ArrayList<>();
        row257.add("达到不计数角度，一直静止");
        row257.add("bufferTimer");
        row257.add("0");
        rule.add(row257);
        List row258 = new ArrayList<>();
        row258.add("达到不计数角度，一直静止");
        row258.add("patientStatus");
        row258.add("1");
        rule.add(row258);
        List row259 = new ArrayList<>();
        row259.add("达到不计数角度，一直静止");
        row259.add("patientTimerGreaterThan");
        row259.add("9");
        rule.add(row259);

        load_data.put("rule", rule);
    }

    //主线程监听
    public class MainHandler extends Handler {
        private Integer count = 0;
        private Integer actionCount = 0;
        private String voice = "";
        private Integer timer = 0;
        private int thighAngle = 0;
        private int shankAngle = 0;
        private int textTimer = 0;
        private Long textStamp = 0L;
        private int jointAngle = 0;

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (textTimer != 0) {
                Long currentTime = (Long) new Date().getTime();
                double diff = (double) (currentTime - textStamp) / 1000;
                if (diff > textTimer) {
                    textTimer = 0;
                    textStamp = 0L;
                    voice = "";
                }
            } else {
                textStamp = 0L;
                voice = "";
            }
            switch (msg.what) {
                case 2101:
                    Log.d(TAG, " parent thread receiver message: " + ((String) msg.obj));

                    break;
                case 2021:
                    Log.d(TAG, "                                                       Got sensor ready");
                    isSensorReady = true;
                    mButton.setText("开始训练");
                    break;
                case 2022:
                    actionCount = msg.arg2;
                    count = msg.arg1;
                    Log.d(TAG, "                                                  Completed Once train" + count);
                    SetMsgMy(count, actionCount, voice, timer, thighAngle, shankAngle, jointAngle, 2022);
                    break;
                case 2023:

                    if (msg.obj != "") {
                        voice = (String) msg.obj;
                        textTimer = (int) Math.round(voice.length() / 4);
                        textStamp = (Long) new Date().getTime();
                    }
                    Log.d(TAG, "                                                          Got voice =  " + voice);
                    if (!voice.equals("-")) {
                        SetMsgMy(count, actionCount, voice, timer, thighAngle, shankAngle, jointAngle, 2023);
                    }
                    break;
                case 2024:
                    timer = (int) msg.arg1;
                    Log.d(TAG, "                                                                 Got Timer" + timer);
                    SetMsgMy(count, actionCount, voice, timer, thighAngle, shankAngle, jointAngle, 2024);
                    break;
                case 2029:
                    if (msg.arg1 == 1) {
                        mButton.setText("继续训练");
                        Message msg9 = sthread.sensorAIHandler.obtainMessage(1025);
                        sthread.sensorAIHandler.sendMessage(msg9);
                        Log.d("2029", "                   Got 2029:  ");
                    }
                    break;
                case 2121:
                    String[] drawPict = {"K7", "K8", "K10", "K11", "K25", "K26", "K29", "K30"};
                    Map report = (Map) msg.obj;
                    shankAngle = (int) report.get("shankAngle") + 90;
                    if (Arrays.asList(drawPict).contains(actName)) {
                        thighAngle = (int) report.get("thighAngle");
                    } else {
                        thighAngle = (int) report.get("thighAngle") + 90;
                    }
                    jointAngle = (int) report.get("jointAngle");
//                    Log.d(TAG, "Got  report");
                    SetMsgMy(count, actionCount, voice, timer, thighAngle, shankAngle, jointAngle, 2121);
                    break;
                case 2131:
                    Map report2 = (Map) msg.obj;
                    shankAngle = (int) report2.get("shankAngle") + 90;
                    thighAngle = (int) report2.get("footAngle") + 75;
                    jointAngle = (int) report2.get("jointAngle");
//                    Log.d(TAG, "Got  report");
                    SetMsgMy(count, actionCount, voice, timer, thighAngle, shankAngle, jointAngle, 2121);
                    break;
                case 2151:
                    Map report3 = (Map) msg.obj;
                    shankAngle = (int) report3.get("footAngle");
                    thighAngle = (int) report3.get("thighAngle") + 90;
                    jointAngle = (int) report3.get("jointAngle");
//                    Log.d(TAG, "Got  report");
                    SetMsgMy(count, actionCount, voice, timer, thighAngle, shankAngle, jointAngle, 2121);
                    break;
                case 2141:
                    Map report1 = (Map) msg.obj;
                    shankAngle = (int) report1.get("angle");
                    thighAngle = (int) report1.get("angle");
//                    jointAngle = (int) report1.get("joint");
                    String fx;
                    if (msg.arg2 == 1) {
                        fx = "左";
                    } else {
                        fx = "右";
                    }
                    Log.d(TAG, "                                     腿角度：      " + thighAngle);
                    Log.d(TAG, "                                     哪条腿：      " + fx);

                    SetMsgMy(count, actionCount, voice, timer, thighAngle, shankAngle, jointAngle, 2121);
                    break;

            }
        }
    }

}