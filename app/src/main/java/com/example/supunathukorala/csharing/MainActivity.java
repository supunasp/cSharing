package com.example.supunathukorala.csharing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {

    private TextView mainText;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainText = (TextView) findViewById(R.id.mainText);
        Button chatButton = (Button) findViewById(R.id.buttonchat);
        chatButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(MainActivity.this,MessageActivity.class);
        EditText user = (EditText) findViewById(R.id.userName);
        i.putExtra("name",user.getText().toString());
        startActivity(i);
    }
}
