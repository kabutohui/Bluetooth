package cn.edu.xidian.bluetoothdemo;

/**
 * Created by lenovo on 2017/12/13.
 */
public class BluetoothChatMsg {
    public int Msgclass;
    public String Msgdetail;

    //静态变量
    public static final int MSG_FROM_ME = 1;
    public static final int MSG_FROM_OTHER = 2;

    public BluetoothChatMsg(int msgclass,String msgdetail){
        this.Msgclass = msgclass;
        this.Msgdetail = msgdetail;
    }
}
