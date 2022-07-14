package com.witmoiton.example.prog;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class TrainingProgram {
    private static final String TAG = "TrainingProgramUpDate";
    public Handler mainHandler;
    public String[] baseAngleName = {"K1","K21","K22","K16","K19","K9","K20","K17","K16","K18"};//K17
    public String[] drawPictureName = {"K7","K8","K10","K11","K25","K26","K29","K30","K19","K20","K9","K27","K31"};
    public String[] responseSpecialName = {"K25","K11","K26", "K29","K30", "K27","K31"};
    public String[] sensorSideMatchSpecialName = {"K1","K5","K6","K11","K25","K26","K12","K13","K9"};
    public String[] twiceCountName = {"K5","K6","K11","K10","K25","K26"};
    public String[] abnormalAction = {"K16","K17","K18"};


    public Common common = new Common();                    //通用算法类
    public Rule ruleClass = new Rule();                     //规则类
    public TimerModules timerClass = new TimerModules();                  //计时器类


    public Map ruleTableMap = new HashMap();                //规则表
    public Map report = new HashMap();                      //回传需要记录的值
    public Map statusData;                                  //透传过来的状态
    public List<Map> sensorTable = new ArrayList<>();       //传感器对应命名
    public Map transTableMap = new HashMap();               //状态表


    public int actionCount = 0;              //计次
    public int angleMax = 0;                 //关节最大值
    public int angleMin = 370;               //关节最小值
    public int twiceCountSecond = 0;         //两次计数动作第二次完成计次数
    public int twiceCountFirst = 0;         //两次计数动作第一次完成计次数
    public int twiceCountSecondInP = 0;      //两次计数动作第二次完成标准数
    public int twiceCountFirstInP = 0;      //两次计数动作第一次完成标准数
    public int baseAngle;                    //主要监控部位角度
    public int otherAngle;                   //次要监控部位角度
    public int countVoice = 0;               //完成一次之后计次，用于选取状态表中voicePrompt的值
    public int holdDuration = 0;             //记录每次的完成时间
    public int injuredSegment = 0;           //患者手术侧别
    public int injSeg = 0;                   //相应时侧别
    public int inPlaceActionCount = 0;       //记录标准完成次数
    public int jointExtendMaxAngle = 0;      //关节伸最大角度
    public int jointFlexedMaxAngle = 0;      //关节屈角度与水平面夹角
    public int lastTime;                     //计时器最后一次整数时间，记录传输到android端的
    public String lastStatus = "初始状态";     //最后一次状态，避免频繁发射信号
    public int onceStatus;                   //完成一次动作状态
    public int segmentMax = 0;               //角度抓取部位最大值
    public int segmentMin = 370;             //角度抓取部位最小值
    public Long sendVoiceTimer = 0L;         //状态计时器，避免状态之间语音冲突
    public int sid = 0;                      //相应时传感器编号
    public String status = "初始状态";         //状态
    public int sumActionCount = 0;                //总数
    public int targetCount;                  //每组动作目标完成次数
    public int transCount = 0;               //转换左右或前后计次时的计次数
    public Object trainingName;              //训练动作名字
    public String voice;                     //当前发送的语音文字
    public int voiceIndex = 0;               //用于选取状态表中voicePrompt的索引值







    public TrainingProgram(int injuredArea, Object name) {
        this.injuredSegment = injuredArea;
        this.trainingName = name;
        setSensorTable((String) this.trainingName);


    }

    public int patientActionStatus = 0; // 1：静止，2：上升，3：下降
    public int beforeAngle = 0;
    public int errorReportStatus = 0;//异常错误播报状态
    public int loopReport = 0;//判断是否需要循环播报
    public String previousStatus = "";//记录异常前状态
    public int loopTimerArea = 0;//从状态表中获取的时间范围
    public String loopVocie = "";//要循环的语音
    public int sensorError = 1;
    public int old = 0;
    public int thighStatic = 0;
    public int shankStatic = 0;



    /*
    循环播报语音
    */
    public void loopPlayVoice(int loopStatus, String loopsContent){
        if(loopStatus != 0){
            if(loopReport != 1) {
                loopReport = 1;
                loopTimerArea = loopStatus;
                loopVocie = loopsContent;
                Long currentTime = new Date().getTime();
                timerClass.setLoopTimer(currentTime);

            }
        }else{
            if (loopReport == 1 && !status.equals("不配合")){
                loopReport = 0;
                timerClass.setLoopTimer(0L);
                loopTimerArea = 0;
                loopVocie = "";

            }
        }
    }

    public void loopCriterion(){
        Long currentTime = new Date().getTime();
        double timeDiff = (double) (currentTime - timerClass.loopTimer) / 1000;
        int diff = (int) Math.floor(timeDiff);
        if (diff >= loopTimerArea){
            timerClass.setLoopTimer(currentTime);
            Message voiceStatus = mainHandler.obtainMessage(2023);
            voiceStatus.obj = loopVocie;
            mainHandler.sendMessage(voiceStatus);
            sendVoiceTimer = new Date().getTime();
            int bufferTime = (int) Math.round((double) loopVocie.length() * 0.3);
            ruleClass.setBufferTimerArea(bufferTime);
            ruleClass.setBufferTimer(new Date().getTime());
        }

    }


    /*
    异常上报处理(在改变状态之前使用）1：开启判断，2上报异常，3重制状态
    */
    public void exceptionReport(int eRStatus){
        if (eRStatus == 1){
            if(errorReportStatus == 0){
                errorReportStatus = 1;
                Long currentTime = new Date().getTime();
                timerClass.setERStatusTimer(currentTime);
                previousStatus = status;
                timerClass.setSavePauseTimer(ruleClass.timer);
            }

        }else{
            errorReportStatus = 0;
            if(timerClass.eRStatusTimer != 0){
                timerClass.setERStatusTimer(0L);
                previousStatus = "";
                ruleClass.setTimer(timerClass.savePauseTimer);
            }
            if(eRStatus == 2){
                Message reportMsg = mainHandler.obtainMessage(2029);
                reportMsg.arg1 = 3;
                mainHandler.sendMessage(reportMsg);
            }

        }
    }
    public double leastSquares(){
        int size = saveTimeStamp.size();
        int meanAngle =0;
        Long meanTime=0L;
        for (int i = 0; i< size; i++){
            meanAngle += (int) saveAngleStamp.get(i);
            meanTime += (Long) saveTimeStamp.get(i);
        }
        meanAngle /= size;
        meanTime /= size;

        double numerator = 0L;
        double denominator = 0L;
        for (int i = 0; i< size; i++){
            Long diff = (Long) saveTimeStamp.get(i) - meanTime ;
            numerator += diff *((int) saveAngleStamp.get(i) - meanAngle);
            denominator += diff * diff;
        }

        double result = numerator/denominator * 1000;
        return result;

    }

    /*
    判断上升或下降函数
    */
    public void angleStatus(int currentAngle){
        //TODO：简单的屈曲度限制
        //TODO：任意状态
        Long currentTime = new Date().getTime();
        double timeDiff = (double) (currentTime - timerClass.globalTimer) / 1000;
        int currStatus;
//        Log.d("leastSquar", " timeDiff:                                " + timeDiff);
//        Log.d("leastSquar", " currentAngle:                                " + currentAngle);
//        Log.d("leastSquar", " beforeAngle:                                " + beforeAngle);
        if (timeDiff > 0.1){
            timerClass.setTimer(currentTime);
            //判断与上一次角度的位置关系
            if (beforeAngle == 0){
                currStatus = 2;
                patientActionStatus = 2;
                timerClass.setActTimer(currentTime);
            }else{
                int diffStandardArea = 5;
//                Log.d("leastSquar", "------------------------------------------------------------------------" );Log.d("leastSquar", " timeStack:                                " + saveTimeStamp);
//                Log.d("leastSquar", " angleStack:                                " + saveAngleStamp);
                double ls = leastSquares();
                Log.d("leastSquar", " leastSquar:                                " + String.format("%.5f", ls));
                if (ls <= diffStandardArea && ls >= -diffStandardArea){
                    currStatus = 1;
                }else if (ls > diffStandardArea){
                    currStatus = 3;
                }else if (ls < -diffStandardArea){
                    currStatus = 2;
                }else {
                    currStatus = patientActionStatus;
                };
            };

            //患者状态持续时间
            beforeAngle = currentAngle;
            if (currStatus != patientActionStatus){
                Log.d("leastSquar", " currStatus:                                " + currStatus);
                Log.d("leastSquar", " patientActionStatus:                                " + patientActionStatus);
                timerClass.setActTimer(currentTime);
                patientActionStatus = currStatus;
            }
//            Log.d("leastSquar", " beforeAngle2:                                " + beforeAngle);
        }
    };

    /*
    加载main进程的Handle
    */
    public void loadHandler(Handler mainHandler){
        this.mainHandler = mainHandler;
    };


    /*
    加载状态表和规则表
    */
    //{"初始状态":{1:{ruleName:"计时器初始0秒",nextStatus:"准备状态",incrCounter:0,timerOp:1,replayStatus:0,endAct:0,voicePrompt:[]}}}
    public void loadProgram(Map transformMap){

        //初始化全局计时器
        Long currentTime = new Date().getTime();
        timerClass.setTimer(currentTime);

        statusData = transformMap;
        targetCount = getInter((double) statusData.get("RepNum")) ;

        //整理状态转换数据
        List transTabList = (List) statusData.get("state");
        for (int i = 0; i < transTabList.size();i++){
            List row = (List) transTabList.get(i);
            String trsTabStatus = (String) row.get(0);
            if(!transTableMap.containsKey(trsTabStatus)){
                Map ruleMaps = new HashMap();
                transTableMap.put(trsTabStatus, ruleMaps);
            }
            int ruleNo = getInter((double) row.get(2));
            Map ruleMaps = (Map) transTableMap.get(trsTabStatus);
            if(!ruleMaps.containsKey(ruleNo)){
                Map rule = new HashMap();
                rule.put("ruleName",row.get(1));
                rule.put("nextStatus",row.get(3));
                rule.put("incrCounter",getInter((double) row.get(4)));
                rule.put("timerOp",getInter((double) row.get(5)));
                rule.put("endAct",getInter((double) row.get(6)));
                if(row.size() > 8){
                    rule.put("loopPlayVoice",getInter((double)row.get(7)));
                    rule.put("exception",getInter((double) row.get(8)));
                    rule.put("resetActState",getInter((double)row.get(9)));
                    rule.put("voicePrompt",row.get(10));
                }else {

                    rule.put("voicePrompt", row.get(7));
                    old = 1;
                }
                ruleMaps.put(ruleNo, rule);
            }
        }

//        {"计时器初始0秒":{"ruleFunction":[],"ruleParams":[]}}
//        整理规则数据

        List ruleTabList = (List) statusData.get("rule");
        for (int i = 0; i < ruleTabList.size();i++){
            List actRow = (List) ruleTabList.get(i);
            String ruleName = (String) actRow.get(0);
            if(!ruleTableMap.containsKey(ruleName)){
                Map rule = new HashMap();
                List<String> name = new ArrayList<>();
                rule.put("ruleFunction", name);
                List<String> ruleParams = new ArrayList<>();
                rule.put("ruleParams", ruleParams);
                ruleTableMap.put(ruleName, rule);
            };
            Map rule = (Map) ruleTableMap.get(ruleName);
            List name = (List) rule.get("ruleFunction");
            name.add(actRow.get(1));
            List params = (List) rule.get("ruleParams");
            params.add(getInter((String) actRow.get(2)));

        }
    }


    /*
    暂停
     */
    public void pause(){
        status = "初始状态";
        baseAngle = 0;
        otherAngle = 0;
        onceStatus = -1;
        voice = "";
        lastTime = 0;
        sendVoiceTimer = 0L;
        lastStatus = "初始状态";
        ruleClass.reset();
        timerClass.reset();
        twiceCountSecond = 0;
        twiceCountFirst = 0;
        twiceCountSecondInP = 0;
        twiceCountFirstInP = 0;
        angleMax = 0;
        angleMin = 370;
        segmentMin = 370;
        segmentMax = 0;
        transCount = 0;
        sid =0;
        injSeg =0;

        patientActionStatus = 0; // 1：静止，2：上升，3：下降
        beforeAngle = 0;
        errorReportStatus = 0;//异常错误播报状态
        loopReport = 0;//判断是否需要循环播报
        previousStatus = "";//记录异常前状态
        loopTimerArea = 0;//从状态表中获取的时间范围
        loopVocie = "";//要循环的语音
        sensorError = 1;
        wTimer =0L;
        saveAngleStamp = new ArrayList<>();
        saveTimeStamp = new ArrayList<>();
        saveWAngle1 = new ArrayList<>();
        saveWAngle2 = new ArrayList<>();
//        shankStatic = 0;
//        thighStatic = 0;
        timerClass.reset();
    }

    /*
    重置
     */
    public void reset(){
        pause();
        shankStatic = 0;
        thighStatic = 0;
        timerClass.setStaticStatusTimer(0L);
        voiceIndex = 0;
        actionCount = 0;
        sumActionCount = 0;
        inPlaceActionCount = 0;

        angleMax = 0;
        angleMin = 370;
        jointExtendMaxAngle = 0;
        jointFlexedMaxAngle = 0;
        holdDuration = 0;
        report = new HashMap();

    }

    /*
     * 生成baseAngle数据和患侧与传感器关系
     * */
    public void getBaseAngle(int angle1, int angle2){
        if (Arrays.asList(baseAngleName).contains(this.trainingName)){
            baseAngle = angle2;
            otherAngle = angle1;
        } else {
            int injAngle = angle1;
            int unInjAngle = angle2;
//            if (trainingName.equals("K11")) {
//
//                if (injuredSegment == 1) {
//
//                    if (twiceCountFirst == 1) {
//                        baseAngle = unInjAngle;
//                        otherAngle = injAngle;
//                        sid = 2;
//                        injSeg = 2;
//                    } else {
//                        baseAngle = injAngle;
//                        otherAngle = unInjAngle;
//                        sid = 1;
//                        injSeg = 1;
//                    }
//                }else if(injuredSegment == 2){
//
//                    if (twiceCountFirst == 1) {
//                        baseAngle = injAngle;
//                        otherAngle = unInjAngle;
//                        sid = 1;
//                        injSeg = 2;
//                    } else {
//                        baseAngle = unInjAngle;
//                        otherAngle = injAngle;
//                        sid = 2;
//                        injSeg = 1;
//                    }
//                }
//
//
//            }
            if (trainingName.equals("K11") || trainingName.equals("K25") || trainingName.equals("K26")) {

                if (injuredSegment == 1) {
                    baseAngle = injAngle;
                    otherAngle = unInjAngle;
                    if (twiceCountFirst == 1) {
                        sid = 2;
                        injSeg = 2;
                    } else {
                        sid = 1;
                        injSeg = 1;
                    }
                }else if(injuredSegment == 2){
                    baseAngle = unInjAngle;
                    otherAngle = injAngle;
                    if (twiceCountFirst == 1) {
                        sid = 1;
                        injSeg = 2;
                    } else {
                        sid = 2;
                        injSeg = 1;
                    }
                }


            }
//            else if(trainingName.equals("K25") ||trainingName.equals("K26")){
//                if(injuredSegment == 1) {
//                    if (actionCount % 2 == 1 && transCount % 2 == 1) {
//                        baseAngle = unInjAngle;
//                        otherAngle = injAngle;
//                        sid = 2;
//                        injSeg = 2;
//                    } else {
//                        baseAngle = injAngle;
//                        otherAngle = unInjAngle;
//                        sid = 1;
//                        injSeg = 1;
//                    }
//                }else if(injuredSegment == 2){
//                    if (actionCount % 2 == 1 && transCount % 2 == 1) {
//                        baseAngle = injAngle;
//                        otherAngle = unInjAngle;
//                        sid = 1;
//                        injSeg = 2;
//                    } else {
//                        baseAngle = unInjAngle;
//                        otherAngle = injAngle;
//                        sid = 2;
//                        injSeg = 1;
//                    }
//                }
//            }
            else if(trainingName.equals("K29") || trainingName.equals("K30") || trainingName.equals("K27")
                    || trainingName.equals("K31")){
                baseAngle = angle1;
                otherAngle = angle2;
                sid = 1;
                injSeg = injuredSegment;
            }else if(trainingName.equals("K10")){
                if (angle1 >= 270 && angle1 <= 360){
                    baseAngle = angle1-360;
                }else{
                    baseAngle = angle1;
                }
                otherAngle = angle2;
            }
            else{
                baseAngle = angle1;
                otherAngle = angle2;
            }
        }
    }


    /*
     * 记录关节角度等数据；
     * */
    public void recordJointAngle(int jointAngle){
        if (baseAngle < segmentMin && status.equals("角度抓取")) {
            segmentMin= baseAngle;
        }

        if (baseAngle > segmentMax && status.equals("角度抓取")) {
            segmentMax = baseAngle;
        }

        if (!(trainingName.equals("K5") || trainingName.equals("K6") )
                && (jointAngle > angleMax && jointAngle < 200)) {
            angleMax = jointAngle;
        }

        if (!(trainingName.equals("K5") || trainingName.equals("K6") )
                && (jointAngle < angleMin && jointAngle > 20)) {
            angleMin = jointAngle;
        }
        if ((trainingName.equals("K5") && status.equals("中立位抓取")) || (trainingName.equals("K6") && status.equals("中立位抓取"))
                || (trainingName.equals("K10") && status.equals("中立位抓取"))|| (trainingName.equals("K7") && status.equals("中立位抓取"))
                || (trainingName.equals("K8") && status.equals("中立位抓取"))
                || (trainingName.equals("K25") && status.equals("中立位抓取"))|| (trainingName.equals("K26") && status.equals("中立位抓取"))
                || (trainingName.equals("K9") && status.equals("中立位抓取"))
        ) {
            if (jointAngle > angleMax  && patientActionStatus == 1) {
                angleMax = jointAngle;
                angleMin = jointAngle;
            }
            if (segmentMax == 0) {
                segmentMax = baseAngle;
                segmentMin = baseAngle;
            }else if (baseAngle > segmentMax && patientActionStatus == 1){
                segmentMax = baseAngle;
                segmentMin = baseAngle;
            }
        }else{
            if (status.equals("第二次静止")|| status.equals("标准")|| status.equals("计数")
                    || status.equals("不计数")
            ) {
                if (jointAngle > jointExtendMaxAngle && jointAngle <= 180 && jointAngle >= 10) {
                    jointExtendMaxAngle = jointAngle;
                }
            }
        }

//        if ((trainingName.equals("K5") && status.equals("最大中立位抓取")) || (trainingName.equals("K6") && status.equals("最大中立位抓取"))
//                || (trainingName.equals("K10") && status.equals("最大中立位抓取"))){
//            if (180 - jointAngle > jointFlexedMaxAngle) {
//                jointFlexedMaxAngle = 180 - jointAngle;
//            }

//            if (segmentMax == 0) {
//                segmentMax = baseAngle;
//            }else if (baseAngle < segmentMax && patientActionStatus == 1){
//                segmentMax = baseAngle;
//            }
//        }

        if (status.equals("第二次静止")|| status.equals("标准")|| status.equals("计数")|| status.equals("不计数")
        ) {
            if (180 - jointAngle > jointFlexedMaxAngle && 180 - jointAngle <= 170) {
                jointFlexedMaxAngle = 180 - jointAngle;
            }
        }

    }


    /*
     * 相应计时器到mainHandler
     * */
    public void responseTimer(){
        if (
//                (!status.equals("初始状态") && !status.equals("准备状态")&& !status.equals("角度抓取")
//                        && !status.equals("抬大腿保持") && !status.equals("大腿标准")&& !status.equals("大腿不标准")
//                        && !status.equals("动作监控"))
                status.equals("第二次静止")|| status.equals("标准")|| status.equals("计数")
                        || status.equals("不计数")|| status.equals("结束") || status.equals("不配合")
                        || status.equals("怀疑不配合")||status.equals("一字步开始")||status.equals("过低计数")
                        ||status.equals("过高计数")
        ){
            int diff;
            int timerStatus = 2;
            if(Arrays.asList(twiceCountName).contains(this.trainingName)){
                timerStatus = 0;
            }
            if (status.equals("结束")) {
                timerStatus = 1;
            }
            if (status.equals("不配合") || status.equals("怀疑不配合")) {

                diff = ruleClass.diffTimer;
                timerStatus = 3;


            } else {
                Long currentTime = new Date().getTime();
                double timeDiff = (double) (currentTime - ruleClass.timer) / 1000;
                diff = (int) Math.floor(timeDiff);
//                if (timeDiff > lastTime + 0.95){
//                    diff = lastTime + 1;
//                }else{
//                    diff = (int) Math.floor(timeDiff);
//                }
            }
            //lastTime == 0 ||
            if(lastTime != diff) {
                Message timerMsg = mainHandler.obtainMessage(2024);
                timerMsg.arg1 = diff;
                timerMsg.arg2 = timerStatus;
                mainHandler.sendMessage(timerMsg);
                lastTime = diff;
            }
        }
    }


    /*
     * 相应report到mainHandler
     * */
    public void responseReport(int angle1, int angle2, int jointAngle, int injSeg, int sid){
        if (!Arrays.asList(responseSpecialName).contains(this.trainingName)) {

            if(this.trainingName.equals("K5")){
                int footAngle = angle1;
                if (footAngle < 135 ){
                    footAngle = 135;
                }else if(footAngle > 190 ){
                    footAngle = 190;
                }
                report.put("footAngle", footAngle - 150);
            }else if(this.trainingName.equals("K6")){
                int footAngle = angle1;
                if (footAngle < 130 ){
                    footAngle = 130;
                }else if(footAngle > 210 ){
                    footAngle = 210;
                }
                report.put("footAngle", footAngle - 150);
            }else if(this.trainingName.equals("K9")){
                int trans = transformUIAngle(angle2);
                report.put("footAngle", trans);
            }else {
                if(Arrays.asList(drawPictureName).contains(this.trainingName)){
                    int trans = transformUIAngle(angle1);
                    report.put("thighAngle", trans);
                }else if(this.trainingName.equals("K15") || this.trainingName.equals("K1")){
                    int trans;
                    if (angle1 > 270 && angle1 <=360){
                        trans = angle1 - 450;
                    }else{
                        trans = angle1 - 90;
                    }
                    if(trans <0){
                        trans = 0;
                    }else if(trans >80){
                        trans = 80;
                    }
                    report.put("thighAngle", trans);
                }else{
                    report.put("thighAngle", angle1 - 90);
                }

            }
            if(trainingName.equals("K2")){
                int shankAngle = angle2;
                if(shankAngle < 60){
                    shankAngle = 60;
                }else if(shankAngle > 180 && shankAngle < 270){
                    shankAngle = 180;
                }else if(shankAngle >= 270 && shankAngle <= 360){
                    shankAngle = 60;
                }
                report.put("shankAngle", shankAngle - 90);
            }else if (trainingName.equals("K21") ||trainingName.equals("K22")){
                int shankAngle = angle2;
                if (shankAngle <= 180 && shankAngle >= 0){
                    shankAngle = 360;
                } else if(shankAngle > 180 && shankAngle <= 270){
                    shankAngle = 270;
                }
                report.put("shankAngle", shankAngle - 90);
            }else if(this.trainingName.equals("K9")){
                report.put("thighAngle", angle1 - 90);
            }else{
                if(Arrays.asList(abnormalAction).contains(this.trainingName)){
                    int trans;
                    if (angle2 > 270 && angle2 <=360){
                        trans = angle2 - 450;
                    }else{
                        trans = angle2 - 90;
                    }
                    if(trans <-90){
                        trans = -90;
                    }else if(trans >0){
                        trans = 0;
                    }
                    report.put("shankAngle", trans);
                }else if(this.trainingName.equals("K1")){
                    int trans;
                    if (angle2 > 270 && angle2 <=360){
                        trans = angle2 - 450;
                    }else{
                        trans = angle2 - 90;
                    }
                    int areaAng = (Integer) report.get("thighAngle");
                    if(trans <-areaAng){
                        trans = -areaAng;
                    }else if(trans >areaAng){
                        trans = areaAng;
                    }
                    report.put("shankAngle", trans);
                }else {
                    report.put("shankAngle", angle2 - 90);
                }
            }
            if(jointAngle > 180){
                jointAngle = 180;
            }else if(jointAngle < 0){
                jointAngle = 180;
            }
            report.put("jointAngle", jointAngle);
            int reportjointExtendMaxAngle = jointExtendMaxAngle;
            if(reportjointExtendMaxAngle > 180){
                reportjointExtendMaxAngle = 180;
            }else if(reportjointExtendMaxAngle < 0){
                reportjointExtendMaxAngle = 0;
            }

            int reportjointFlexedMaxAngle = jointFlexedMaxAngle;
            if(reportjointFlexedMaxAngle > 180){
                reportjointFlexedMaxAngle = 180;
            }else if(reportjointFlexedMaxAngle < 0){
                reportjointFlexedMaxAngle = 0;
            }
            if (reportjointExtendMaxAngle != 0) {
                report.put("jointExtendMaxAngle", reportjointExtendMaxAngle);
            }
            if (reportjointFlexedMaxAngle != 0) {
                report.put("jointFlexedMaxAngle", reportjointFlexedMaxAngle);
            }
            report.put("holdDuration", holdDuration);
            Message reportMsg;
            if(this.trainingName.equals("K5") || this.trainingName.equals("K6")) {
                reportMsg = mainHandler.obtainMessage(2131);
            }else if(this.trainingName.equals("K9")) {
                reportMsg = mainHandler.obtainMessage(2151);
            }else{
                Log.d("2121", ":    report status " + report.get("status"));
                if (status.equals("不配合")||status.equals("怀疑不配合")) {
                    report.put("status", 3);
                }
                reportMsg = mainHandler.obtainMessage(2121);
            }

            reportMsg.obj = report;
            mainHandler.sendMessage(reportMsg);
        }else{
            int injAngle = angle1;
            int unInjAngle = angle2;

            if (trainingName.equals("K11")) {
                if (injAngle >=0 && injAngle <= 180){
                    injAngle = 360 - injAngle;
                }
                if (unInjAngle >=0 && unInjAngle <= 180){
                    unInjAngle = 360 - unInjAngle;
                }
                if (injuredSegment == 1) {
                    if (unInjAngle < injAngle - 5 || injSeg == 2 && unInjAngle < injAngle  + 5) {
                        baseAngle = unInjAngle;
                        otherAngle = injAngle;
                        injSeg = 2;
                    } else {
                        baseAngle = injAngle;
                        otherAngle = unInjAngle;
                        injSeg = 1;
                    }
                }else if(injuredSegment == 2 ){
                    if (injAngle < unInjAngle - 5 || injSeg == 2 && injAngle < unInjAngle + 5) {
                        baseAngle = injAngle;
                        otherAngle = unInjAngle;
                        injSeg = 2;
                    } else {
                        baseAngle = unInjAngle;
                        otherAngle = injAngle;
                        injSeg = 1;
                    }
                }


            }else if(trainingName.equals("K25") ||trainingName.equals("K26")){
                if(injuredSegment == 1) {
                    if (unInjAngle > injAngle + 5 || injSeg == 2 && unInjAngle > injAngle  - 5) {
                        baseAngle = unInjAngle;
                        otherAngle = injAngle;
                        injSeg = 2;
                    } else {
                        baseAngle = injAngle;
                        otherAngle = unInjAngle;
                        injSeg = 1;
                    }
                }else if(injuredSegment == 2){
                    if (injAngle > unInjAngle + 5 || injSeg == 2 && unInjAngle > injAngle  - 5) {
                        baseAngle = injAngle;
                        otherAngle = unInjAngle;
                        injSeg = 2;
                    } else {
                        baseAngle = unInjAngle;
                        otherAngle = injAngle;
                        injSeg = 1;
                    }
                }
            }
            int trans = transformUIAngle(baseAngle);
            report.put("angle", trans);
            Message reportMsg = mainHandler.obtainMessage(2141);
            reportMsg.arg1 = sid;
            reportMsg.arg2 = injSeg;
            reportMsg.obj = report;
            mainHandler.sendMessage(reportMsg);
        }
    }

    /*
    处理信号返回结果
     */


    public List<Integer> saveAngleStamp = new ArrayList<>();
    public List<Long> saveTimeStamp = new ArrayList<>();
    public List<Double> saveWAngle1 = new ArrayList<>();//存储angle1角速度数据
    public List<Double> saveWAngle2 = new ArrayList<>();//存储angle2角速度数据
    public Long wTimer = 0L;

    public void judgeSensorStatic(){
        int size = saveWAngle1.size();
        double meanAngle1 =0;
        double meanAngle2 =0;
        for (int i = 0; i< size; i++){
            meanAngle1 += saveWAngle1.get(i);
            meanAngle2 += saveWAngle2.get(i);
        }
        meanAngle1 /= size;
        meanAngle2 /= size;
        Log.d("aaa", "----------------------------------------------------------------");
        Log.d("aaa", "aaa  meanAngle1 :                                " + meanAngle1);
        Log.d("aaa", "aaa  meanAngle2 :                                " + meanAngle2);
        if (wTimer == 0){
            wTimer = new Date().getTime();
        }else {
            if (meanAngle1 > 0.1 && meanAngle2 > 0.1) {
                wTimer = new Date().getTime();
            }
        }
        Long currentTime = new Date().getTime();
        double timeDiff = (double) (currentTime - wTimer) / 1000;
        if (timeDiff > 5 && sensorError == 1){
            sensorError = 0;
            Message reportMsg = mainHandler.obtainMessage(2029);
            reportMsg.arg1 = 1;
            mainHandler.sendMessage(reportMsg);
            Message voiceStatus = mainHandler.obtainMessage(2023);
            voiceStatus.obj = "未佩戴好传感器";
            mainHandler.sendMessage(voiceStatus);
        }


    }

    public void saveWData(double angle,int type){

        if(type == 1) {
            if (saveWAngle1.size() > 21) {
                saveWAngle1.remove(0);
            }
            saveWAngle1.add(angle);
        }else {
            if (saveWAngle2.size() > 21) {
                saveWAngle2.remove(0);
            }
            saveWAngle2.add(angle);
        }

    }

    public void saveData(int angle){
        if(saveTimeStamp.size() > 42){
            saveTimeStamp.remove(0);
        }
        if(saveAngleStamp.size() > 42){
            saveAngleStamp.remove(0);
        }
        Long currentTime = new Date().getTime();
        saveTimeStamp.add(currentTime);
        saveAngleStamp.add(angle);

    }

    public void process(Map vecs) {
        sid =0;
        injSeg =0;
        if (loopReport == 1){
            loopCriterion();
        }
        if (status.equals("休息监控")){
            Log.d(TAG, "                         report status :                                " + report.get("status"));
        }
        String thighName = (String) sensorTable.get(0).get("tag");
        String shankName = (String) sensorTable.get(1).get("tag");
        int angle1 = (int) Math.round(common.getAngleY((Map) vecs.get(thighName)));
        int angle2 = (int) Math.round(common.getAngleY((Map) vecs.get(shankName)));
        double w1 = common.getW((Map) vecs.get(thighName));
        double w2 = common.getW((Map) vecs.get(shankName));
        saveWData(w1, 1);
        saveWData(w2, 2);
        Log.d(TAG, "                             angle1 :                                " + common.getAngleY((Map) vecs.get(thighName)));
        Log.d(TAG, "                             angle2 :                                " + common.getAngleY((Map) vecs.get(shankName)));
        if (timerClass.staticStatusTimer == 0){
            timerClass.setStaticStatusTimer(new Date().getTime());
            thighStatic = angle1;
            shankStatic = angle2;
        }
        int jointAngle = 180 - angle1 + angle2;
        if((angle1 > 270 || angle2 > 270) && !(angle1 > 270 && angle2 > 270)){
            if (jointAngle < 0){
                jointAngle = jointAngle + 360;
            }else {
                jointAngle = jointAngle - 360;
            }
        }


        report.put("status", 0);
        //todo:找到所有两个传感器没有夹角的训练动作
//        if ((!(jointAngle < 180 && jointAngle > 10) && !Arrays.asList(noJointAction).contains(this.trainingName)) || status.equals("不配合")) {
//            report.put("status", 3);
//        }
        if (status.equals("不配合")||status.equals("怀疑不配合")) {
            report.put("status", 3);
        }
        Log.d(TAG, "                         report status :                                " + report.get("status"));
        getBaseAngle(angle1, angle2);

        int globalAngle;
        if(trainingName.equals("K21")|| trainingName.equals("K22")){
            globalAngle = baseAngle - 360;
        }else {
            if (baseAngle >= 270 && baseAngle <= 360) {
                globalAngle = 270 - baseAngle;
            } else {
                globalAngle = baseAngle;
            }
        }
        saveData(globalAngle);

        angleStatus(globalAngle);

        judgeSensorStatic();

        Log.d(TAG, "baseAngle                       :                                " + baseAngle);
        Log.d(TAG, "otherAngle                     :                                " + otherAngle);


        /*判断进入状态是否到达制定时间的计时器*/
        satisfySendVoice();

        statusTransForm(jointAngle);


        recordJointAngle(jointAngle);

        responseTimer();

        responseReport(angle1, angle2, jointAngle, injSeg, sid);

    }


    /*
     * K7K8传感器角度转换UI图角度
     * */
    public int K7K8MatchUIAngle(int angle){
        int UIAngle =10;
        if(angle > 25){
            UIAngle = 40;
        }else if(angle == 0){
            UIAngle = 10;
        }else if (angle <= 5 && angle > 0){
            UIAngle = angle * 4;
        }else if (angle <= 25 && angle >5){
            UIAngle = angle + 15;
        }
        return UIAngle;
    }

    /*
     * K7K8传感器角度转换UI图角度
     * */
    public int K9MatchUIAngle(int angle){
        int UIAngle =0;
        if(angle > 25){
            UIAngle = 30;
        }else if(angle == 0){
            UIAngle = 0;
        }else if (angle <= 5 && angle > 0){
            UIAngle = angle * 2;
        }else if (angle <= 25 && angle >5){
            UIAngle = angle + 5;
        }
        return UIAngle;
    }


    /*
     * 传感器角度转换UI图角度
     * */
    public int transformUIAngle(int angle){
        int transAngle = 0;
        if(trainingName.equals("K7") || trainingName.equals("K8")){
            int diff;
            if(segmentMin == 370){
                diff = 0;
            }else{
                diff = angle -segmentMin;
            }
            transAngle = K7K8MatchUIAngle(diff);
        }else if(trainingName.equals("K9")){
            int diff;
            if(segmentMax > 300 && segmentMax < 360){
                segmentMax  = 0;
            }
            if(segmentMax == 0){
                diff = 0;
            }else{

                if (angle > 300){
                    angle = 360 - angle;
                }

                if (angle > segmentMax){
                    segmentMax = angle;
                }
                if (segmentMax > 70){
                    diff = 70- angle;
                }else {
                    diff = segmentMax - angle;
                }
            }
            transAngle = K9MatchUIAngle(diff);
        }else if(trainingName.equals("K10")){
            transAngle = angle;
            if(angle < 200 && angle > 20){
                transAngle = 0;
            } else if (angle == 360) {
                transAngle = 20;
            }else if (angle > 200 && angle < 350){
                transAngle = 30;
            }else if (angle >= 350 && angle <= 359){
                transAngle = 360 - angle +20;
            }else if (angle >=0 && angle <= 20){
                transAngle = 20 - angle;
            };
        }else if(trainingName.equals("K11")){
            if(angle < 170 && angle >=0){
                transAngle = 0;
            }else if ( angle >= 170 && angle<330){
                transAngle = 30;
            }else if(angle <=360 && angle >= 330){
                transAngle = 360 -angle;
            }
        }else if(trainingName.equals("K25") || trainingName.equals("K26")){
            if(angle<90){
                transAngle = 0;
            }else if(angle <=360 && angle >135){
                transAngle = 45;
            }else if(angle <=135 && angle >= 90){
                transAngle = angle - 90;
            }
        }else if(trainingName.equals("K29") || trainingName.equals("K30")|| trainingName.equals("K31")){
            if (angle < 90){
                transAngle = 0;
            }else if (angle <= 360 && angle > 290){
                transAngle = 0;
            }else if (angle <= 290 && angle > 180){
                transAngle = 180;
            }else if(angle <=180 && angle >= 90){
                transAngle = angle - 90;
            }
        }else if(trainingName.equals("K19") || trainingName.equals("K20")){
            if (angle < 1){
                transAngle = 1;
            }else if (angle <= 360 && angle > 180){
                transAngle = 1;
            }else if (angle <= 180 && angle > 90){
                transAngle = 120;
            }else if (angle <= 90 && angle >= 1){

                transAngle = Math.round(angle * 4 / 3);
            }
        }else if(trainingName.equals("K31") || trainingName.equals("K27")){
            if (angle <= 360 && angle > 180){
                transAngle = 0;
            }else if (angle <= 180 && angle > 45){
                transAngle = 45;
            }else if (angle <= 45 && angle >= 0){

                transAngle = angle;
            }
        }
        return transAngle;

    }

    /*
     * 状态机判断
     * */
    public void statusTransForm(int jointAngle){
        int diffVoice;
        Map getRuleMap = (Map) transTableMap.get(status);
//        try {
//            Set priorityKeys = getRuleMap.keySet();
//            List<Integer> priorityKeysList = new ArrayList<Integer>(priorityKeys);
//            Collections.sort(priorityKeysList);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return;
//        }

        Set priorityKeys = getRuleMap.keySet();
        List<Integer> priorityKeysList = new ArrayList<Integer>(priorityKeys);
        Collections.sort(priorityKeysList);
        if(sendVoiceTimer != 0){
            Long currentTime = new Date().getTime();
            diffVoice = Math.round((currentTime - sendVoiceTimer)/ 1000);
        }else{
            diffVoice = 0;
        }
//    public int patientActionStatus = 0; // 1：静止，2：上升，3：下降
//    public int beforeAngle = 0;
//    public int errorReportStatus = 0;//异常错误播报状态
//    public int loopReport = 0;//判断是否需要循环播报
//    public String previousStatus = "";//记录异常前状态
//    public int loopTimerArea = 0;//从状态表中获取的时间范围
//    public String loopVocie = "";//要循环的语音
//    public int sensorError = 1;
        Log.d("loop", "                             patientActionStatus :                                " + patientActionStatus);
        Log.d("loop", "                             status :                                " + status);
        Log.d("loop", "                             beforeAngle :                                " + beforeAngle);
        Log.d("loop", "                             errorReportStatus :                                " + errorReportStatus);
        Log.d("loop", "                             loopReport :                                " + loopReport);
        Log.d("loop", "                             previousStatus :                                " + previousStatus);
        Log.d("loop", "                             loopTimerArea :                                " + loopTimerArea);
        Log.d("loop", "                             loopVocie :                                " + loopVocie);
        Log.d("loop", "                             sensorError :                                " + sensorError);
        Log.d("loop", "                             previousStatus :                                " + previousStatus);


        Log.d(TAG, "                             diffVoice :                                " + diffVoice);
        Log.d(TAG, "                             status :                                " + status);
        Log.d(TAG, "                             jointAngle :                                " + jointAngle);
        Log.d(TAG, "                             angleMax :                                " + angleMax);
        Log.d(TAG, "                             angleMin :                                " + angleMin);
        Log.d(TAG, "                             jointExtendMaxAngle :                                " + jointExtendMaxAngle);
        Log.d(TAG, "                             jointFlexedMaxAngle :                                " + jointFlexedMaxAngle);
        Log.d(TAG, "                             segmentMin :                                " + segmentMin);
        Log.d(TAG, "                             segmentMax :                                " + segmentMax);

        Long tcurrentTime = new Date().getTime();
        int ddc = Math.round((tcurrentTime - ruleClass.timer)/ 1000);
        if( ddc >= 3 && (status.equals("动作开始") || status.equals("第一次静止"))){
            Log.d(TAG, "                             ddddddddddd :                                " + ddc);
        }

        for(int i=0; i < priorityKeysList.size(); i++){
            int key = priorityKeysList.get(i);
            Map rule = (Map) getRuleMap.get(key);

            if( isSatisfyRule((String) rule.get("ruleName"),jointAngle, diffVoice)){
                String nextStatus = ( String)rule.get("nextStatus");
                if(nextStatus.equals("上一状态")){
                    status = previousStatus;

                }

                //自适应缓冲时间
                voiceIndex = common.getVoiceIndex(countVoice, voiceIndex, targetCount);
                String[] textList = ((String) rule.get("voicePrompt")).split(",");
                String currentVoice1 = getText(textList);
//                String nextStatus1 = nextStatus.substring(0, nextStatus.length() - 1);
//                if(!currentVoice1.equals("-")) {
//                    String currentVoice2 = currentVoice1.substring(1, currentVoice1.length() - 1);
//                    int bufferTime = (int) Math.round((double) currentVoice2.length() * 0.3);
//                    ruleClass.setBufferTimer(bufferTime);
//                }else{
//                    ruleClass.setBufferTimer(0);
//                }
//                if(nextStatus1.equals("语音播报") || nextStatus1.equals("休息播报")){
//                    if(!currentVoice1.equals("-")) {
//                        String currentVoice2 = currentVoice1.substring(1, currentVoice1.length() - 1);
//                        int bufferTime = (int) Math.round((double) currentVoice2.length() * 0.3);
//                        ruleClass.setBufferTimer(bufferTime);
//                    }else{
//                        ruleClass.setBufferTimer(0);
//                    }
//
//                }
                if (old != 1) {
                    if ((Integer) rule.get("exception") != 0) {
                        exceptionReport((Integer) rule.get("exception"));
                    }
                }
                if((Integer) rule.get("timerOp") != 0){
                    timerOp((Integer)rule.get("timerOp"),(String) nextStatus);
                }

                if (old != 1) {
                    if ((Integer) rule.get("loopPlayVoice") >= 0) {
                        if(!currentVoice1.equals("-")) {
                            String currentVoice2 = currentVoice1.substring(1, currentVoice1.length() - 1);
                            loopPlayVoice((Integer) rule.get("loopPlayVoice"), currentVoice2);
                        }
                    }
                }

                if((Integer) rule.get("incrCounter") != 0){
                    incrCounter((Integer)rule.get("incrCounter"));
                }


                if(!nextStatus.equals("上一状态")){
                    status = (String) nextStatus;
                }

                if((Integer) rule.get("endAct") != 0){
                    endAct();
                }

                if(status.contains("结束") && countVoice != actionCount){
                    countVoice += 1;
                }

                voicePrompt(currentVoice1);
                Log.d("sendVoiceTimer", "before sendVoiceTimer                  :                                " + sendVoiceTimer);

                if(!lastStatus.equals(status) ) {
                    sendVoiceTimer = 0L;
                }else{
                    sendVoiceTimer = new Date().getTime();
                }
                Log.d("sendVoiceTimer", "after1 sendVoiceTimer                    :                                " + sendVoiceTimer);

                Log.d("sendVoiceTimer", "after2 sendVoiceTimer                    :                                " + sendVoiceTimer);
                if (old != 1) {
                    if ((Integer) rule.get("resetActState") != 0) {
                        patientActionStatus = 0;
                        timerClass.setActTimer(new Date().getTime());
                        if ((Integer) rule.get("resetActState") == 1) {
                            beforeAngle = baseAngle;
                        } else {
                            beforeAngle = 0;
                        }
                    }
                }
                break;
            }
        }
    }


    /*
     * 解析规则是否满足
     * */
    public boolean isSatisfyRule(String ruleName, int jointAngle, int diff){
        try {
            Map rule = (Map) ruleTableMap.get(ruleName);
            List ruleFunction = (List) rule.get("ruleFunction");
            List ruleParams = (List) rule.get("ruleParams");

            for (int i=0;i < ruleFunction.size();i++){
                String ruleF = (String) ruleFunction.get(i);
                int ruleP = (Integer) ruleParams.get(i);
                if (!ruleClass.process(ruleF,ruleP,baseAngle,otherAngle,jointAngle,angleMax,angleMin,
                        diff,segmentMin,segmentMax,timerClass,patientActionStatus)){
                    return false;
                };
            }

            return true;
        }catch (Exception e){
            Log.d(TAG, "error :                                " + e);
            return  false;
        }

    };


    /*
     * 判断语音信号是否重复。
     * */
    public void satisfySendVoice(){
        if(!lastStatus.equals(status)){
            Long currentTime = new Date().getTime();
            sendVoiceTimer = currentTime;
            lastStatus = status;
        }
    };


    /*
     * 计数状态处理
     * */
    public void incrCounter(int type){
        switch (type){
            case 1:
                sumActionCount+=1;
                onceStatus = 0;
                break;
            case 2:
                if(Arrays.asList(twiceCountName).contains(this.trainingName)){
                    if(status.equals("勾脚计数") || status.equals("前重心计数")
                            || status.equals("左小腿计数")|| status.equals("左大腿计数")){
                        twiceCountFirst += 1;
                    }else{
                        twiceCountSecond += 1;
                    }
                    Log.d(TAG, "add actionCount twiceCountFirst :                                " + twiceCountFirst);
                    Log.d(TAG, "add actionCount twiceCountSecond :                                " + twiceCountSecond);
                }else{
                    sumActionCount += 1;
                    actionCount += 1;
                    onceStatus = 1;
                }

                Log.d(TAG, "add actionCount status :                                " + status);
                break;
            case 3:
//                if(this.trainingName.equals("K5") || this.trainingName.equals("K10")){
                if(Arrays.asList(twiceCountName).contains(this.trainingName)){
                    if(status.equals("勾脚标准") || status.equals("前重心标准")|| status.equals("左小腿标准")
                            || status.equals("左大腿标准")){
                        twiceCountFirst += 1;
                        twiceCountFirstInP += 1;
                    }else{
                        twiceCountSecond += 1;
                        twiceCountSecondInP += 1;
                    }
                    Log.d(TAG, "add actionCount  :                                                    test ");
                    Log.d(TAG, "add actionCount twiceCountFirst :                                " + twiceCountFirst);
                    Log.d(TAG, "add actionCount twiceCountSecond :                                " + twiceCountSecond);
                    Log.d(TAG, "add actionCount twiceCountFirstInP :                                " + twiceCountFirstInP);
                    Log.d(TAG, "add actionCount twiceCountSecondInP :                                " + twiceCountSecondInP);

                }else {
                    sumActionCount += 1;
                    actionCount += 1;
                    inPlaceActionCount += 1;
                    onceStatus = 2;
                }
                break;



        }
    };


    /*
     * 计时器处理
     * */
    public void timerOp(int type, String nextStatus){
        switch (type) {
            case 1:
                ruleClass.setTimer(new Date().getTime());
                Message timerMsg = this.mainHandler.obtainMessage(2024);
                timerMsg.arg1 = 0;
                timerMsg.arg2 = 0;
                this.mainHandler.sendMessage(timerMsg);
                lastTime = 0;
                break;
            case 3:
                ruleClass.setTimer(new Date().getTime());
                Log.d(TAG, "nextStatus:   " + nextStatus);
//                if (nextStatus.equals("保持状态") || nextStatus.equals("落小腿保持")|| nextStatus.equals("结束")){
                //补充计时器开始的时间
                if(!Arrays.asList(twiceCountName).contains(this.trainingName)) {
                    if ( (nextStatus.equals("结束"))
                            || (!status.equals("第二次静止") && nextStatus.equals("第二次静止"))
                            || (!(status.equals("第二次静止") || status.equals("标准")
                            || status.equals("一字步开始")) && nextStatus.equals("标准"))
                            || (!status.equals("一字步开始") && nextStatus.equals("一字步开始"))

                    ) {
                        int timerStatus = 2;
                        if (Arrays.asList(twiceCountName).contains(this.trainingName)) {
                            timerStatus = 0;
                        }
                        if (nextStatus.equals("结束")) {
                            timerStatus = 1;
                            Message timerMsge = this.mainHandler.obtainMessage(2024);
                            timerMsge.arg1 = lastTime + 1;
                            timerMsge.arg2 = 2; //timerStatus
                            this.mainHandler.sendMessage(timerMsge);
                        }

                        Message timerMsg1 = this.mainHandler.obtainMessage(2024);
                        timerMsg1.arg1 = 0;
                        timerMsg1.arg2 = timerStatus;
                        this.mainHandler.sendMessage(timerMsg1);
                        lastTime = 0;
                    }
                    //TODO：统一训练结束语音名称，休息播报监控
                    //补充训练结束的计时器时间
                    if (nextStatus.equals("休息播报")) {
                        Message timerMsge = this.mainHandler.obtainMessage(2024);
                        timerMsge.arg1 = lastTime + 1;
                        timerMsge.arg2 = 2; //timerStatus
                        this.mainHandler.sendMessage(timerMsge);
                    }
                }
                //补充结束休息结束后的计时器
                if (status.equals("结束") && (nextStatus.equals("角度抓取") ||
                        nextStatus.equals("准备状态") || nextStatus.equals("动作监控") ||
                        nextStatus.equals("准备动作检查") || nextStatus.equals("勾脚准备"))) {
                    int timerStatus = 1;

                    Message timerMsg1 = this.mainHandler.obtainMessage(2024);
                    timerMsg1.arg1 = lastTime + 1;
                    timerMsg1.arg2 = timerStatus;
                    this.mainHandler.sendMessage(timerMsg1);
                    lastTime = lastTime + 1;
                }


//                if ( status.equals("不配合") &&(!nextStatus.equals("不配合"))){
//                    report.put("status", 0);
//                }

                break;
            case 2:
                pause();

                break;





        }
    };


    /*
     * 完成一次动作处理
     * */
    public void endAct(){
        //清楚计时器
//        report.put("status", 3);
//        Message reportMsg = mainHandler.obtainMessage(2121);
//        reportMsg.obj = report;
//        mainHandler.sendMessage(reportMsg);

        ruleClass.setTimer(new Date().getTime());
        ruleClass.setDiffTimer(0);
        Message countS = mainHandler.obtainMessage(2022);
        if(Arrays.asList(twiceCountName).contains(this.trainingName)){
            sumActionCount += 1;
            if(twiceCountSecond == 1 || twiceCountFirst == 1){
                actionCount +=1;
                onceStatus = 1;
            }
            if(twiceCountSecondInP == 1 && twiceCountFirstInP == 1){
                inPlaceActionCount += 1;
                onceStatus = 2;
            }

            twiceCountSecond = 0;
            twiceCountFirst = 0;
            twiceCountSecondInP = 0;
            twiceCountFirstInP = 0;

        }
        if(actionCount != transCount){
            transCount = actionCount;
        }


        if(!(trainingName.equals("K5") || trainingName.equals("K6") || trainingName.equals("K10")
                || trainingName.equals("K8")|| trainingName.equals("K7")|| trainingName.equals("K9")
                || trainingName.equals("K25")|| trainingName.equals("K26"))
        ) {
            angleMax = 0;
            angleMin = 370;
            segmentMin = 370;
            segmentMax = 0;
        }
        if(onceStatus != -1) {
//            countS.arg1 = onceStatus;
            countS.arg1 = inPlaceActionCount;
            countS.arg2 = actionCount;
//            countS.arg2 = sumActionCount;
            mainHandler.sendMessage(countS);
            onceStatus = -1;
        }
    };


    /*
     * 获取当前次对应语音
     * */
    public String getText(String[] textList){ ;
        String text = textList[voiceIndex];
        return text;
    };


    /*
    语音动作处理
    */
    public void voicePrompt(String currentVoice1){
//        voiceIndex = common.getVoiceIndex(countVoice, voiceIndex, targetCount);
//        String currentVoice1 = getText(textList);
        String currentVoice;

        if(!currentVoice1.equals("-")) {
            currentVoice = currentVoice1.substring(1, currentVoice1.length() - 1);
            int bufferTime = (int) Math.round((double) currentVoice.length() * 0.3);
            ruleClass.setBufferTimerArea(bufferTime);
            ruleClass.setBufferTimer(new Date().getTime());

            if(!currentVoice.equals(voice)){
                voice = currentVoice;
                Message voiceStatus = mainHandler.obtainMessage(2023);
                voiceStatus.obj = voice;
                mainHandler.sendMessage(voiceStatus);
                Log.d(TAG, "Voice :                                " + voice);
            }

        }else{
            ruleClass.setBufferTimerArea(0);
            ruleClass.setBufferTimer(new Date().getTime());

        }

    };


    /*
    增加传感器（传感器编号顺序1：A，2：B，传入参数也有先后顺序）
    */
    public void addSensor(String sensorACN,String sensorAEN,String sensorBCN,String sensorBEN){
        Map sensorA =new HashMap<String, Object>();
        Map sensorB =new HashMap<String, Object>();
        sensorA.put("tag", sensorAEN);
        sensorA.put("bodySite",sensorACN);
        sensorB.put("tag", sensorBEN);
        sensorB.put("bodySite",sensorBCN);
        sensorTable.add(sensorA);
        sensorTable.add(sensorB);
    };


    /*
    设置特殊动作传感器命名
     */
    public void setSensorTable(String name){
        if(Arrays.asList(sensorSideMatchSpecialName).contains(name)){
            if(name.equals("K5") || name.equals("K6")){
                addSensor("足","foot","小腿1","shank");
            }else if(name.equals("K11")){
                addSensor("小腿","shankA","小腿1","shankHealth");
            }else if(name.equals("K25")){
                addSensor("大腿","thigh","大腿1","thighHealth");
            }else if(name.equals("K26")){
                addSensor("大腿","thigh","大腿1","thighHealth");
            }else if(name.equals("K12")){
                addSensor("大腿","thigh","大腿1","thighHealth");
            }else if(name.equals("K13")){
                addSensor("大腿","thigh","大腿1","thighHealth");
            }else if(name.equals("K1")){
                addSensor("大腿","thigh","小腿1","shank");
            }else if(name.equals("K9")){
                addSensor("大腿","shankA","足1","shankHealth");
            }


        }else{
            addSensor("大腿","thigh","小腿1","shank");
        }
    };



    /*
    导入数据double转换
     */
    public int getInter( double s) {
        String num = String.valueOf(s);
        if (num.contains(".")) {
            String substring = num.substring(0, num.indexOf("."));
            return Integer.valueOf(substring);
        } else {
            return  Integer.valueOf(num);
        }
    }



    /*
    导入数据string转换
     */
    public int getInter( String num ) {
        if (num.contains(".")) {
            String substring = num.substring(0, num.indexOf("."));
            return Integer.valueOf(substring);
        } else {
            return  Integer.valueOf(num);
        }
    }



}
