package cn.edu.xidian.bluetoothdemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import cn.edu.xidian.bluetoothdemo.fileView.SelectFileActivity;

/**
 * Created by lenovo on 2017/12/12.
 */
public class BluetoothChat extends Activity implements View.OnClickListener {

    //获取由主activity传过来的设备名称和地址
    private String device_name, device_address;
    // 服务端利用线程不断接受客户端信息
    private AcceptThread thread;
    // 获取到蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter;
    // UUID，蓝牙建立链接需要的
    private final UUID MY_UUID = UUID.fromString("db764ac8-4b08-7f25-aafe-59d03c27bae3");
    // 为其链接创建一个名称
    private final String NAME = "Bluetooth_Socket";
    // 消息存储与显示
    private ArrayList<BluetoothChatMsg> mArray;
    private BluetoothChatList mAdapter;
    //文本编辑与发送按钮与列表
    private EditText et_msg;
    private Button btn_send;
    private ListView lv_message;
    private Button btn_filesend;
    // 获取目标设备
    private BluetoothDevice desDevice;
    // 获取到选中设备的客户端串口
    private BluetoothSocket clientSocket;
    //服务端
    private BluetoothServerSocket serverSocket;// 服务端接口
    private BluetoothSocket socket;// 获取到客户端的接口
    // 获取到向设备写的输出流
    private OutputStream os;
    //获取欲发送文件绝对路径相应代码
    private static final int REQUEST_CODE_FOR_FILEABSOLUTEPATH = 3;
    //文件发送socket
    private BluetoothSocket btSocket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        Intent intent = getIntent();
        device_name = intent.getStringExtra("device_name");
        device_address = intent.getStringExtra("device_addr");

        //设置标题
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText(device_name);

        //初始化数据
        initData();

        //接收客户端传过来的数据线程
        thread = new AcceptThread();
        //线程开始
        thread.start();
    }



    private void initData() {

        //初始化蓝牙Adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //获取控件
        et_msg = findViewById(R.id.etMessage);
        btn_send = findViewById(R.id.btn_send);
        lv_message = findViewById(R.id.message_list);
        btn_filesend = findViewById(R.id.btn_filesend);

        //监听发送按钮
        btn_send.setOnClickListener(this);
        btn_filesend.setOnClickListener(this);

        //初始化list
        mArray = new ArrayList<BluetoothChatMsg>();

        mAdapter = new BluetoothChatList(BluetoothChat.this, mArray);

        lv_message.setAdapter(mAdapter);


    }


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>"+(String)msg.obj);
            mArray.add(new BluetoothChatMsg(BluetoothChatMsg.MSG_FROM_OTHER, (String) msg.obj));
            mAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                //发送事件
                if (desDevice == null) {
                    //通过地址获取到该设备
                    desDevice = mBluetoothAdapter.getRemoteDevice(device_address);
                }
                try {
                    // 判断客户端接口是否为空
                    if (clientSocket == null) {
                        // 获取到客户端接口
                        clientSocket = desDevice.createRfcommSocketToServiceRecord(MY_UUID);
                        // 向服务端发送连接
                        clientSocket.connect();
                        // 获取到输出流，向外写数据
                        os = clientSocket.getOutputStream();
                    }
                    // 判断是否拿到输出流
                    if (os != null) {
                        // 需要发送的信息
                        String text = et_msg.getText().toString();
                        //将消息显示在聊天窗口中
                        mArray.add(new BluetoothChatMsg(BluetoothChatMsg.MSG_FROM_ME, text));
                        mAdapter.notifyDataSetChanged();
                        // 以utf-8的格式发送出去
                        os.write(text.getBytes("UTF-8"));
                    }
                    //清空edittext
                    et_msg.getText().clear();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    // 如果发生异常则告诉用户发送失败
                    Toast.makeText(this, "发送信息失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_filesend:
                //打开文件浏览
                Intent serverIntent = new Intent(this, SelectFileActivity.class);
                startActivityForResult(serverIntent, REQUEST_CODE_FOR_FILEABSOLUTEPATH);
                break;
        }
    }


    // 服务端接收信息线程
    private class AcceptThread extends Thread {

        private InputStream is;// 获取到输入流
        private OutputStream os;// 获取到输出流

        public AcceptThread() {
            try {
                // 通过UUID监听请求，然后获取到对应的服务端接口
                serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

        public void run() {
            try {
                // 接收其客户端的接口
                socket = serverSocket.accept();
                // 获取到输入流
                is = socket.getInputStream();
                // 获取到输出流
                //os = socket.getOutputStream();

                // 无线循环来接收数据
                while (true) {
                    // 创建一个128字节的缓冲
                    byte[] buffer = new byte[1024];
                    // 每次读取128字节，并保存其读取的角标
                    int count = is.read(buffer);
                    // 创建Message类，向handler发送数据
                    System.out.println(">>>>>>>>>>>>>>>>" + buffer.toString());
                    Message msg = new Message();
                    // 发送一个String的数据，让他向上转型为obj类型
                    msg.obj = new String(buffer, 0, count, "utf-8");
                    // 显示数据

                    handler.sendMessage(msg);
                }
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
            if (socket != null) {
                socket.close();
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_CODE_FOR_FILEABSOLUTEPATH:
                if (resultCode == Activity.RESULT_OK){
                    //返回的data是所选择文件的绝对路径
                    String path = data.getExtras().getString("fileAbsolutePath");
                    Toast.makeText(this,"返回的路径为："+path,Toast.LENGTH_LONG).show();

                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("*/*");
                    sharingIntent.setComponent(new ComponentName("com.android.bluetooth", "com.android.bluetooth.opp.BluetoothOppLauncherActivity"));
                  sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));
                    //此种方法兼容android 7.0以上版本，并向下兼容低版本的android系统
//                    sharingIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(BluetoothChat.this,"cn.edu.xidian.bluetoothdemo"+".fileprovider",new File(path)));
//                    sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    startActivity(sharingIntent);
                }
                break;
        }
    }
}
