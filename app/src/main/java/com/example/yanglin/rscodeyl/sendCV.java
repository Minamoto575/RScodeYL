
package com.example.yanglin.rscodeyl;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.yanglin.rscodeyl.Generic.GenericGFPoly;
import com.example.yanglin.rscodeyl.Generic.MD5;
import com.example.yanglin.rscodeyl.Generic.SensorDataProcess;
import com.example.yanglin.rscodeyl.Generic.Utils2;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by yanglin on 2019/2/23.
 */

public class sendCV extends AppCompatActivity{
    private static final String TAG = "测试log.i";
    Button start ,AccSyd,AccKey,GyrSyd,GyrKey;
    EditText ip;

    SensorManager sensorManager;
    private android.hardware.Sensor accelerometer; // 加速度传感器
    private android.hardware.Sensor magnetic; // 地磁场传感器
    private android.hardware.Sensor gyroscope;//线性加速度传感器

    boolean tag_acc;   //标志位 tag_acc标志产生了加速度
    boolean tag_g;  // tag_g标志产生了新的磁场数据
    boolean tag_Gry; // tag_lineAcc标志产生了新的线性加速度数据

    StringBuilder sensorBuilderAcc = new StringBuilder("");
    StringBuilder sensorBuilderGry = new StringBuilder("");
    int count=0;

    private float[] accValues = new float[3];//用于之后计算方向
    private float[] magFieldValues = new float[3];
    private float[] gryFieldValues = new float[3];

    private float[] rotationMatrix = new float[9];  //存放旋转矩阵
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        start=(Button)findViewById(R.id.start);

        AccSyd=(Button)findViewById(R.id.AccSyd);
        AccKey=(Button)findViewById(R.id.AccKey);

        GyrSyd=(Button)findViewById(R.id.GyrSyd);
        GyrKey=(Button)findViewById(R.id.GyrKey);

        ip=(EditText)findViewById(R.id.ip);

      //开始记录传感器的数据  并进行0,1 处理 放到 static 中
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   Toast.makeText(sendCV.this,"开始记录加速度数据",Toast.LENGTH_SHORT).show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        begin();

                    }
                }).start();

            }
        });
/*

 加速度  对应的校正子  加密数据
 */
        //校正子
        AccSyd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("发送", "***********发送 校正子");

                        sendData(Utils2.getSyndromes(),8888);

                    }
                }).start();


            }
        });
//加密数据
        AccKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        MD5 md = new MD5();
                        Log.d("发送", "***********发送 加密  之前   数据"+Utils2.getRawKey());
                        sendData(md.print(Utils2.getRawKey()),6666);

                    }
                }).start();

            }
        });

