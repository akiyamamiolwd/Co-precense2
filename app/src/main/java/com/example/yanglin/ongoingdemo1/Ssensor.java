package com.example.yanglin.ongoingdemo1;

import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
/**
 * Created by yanglin on 2018/11/22.
 */
public class Ssensor {
    File sdcard= Environment.getExternalStorageDirectory();

    String mfileName = "update.txt";//存放修正后的数据
    double i=0;
    private static final String TAG = "sensor";
    private SensorManager sm;
    private android.hardware.Sensor accelerometer; // 加速度传感器
    private android.hardware.Sensor magnetic; // 地磁场传感器
    private android.hardware.Sensor Gry;//线性加速度传感器
    private float[] accelerometerValues = new float[3];//用于之后计算方向
    private float[] magneticFieldValues = new float[3];
    private float[] mRotationMatrix = new float[9];  //存放旋转矩阵
    boolean tag_acc;   //标志位 tag_acc标志产生了加速度
    boolean tag_g;  // tag_g标志产生了新的磁场数据
    boolean tag_Gry; // tag_lineAcc标志产生了新的线性加速度数据
    float X_Gry;//定义x y z 存放当前实际线性加速度数据
    float Y_Gry;
    float Z_Gry;


    private Context mContext; //抽象类
     File file = new File(sdcard,mfileName);
    public void begin(SensorManager sm){

         if(file.exists()){
            file.delete();
        }
        // 初始化加速度传感器
        accelerometer = sm.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER);
        // 初始化地磁场传感器
        magnetic = sm.getDefaultSensor(android.hardware.Sensor.TYPE_MAGNETIC_FIELD);
        //初始化线性加速度传感器
        Gry = sm.getDefaultSensor(android.hardware.Sensor.TYPE_GYROSCOPE);
        //初始化标志位
        tag_acc = false;
        tag_g = false;
        tag_Gry = false;
        //20Hz=50000,50Hz=20000 100Hz=10000
        //注册线性加速度传感器
        sm.registerListener(GryListener, Gry, 10000);
        //注册磁场传感器
        sm.registerListener(GryListener,magnetic,10000);
        //注册加速度传感器
        sm.registerListener(GryListener,accelerometer,10000);
    }
    final SensorEventListener GryListener = new SensorEventListener(){
        //复写onSensorChanged方法
        public void onSensorChanged(SensorEvent sensorEvent){
            //三组数据都有，开始通过旋转矩阵修正线性加速度数据
            if (tag_Gry && tag_g && tag_acc)
            {
                //根据磁场数据（磁场传感器）和加速度数据（加速度传感器）计算旋转矩阵
                calculateRotationMatrix();
                double f[] = {X_Gry, Y_Gry, Z_Gry};
                File file = new File(sdcard,mfileName);
                UpdateRealDate(f);  //通过旋转矩阵，修正实际加速度数据

                try {
                    FileOutputStream out = new FileOutputStream(file,true);
                    OutputStreamWriter osw = new OutputStreamWriter(out);
                    i=i+1.00;
                    DecimalFormat df = new DecimalFormat("#00.0000");
                    String result = df.format(f[2]);
                    String result1 = df.format(i);
                    String  str = result1+","+result;
                    osw.write(str+";");
                    osw.flush();
                    osw.close();
                    out.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }

//                count++;
//                if (count == countSize) {
//                    sm.unregisterListener(GryListener);
//
//                    Toast.makeText(receiveFile.this, "时间到，已保存文件！.", Toast.LENGTH_SHORT).show(); //提示停止记录
//                    tag_Gry = false;
//                    tag_acc = false;
//                    tag_g = false;
//                }

                tag_Gry = false;
                tag_acc = false;
                tag_g = false;
            }
            if(sensorEvent.sensor.getType() == android.hardware.Sensor.TYPE_ACCELEROMETER) //加速度
            {
                accelerometerValues = sensorEvent.values;
                tag_acc = true;
            }
            if(sensorEvent.sensor.getType() == android.hardware.Sensor.TYPE_MAGNETIC_FIELD) //磁场
            {
                magneticFieldValues = sensorEvent.values;
                tag_g = true;
            }
            if(sensorEvent.sensor.getType() == android.hardware.Sensor.TYPE_GYROSCOPE) //陀螺仪
            {
                Log.i(TAG, "传感器的值改变");
                float X_lateral = sensorEvent.values[0];
                float Y_longitudinal = sensorEvent.values[1];
                float Z_vertical = sensorEvent.values[2];
                X_Gry = X_lateral;
                Y_Gry = Y_longitudinal;
                Z_Gry = Z_vertical;
                tag_Gry = true;
            }
        }
        //复写onAccuracyChanged方法
        public void onAccuracyChanged(android.hardware.Sensor sensor , int accuracy){
            Log.i(TAG, "onAccuracyChanged");
        }
    };

    //计算旋转矩阵
    private void calculateRotationMatrix() {
        SensorManager.getRotationMatrix(mRotationMatrix, null, accelerometerValues,
                magneticFieldValues);
    }
    //通过旋转矩阵，修正实际陀螺仪数据
    private void UpdateRealDate(double [] f) {
        f[0] = mRotationMatrix[0]*f[0]+mRotationMatrix[1]*f[1]+mRotationMatrix[2]*f[2];
        f[1] = mRotationMatrix[3]*f[0]+mRotationMatrix[4]*f[1]+mRotationMatrix[5]*f[2];
        f[2] = mRotationMatrix[6]*f[0]+mRotationMatrix[7]*f[1]+mRotationMatrix[8]*f[2];
    }


    public void stop(SensorManager sm){
        sm.unregisterListener(GryListener);
        tag_Gry = false;
        tag_acc = false;
        tag_g = false;

    }

}
