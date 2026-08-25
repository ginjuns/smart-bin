package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AutoActivity extends AppCompatActivity {

    private Switch switchManualMode, switchOpenLid;
    private TextView tvOpenLidLabel;

    // 네트워킹을 위한 스레드 풀
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // 기기가 어떤 와이파이에 붙어서 어떤 IP를 받든 앱은 이 고유 이름으로만 통신
    private final String esp32Host = "smarttrashcan.local";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto);

        // 상단 보라색 액션바 숨기기
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 컴포넌트 연결
        ImageButton btnBack = findViewById(R.id.btnBack);
        switchManualMode = findViewById(R.id.switchManual);
        switchOpenLid = findViewById(R.id.switchOpenLid);
        tvOpenLidLabel = findViewById(R.id.tvOpenLid);

        // 처음 시작 시 Open Lid 글자 흐리게 비활성화 시각화
        tvOpenLidLabel.setAlpha(0.4f);

        // 뒤로가기 버튼 -> 이전 화면(메뉴 화면 등)으로
        btnBack.setOnClickListener(v -> finish());

        // X 버튼 -> 첫 화면(MainActivity)으로 바로 가기
        ImageButton btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {
            // 본인 액티비티 클래스명을 앞에 적어주세요 (예: AutoActivity.this)
            Intent intent = new Intent(AutoActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        // Manual Mode 스위치 제어 (켜야만 오픈리드 조작 가능)
        switchManualMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchOpenLid.setEnabled(true);
                    tvOpenLidLabel.setAlpha(1.0f);
                    Toast.makeText(AutoActivity.this, "Manual control enabled.", Toast.LENGTH_SHORT).show();
                } else {
                    if (switchOpenLid.isChecked()) {
                        switchOpenLid.setChecked(false);
                    }
                    switchOpenLid.setEnabled(false);
                    tvOpenLidLabel.setAlpha(0.4f);
                    Toast.makeText(AutoActivity.this, "Auto mode enabled.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Open Lid 스위치 켜고 끌 때 아두이노에 명령 전송
        switchOpenLid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sendServoCommand("SERVO_OPEN");
                    LogHelper.saveLog(AutoActivity.this, "MANUAL", "Opened");
                } else {
                    sendServoCommand("SERVO_CLOSE");
                }
            }
        });
    }

    /**
     * 아두이노에 뚜껑 열고 닫는 HTTP GET 신호를 보내는 메서드 (mDNS 도메인 통신)
     */
    private void sendServoCommand(final String commandPath) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 폰이 어떤 와이파이에 있든 이 호스트 이름 주소로 자동 타게팅
                    URL url = new URL("http://" + esp32Host + "/" + commandPath);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(2000);

                    int responseCode = connection.getResponseCode();
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}