/*
  陀螺仪 对应的校正子  加密数据
 */
        //校正子
        GyrSyd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("发送", "***********发送 校正子"+Utils2.getSyndromesGyr());

                        sendData(Utils2.getSyndromesGyr(),5555);

                    }
                }).start();


            }
        });

        //加密数据
        GyrKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        MD5 md = new MD5();
                        Log.d("发送", "***********发送 加密  之前   数据"+Utils2.getRawKeyGyr());
                        sendData(md.print(Utils2.getRawKeyGyr()),3333);

                    }
                }).start();

            }
        });


    }

    public void begin(){
        sensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        //初始化加速度传感器
        accelerometer=sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER);
        // 初始化地磁场传感器
        magnetic = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_MAGNETIC_FIELD);
        //初始化线性加速度传感器
        gyroscope = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_GYROSCOPE);
        //初始化标志位
        tag_acc = false;
        tag_g = false;
        tag_Gry = false;
        //注册加速度传感器
        sensorManager.registerListener(sensorListener,accelerometer,10000);
        //注册磁场传感器
        sensorManager.registerListener(sensorListener,magnetic,10000);
        //注册磁场传感器
        sensorManager.registerListener(sensorListener,gyroscope,10000);
    }

    final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(tag_acc&&tag_g&&tag_Gry){
                calculateRotationMatrix();
                double f[] ={gryFieldValues[0],gryFieldValues[1],gryFieldValues[2]};
                double fUpdate[] =  UpdateRealDate(f);
                sensorBuilderGry.append(fUpdate[2]).append("\r\n");
                Log.d("陀螺仪", "数据开始记录 event.values[2]"+event.values[2]);
                Log.d("陀螺仪", "数据开始记录  fUpdate[2]  "+fUpdate[2]);

                sensorBuilderAcc.append(accValues[2]).append("\r\n");
                Log.d("加速度", "数据开始记录"+event.values[2]);

                count++;
                if(count==12800){
                    Toast.makeText(sendCV.this,"已采样12800次",Toast.LENGTH_LONG).show();
                    sensorManager.unregisterListener(sensorListener);
                    tag_Gry = false;
                    tag_acc = false;
                    tag_g = false;

                    String sensorSendDataBuilder =sensorBuilderAcc.toString();
                    SensorDataProcess s = new SensorDataProcess();
                   float[] signal = s.load(sensorSendDataBuilder);  //将文件中的数据放到数组[12800]
                    String rawKeySend = s.startKeyGen(signal);

                    Utils2.setRawKey(rawKeySend); //将原始数据（0,1处理后）进行保存  原始数据：计算校正子，加密后发送

                    doSyndromes(Utils2.getRawKey());

                    Utils2.setSyndromes(doSyndromes(Utils2.getRawKey()));  //将加速度的校正子进行保存
                    Log.d("加速度", "加速度 测试输出 Utils2.setSyndromes 中的值"+Utils2.getSyndromes() );


                    String sensorSendDataBuilderGry =sensorBuilderGry.toString();
                    float[] signalGyr = s.load(sensorSendDataBuilderGry);  //将文件中的数据放到数组[12800]
                    String rawKeySendGyr = s.startKeyGen(signalGyr,0); //重载函数
                    Utils2.setRawKeyGyr(rawKeySendGyr); //将原始数据（0,1处理后）进行保存  原始数据：计算校正子，加密后发送
                    doSyndromes(Utils2.getRawKeyGyr());
                    Utils2.setSyndromesGyr(doSyndromes(Utils2.getRawKeyGyr())); //将陀螺仪的校正子进行保存
                    Log.d("陀螺仪", "陀螺仪 测试输出 Utils2.setSyndromes 中的值"+Utils2.getSyndromesGyr() );

                }
                tag_Gry = false;
                tag_acc = false;
                tag_g = false;
            }

            if(event.sensor.getType()==android.hardware.Sensor.TYPE_ACCELEROMETER){

                accValues=event.values; //计算选择矩阵用得着
                tag_acc = true;
            }
            if(event.sensor.getType() == android.hardware.Sensor.TYPE_GYROSCOPE)//陀螺仪
            {
                Log.i(TAG, "陀螺仪传感器 的值改变");
                gryFieldValues =event.values;
                tag_Gry = true;
            }
            if(event.sensor.getType() == android.hardware.Sensor.TYPE_MAGNETIC_FIELD) //磁场
            {
                Log.i(TAG, "磁场传感器 的值改变");
                magFieldValues = event.values;  //计算旋转矩阵用得着
                tag_g = true;
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private double[] UpdateRealDate(double [] f) {

        f[0] = rotationMatrix[0]*f[0]+rotationMatrix[1]*f[1]+rotationMatrix[2]*f[2];
        f[1] = rotationMatrix[3]*f[0]+rotationMatrix[4]*f[1]+rotationMatrix[5]*f[2];
        f[2] = rotationMatrix[6]*f[0]+rotationMatrix[7]*f[1]+rotationMatrix[8]*f[2];
        return f;
    }

    //计算旋转矩阵
    private void calculateRotationMatrix() {
        SensorManager.getRotationMatrix(rotationMatrix, null, accValues,
                magFieldValues);
    }
    public String doSyndromes(String data){
      // 计算其多项式系数
      GenericGFPoly[] sendSyndrome= Utils2.getSyndromePoly(data);
      //将多项式系数转化为字符串
      StringBuilder syndromesStr= new StringBuilder();
      for(int i=0;i<3;i++) {
          int[] coefficients = sendSyndrome[i].getCoefficients();
          int[] padding = new int[12];
          int distance = 12 - coefficients.length;
          for (int j = 0; j < coefficients.length; j++) {
              padding[distance + j] = coefficients[j];
          }
          String cofficientsStr = Utils2.int2String(padding);
          syndromesStr.append(cofficientsStr);


          return syndromesStr.toString();
      }
      return null;
  }

// UDP  发送 校正子 加密数据
     public void sendData( String data,int port){
         String host_address = ip.getText().toString();
         try{
             DatagramSocket ds = new DatagramSocket();
             byte[] buf = data.getBytes();
             DatagramPacket dp= new DatagramPacket(buf,buf.length, InetAddress.getByName(host_address),port);
             ds.send(dp);
             ds.close();
         }catch (Exception e){
             e.printStackTrace();
         }
     }



}
