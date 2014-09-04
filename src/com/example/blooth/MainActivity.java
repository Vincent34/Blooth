package com.example.blooth;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.Menu;
import android.bluetooth.*;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.util.Log;
import android.view.View;
import java.io.*;
import java.net.Socket;
import java.util.UUID;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity implements SensorEventListener {
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static String address = "98:D3:31:B3:7E:B1";
	public ReadThread myReadThread;
	public EditText editText;
	public TextView textView, HUD, leftText, rightText;
	public StringBuilder builder = new StringBuilder();
	public boolean Accelerometer_on = false;
	public SeekBar leftBar,rightBar;
	public void PrepareBluetooth() {
		//获取Adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		if (!mBluetoothAdapter.isEnabled()) {
			Toast.makeText(this, "Bluetooth not enable", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		//获取Device
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		if (device == null) {
			Toast.makeText(this,"Can't get remote device", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		//获取Socket
		try {
			btSocket = 	device.createRfcommSocketToServiceRecord(myUUID);
		} catch (IOException e) {
			Log.e("TAG", "Socket creation failed");
		}
		//取消Discover
		mBluetoothAdapter.cancelDiscovery();
		//连接
		try {
			btSocket.connect();
			Log.d("TAG", "BT connection established, data transfer link open.");
		} catch (IOException e) {
			try {
				btSocket.close();
				e.printStackTrace();
			} catch (IOException ee) {
				ee.printStackTrace();
			}
		}
		myReadThread = new ReadThread();
		myReadThread.start();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		editText= (EditText)findViewById(R.id.Send_text);
		textView = (TextView)findViewById(R.id.Receive_text);
		HUD = (TextView)findViewById(R.id.HUD);
		PrepareBluetooth();
		SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if (manager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() ==0) {
			Toast.makeText(getBaseContext(), "No Accelerometer installed", Toast.LENGTH_SHORT).show();
		} else {
			Sensor accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
			if (!manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)) {
				Toast.makeText(this, "Can't register listenter", Toast.LENGTH_SHORT).show();
			}
		}
		leftText = (TextView)findViewById(R.id.lefttext);
		rightText = (TextView)findViewById(R.id.righttext);
		leftBar = (SeekBar)findViewById(R.id.leftbar);
		leftBar.setProgress(50);
		rightBar = (SeekBar)findViewById(R.id.rightbar);
		rightBar.setProgress(50);
		leftBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}
			
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				int tmp = arg1 - 50;
				tmp = (int)((double)tmp * 5.1);
				leftText.setText(String.valueOf(tmp));	
				Send("L" + String.valueOf(tmp));
			}
		});
		rightBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}
			
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				int tmp = arg1 - 50;
				tmp = (int)((double)tmp * 5.1);
				rightText.setText(String.valueOf(tmp));
				Send("R" + String.valueOf(tmp));
			}
		});
	}
	public void onSensorChanged(SensorEvent event) {
		if (Accelerometer_on) {
			int direction = (int) (event.values[0]/9.8 * 255.0);
			int power = (int)(event.values[1]/9.8 * 255.0);
			int Left,Right;
			Left = Right =  power;
			if (direction < -50) {
				Left += direction;
			} else if (direction > 50) {
				Right -= direction;
			}
			if (Left > 255) Left = 255;
			if (Right > 255) Right = 255;
			if (Left < -255) Left = -255;
			if (Right < -255) Right = -255;
			Left = -Left;
			Right = -Right;
			try {
				builder.setLength(0);
				builder.append('L');
				builder.append(Left);
				builder.append('R');
				builder.append(Right);
				byte[] buffer = builder.toString().getBytes();
				OutputStream output = btSocket.getOutputStream();
				output.write(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
			HUD.setText(builder.toString());
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
	
	Handler listenHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				String s = msg.obj.toString();
				textView.setText(s);
			}
		}
	};
	
	private class ReadThread extends Thread {
		public void run() {
			byte[] buffer = new byte[1024];
			int bytes = 0;
			InputStream myIn = null;
			
			try {
				myIn = btSocket.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			while (true) {
				try {
					if ((bytes = myIn.read(buffer)) > 0) {
						char data[] = new char[bytes];
						for (int i = 0;i < bytes;i++) 
							data[i] = (char)buffer[i];
						String s = new String(data);
						Message msg = new Message();
						msg.obj = s;
						msg.what = 0;
						listenHandler.sendMessage(msg);
					}
				} catch (IOException e) {
					try {
						myIn.close();
					} catch (IOException ee) {
						ee.printStackTrace();
					}
					break;
				}
			}
		}
	}
	public void Send(String st) {
		if (!btSocket.isConnected()) {
			try {
				Log.d("Connect","Not Connected");
				btSocket.connect();
				Log.d("Connect","Connect established");
			} catch (IOException e) {
				e.printStackTrace();
				Log.e("Connect","Send Failed");
				return;
			}
		}
		try {
			outStream = btSocket.getOutputStream();
		} catch (IOException e) {
			Log.e("Connect","Output stream creation failed");
			e.printStackTrace();
		}
		byte[] buffer = st.getBytes();
		try {
			outStream.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void Send(View view) {
//		if (!btSocket.isConnected()) {
//			try {
//				Log.d("Connect","Not Connected");
//				btSocket.connect();
//				Log.d("TAG","Connect established");
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		try {
//		outStream = btSocket.getOutputStream();
//		} catch (IOException e) {
//			Log.e("TAG","Output stream creation failed.");
//		}
		String msg = editText.getText().toString();
		Send(msg);
//		Log.d("TAG",msg);
//		byte[] myBuffer = msg.getBytes();
//		try {
//			outStream.write(myBuffer);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	public void ClickForward(View view){
		Send("w");
//		try {
//		outStream = btSocket.getOutputStream();
//		byte ch = 'w';
//		outStream.write(ch);
//	} catch (IOException e) {
//		e.printStackTrace();
//	}
	}
	public void ClickBack(View view) {
		Send("s");
	}
	public void ClickLeft(View view) {
		Send("a");
	}
	public void ClickRight(View view) {
		Send("d");
	}
	public void ClickPause(View view) {
		Send("p");
	}
	public void ClickAccelerometer(View view) {
		Accelerometer_on = !Accelerometer_on;
		if (!Accelerometer_on) HUD.setText("Accelerometer off");
	}
	public void ClickMin(View view) {
		Send("l0r0");
	}
	public void ClickMax(View view) {
		Send("l255r255");
	}
}
