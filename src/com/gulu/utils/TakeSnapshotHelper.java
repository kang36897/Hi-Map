package com.gulu.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapScreenShotListener;

public class TakeSnapshotHelper {
	
	private AMap mAMap;
	private Handler mHandler;
	
	public TakeSnapshotHelper(AMap map, Handler handler) {
		this.mAMap = map;
		this.mHandler = handler;
	}
	
	private OnMapScreenShotListener mScreenShotListner = new OnMapScreenShotListener() {
		
		@Override
		public void onMapScreenShot(Bitmap bitmap) {
			SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
			
			File directory = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			FileOutputStream fos = null;
			try {
				File png = new File(directory + File.separator + "HiMap"
						+ sf.format(new Date()) + ".png");
				fos = new FileOutputStream(png);
				boolean success = bitmap.compress(CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();
				
				if (success) {
					Message msg = mHandler
							.obtainMessage(MsgConstants.SNAPSHOT_SUCCEED);
					msg.obj = Uri.fromFile(png);
					msg.sendToTarget();
				} else {
					mHandler.sendEmptyMessage(MsgConstants.SNAPHOT_FAILED);
				}
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				mHandler.sendEmptyMessage(MsgConstants.SNAPHOT_FAILED);
			} catch (IOException e) {
				e.printStackTrace();
				mHandler.sendEmptyMessage(MsgConstants.SNAPHOT_FAILED);
			} finally {
				
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		}
	};
	
	public void takeSnapshot() {
		if (mAMap == null) {
			return;
		}
		
		mAMap.getMapScreenShot(mScreenShotListner);
	}
	
}
