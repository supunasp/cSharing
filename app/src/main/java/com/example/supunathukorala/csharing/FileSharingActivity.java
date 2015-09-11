package com.example.supunathukorala.csharing;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
    private static final int SEND_PICTURE = 2;
    public static final String TAG = "wifidirectdemo";
    private String username;

    Uri newImageUri;

    private WifiApiManager wifiApManager;
    private String[] values;
    private ListView listView;
    private ArrayAdapter<String> adapter;

    //-------------- Client

    Button buttonConnect;
    TextView textPort;

    static final int SocketServerPORT = 8080;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_sharing);

        wifiApManager = new WifiApiManager(this);
        listView = (ListView) findViewById(R.id.listView2);

        username = (String) getIntent().getExtras().get("username");
        Intent i = new Intent(Intent.ACTION_PICK, Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);

        textPort = (TextView) findViewById(R.id.port);
        textPort.setText("port: " + SocketServerPORT);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                selectImage sendImage = new selectImage(selectedImageUri);
                sendImage.execute((Void) null);
            }
        }
    }

    private class selectImage extends AsyncTask<Void, Void, Boolean> {

        Uri imageUri;

        selectImage(Uri selectedImageUri) {
            imageUri = selectedImageUri;
            newImageUri=selectedImageUri;

        }

        @Override
        protected Boolean doInBackground(Void... params) {

            final String selectedImagePath = getPath(imageUri);
            final ArrayList<ClientScanResult> clients = wifiApManager.getClientList(false);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    ClientScanResult clientScanResult;
                    values = new String[clients.size()];
                    for (int i = 0; i < clients.size(); i++) {
                        clientScanResult = clients.get(i);
                        values[i] = "IpAddress: " + clientScanResult.getIpAddr();
                    }
                    adapter = new ArrayAdapter<>(FileSharingActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Toast.makeText(getApplicationContext(), clients.get(position).getIpAddr(), Toast.LENGTH_SHORT).show();

                            sendMessage sendImage = new sendMessage(imageUri, clients.get(position).getIpAddr());
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

            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(
                    selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();

            ClientRxThread clientRxThread =
                    new ClientRxThread(
                            ipAddress,
                            SocketServerPORT);
            clientRxThread.start();


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

    private class ClientRxThread extends Thread {
        String dstAddress;
        int dstPort;

        ClientRxThread(String address, int port) {
            dstAddress = address;
            dstPort = port;
        }

        @Override
        public void run() {
            Socket socket = null;

            try {
                socket = new Socket(dstAddress, dstPort);

                FileTxThread fileTxThread = new FileTxThread(socket);
                fileTxThread.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class FileTxThread extends Thread {
        Socket socket;

        FileTxThread(Socket socket){
            this.socket= socket;
        }

        @Override
        public void run() {
            File file = new File(getPath(newImageUri));

            byte[] bytes = new byte[(int) file.length()];
            BufferedInputStream bis;
            try {
                bis = new BufferedInputStream(new FileInputStream(file));
                final int read = bis.read(bytes, 0, bytes.length);

                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(bytes);
                oos.flush();

                socket.close();

                final String sentMsg = "File sent to: " + socket.getInetAddress();
                FileSharingActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(FileSharingActivity.this,
                                sentMsg,
                                Toast.LENGTH_LONG).show();
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