package com.example.supunathukorala.csharing;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.supunathukorala.csharing.wifimanager.ClientScanResult;
import com.example.supunathukorala.csharing.wifimanager.WifiApiManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import static android.provider.MediaStore.Images;

public class FileSharingActivity extends Activity {

    private static final int SELECT_PICTURE = 1;
    private String username;

    Uri newImageUri;

    private WifiApiManager wifiApManager;
    private String[] values;
    private ListView listView;
    private ArrayAdapter<String> adapter;

    //-------------- Client

    TextView textPort;

    static final int SocketServerPORT = 8080;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_sharing);

        wifiApManager = new WifiApiManager(this);
        listView = (ListView) findViewById(R.id.listView2);

        username = (String) getIntent().getExtras().get("name");
        Intent i = new Intent(Intent.ACTION_PICK, Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);

        textPort = (TextView) findViewById(R.id.port);
        textPort.setText("port: " + SocketServerPORT);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                selectClient sendImage = new selectClient(selectedImageUri);
                sendImage.execute((Void) null);
            }
        }
    }

    private class selectClient extends AsyncTask<Void, Void, Boolean> {

        Uri imageUri;

        selectClient(Uri selectedImageUri) {
            imageUri = selectedImageUri;
            newImageUri=selectedImageUri;

        }

        @Override
        protected Boolean doInBackground(Void... params) {

            final ArrayList<ClientScanResult> clients = wifiApManager.getClientList(false);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    ClientScanResult clientScanResult;
                    values = new String[clients.size()];
                    for (int i = 0; i < clients.size(); i++) {
                        clientScanResult = clients.get(i);
                        values[i] = "IpAddress: " + clientScanResult.getIpAddress();
                    }
                    adapter = new ArrayAdapter<>(FileSharingActivity.this, R.layout.list_white_text, R.id.list_content, values);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Toast.makeText(getApplicationContext(), clients.get(position).getIpAddress(), Toast.LENGTH_SHORT).show();

                            sendMessage sendImage = new sendMessage(imageUri, clients.get(position).getIpAddress());
                            sendImage.execute((Void) null);



                        }
                    });
                }
            });
            return true;
        }
    }

    private class sendMessage extends AsyncTask<Void, Void, Boolean> {

        Uri selectedImage;
        String ipAddress;

        sendMessage(Uri imageUri, String ipAddress) {

            selectedImage = imageUri;
            this.ipAddress = ipAddress;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            ClientSenderThread clientSenderThread = new ClientSenderThread(ipAddress, SocketServerPORT);
            clientSenderThread.start();

            Intent intent = new Intent(FileSharingActivity.this,MessageActivity.class);
            intent.putExtra("name", username);
            startActivity(intent);


            return true;
        }
    }

    private String getPath(Uri uri) {
        String[] projection = new String[]{Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(Images.Media.DATA);
        cursor.moveToFirst();
        String returnPath = cursor.getString(column_index);
        cursor.close();
        return returnPath;
    }

    private class ClientSenderThread extends Thread {
        String dstAddress;
        int dstPort;

        ClientSenderThread(String address, int port) {
            dstAddress = address;
            dstPort = port;
        }

        @Override
        public void run() {
            Socket socket;

            try {
                socket = new Socket(dstAddress, dstPort);

                FileTransferThread fileTransferThread = new FileTransferThread(socket);
                fileTransferThread.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class FileTransferThread extends Thread {
        Socket socket;

        FileTransferThread(Socket socket){
            this.socket= socket;
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
            File file = new File(getPath(newImageUri));

            String fileName = file.getName();


            byte[] bytes = new byte[(int) file.length()];
            BufferedInputStream bis;

            try {
                bis = new BufferedInputStream(new FileInputStream(file));
                 bis.read(bytes, 0, bytes.length);

                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeUTF(fileName);
                oos.writeObject(bytes);
                oos.flush();
                socket.close();

                final String sentMsg = "File sent to: " + socket.getInetAddress();
                FileSharingActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FileSharingActivity.this, sentMsg, Toast.LENGTH_LONG).show();
                    }});

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}