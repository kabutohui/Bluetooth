package cn.edu.xidian.bluetoothdemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //获取蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    //按钮
    private Button btn_open,btn_scan,btn_close;
    //textview
    private TextView tv_info;
    //已配对列表
    private ListView list_paired;
    //已配对列表data
    private List<BluetoothDevice> devices_paired = new ArrayList<BluetoothDevice>();
    private listView data_paired;
    //请求码
    private static final int REQUEST_CODE_FOR_SEARCH = 1;
    private static final int REQUEST_CODE_FOR_OPEN = 2;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initEvent();
    }

    private void initData(){
        btn_open = (Button) findViewById(R.id.btn_open);
        btn_scan = (Button) findViewById(R.id.btn_scan);
        btn_close = (Button) findViewById(R.id.btn_close);
        tv_info = (TextView) findViewById(R.id.tv_btdetail);
        list_paired = (ListView) findViewById(R.id.list_paired);
    }

    private void initEvent(){
        btn_open.setOnClickListener(this);
        btn_scan.setOnClickListener(this);
        btn_close.setOnClickListener(this);

        //监听已配对列表
        list_paired.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,BluetoothChat.class);
                intent.putExtra("device_name",devices_paired.get(position).getName().toString());
                intent.putExtra("device_addr",devices_paired.get(position).getAddress().toString());
                startActivity(intent);
            }
        });

        //监听已配对列表长按事件
        list_paired.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //长按取消配对
                try {
                    removeBond(devices_paired.get(position));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //在已配对列表中移除
                devices_paired.remove(position);

                return true;
            }
        });


    }

    public void getBindDevice() {
        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        devices_paired.clear();
        devices_paired.addAll(bondedDevices);
        if(devices_paired.size() > 0) {
            data_paired = new listView(MainActivity.this, list_paired, devices_paired);
            data_paired.listShow1();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_open:
                StartBluetooth();
                break;
            case R.id.btn_close:
                CloseBluetooth();
                break;
            case R.id.btn_scan:
                ScanBuletooth();
                break;
        }
    }


    private void StartBluetooth(){
        if(!mBluetoothAdapter.isEnabled()){
            //弹出对话框提示用户是后打开
            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabler, REQUEST_CODE_FOR_OPEN);
        }else{
            Toast.makeText(this, "蓝牙已打开", Toast.LENGTH_LONG).show();
        }

        //显示本机蓝牙信息
        String name = mBluetoothAdapter.getName();//获取本机蓝牙名称
        String address = mBluetoothAdapter.getAddress(); //获取本机蓝牙地址
        tv_info.setText("本机蓝牙：" + name + "\n本机地址：" + address);

    }

    private void CloseBluetooth(){
        if(mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.disable();
            Toast.makeText(this, "关闭蓝牙", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "蓝牙已关闭", Toast.LENGTH_SHORT).show();
        }
    }

    private void ScanBuletooth(){
        //显示已配对蓝牙信息
        getBindDevice();

        //打开蓝牙搜索界面
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CODE_FOR_SEARCH);

        //监听蓝牙状态
        IntentFilter filter = new IntentFilter();
        //设备连接状态改变
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBluetoothReceiver, filter);
    }


    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("info","mBluetoothReceiver action ="+action);

            if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_NONE:
                        Toast.makeText(MainActivity.this,"取消配对",Toast.LENGTH_LONG).show();
                        //取消配对成功，刷新配对列表
                        if (devices_paired.size() == 0){
                            data_paired.listShow1();
                        }else {
                            getBindDevice();
                        }
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Toast.makeText(MainActivity.this,"配对中",Toast.LENGTH_LONG).show();
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Toast.makeText(MainActivity.this,"配对成功",Toast.LENGTH_LONG).show();
                        //配对成功，刷新已配对列表
                        getBindDevice();
                        break;
                }
            }
        }

    };


    /**
     * 与设备配对 参考源码：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
    static public boolean createBond(BluetoothDevice btDevice) throws Exception {
        Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    /**
     * 与设备解除配对 参考源码：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
    static public boolean removeBond(BluetoothDevice btDevice) throws Exception {
        Method removeBondMethod = BluetoothDevice.class.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    //接受从DeviceListActivity中返回的蓝牙Address的值
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_CODE_FOR_SEARCH:
                if (resultCode == Activity.RESULT_OK){
                    //返回的data是选择蓝牙的地址，在这里进行配对
                    mBluetoothAdapter.cancelDiscovery();
                    BluetoothDevice btDevice = mBluetoothAdapter.getRemoteDevice(data.getExtras().getString("device_address"));
                    try {
                        createBond(btDevice);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case REQUEST_CODE_FOR_OPEN:
                if (resultCode == Activity.RESULT_OK){
                    Toast.makeText(MainActivity.this,"蓝牙已打开",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
