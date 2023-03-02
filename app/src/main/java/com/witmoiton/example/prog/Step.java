package com.witmoiton.example.prog;

public class Step {

    private int minutes;
    private String stepCount;

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public String getStepCount() {
        return stepCount;
    }

    public void setStepCount(String stepCount) {
        this.stepCount = stepCount;
    }

    public Step() {
    }

    public Step(int minutes, String stepCount) {
        this.minutes = minutes;
        this.stepCount = stepCount;
    }

    @Override
    public String toString() {
        return "Step{" +
                "minutes=" + minutes +
                ", stepCount='" + stepCount + '\'' +
                '}';
    }
}
