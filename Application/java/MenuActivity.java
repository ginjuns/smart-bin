package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // X 버튼 → 닫기
        ImageButton btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 와이파이 설정 → WifiSettingActivity로 이동
        TextView btnWifi = findViewById(R.id.btnWifi);
        btnWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, WifiSettingActivity.class));
            }
        });

        // 자동 개폐 → AutoActivity로 이동
        TextView btnAutoOpen = findViewById(R.id.btnAutoOpen);
        btnAutoOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, AutoActivity.class));
            }
        });

        // 사용 로그 기록 → LogActivity로 이동
        TextView btnLog = findViewById(R.id.btnLog);
        btnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, LogActivity.class));
            }
        });
    }
}