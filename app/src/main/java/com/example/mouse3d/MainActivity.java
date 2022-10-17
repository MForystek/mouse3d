package com.example.mouse3d;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button leftButton = (Button) findViewById(R.id.leftButton);
        Button rightButton = (Button) findViewById(R.id.rightButton);

        if (leftButton != null) {
            leftButton.setOnClickListener((View.OnClickListener) (view ->
                    Toast.makeText((Context) MainActivity.this, R.string.leftButton, Toast.LENGTH_LONG).show())
            );
        }

        if (rightButton != null) {
            rightButton.setOnClickListener((View.OnClickListener) (view ->
                    Toast.makeText((Context) MainActivity.this, R.string.rightButton, Toast.LENGTH_LONG).show())
            );
        }

    }
}