package cn.edu.xidian.bluetoothdemo;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2017/12/5.
 */
public class listView {
    private ListView lv;
    private Context context;
    private List<BluetoothDevice> bluetoothDevice = null;


    public listView(Context context,ListView lv,List<BluetoothDevice> bluetoothDevice)
    {
        this.context = context;
        this.lv = lv;
        this.bluetoothDevice = bluetoothDevice;
    }



    public void listShow1()
    {
        List<String> data = new ArrayList<String>();
        for(BluetoothDevice btDevice : bluetoothDevice){
            data.add(btDevice.getName());
        }
        lv.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, data));
    }

}
