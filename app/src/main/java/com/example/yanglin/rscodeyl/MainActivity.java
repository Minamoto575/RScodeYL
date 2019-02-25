
package com.example.yanglin.rscodeyl;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by yanglin on 2019/2/23.
 */

public class MainActivity extends AppCompatActivity {
    Button lv,cv;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv=(Button)findViewById(R.id.lv);
        cv=(Button)findViewById(R.id.cv);
        lv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,receiveLV.class));
            }
        });
        cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,sendCV.class));
            }
        });
    }
}
