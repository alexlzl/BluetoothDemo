package com.witmoiton.example.prog;

import android.util.Log;

import java.util.Date;

public class Rule {

    public Long timer = 0L;
    public int diffTimer = 0;
    public int buffer = 0;
    public long bufferT = 0L;

    public void setTimer(Long timeStamp){
        timer = timeStamp;
    }

    public void setDiffTimer(int diff){
        diffTimer = diff;
    }
    public void setBufferTimerArea(int buf){
        buffer = buf;
    }
    public void setBufferTimer(Long bufT){bufferT = bufT;}


    public void reset(){
        timer = 0L;
        diffTimer = 0;
    };


    /*缓冲计时器*/
    public boolean bufferTimer(){
        Long currentTime = new Date().getTime();
        int diff = Math.round((currentTime - bufferT) / 1000);
        if(diff >= buffer + 1){
            return true;
        }
        return false;
    };

    /*另一个身体部位大于角度*/
    public boolean otherSegmentAnglesGreaterThan(int otherSensorAngle, int angle){
        if(otherSensorAngle >= angle){
            return true;
        }
        return false;
    };


    /*另一个身体部位小于角度*/
    public boolean otherSegmentAnglesLessThan(int otherSensorAngle, int angle){

        if(otherSensorAngle <= angle){
            return true;
        }
        return false;
    };

    /*身体部位大于角度*/
    public boolean segmentAnglesGreaterThan(int sensorAngle, int angle){
        if(sensorAngle >= angle){
            return true;
        }
        return false;
    };


    /*身体部位小于角度*/
    public boolean segmentAnglesLessThan(int sensorAngle, int angle){

        if(sensorAngle <= angle){
            return true;
        }
        return false;
    };


    /*关节大于角度*/
    public boolean jointAnglesGreaterThan(int sensorAngle, int angle){
        if(sensorAngle >= angle){
            return true;
        }
        return false;
    };



    /*关节部位小于角度*/
    public boolean jointAnglesLessThan(int sensorAngle, int angle){
        if(sensorAngle <= angle){
            return true;
        }
        return false;
    };



    /*计时器等于参数*/
    public boolean timerEqual(int params){
        if (timer == 0){
            if(timer == params){
                return true;
            };
        }else{
            Long currentTime = (Long) new Date().getTime();
            int diff = Math.round((currentTime - timer) / 1000) + diffTimer;
            if(diff == params){
                return true;
            }
        };
        return false;
    };



    /*计时器大于参数*/
    public boolean timerGreaterThan(int params){
        Long currentTime = (Long) new Date().getTime();
        int diff = Math.round((currentTime - timer) / 1000) + diffTimer;
        if(diff >= params){
            if (params == 30){
                Log.d("text", "error :                                ");
            }
            return true;
        }
        return false;
    };

    /*计时器小于参数*/
    public boolean timerLessThan(int params){
        Long currentTime = (Long) new Date().getTime();
        int diff = Math.round((currentTime - timer) / 1000) + diffTimer;
        if(diff < params){
            if (params == 5){
                Log.d("text", "error :                                ");
            }
            return true;
        }
        return false;
    };

    /*语音状态大于参数*/
    public boolean statusHoldingDuration(int sensorAngle, int diff, int params){
        if(diff >= params){
            return true;
        }
        return false;
    };


    /*关节角度与最大值差的值大于参数*/
    public boolean jointMaxAnglesDiffGreaterThan(int sensorAngle, int angle, int jointMax){
        if (jointMax !=0) {
            if (jointMax  - sensorAngle >= angle) {
                return true;
            }
        }
        return false;
    };


    /*关节角度与最大值差的值小于于参数*/
    public boolean jointMaxAnglesDiffLessThan(int sensorAngle, int angle,int jointMax){
        if (jointMax !=0) {

            if (jointMax - sensorAngle  <= angle) {
                return true;
            }
        }
        return false;
    };

    /*关节角度与最小值差的值大于参数*/
    public boolean jointMinAnglesDiffGreaterThan(int sensorAngle, int angle, int jointMin){
        if (jointMin !=370) {
            if (sensorAngle - jointMin >= angle) {
                return true;
            }
        }
        return false;
    };


    /*关节角度与最小值差的值小于参数*/
    public boolean jointMinAnglesDiffLessThan(int sensorAngle, int angle,int jointMin){
        if (jointMin !=370 ) {
            if (sensorAngle - jointMin <= angle) {
                return true;
            }
        }
        return false;
    };

    /*关节角度最大值差与最小差的值大于参数*/
    public boolean jointMaxAnglesDiffMinAngleGreaterThan(int angle, int jointMax, int jointMin){
        if (jointMax !=0 && jointMin != 370) {
            if (jointMax  - jointMin >= angle) {
                return true;
            }
        }
        return false;
    };

    /*关节角度最大值差与最小差的值小雨于参数*/
    public boolean jointMaxAnglesDiffMinAngleLessThan(int angle, int jointMax, int jointMin){
        if (jointMax !=0 && jointMin != 370) {
            if (jointMax  - jointMin <= angle) {
                return true;
            }
        }
        return false;
    };

