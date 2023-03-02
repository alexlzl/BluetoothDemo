package com.witmoiton.example.sensor;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.witmoiton.example.prog.TrainingProgram;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class SensorAI extends Handler {
    public Handler mainHandler;
    public int sensorStatus = 0;//0:初始，1：开始训练，2：暂停训练，3：停止训练；
    private static final String TAG = "SensorAI";
    public TrainingProgram prog;
    public Map sensorTags = new HashMap();//sensorTags = {1(传感器编号): "thigh"}
    public Map sensorIdByTag = new HashMap();// sensorIdByTag = {"thigh": 1(传感器编号)}
    public Map sensorSetups = new HashMap();//sensorSetups = {1(传感器编号):"大腿"(bodySite), 2(传感器编号):"小腿"}
    public Map sensorVecByTag = new HashMap();// sensorVecByTag = {"thigh": data}
    public String repeatName = "";

    //监听各个环节数据
    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
//            case 1101:
//                Log.d(TAG, " child thread receiver message: " + ((String) msg.obj));
//                try{
//                    Log.d(TAG, " sleep 1s");
//                    Thread.sleep(1000);
//                }catch (InterruptedException e){
//                    e.printStackTrace();
//                }
//                Message sensorMsg = mainHandler.obtainMessage(2101);
//                sensorMsg.obj = "SensorAI send message of Hello World for you ";
//                mainHandler.sendMessage(sensorMsg);
//                break;

            case 1011:
                Log.d(TAG, " 1011 Add sensor :" + msg.arg1);
                String sName;

                if (!repeatName.equals(msg.obj)) {
                    sName = (String) msg.obj;
                    if (msg.arg1 != 1) {
                        sName = msg.obj + "1";
                    }
                    repeatName = sName;
                } else {
                    sName = msg.obj + "1";
                }
                sensorSetups.put(msg.arg1, sName);
                if (sensorReady()) {
                    Message readyMessage = mainHandler.obtainMessage(2021);
                    readyMessage.obj = "Sensor Ready!!! ";
                    mainHandler.sendMessage(readyMessage);
                }
                ;
                break;

            case 1012:
                Log.d(TAG, " 1012 Remove sensor");
                sensorSetups.remove(msg.arg1);
                break;

            case 1013:
//                Log.d(TAG, " Update sensor: " + msg.arg1);
                Long sensorTimeStamp = 0L;
                Map data = (Map) msg.obj;
                sensorTimeStamp = (Long) data.get("timeStamp");
                Object raw = data.get("raw");
                try {
                    updateSensorValue(msg.arg1, sensorTimeStamp, raw);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case 1113:
//                Log.d(TAG, " Update sensor: " + msg.arg1);
                Long sensorTimeStamp1 = 0L;
                Map data1 = (Map) msg.obj;
                sensorTimeStamp1 = (Long) data1.get("timeStamp");
                Object raw1 = data1.get("raw");
                try {
                    updateSensorValue1(msg.arg1, sensorTimeStamp1, raw1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case 1021:
                Log.d(TAG, " 1021 Setting training program name");
                if (msg.obj != null) {

                    this.prog = new TrainingProgram(msg.arg2, msg.obj);
                    reset();
                    this.prog.loadHandler(mainHandler);
                }
                break;

            case 1022:
                Log.d(TAG, " 1022 Loading training program");
                if (msg.obj != null && this.prog != null) {

                    this.prog.loadProgram((Map) msg.obj);

                }
                break;
            case 1024:
                Log.d(TAG, " 1024 Start traning program");
                if (this.prog != null) {
                    sensorStatus = 1;
                }
                break;
            case 1025:
                Log.d(TAG, " 1025 Pause training program");
                if (this.prog != null) {
                    sensorStatus = 2;
                    prog.pause();
                }
                break;
            case 1026:
                Log.d(TAG, " 1026 Continue training program");
                if (this.prog != null) {
                    sensorStatus = 1;
                }
                break;
            case 1027:
                Log.d(TAG, " 1027 Stop training program");
                if (this.prog != null) {
                    sensorStatus = 3;
                    prog.reset();
                    sensorStatus = 0;
                }
                break;

        }
    }

    //重置训练
    public void reset() {
        sensorTags = new HashMap();
        sensorIdByTag = new HashMap();
        sensorSetups = new HashMap();
        sensorVecByTag = new HashMap();
        sensorStatus = 0;
        repeatName = "";
    }

    ;

    //更新传感器数据
    public void updateSensorValue1(int id, Long timestamp, Object raw) throws JSONException {
        if (!sensorTags.containsKey(id)) {
            return;
        }
        Object tag = sensorTags.get(id);
        Map sensorData = parseSensorData1((byte[]) raw);
        sensorData.put("timestamp", timestamp);
        sensorVecByTag.put(tag, sensorData);
        int swx = (int) Math.round((float) sensorData.get("wx"));
        if (swx > 110 || swx < -110) {
            return;
        }

        // 是否所有传感器都已经有读数，并且在近期有回传数据
        Long oldestTimestamp = 8640000000000000L;
        Long newestTimestamp = -8640000000000000L;
        for (Object key : sensorIdByTag.keySet()) {
            if (!sensorVecByTag.containsKey(key)) {
                return;
            }
            if ((Long) sensorData.get("timestamp") < oldestTimestamp) {
                oldestTimestamp = (Long) sensorData.get("timestamp");
            }
            ;
            if ((Long) sensorData.get("timestamp") > newestTimestamp) {
                newestTimestamp = (Long) sensorData.get("timestamp");
            }
            ;
        }

        if (newestTimestamp - oldestTimestamp > 1000) {
            Log.d(TAG, " SensorFailure : have sensor disconnect!");
            return;
        }

        //开始训练回传信号
        if (sensorStatus == 1) {
            prog.process(sensorVecByTag);


        } else if (sensorStatus == 2) {
            prog.pause();
        } else if (sensorStatus == 3) {
            prog.reset();
            sensorStatus = 0;
        }
        ;

    }

    //更新传感器数据
    public void updateSensorValue(int id, Long timestamp, Object raw) throws JSONException {
        if (!sensorTags.containsKey(id)) {
            return;
        }
        Object tag = sensorTags.get(id);
        Map sensorData = parseSensorData((byte[]) raw);
        sensorData.put("timestamp", timestamp);
        sensorVecByTag.put(tag, sensorData);
        int swx = (int) Math.round((float) sensorData.get("wx"));
        if (swx > 110 || swx < -110) {
            return;
        }

        // 是否所有传感器都已经有读数，并且在近期有回传数据
        Long oldestTimestamp = 8640000000000000L;
        Long newestTimestamp = -8640000000000000L;
        for (Object key : sensorIdByTag.keySet()) {
            if (!sensorVecByTag.containsKey(key)) {
                return;
            }
            if ((Long) sensorData.get("timestamp") < oldestTimestamp) {
                oldestTimestamp = (Long) sensorData.get("timestamp");
            }
            ;
            if ((Long) sensorData.get("timestamp") > newestTimestamp) {
                newestTimestamp = (Long) sensorData.get("timestamp");
            }
            ;
        }

        if (newestTimestamp - oldestTimestamp > 1000) {
            Log.d(TAG, " SensorFailure : have sensor disconnect!");
            return;
        }

        //开始训练回传信号
        if (sensorStatus == 1) {
            prog.process(sensorVecByTag);


        } else if (sensorStatus == 2) {
            prog.pause();
        } else if (sensorStatus == 3) {
            prog.reset();
            sensorStatus = 0;
        }
        ;

    }

    //判断传感器是否准备就绪
    public boolean sensorReady() {
        if (prog != null) {
            for (int i = 0; i < prog.sensorTable.size(); i++) {
                Map s = prog.sensorTable.get(i);
                if (!sensorIdByTag.containsKey(s.get("tag"))) {
                    for (Object key : sensorSetups.keySet()) {
                        if (sensorSetups.get(key) != null && sensorSetups.get(key).equals(s.get("bodySite"))) {
                            sensorTags.put(key, s.get("tag"));
                            sensorIdByTag.put(s.get("tag"), key);
                            break;
                        }

                    }
                    if (!sensorIdByTag.containsKey(s.get("tag"))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    ;

    //传感器数据解析
    public Map parseSensorData(byte[] value) {
        Map result = new HashMap();
        if (value[0] != 0x55) {
            return null;
        }
        switch (value[1]) {

            case 0x61:
                //加速度数据
                float ax = ((((short) value[3]) << 8) | ((short) value[2] & 0xff)) / 32768.0f * 16;   //x轴
                float ay = ((((short) value[5]) << 8) | ((short) value[4] & 0xff)) / 32768.0f * 16;   //y轴
                float az = ((((short) value[7]) << 8) | ((short) value[6] & 0xff)) / 32768.0f * 16;   //z轴
                //角速度数据
                float wx = ((((short) value[9]) << 8) | ((short) value[8] & 0xff)) / 32768.0f * 2000;  //x轴
                float wy = ((((short) value[11]) << 8) | ((short) value[10] & 0xff)) / 32768.0f * 2000;  //x轴
                float wz = ((((short) value[13]) << 8) | ((short) value[12] & 0xff)) / 32768.0f * 2000;  //x轴
                //角度
                float roll = ((((short) value[15]) << 8) | ((short) value[14] & 0xff)) / 32768.0f * 180;   //x轴
                float pitch = ((((short) value[17]) << 8) | ((short) value[16] & 0xff)) / 32768.0f * 180;   //y轴
                float yaw = ((((short) value[19]) << 8) | ((short) value[18] & 0xff)) / 32768.0f * 180;   //z轴
                result.put("ax", ax);
                result.put("ay", ay);
                result.put("az", az);
                result.put("wx", wx);
                result.put("wy", wy);
                result.put("wz", wz);
                result.put("roll", roll);
                result.put("pitch", pitch);
                result.put("yaw", yaw);
                break;
            case 0x62:


                break;
        }


        return result;
    }

    ;

    public Map parseSensorData1(byte[] value) {
        Map result = new HashMap();

        float ax = ((((short) value[1]) << 8) | ((short) value[0] & 0xff)) * 0.488f;   //x轴
        float ay = ((((short) value[3]) << 8) | ((short) value[2] & 0xff)) * 0.488f;   //y轴
        float az = ((((short) value[5]) << 8) | ((short) value[4] & 0xff)) * 0.488f;   //z轴

        float wx = ((((short) value[6]) << 8) | ((short) value[7] & 0xff)) * 0.07f;  //x轴
        float wy = ((((short) value[8]) << 8) | ((short) value[9] & 0xff)) * 0.07f;  //x轴
        float wz = ((((short) value[10]) << 8) | ((short) value[11] & 0xff)) * 0.07f;  //x轴


        result.put("ax", ax);
        result.put("ay", ay);
        result.put("az", az);
        result.put("wx", wx);
        result.put("wy", wy);
        result.put("wz", wz);

        return result;
    }

}
