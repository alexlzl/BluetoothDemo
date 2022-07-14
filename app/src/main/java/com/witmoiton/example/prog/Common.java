package com.witmoiton.example.prog;

import java.util.Date;
import java.util.Map;

public class Common {

    public double radToDeg(double rad){
        double result = rad * 180 / Math.PI;
        return result;
    };


    public double getW(Map vec){
        float wx = (float) vec.get("wx");
        float wy = (float) vec.get("wy");
        float wz = (float) vec.get("wz");

        double w = Math.sqrt(wx * wx + wy * wy + wz * wz);
       return w;

    };

    public double getAngleY(Map vec){
        float ax = (float) vec.get("ax");
        float ay = (float) vec.get("ay");
        float az = (float) vec.get("az");

        double angleY = radToDeg( Math.acos(ay / Math.sqrt(ax * ax + ay * ay + az * az)) );
        double angleZ = radToDeg( Math.asin(az / Math.sqrt(ax * ax + ay * ay + az * az)) );
        if (angleZ <0){
            angleY = 360 - angleY;
        };
//        double angle = radToDeg( Math.asin(ay / Math.sqrt(ax * ax + ay * ay + az * az)) );
        return angleY;

    };

    public double getAngleX(Map vec){
        float ax = (float) vec.get("ax");
        float ay = (float) vec.get("ay");
        float az = (float) vec.get("az");

        double angleX = radToDeg( Math.acos(ax / Math.sqrt(ax * ax + ay * ay + az * az)) );
        double angleZ = radToDeg( Math.asin(az / Math.sqrt(ax * ax + ay * ay + az * az)) );
        if (angleZ >90){
            angleX = 360 - angleX;
        };
        return angleX;

    };

    public double getAngleZ(Map vec){
        float ax = (float) vec.get("ax");
        float ay = (float) vec.get("ay");
        float az = (float) vec.get("az");
        double angleZ = radToDeg( Math.asin(az / Math.sqrt(ax * ax + ay * ay + az * az)) );
        return angleZ;

    };

    public int getVoiceIndex(int actionCount, int voiceIndex, int targetCount){
        if (actionCount == 0){
            voiceIndex = 0;
        }else if(actionCount == 1){
            voiceIndex = 1;
        }else if(targetCount > 3 &&targetCount - actionCount == 2){
            voiceIndex = 3;
        }else if(targetCount > 3 && targetCount - actionCount == 1){
            voiceIndex = 4;
        }else{
            voiceIndex = 2;
        };
        return voiceIndex;
    };

    public boolean getIsVoiceStatus(boolean isVoice, Long statusTimer){
        Long current = (Long) new Date().getTime();
        int diff = (int) Math.round((current - statusTimer)/ 1000);
        if (diff > 1.5){
            isVoice = true;
        }
        return isVoice;
    };


}
