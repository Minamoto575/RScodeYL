
package com.example.yanglin.rscodeyl.Generic;

import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Created by yanglin on 2019/2/24.
 */

public class SensorDataProcess {

    public  String startKeyGen( float[] signal){
        String key_str="";
        try{

            float[] abs_z = abs(signal);   //对数组数据取绝对值
            int[] bin_z = bin_thre(abs_z, 1);  //二值化处理
            float[] win_z = window(bin_z);    // 分组
            int[] key = bin_thre(win_z, (float) 0.6);
            key_str = new String(toString(key));   // toString(key) 将key值 字符串处理
            Log.i("startKeyGen", "数据内容 " + key_str);

        }catch (Exception e){
            e.printStackTrace();
        }
        return key_str;
    }
    public  String startKeyGen( float[] signal,int flag){
        String key_str="";
        try{
            int[] bin_z = bin_thre(signal, 1,flag);  //二值化处理
            float[] win_z = window(bin_z);    // 分组
            int[] key = bin_thre(win_z, (float) 0.6,flag);
            key_str = new String(toString(key));   // toString(key) 将key值 字符串处理
            Log.i("startKeyGen", "数据内容 " + key_str);

        }catch (Exception e){
            e.printStackTrace();
        }
        return key_str;
    }

    public float[] load(String Data)
    {
        int i=0;
        float[] data = new float[12800];

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(Data.getBytes(Charset.forName("utf-8"))),Charset.forName("utf-8")));
        String line;
        try {
            while ((line = br.readLine()) != null){
                data[i]=Float.parseFloat(line);
                i++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return data;
    }
    public float[] abs(float[] x)    //对数据取绝对值
    {
        float[] y = new float[x.length];
        for(int i=0;i<x.length;i++){
            y[i] = Math.abs(x[i]);
        }
        return y;
    }
    public int[] bin_thre(float[] x,float thres)  //对加速度值 进行 0,1处理  阈值 thres
    {
        int[] y= new int[x.length];
        for(int i=0;i<x.length;i++){
            if(x[i] >= thres){
                y[i] = 1;
            }
            else y[i] = 0;
        }
        return y;
    }
    public int[] bin_thre(float[] x,float thres,int flag)  //对加速度值 进行 0,1处理  阈值 thres
    {
        int[] y= new int[x.length];
        for(int i=0;i<x.length;i++){
            if(x[i] >= thres){
                y[i] = 11;
            }
            else if(x[i]<=-thres){
                x[i]=00;
            }
            else y[i] = 10;
        }
        return y;
    }
    public float[] window(int[] x)
    {
        float[] y=new float[128];
        for(int i=0;i<128;i++){
            float sum = 0;
            for(int j=0;j<100;j++){
                sum += x[100*i+j];
            }
            y[i] = sum / (float) 100;

        }
        return y;
    }
    public static StringBuilder toString(int[] x)
    {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<x.length;i++)
        {
            sb.append(x[i]);
        }
        return sb;
    }
}
