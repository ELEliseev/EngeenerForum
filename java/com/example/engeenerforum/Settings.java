package com.example.engeenerforum;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class Settings extends AppCompatActivity {
    final String SAVED_TEXT = "saved_text";
    EditText userEditText;
    SharedPreferences sPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        userEditText =  findViewById(R.id.login);
        Button button =findViewById(R.id.button);
        Button stop =findViewById(R.id.stService);
button.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        sPref = getSharedPreferences("MyPref",MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(SAVED_TEXT, userEditText.getText().toString());
        ed.commit();
        Toast.makeText(getApplicationContext(),"Сохранено",   Toast.LENGTH_SHORT).show();
        startService(new Intent(Settings.this, MyService.class));
    }
});
stop.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        stopService(new Intent(Settings.this, MyService.class));
    }
});

    }
}
