package it.polimi.mediasharing.activities;

import it.polimi.mediasharing.sockets.Client;
import it.polimi.mediasharing.sockets.Server;
import it.polimi.mediasharing.util.StringTimeUtil;
import it.polimit.mediasharing_sender.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import a3.a3droid.A3DroidActivity;
import a3.a3droid.A3Node;
import a3.a3droid.Timer;
import a3.a3droid.TimerInterface;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends A3DroidActivity implements TimerInterface{
	
	public static final String PACKAGE_NAME = "it.polimi.mediasharing.a3";
	public static final String EXPERIMENT_PREFIX = "A3Test3_";
	public static final int NUMBER_OF_EXPERIMENTS = 32;
	
	public static final int CREATE_GROUP = 31;
	public static final int STOP_EXPERIMENT = 32;
	public static final int CREATE_GROUP_USER_COMMAND = 33;
	public static final int LONG_RTT = 34;
	public static final int STOP_EXPERIMENT_COMMAND = 35;
	public static final int PING = 36;
	public static final int PONG = 37;
	public static final int NEW_PHONE = 38;
	public static final int START_EXPERIMENT_USER_COMMAND = 39;
	public static final int START_EXPERIMENT = 40;
	public static final int DATA = 41;
	public static final int MEDIA_DATA = 42;
	public static final int MEDIA_DATA_SHARE = 43;
	public static final int RFS = 50;
	public static final int MC = 51;
	public static final int SID = 52;
	public static final int MCR = 53;
	public static final int SERVLET_PORT = 4444;
	
	private A3Node node;
	private EditText inText;
	private Handler toGuiThread;
	private Handler fromGuiThread;
	private Client client = new Client();
	private Server server;
	private EditText supervisorAddress;
	private boolean experimentIsRunning = false;
	private int sentCont = 0;
	private double avgRTT = 0;
	private long sendTime;
	public static int runningExperiment;
	private String startTimestamp;
	private final static long MAX_INTERNAL = 5 * 1000;
	private final static long TIMEOUT = 60 * 1000;
	

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		toGuiThread = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				
				inText.append(msg.obj + "\n");
			}
		};
		
		HandlerThread thread = new HandlerThread("Handler");
		thread.start();
		fromGuiThread = new Handler(thread.getLooper()){
			@Override
			public void handleMessage(Message msg){
				
				long rtt;
				switch (msg.what) {
					case STOP_EXPERIMENT:
						if(experimentIsRunning){
							this.sendEmptyMessage(LONG_RTT);
						}
						break;
					case START_EXPERIMENT:
						if(!experimentIsRunning){
							experimentIsRunning = true;
							sentCont = 0;
							avgRTT = 0;
							startTimestamp = StringTimeUtil.getTimestamp();
							showOnScreen("Experiment has started");
							new Timer(MainActivity.this, 0, (int) (Math.random() * 1000)).start();
						}
						break;	
					case SID:
						showOnScreen("Received supervisor address");
						sendFileToAnotherGroup((String)msg.obj);
						break;
					case MCR:
						showOnScreen("Received supervisor confirmation");
						sentCont ++;
						rtt = StringTimeUtil.roundTripTime(sendTime, StringTimeUtil.getTimestamp()) / 1000;
						avgRTT = (avgRTT * (sentCont - 1) + rtt) / sentCont; 

						if(rtt > TIMEOUT && experimentIsRunning){
							experimentIsRunning = false;
							this.sendEmptyMessage(LONG_RTT);
						}
						else{
							new Timer(MainActivity.this, 0, (int) (Math.random() * MAX_INTERNAL)).start();
						}
						if(sentCont % 10 == 0)
							showOnScreen(sentCont + " mex spediti.");
						break;
					case LONG_RTT:
						showOnScreen("Experiment has stopped");
						experimentIsRunning = false;
						long runningTime = StringTimeUtil.roundTripTime(startTimestamp, StringTimeUtil.getTimestamp()) / 1000;
						float frequency = sentCont / ((float)(runningTime));
						File sd;
						File f;
						FileWriter fw;
						BufferedWriter bw;
						sd = Environment.getExternalStorageDirectory();
						f = new File(sd, MainActivity.EXPERIMENT_PREFIX + "Mediasharing" + ".txt");
						try {
							fw = new FileWriter(f, true);
							bw = new BufferedWriter(fw);
							bw.write((sentCont + "\t" +
									runningTime + "\t" + frequency + "\t" + avgRTT).replace(".", ",") + "\n");
							bw.flush();
						} catch (IOException e) {
							showOnScreen(e.getLocalizedMessage());
						}
						break;
					default:
						break;
				}					
			}
		};

		inText=(EditText)findViewById(R.id.oneInEditText);
		supervisorAddress = (EditText)findViewById(R.id.editText1);
		try {
			server = new Server(SERVLET_PORT, fromGuiThread);
			server.start();
		} catch (IOException e) {
			showOnScreen("Error creating the file server.");
		}
	}
	
	private void sendRequestForSharing(){
		showOnScreen("Sending a Request for Sharing (RFS)");
		sendTime = new Date().getTime();
		try {
			client.sendMessage(supervisorAddress.getText().toString(), 4444, MainActivity.RFS,  "");
		} catch (IOException e) {			
			e.printStackTrace();
			showOnScreen("Error sending file to another group");
		}
	}
	
	private void sendFileToAnotherGroup(String host){
		showOnScreen("Sending Media Content (MC) to supervisor address");
		InputStream is = getResources().openRawResource(R.raw.image);
		try {
			client.sendFile(host, SERVLET_PORT, MainActivity.MC, is);
		} catch (IOException e) {
			e.printStackTrace();
			showOnScreen("Error sending file to another group");
		}
	}

	public void stopExperiment(View v){
		fromGuiThread.sendEmptyMessage(STOP_EXPERIMENT);
	}
	
	public void startExperiment(View v){
		fromGuiThread.sendEmptyMessage(START_EXPERIMENT);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onDestroy(){
		node.disconnect("control", true);
		server.interrupt();
		System.exit(0);
	}
	
	@Override
	public void showOnScreen(String message) {
		toGuiThread.sendMessage(toGuiThread.obtainMessage(0, message));
	}

	@Override
	public void timerFired(int reason) {	
		if(experimentIsRunning)
			sendRequestForSharing();
	}	
}
