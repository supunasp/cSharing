package com.example.supunathukorala.csharing;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.util.ArrayList;


/**
 * @author (C) Dushk
 */
public class MsgActivity extends Activity implements View.OnClickListener {

    private static final int SHARE_PICTURE = 4;

    ArrayList<String> recQue;
    String[] values ;
    MulticastSocket socket;
    InetAddress group;
    ListView listView;
    ArrayAdapter adapter;
    String username;

    final int portNum = 3238;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        username = (String) getIntent().getExtras().get("name");

        TextView userNm = (TextView) findViewById(R.id.usrName);

        userNm.setText(username);

        Button shareButton = (Button) findViewById(R.id.buttonshare);

        shareButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(i, "Select Picture"), SHARE_PICTURE);

            }
        });

        listView = (ListView) findViewById(R.id.listView);

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
        recQue =new ArrayList<>();

        try {
            if(socket == null){
                socket = new MulticastSocket(portNum);
                socket.setBroadcast(true);
                group = InetAddress.getByName("224.0.0.1");
                socket.joinGroup(group);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        receiverMeg recvMsgThread = new receiverMeg(recQue);
        recvMsgThread.execute((Void) null);
        Button send = (Button)findViewById(R.id.button);
        send.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        EditText text = (EditText)findViewById(R.id.editText2);
        String textMsg = text.getText().toString();

            text.setText("");


        sendMesg sendMesg = new sendMesg(textMsg);
        sendMesg.execute((Void) null);

    }


    private class receiverMeg extends AsyncTask<Void , Void , Boolean > {
        ArrayList<String> msgList;

        receiverMeg(ArrayList<String> msgList){


            recQue = msgList;
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

                        final String medd = new String(recvPkt, 0 , recv.getLength());

                        recQue.add(medd);
                        values = new String[recQue.size()];
                        for(int x =0 ; x < recQue.size() ; x++){
                            values[x] = recQue.get(x);
                        }


                        //              Toast.makeText(getApplicationContext(), medd, Toast.LENGTH_SHORT);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /**
                                 *   If the network is not available
                                 * */
                                Toast.makeText(getApplicationContext(), medd +" : " +recQue.size(), Toast.LENGTH_LONG).show();


                                adapter = new ArrayAdapter<>(MsgActivity.this,android.R.layout.simple_list_item_1, android.R.id.text1, values);
                                listView.setAdapter(adapter);
                                listView.setSelection(adapter.getCount()-1);
                            }
                        });


                        Log.d("Audiocast", "received : "+medd);


                    }
                }

            };
            newThread.start();

            return null;
        }


    }

    private class sendMesg extends AsyncTask<Void , Void ,Boolean>{

        String textMsg;
        sendMesg(String message){

            textMsg = username+" : "+ message;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            byte[] data = textMsg.getBytes();

            //    Create and send a packet
            DatagramPacket packet = new DatagramPacket(data, data.length, group, portNum);

            try {
                socket.send(packet);
                return true;
            } catch (IOException e) {
                return false;
            }

        }
    }

    private class shareImage extends AsyncTask<Void, Void, Boolean> {

        Uri imageUri;
        shareImage(Uri selectedImageUri){
            imageUri = selectedImageUri;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            final String selectedImagePath = getPath(imageUri);

            String textMsg= username + " : "+selectedImagePath;
            byte[] data = textMsg.getBytes();

            //    Create and send a packet
            DatagramPacket packet = new DatagramPacket(data, data.length, group, portNum);

            try {
                socket.send(packet);
                return true;
            } catch (IOException e) {
                return false;
            }



        }
    }


    // handle intent results.
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (resultCode == RESULT_OK) {
            if (requestCode == SHARE_PICTURE) {
                Uri selectedImageUri = data.getData();

                shareImage sendImage = new shareImage(selectedImageUri);
                sendImage.execute((Void) null);

            }

        }
    }

    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String returnPath = cursor.getString(column_index);
        cursor.close();
        return returnPath;
    }
}