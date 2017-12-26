package cn.edu.xidian.bluetoothdemo.fileView;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.edu.xidian.bluetoothdemo.R;



public class AdapterManager {
	private Context mContext;
	private FileListAdapter mFileListAdapter;
	private List<BluetoothDevice> mDeviceList;
	private List<File> mFileList;
	private Handler mainHandler;
	
	public AdapterManager(Context context){
		this.mContext = context;
	}
	


	/**
	 * getFileListAdapter()
	 * @return
	 */
	public FileListAdapter getFileListAdapter(){
		if(null == mFileListAdapter){
			mFileList = new ArrayList<File>();
			mFileListAdapter = new FileListAdapter(mContext, mFileList, R.layout.file_list_item);
		}
		return mFileListAdapter;
	}
	

	/**
	 * 清除设备
	 */
	public void clearDevice(){
		if(null != mDeviceList){
			mDeviceList.clear();
		}
	}
	
	/**
	 * 添加设备
	 * @param bluetoothDevice
	 */
	public void addDevice(BluetoothDevice bluetoothDevice){
		mDeviceList.add(bluetoothDevice);
	}
	
	/**
	 *改变设备
	 * @param listId
	 * @param bluetoothDevice
	 */
	public void changeDevice(int listId, BluetoothDevice bluetoothDevice){
		mDeviceList.remove(listId);
		mDeviceList.add(listId, bluetoothDevice);
	}
	
	/**
	 * 更新文件列表
	 * @param path
	 */
	public void updateFileListAdapter(String path){
		mFileList.clear();

		mFileList.addAll(FileUtil.getFileList(path));
		if(null == mainHandler){
			mainHandler = new Handler(mContext.getMainLooper());
		}
		mainHandler.post(new Runnable() {
			
			@Override
			public void run() {
				mFileListAdapter.notifyDataSetChanged();
			}
		});
	}
	
	/**
	 * 获取设备列表
	 * @return
	 */
	public List<BluetoothDevice> getDeviceList() {
		return mDeviceList;
	}
}
