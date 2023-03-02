package com.witmoiton.example.prog;

import java.util.List;

public class OffLineStep {
    private String year;
    private String month;
    private String day;
    private String hour;
    private List<Step> stepList;

    public List<Step> getStepList() {
        return stepList;
    }

    public void setStepList(List<Step> stepList) {
        this.stepList = stepList;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public OffLineStep() {
    }

    public OffLineStep(String year, String month, String day, String hour) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
    }

    @Override
    public String toString() {
        return "OffLineStep{" +
                "year='" + year + '\'' +
                ", month='" + month + '\'' +
                ", day='" + day + '\'' +
                ", hour='" + hour + '\'' +
                ", stepList=" + stepList.toString() +
                '}';
    }
}
