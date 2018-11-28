package com.example.yanglin.ongoingdemo1;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;



public class SendFile extends AppCompatActivity
{

    private TextView txt;
    private Button btn;
    private EditText edit_server;
    private Button server_OK;
    private EditText edit_ip;
    private LinearLayout mLinearLayout;
    private LinearLayout mLinearLayout_client;
    private SendHandler sendHandler = new SendHandler();

    BufferedInputStream bis=null;
    DatagramSocket ds=null;
    DatagramPacket dp=null,messagepkg=null;
    private int pite;
    private String ip="";
    File sdcard= Environment.getExternalStorageDirectory();
    File f1 =  new File(sdcard,"onefile.txt");

    File f1sensor = new File(sdcard,"update.txt");
    private SensorManager sm;
    Button btn_start,btn_stop; //定义传感器样式
     Button sensorSend;
    Button btn_gps,btn_gry ;
    class  SendHandler extends  Handler{
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(msg.what==1) {
                txt.setText("文件发送成功");
                Toast.makeText(SendFile.this, "我是CV，文件发送成功", Toast.LENGTH_SHORT).show();
            }
            else if(msg.what==2){
                txt.setText("收到反馈：ok");
                Toast.makeText(SendFile.this, "接受到反馈", Toast.LENGTH_SHORT).show();
            }
            else if(msg.what==3){
                txt.setText("收到反馈：disable");
                Toast.makeText(SendFile.this, "接受到反馈", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_udp);
        txt=(TextView)findViewById ( R.id.textView );
        btn=(Button)findViewById ( R.id.btn );
        edit_server=(EditText)findViewById ( R.id.editText_server );
        edit_ip=(EditText)findViewById ( R.id.client_ip );
        server_OK=(Button)findViewById ( R.id.server_OK );
        mLinearLayout_client=(LinearLayout)findViewById ( R.id.lin_ip ) ;//服务端的IP地址
        mLinearLayout=(LinearLayout)findViewById ( R.id.lin_1 ) ;       //端口号+确认按钮
        server_OK.setVisibility(View.GONE);

        btn.setOnClickListener ( new View.OnClickListener ( )//发送GPS数据
        {@Override
            public void onClick(View v)
            {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        send(f1);
                    }
                }).start();
            }
        } );

        initView();
        sensorSend = (Button)findViewById(R.id.sensorSend);
        sensorSend.setOnClickListener(new View.OnClickListener() {  //发送陀螺仪数据
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        send(f1sensor);
                    }
                }).start();
            }
        });
        btn_gps=(Button)findViewById(R.id.btn_gps);
        btn_gps.setVisibility(View.GONE);
        btn_gry=(Button)findViewById(R.id.btn_gry);
        btn_gry.setVisibility(View.GONE);
    }

    public void send(File f1){
        ip=edit_ip.getText ().toString ();  //获取输入框的ip地址
        pite= Integer.parseInt(edit_server.getText ().toString ());//获取输入框的端口号

        try {
              bis = new BufferedInputStream(new FileInputStream(f1));
             ds = new DatagramSocket(8090);
            int temp=0;
            byte[] bytes= new byte[1024];
            byte[] messagebuf = new byte[1024];
            while((temp=bis.read(bytes))!=-1) {
                dp = new DatagramPacket(bytes,temp, InetAddress.getByName(ip), pite);
                System.out.println("正在发送数据....");
                ds.send(dp);
            }
            bytes = "end".getBytes();
             dp = new DatagramPacket(bytes,bytes.length,InetAddress.getByName(ip),pite);
            ds.send(dp);//将end发送出去
             System.out.print("*******文件已经发送完毕********");
            sendHandler.sendEmptyMessage(1);

            messagepkg = new DatagramPacket(messagebuf,messagebuf.length);
            System.out.println("------打印信息初始-------"+new String(messagepkg.getData()));

            ds.receive(messagepkg);
            System.out.println("打印接收的信息"+new String(messagepkg.getData()));
            if(new String(messagepkg.getData(),0,messagepkg.getLength()).equals("ok")) {
                sendHandler.sendEmptyMessage(2);
            }else {
                sendHandler.sendEmptyMessage(3);
            }

        }catch (IOException ex){
         System.out.print("请检查ip及地址");
            ex.printStackTrace();
        }finally {
            try{

                bis.close();
                ds.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public  void initView(){
        //传感器的样式,当 反馈数值为..
        btn_start =(Button)findViewById(R.id.btn_start);
        btn_stop=(Button)findViewById(R.id.btn_stop) ;

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(SendFile.this,"start...",Toast.LENGTH_SHORT).show();//提示开始记录
                Ssensor s = new Ssensor();
                sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                s.begin(sm);

            }
        });
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Ssensor s = new Ssensor();
                sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                s.stop(sm);
                Toast.makeText(SendFile.this,"stop！.",Toast.LENGTH_SHORT).show();

            }
        });
    }

}