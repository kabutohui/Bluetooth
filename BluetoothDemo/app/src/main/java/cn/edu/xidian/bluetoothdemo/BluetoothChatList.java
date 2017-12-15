package cn.edu.xidian.bluetoothdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by lenovo on 2017/12/13.
 */
public class BluetoothChatList extends BaseAdapter {

    private Context context;
    private LayoutInflater mInflater;
    private ArrayList<BluetoothChatMsg> message;


    public BluetoothChatList(Context context,ArrayList<BluetoothChatMsg> message){
        this.context = context;
        this.message = message;
        this.mInflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        if (message == null){
            return 0;
        }else{
            return message.size();
        }
    }

    @Override
    public BluetoothChatMsg getItem(int position) {
        if (message == null){
            return null;
        }else{
            return message.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MsgDetails msgDetails;
        if (convertView == null){

            convertView = mInflater.inflate(R.layout.chat_item_details,null);
            msgDetails = new MsgDetails();

            msgDetails.linearLayout_me = (LinearLayout)convertView.findViewById(R.id.LinearLayout_me);
            msgDetails.linearLayout_other = (LinearLayout)convertView.findViewById(R.id.LinearLayout_other);
            msgDetails.tv_mymsg = (TextView)convertView.findViewById(R.id.tv_mymsg);
            msgDetails.tv_othermsg = (TextView)convertView.findViewById(R.id.tv_othermsg);

            convertView.setTag(msgDetails);
        }else{
            msgDetails = (MsgDetails) convertView.getTag();
        }

        BluetoothChatMsg msg = getItem(position);
        if (msg.Msgclass == BluetoothChatMsg.MSG_FROM_ME && msg != null){

            msgDetails.linearLayout_me.setVisibility(View.VISIBLE);
            msgDetails.tv_mymsg.setText(msg.Msgdetail);
            msgDetails.linearLayout_other.setVisibility(View.GONE);

        }else if (msg.Msgclass == BluetoothChatMsg.MSG_FROM_OTHER && msg != null){

            msgDetails.linearLayout_other.setVisibility(View.VISIBLE);
            msgDetails.tv_othermsg.setText(msg.Msgdetail);
            msgDetails.linearLayout_me.setVisibility(View.GONE);

        }
        return convertView;
    }

    private class MsgDetails{
        //关于显示部分的控件
         LinearLayout linearLayout_me;
         LinearLayout linearLayout_other;
         TextView tv_mymsg;
         TextView tv_othermsg;

    }
}
