package com.example.supunathukorala.csharing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.supunathukorala.csharing.filebrowser.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;


/**
 * @author (C) Dushk
 */
@SuppressWarnings("ALL")
public class MessageActivity extends Activity implements View.OnClickListener {

    static final int SocketServerPORT = 8080;
    private static final int SHARE_PICTURE = 2;
    private static final int REQUEST_PATH = 1;
    final int portNum = 3238;
    InetAddress ip = null;
    NetworkInterface networkInterface = null;
    ServerSocket serverSocket;
    ServerSocketThread serverSocketThread;
    String curFileName;
    EditText edittext;
    private ArrayList<String> recQue;
    private String[] values;
    private ListView listView;
    private ArrayAdapter adapter;
    private MulticastSocket socket;
    private InetAddress group;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        username = (String) getIntent().getExtras().get("name");
        TextView userNm = (TextView) findViewById(R.id.usrName);
        userNm.setText(username);

        listView = (ListView) findViewById(R.id.listView);

        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi != null) {
            WifiManager.MulticastLock lock =
                    wifi.createMulticastLock("cSharing");
            lock.setReferenceCounted(true);
            lock.acquire();
        } else {
            Log.e("cSharing", "Unable to acquire multicast lock");
            Toast.makeText(getApplicationContext(), "Unable to acquire multicast lock", Toast.LENGTH_SHORT).show();

            finish();
        }
        recQue = new ArrayList<>();

        try {
            if (socket == null) {


                Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
                while (enumNetworkInterfaces.hasMoreElements()) {

                    networkInterface = enumNetworkInterfaces.nextElement();
                    Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();

                    while (enumInetAddress.hasMoreElements()) {
                        InetAddress inetAddress = enumInetAddress.nextElement();

                        if (inetAddress.isSiteLocalAddress()) {
                            ip = inetAddress;
                            break;
                        }
                    }
                    if (ip != null) {
                        break;
                    }
                }
                socket = new MulticastSocket(portNum);
                socket.setInterface(ip);
                socket.setBroadcast(true);

                group = InetAddress.getByName("224.0.0.1");
                socket.joinGroup(new InetSocketAddress(group, portNum), networkInterface);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        receiverMeg recvMsgThread = new receiverMeg(recQue);
        recvMsgThread.execute((Void) null);
        Button send = (Button) findViewById(R.id.buttonSend);
        send.setOnClickListener(this);
        Button shareButton = (Button) findViewById(R.id.buttonshare);
        shareButton.setOnClickListener(this);

        Button selectButton = (Button) findViewById(R.id.buttonSelect);
        selectButton.setOnClickListener(this);
        edittext = (EditText) findViewById(R.id.editText);

        //---------------------------------------------------------server
        serverSocketThread = new ServerSocketThread();
        serverSocketThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        serverSocketThread = new ServerSocketThread();
        serverSocketThread.start();

        try {
            socket = new MulticastSocket(portNum);
            socket.setInterface(ip);
            socket.setBroadcast(true);

            group = InetAddress.getByName("224.0.0.1");
            socket.joinGroup(new InetSocketAddress(group, portNum), networkInterface);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonSend:
                EditText text = (EditText) findViewById(R.id.editText2);
                String textMsg = text.getText().toString();
                text.setText("");
                sendMessage sendMessage = new sendMessage(textMsg);
                sendMessage.execute((Void) null);
                break;
            case R.id.buttonshare:
                Intent intent = new Intent(MessageActivity.this, FileSharingActivity.class);
                intent.putExtra("username", username);
                startActivityForResult(intent, SHARE_PICTURE);

                break;
            case R.id.buttonSelect:

                Intent intent1 = new Intent(this, FileChooser.class);
                startActivityForResult(intent1, REQUEST_PATH);
                break;
        }
    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {

                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();

                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // See which child activity is calling us back.
        if (requestCode == REQUEST_PATH) {
            if (resultCode == RESULT_OK) {
                curFileName = data.getStringExtra("GetFileName");
                curFileName = data.getStringExtra("GetPath");
                edittext.setText(curFileName);
            }
        }
    }

    private class receiverMeg extends AsyncTask<Void, Void, Boolean> {
        ArrayList<String> msgList;

        receiverMeg(ArrayList<String> msgList) {
            recQue = msgList;
            this.msgList = msgList;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            Thread newThread = new Thread() {

                public void run() {
                    while (true) {
                        byte[] recvPkt = new byte[1024];
                        DatagramPacket recv = new DatagramPacket(recvPkt, recvPkt.length);
                        try {
                            socket.receive(recv);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        final String medd = new String(recvPkt, 0, recv.getLength());
                        recQue.add(medd);
                        values = new String[recQue.size()];
                        for (int x = 0; x < recQue.size(); x++) {
                            values[x] = recQue.get(x);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /**
                                 *   If the network is not available
                                 * */
                                Toast.makeText(getApplicationContext(), medd + " : " + recQue.size(), Toast.LENGTH_SHORT).show();
                                adapter = new ArrayAdapter<>(MessageActivity.this, R.layout.list_white_text, R.id.list_content, values);
                                listView.setAdapter(adapter);
                                listView.setSelection(adapter.getCount() - 1);
                            }
                        });
                        Log.d("cSharing", "received : " + medd);
                    }
                }
            };
            newThread.start();
            return null;
        }
    }

    private class sendMessage extends AsyncTask<Void, Void, Boolean> {

        String textMsg;

        sendMessage(String message) {

            textMsg = username + " : " + message;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            byte[] data = textMsg.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, group, portNum);

            try {
                socket.send(packet);
                return true;
            } catch (IOException e) {
                return false;
            }

        }
    }

    public class ServerSocketThread extends Thread {

        @Override
        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SocketServerPORT);


                while (true) {
                    socket = serverSocket.accept();

                    //---------------------------------
                    ClientRxThread clientRxThread = new ClientRxThread(socket);
                    clientRxThread.start();
                    //----------------------------------------
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private class ClientRxThread extends Thread {
        Socket socket = null;


        ClientRxThread(Socket socket) {
            this.socket = socket;
        }


        @Override
        public void run() {

            File file;
            ObjectInputStream ois;
            ois = null;
            InputStream in = null;
            byte[] bytes;
            FileOutputStream fos = null;


            File theDir = new File(Environment.getExternalStorageDirectory() + "/cSharing");


            // if the directory does not exist, create it
            if (!theDir.exists()) {
                System.out.println("creating directory: " + "cSharing");
                boolean result = false;

                try {
                    theDir.mkdir();
                    result = true;
                } catch (SecurityException ignored) {
                }
                if (result) {
                    System.out.println("DIR created");
                }
            }
            int length = new File(Environment.getExternalStorageDirectory() + "/cSharing").listFiles().length;
            String fileName = "test" + (length + 1) + ".png";


            try {
                in = socket.getInputStream();
            } catch (IOException ex) {
                System.out.println("Can't get socket input stream. ");
            }
            try {
                ois = new ObjectInputStream(in);
            } catch (IOException e1) {
                System.out.println("Can't get Object Input Stream. ");
                e1.printStackTrace();
            }

            try {
                assert ois != null;
                fileName = ois.readUTF();
            } catch (IOException e) {
                System.out.println("Can't get file name. ");
                e.printStackTrace();
            }
            file = new File(Environment.getExternalStorageDirectory() + "/cSharing", fileName);
            try {
                assert ois != null;
                bytes = (byte[]) ois.readObject();
            } catch (ClassNotFoundException | IOException e) {
                System.out.println("Can't read Object . ");
                bytes = new byte[0];
                e.printStackTrace();
            }

            try {
                fos = new FileOutputStream(file);
            } catch (FileNotFoundException e1) {
                System.out.println("Can't get file output stream . ");
                e1.printStackTrace();
            }


            try {
                assert fos != null;
                fos.write(bytes);
            } catch (IOException e1) {
                System.out.println("Can't file output stream write . ");
                e1.printStackTrace();
            } finally {
                if (fos != null) {

                    try {
                        fos.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            MessageActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(MessageActivity.this, "Finished", Toast.LENGTH_SHORT).show();
                }
            });
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