    /*身体部位与最小值差的值大于参数*/
    public boolean segmentMaxAnglesDiffMinAngleGreaterThan(int angle,int sensorMax,int segmentMin){
        if (segmentMin !=370 && sensorMax != 0) {
            if (sensorMax - segmentMin >= angle) {
                return true;
            }
        }
        return false;
    };

    /*身体部位与最小值差的值大于参数*/
    public boolean segmentMaxAnglesDiffMinAngleLessThan(int angle,int sensorMax,int segmentMin){
        if (segmentMin !=370 && sensorMax != 0) {
            if (sensorMax - segmentMin <= angle) {
                return true;
            }
        }
        return false;
    };

    /*身体部位与最小值差的值大于参数*/
    public boolean segmentMinAnglesDiffGreaterThan(int sensorAngle, int angle,int segmentMin){
        if (segmentMin !=370 ) {
            if (sensorAngle - segmentMin >= angle) {
                return true;
            }
        }
        return false;
    };

    /*身体部位与最小值差的值小于参数*/
    public boolean segmentMinAnglesDiffLessThan(int sensorAngle, int angle,int segmentMin){
        if (segmentMin !=370 ) {
            if (sensorAngle - segmentMin <= angle) {
                return true;
            }
        }
        return false;
    };


    /*身体部位与最大值差的值大于参数*/
    public boolean segmentMaxAnglesDiffGreaterThan(int sensorAngle, int angle,int segmentMax){
        if (segmentMax !=0 ) {
            if(sensorAngle > 300){
                sensorAngle = 360 -sensorAngle;
            }
            if (segmentMax - sensorAngle >= angle) {
                return true;
            }
        }
        return false;
    };




    /*身体部位与最大值差的值小于参数*/
    public boolean segmentMaxAnglesDiffLessThan(int sensorAngle, int angle,int segmentMax){
        if (segmentMax !=0 ) {
            if(sensorAngle > 300){
                sensorAngle = 360 -sensorAngle;
            }
            if (segmentMax - sensorAngle  <= angle) {
                return true;
            }
        }
        return false;
    };


    /*另一个身体部位与最小值差的值大于参数*/
    public boolean otherSegmentMinAnglesDiffGreaterThan(int sensorAngle, int angle,int segmentMin){
        if (segmentMin !=370 ) {
            if (sensorAngle - segmentMin >= angle) {
                return true;
            }
        }
        return false;
    };

    /*另一个身体部位与最小值差的值小于参数*/
    public boolean otherSegmentMinAnglesDiffLessThan(int sensorAngle, int angle,int segmentMin){
        if (segmentMin !=370 ) {
            if (sensorAngle - segmentMin <= angle) {
                return true;
            }
        }
        return false;
    };


    /*另一个身体部位与最大值差的值大于参数*/
    public boolean otherSegmentMaxAnglesDiffGreaterThan(int sensorAngle, int angle,int segmentMax){
        if (segmentMax !=0 ) {
            if(sensorAngle > 300){
                sensorAngle = 360 -sensorAngle;
            }
            if (segmentMax - sensorAngle >= angle) {
                return true;
            }
        }
        return false;
    };




    /*另一个身体部位与最大值差的值小于参数*/
    public boolean otherSegmentMaxAnglesDiffLessThan(int sensorAngle, int angle,int segmentMax){
        if (segmentMax !=0 ) {
            if(sensorAngle > 300){
                sensorAngle = 360 -sensorAngle;
            }
            if (segmentMax - sensorAngle  <= angle) {
                return true;
            }
        }
        return false;
    };



    /*判断患者状态*/
    public boolean patientStatus(int currStatus, int actStatus){
        if (currStatus == actStatus){
            return true;
        }
        return false;
    }

    /*判断患者状态持续时间*/
    public boolean patientTimerGreaterThan(TimerModules timerM, int params){
        Long currentTime = new Date().getTime();
        int timeDiff = (int) Math.floor((currentTime - timerM.actTimer) / 1000);
        if (timeDiff >= params){
            return true;
        }
        return false;
    }

    public boolean patientTimerEqual(TimerModules timerM, int params){
        Long currentTime = new Date().getTime();
        int timeDiff = (int) Math.floor((currentTime - timerM.actTimer) / 1000);
        if (timeDiff == params){
            return true;
        }
        return false;
    }

    public boolean patientTimerLessThan(TimerModules timerM, int params){
        Long currentTime = new Date().getTime();
        int timeDiff = (int) Math.floor((currentTime - timerM.actTimer) / 1000);
        if (timeDiff <= params){
            return true;
        }
        return false;
    }

    public boolean exceptionGreaterThan(TimerModules timerM, int params){
        Long currentTime = new Date().getTime();
        int timeDiff = (int) Math.floor((currentTime - timerM.eRStatusTimer) / 1000);
        if (timeDiff >= params){
            return true;
        }
        return false;
    }

