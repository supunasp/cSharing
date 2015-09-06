/* E numbers
 * 	E/11/060
 *  E/11/488
 */
package com.example.supunathukorala.csharing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


/**
 * @author (C) Dushk
 */
public class AudiocastActivity extends Activity {
	final static int SAMPLE_HZ = 11025, BACKLOG = 8;
	public static int [] sysNum;
    ArrayList<String> recQue;
    String[] values;
    MulticastSocket socket;

	//final static InetSocketAddress group = new InetSocketAddress("224.0.0.1", 3238);
	//public static MulticastSocket socket= null;

	Receive receive;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);
		
		WifiManager wifi = (WifiManager)getSystemService( Context.WIFI_SERVICE );
		if(wifi != null){
		    WifiManager.MulticastLock lock = 
		    		wifi.createMulticastLock("Audiocast");
		    lock.setReferenceCounted(true);
		    lock.acquire();
		} else {
			Log.e("Audiocast", "Unable to acquire multicast lock");
			finish();
		}
        receiverMeg recvMsgThread = new receiverMeg(recQue);
        recvMsgThread.execute((Void) null);
	}


    private class receiverMeg extends AsyncTask<Void , Void , Boolean >{
        ArrayList<String> msgList;

        receiverMeg(ArrayList<String> msgList){
            try {
                if(socket == null){
                    socket = new MulticastSocket(3238);
                    socket.setBroadcast(true);
                    InetAddress group = InetAddress.getByName("224.0.0.1");
                    socket.joinGroup(group);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            recQue = msgList;

            int strSize = msgList.size();

            Log.i("Audiocast","initialised player with buffer length "+ strSize);
            this.msgList = msgList;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            Thread newThread = new Thread(){

                public void run(){
                    while (true) {
                        byte [] recvPkt = new byte[1024];
                        DatagramPacket recv = new DatagramPacket(recvPkt, recvPkt.length);
                        try {
                            socket.receive(recv);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        String medd = new String(recvPkt, 0 , recv.getLength());
                        Toast.makeText(getApplicationContext(),medd,Toast.LENGTH_SHORT);
                        recQue.add(medd);
                        values = new String[recQue.size()];
                        for(int x =0 ; x < recQue.size() ; x++){
                            values[x] = recQue.get(x);
                        }
                        Log.d("Audiocast", "received : "+medd);


                    }
                }

            };
            newThread.start();



            return null;
        }


    }
	
	@Override
	protected void onStart() {
		super.onStart();
		//Server s = new Server();
		
		  /*if(socket == null)
			try {
				socket = new MulticastSocket(3238);
				socket.setBroadcast(true);
				InetAddress group = InetAddress.getByName("224.0.0.1");
				socket.joinGroup(group);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		*/



	}
	
	@Override
	protected void onStop() {
		super.onStop();
		receive.interrupt();
		
	}
}
