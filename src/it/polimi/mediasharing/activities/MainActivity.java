package it.polimi.mediasharing.activities;

import it.polimi.mediasharing.sockets.Client;
import it.polimit.mediasharing_sender.R;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import a3.a3droid.A3DroidActivity;
import a3.a3droid.A3Node;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends A3DroidActivity{
	
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
	
	private A3Node node;
	private EditText inText;
	private Handler toGuiThread;
	private Handler fromGuiThread;
	private EditText experiment;
	public static int runningExperiment;
	private Client client = new Client();
	//private EditText numberOfGroupsToCreate;
	
	public static void setRunningExperiment(int runningExperiment) {
		MainActivity.runningExperiment = runningExperiment;
	}

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
				
				switch (msg.what) {
					case 1:
						break;
					case SID:			
						sendFileToAnotherGroup((String)msg.obj);
						break;
					case 6:
						sendRequestForSharing();
						break;	
					default:
						break;
				}					
			}
		};

		inText=(EditText)findViewById(R.id.oneInEditText);
		experiment = (EditText)findViewById(R.id.editText1);
		
		/*ArrayList<String> roles = new ArrayList<String>();
		roles.add(ControlSupervisorRole.class.getName());
		roles.add(ControlFollowerRole.class.getName());
		roles.add(ExperimentSupervisorRole.class.getName());
		roles.add(ExperimentFollowerRole.class.getName());
		
		
		ArrayList<GroupDescriptor> groupDescriptors = new ArrayList<GroupDescriptor>();
		groupDescriptors.add(new ControlDescriptor());
		groupDescriptors.add(new ExperimentDescriptor());
		
		node = new A3Node(this, roles, groupDescriptors);
		node.connect("control", false, true);*/
	}
	
	private void sendRequestForSharing(){
		File sd = Environment.getExternalStorageDirectory();
		File f = new File(sd, "image.jpg");
		try {
			client.sendMessage("192.168.1.101", 4444, MainActivity.RFS, new Date().getTime() + "");
		} catch (IOException e) {
			System.out.println("Error sending file to another group");
			e.printStackTrace();
			showOnScreen("Error sending file to another group");
		}
	}
	
	private void sendFileToAnotherGroup(String host){
		File sd = Environment.getExternalStorageDirectory();
		File f = new File(sd, "image.jpg");
		try {
			client.sendFile(host, 4444, MainActivity.MC, f);
		} catch (IOException e) {
			System.out.println("Error sending file to another group");
			e.printStackTrace();
			showOnScreen("Error sending file to another group");
		}
	}

	public void start1(View v){
		fromGuiThread.sendEmptyMessage(1);
	}
	
	public void stopExperiment(View v){
		fromGuiThread.sendEmptyMessage(5);
	}
	
	public void startExperiment(View v){
		fromGuiThread.sendEmptyMessage(6);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onDestroy(){
		node.disconnect("control", true);
		System.exit(0);
	}
	
	@Override
	public void showOnScreen(String message) {
		// TODO Auto-generated method stub
		toGuiThread.sendMessage(toGuiThread.obtainMessage(0, message));
	}
}
