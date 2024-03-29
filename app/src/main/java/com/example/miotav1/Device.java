package com.example.miotav1;

import android.util.Log;

import java.io.Serializable;

public class Device implements Serializable {
    private int id;
    private String device, topic, name, newValue;
    int type;
    private String statisticValue = "N/A";
    public Device(String type, String device, String topic, String name){
        this.type = Integer.parseInt(type);
        this.device = device;
        this.topic = topic;
        this.name = name;
    }
    public String getStatisticValue() {
        Log.d("mqtt-tri4", "getStatisticValue: " + statisticValue);
        return statisticValue;
    }
    public void setStatisticValue(String statisticValue) {
        this.statisticValue = statisticValue;
    }
    public int getID(){
        return id;
    }

    public String getDevice() {
        return device;
    }
    public String getName(){
        return name;
    }

    public String getTopic(){
        return topic;
    }
    public void setType(int typeDevice){
        type = typeDevice;
    }
    public int TypeDevice() {return type;}
    public void updateStatisticValue(String newValue) {
        Log.d("mqtt-tri3", "updateStatisticValue: " + newValue);
        this.statisticValue = newValue;
    }
    public String getStatistic(){
        return statisticValue;
    }
}

