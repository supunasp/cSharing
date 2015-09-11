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
import java.util.concurrent.BlockingQueue;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public final class Receive extends Thread {


	ArrayList<String> recQue;
	public static MulticastSocket socket = null;
    String[] values;
	
	public Receive(ArrayList<String> msg ) {
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

        recQue = msg;

        int strSize = msg.size();

		Log.i("Audiocast","initialised player with buffer length "+ strSize);
		

	}

	@Override
	public void run() {
		try {
			while (!Thread.interrupted()) {
				byte [] recvPkt = new byte[1024];
				DatagramPacket recv = new DatagramPacket(recvPkt, recvPkt.length);
				socket.receive(recv);

                String medd = new String(recvPkt, 0 , recv.getLength());
                recQue.add(medd);
                values = new String[recQue.size()];
                for(int x =0 ; x < recQue.size() ; x++){
                    values[x] = recQue.get(x);
                }
				Log.d("Audiocast", "received : "+medd);



			}
		} catch (IOException e) {
            e.printStackTrace();
		}
	}

}
