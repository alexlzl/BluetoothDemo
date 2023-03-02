package com.witmoiton.example.pojo;

import android.bluetooth.BluetoothDevice;

/**
 * 作者：yeqianyun on 2019/11/6 17:22
 * 邮箱：1612706976@qq.com
 *
 * BLE蓝牙设备
 */
public class BLEDevice {
    private BluetoothDevice bluetoothDevice;  //蓝牙设备
    private int RSSI;  //蓝牙信号
    private boolean isConnnected = false;  //是否已经连接

    public BLEDevice(BluetoothDevice bluetoothDevice, int RSSI) {
        this.bluetoothDevice = bluetoothDevice;
        this.RSSI = RSSI;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public int getRSSI() {
        return RSSI;
    }

    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
    }

    public boolean isConnnected() {
        return isConnnected;
    }

    public void setConnnected(boolean connnected) {
        isConnnected = connnected;
    }
}
