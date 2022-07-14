package com.witmoiton.example.prog;

public class TimerModules {
    public Long globalTimer = 0L;
    public Long actTimer = 0L;
    public Long eRStatusTimer = 0L;
    public Long loopTimer = 0L;
    public Long savePauseTimer = 0L;
    public Long staticStatusTimer = 0L;
    public void setTimer(Long timeStamp){
        globalTimer = timeStamp;
    }
    public void setActTimer(Long timeStamp){
        actTimer = timeStamp;
    }
    public void setERStatusTimer(Long timeStamp){
        eRStatusTimer = timeStamp;
    }
    public void setLoopTimer(Long timeStamp){
        loopTimer = timeStamp;
    }

    public void setSavePauseTimer(Long timeStamp){
        savePauseTimer = timeStamp;
    }
    public void setStaticStatusTimer(Long timeStamp){
        staticStatusTimer= timeStamp;
    }

    public void reset(){
//        globalTimer = 0L;
        actTimer = 0L;
        eRStatusTimer = 0L;
        loopTimer = 0L;
        savePauseTimer = 0L;
    }
}
