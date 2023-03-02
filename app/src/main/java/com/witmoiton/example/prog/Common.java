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
        return angleY;

    };

    public double getAngleYZAngle1(Map vec){
        float ax = (float) vec.get("ax");
        float ay = (float) vec.get("ay");
        float az = (float) vec.get("az");

        double angleY = radToDeg( Math.acos(ay / Math.sqrt(ay * ay + az * az)) );
        double angleZ = radToDeg( Math.asin(az / Math.sqrt(ax * ax + ay * ay + az * az)) );
        if (angleZ <0){
            angleY = 360 - angleY;
        };
        return angleY;


    };

    public double getAngleYZAngle2(Map vec, Map vecThigh){
        float ax = (float) vec.get("ax");
        float ay = (float) vec.get("ay");
        float az = (float) vec.get("az");
        float axT = (float) vecThigh.get("ax");

        double g2 = ax * ax + ay * ay + az * az;
        double shank = (1- az*az / g2) * Math.acos(ay/Math.sqrt(ay*ay + az * az));
        double g2Diff = g2 - axT*axT;
//        Log.d("common", "g2Diff:" + g2Diff);
        if(g2Diff< 0){
            g2Diff = -g2Diff;
        }
        double acosMax = ay/Math.sqrt(g2Diff);
        if (acosMax > 1){
            acosMax = 1;
        }
        double thigh = (az*az/g2) * Math.acos(acosMax);

        double angleY = radToDeg(shank + thigh);
        if (az <0){
            angleY = 360 - angleY;
        };
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
