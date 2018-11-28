package com.example.yanglin.ongoingdemo1;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ReceiveFile extends AppCompatActivity  {
    private TextView txt;
    private Button btn;
    private EditText edit_server;
    private EditText edit_ip;
    private Button server_OK;
    private LinearLayout mLinearLayout_port;
    private LinearLayout mLinearLayout_ip;

    private ReceiveHandler receiveHandler = new ReceiveHandler();
   // final int countSize = 12800;
   // int count=0;

    private int pite;
    File sdcard= Environment.getExternalStorageDirectory();
    DatagramSocket ds=null;
    DatagramPacket dp=null,messagepkg=null;
    FileOutputStream fos=null;
    Button btn_start,btn_stop;  //传感器样式
    File f1=new File(sdcard,"onefile.txt");
    File f2=new File(sdcard,"twofile.txt");

    File f1sensor = new File(sdcard,"update.txt");
    File f2sensor = new File(sdcard,"update2.txt");
    private SensorManager sm;
Button sensorSend ;

    Button btn_gps,btn_gry ;

    class ReceiveHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(msg.what==1) {
                txt.setText("收到文件");
                Toast.makeText(ReceiveFile.this, "我是LV，我接收到文件", Toast.LENGTH_SHORT).show();
            }
            else
                if(msg.what==2){
                    txt.setText("发送反馈");
                    Toast.makeText(ReceiveFile.this, "我是LV，发送反馈", Toast.LENGTH_SHORT).show();
                }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_udp);
        txt = (TextView) findViewById ( R.id.textView );
        btn = (Button) findViewById ( R.id.btn );
        edit_server=(EditText)findViewById ( R.id.editText_server );
        edit_ip=(EditText)findViewById ( R.id.client_ip );
        server_OK=(Button)findViewById ( R.id.server_OK );
        //样式
        mLinearLayout_port=(LinearLayout)findViewById ( R.id.lin_1 ) ;
        mLinearLayout_ip=(LinearLayout)findViewById ( R.id.lin_ip ) ;
        //隐去以下按钮
        btn.setVisibility(View.GONE);
        mLinearLayout_ip.setVisibility ( View.GONE );

        server_OK.setOnClickListener ( new View.OnClickListener ( )
        {@Override
            public void onClick(View v)
            {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        receive(f2);
                    }
                }).start();

            }
        } );

        sensorSend = (Button)findViewById(R.id.sensorSend ) ;
        sensorSend.setText("接收");
        sensorSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        receive(f2sensor);
                    }
                }).start();
            }
        });
        initView();
        btn_gps = (Button)findViewById(R.id.btn_gps);
        btn_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               new Thread(new Runnable() {
                   @Override
                   public void run() {
                       CompareFile(f1,f2);
                   }
               }).start();

            }
        });
        btn_gry=(Button)findViewById(R.id.btn_gry);
        btn_gry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CompareFile(f1sensor ,f2sensor );
                    }
                }).start();
            }
        });
    }

    public void receive(File f2){
        pite= Integer.parseInt(edit_server.getText ().toString ());
        try {
              ds= new DatagramSocket(pite);
            byte[] by = new byte[1024];
            fos = new FileOutputStream(f2);
             dp = new DatagramPacket(by,by.length);
            System.out.println("等待接受数据");

            while(true) {
                ds.receive(dp);
                if(new String(dp.getData(),0,dp.getLength()).equals("end")) {
                    receiveHandler.sendEmptyMessage(1); //通知本机接受完毕
                    break;
                }
                fos.write(dp.getData(), 0, dp.getLength());
                fos.flush();
                String str= new String(dp.getData(),0,dp.getLength());
                System.out.println("接收到的数据是：");
                System.out.println(str+"--->"+dp.getAddress().getHostAddress()
                        +":"+dp.getPort());
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                fos.close();
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
                Toast.makeText(ReceiveFile.this,"start...",Toast.LENGTH_SHORT).show();//提示开始记录
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
                Toast.makeText(ReceiveFile.this,"stop！.",Toast.LENGTH_SHORT).show();
            }
        });
    }


public void CompareFile(File f1,File f2){
    pite= Integer.parseInt(edit_server.getText ().toString ());
    byte[] messagebuf = new byte[1024];  //反馈信息
    DiscreteFrechetDistance dt = new DiscreteFrechetDistance();
    String s1 = dt.createStr(f1);
    String s2 = dt.createStr(f2);

    String d = dt.test(s1,s2);
    System.out.println("*******CompareFile中的d******"+d);

if(d.equals("00.0000")){
    messagebuf = "ok".getBytes();
}else {
    messagebuf="disable".getBytes();
}
    try {
        ds= new DatagramSocket(pite);
        messagepkg = new DatagramPacket(messagebuf, messagebuf.length, InetAddress.getByName(dp.getAddress().getHostAddress()), 8090);
        System.out.println("------打印信息-------" + new String(messagepkg.getData()));
        ds.send(messagepkg);
    }catch (IOException e){
        e.printStackTrace();
    }
    receiveHandler.sendEmptyMessage(2);
}



}
