package com.witmoiton.example.sensor;

import android.os.Handler;
import android.os.Looper;

public class SensorThread extends Thread{

    public SensorAI sensorAIHandler;
    private Handler mainHandler;

    public SensorThread(Handler mainHandler){
        this.mainHandler = mainHandler;
    };

    @Override
    public void run() {
        super.run();
        Looper.prepare();
        this.sensorAIHandler = new SensorAI();
        this.sensorAIHandler.mainHandler = mainHandler;
        Looper.loop();
    }
}

