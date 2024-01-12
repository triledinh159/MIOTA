//
// Hàm Device để tạo kiểu dữ liệu Device để lấy các thông số từ add_item và đưa qua RecyclerView
//
package com.example.miotav1;

import java.io.Serializable;

public class Device implements Serializable {
    private int id;
    private String device, topic, name;
    int type;

    public Device(String typeDevice, String device, String topic, String name){}
    public Device( int type, String device, String topic, String name){
        this.type = type;
        this.device = device;
        this.topic = topic;
        this.name = name;
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
}