    public boolean exceptionLessThan(TimerModules timerM, int params){
        Long currentTime = new Date().getTime();
        int timeDiff = (int) Math.floor((currentTime - timerM.eRStatusTimer) / 1000);
        if (timeDiff < params){
            return true;
        }
        return false;
    }

    /*规则处理*/
    public boolean process(String ruleName, int ruleParam, int sensorAngle, int otherSensorAngle,
                           int jointAngle, int angleMax, int angleMin, int diffVoiceTimer,
                           int segmentMin, int segmentMax, TimerModules timerM, int patientStatusParam
    ){
        switch (ruleName){
            case "otherSegmentAnglesGreaterThan":
                return otherSegmentAnglesGreaterThan(otherSensorAngle, ruleParam);
            case "otherSegmentAnglesLessThan":
                return otherSegmentAnglesLessThan(otherSensorAngle, ruleParam);
            case "segmentAnglesGreaterThan":
                return segmentAnglesGreaterThan(sensorAngle, ruleParam);
            case "segmentAnglesLessThan":
                return segmentAnglesLessThan(sensorAngle, ruleParam);
            case "jointAnglesGreaterThan":
                return jointAnglesGreaterThan(jointAngle, ruleParam);
            case "jointAnglesLessThan":
                return jointAnglesLessThan(jointAngle, ruleParam);
            case "timerEqual":
                return timerEqual(ruleParam);
            case "timerGreaterThan":
                return timerGreaterThan(ruleParam);
            case "timerLessThan":
                return timerLessThan(ruleParam);
            case "statusHoldingDuration":
                return statusHoldingDuration(sensorAngle,diffVoiceTimer, ruleParam);
            case "jointMaxAnglesDiffGreaterThan":
                return jointMaxAnglesDiffGreaterThan(jointAngle, ruleParam, angleMax);
            case "jointMaxAnglesDiffLessThan":
                return jointMaxAnglesDiffLessThan(jointAngle, ruleParam, angleMax);
            case "jointMinAnglesDiffGreaterThan":
                return jointMinAnglesDiffGreaterThan(jointAngle, ruleParam, angleMin);
            case "jointMinAnglesDiffLessThan":
                return jointMinAnglesDiffLessThan(jointAngle, ruleParam, angleMin);
            case "segmentMinAnglesDiffGreaterThan":
                return segmentMinAnglesDiffGreaterThan(sensorAngle, ruleParam, segmentMin);
            case "segmentMinAnglesDiffLessThan":
                return segmentMinAnglesDiffLessThan(sensorAngle, ruleParam, segmentMin);
            case "segmentMaxAnglesDiffGreaterThan":
                return segmentMaxAnglesDiffGreaterThan(sensorAngle, ruleParam, segmentMax);
            case "segmentMaxAnglesDiffLessThan":
                return segmentMaxAnglesDiffLessThan(sensorAngle, ruleParam, segmentMax);
            case "otherSegmentMinAnglesDiffGreaterThan":
                return otherSegmentMinAnglesDiffGreaterThan(otherSensorAngle, ruleParam, segmentMin);
            case "otherSegmentMinAnglesDiffLessThan":
                return otherSegmentMinAnglesDiffLessThan(otherSensorAngle, ruleParam, segmentMin);
            case "otherSegmentMaxAnglesDiffGreaterThan":
                return otherSegmentMaxAnglesDiffGreaterThan(otherSensorAngle, ruleParam, segmentMax);
            case "otherSegmentMaxAnglesDiffLessThan":
                return otherSegmentMaxAnglesDiffLessThan(otherSensorAngle, ruleParam, segmentMax);
            case "patientStatus":
                return patientStatus(patientStatusParam, ruleParam);
            case "patientTimerGreaterThan":
                return patientTimerGreaterThan(timerM, ruleParam);
            case "patientTimerLessThan":
                return patientTimerLessThan(timerM, ruleParam);
            case "patientTimerEqual":
                return patientTimerEqual(timerM, ruleParam);
            case "exceptionGreaterThan":
                return exceptionGreaterThan(timerM, ruleParam);
            case "exceptionLessThan":
                return exceptionLessThan(timerM, ruleParam);
            case "bufferTimer":
                return bufferTimer();
            case "jointMaxAnglesDiffMinAngleGreaterThan":
                return jointMaxAnglesDiffMinAngleGreaterThan(ruleParam,angleMax,angleMin);
            case "jointMaxAnglesDiffMinAngleLessThan":
                return jointMaxAnglesDiffMinAngleLessThan(ruleParam,angleMax,angleMin);
            case "segmentMaxAnglesDiffMinAngleGreaterThan":
                return segmentMaxAnglesDiffMinAngleGreaterThan(ruleParam,segmentMax,segmentMin);
            case "segmentMaxAnglesDiffMinAngleLessThan":
                return segmentMaxAnglesDiffMinAngleLessThan(ruleParam,segmentMax,segmentMin);
        }
        return false;
    };
}
