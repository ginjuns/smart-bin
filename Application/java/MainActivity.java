package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject; // JSON 임포트가 정상 위치에 포함

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    TrashCanView trashCanView;
    TextView tvPercent;

    // 네트워킹을 위한 백그라운드 스레드 및 핸들러
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final String esp32Host = "smarttrashcan.local";
    private boolean isPolling = true;

    // 직전 뚜껑 상태를 기억할 변수 (처음엔 닫혀있다고 가정하므로 false)
    private boolean lastLidState = false;

    // 휴지통 크기에 맞게 숫자 조절 가능
    private final int EMPTY_DISTANCE = 13; // 완전히 비었을 때
    private final int FULL_DISTANCE = 3;   // 완전히 찼을 때

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        trashCanView = findViewById(R.id.trashCanView);
        tvPercent = findViewById(R.id.tvPercent);

        // 첫 시작 시 기본값 설정
        setTrashLevel(0);

        // 메뉴 버튼 설정
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MenuActivity.class));
            }
        });

        // 3초마다 아두이노에게 거리 데이터 가져오는 루프 시작
        startTrashDataPolling();
        //setTrashLevel(75);
    }

    /**
     * 3초마다 지속적으로 ESP32에게 데이터를 요청하는 타이머 루프
     */
    private void startTrashDataPolling() {
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isPolling) {
                    getTrashLevelFromESP32();
                    mainHandler.postDelayed(this, 3000); // 3초 주기
                }
            }
        }, 1000);
    }

    /**
     * ESP32 서버에 HTTP GET 요청을 보내고 거리 데이터를 받아오는 메서드
     */
    private void getTrashLevelFromESP32() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://" + esp32Host + "/data");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(2000);
                    connection.setReadTimeout(2000);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            response.append(line);
                        }
                        br.close();

                        // JSON 문자열 파싱
                        JSONObject jsonObject = new JSONObject(response.toString());

                        // 서버가 보낸 거리 값 읽기
                        int distance = jsonObject.getInt("distance");

                        // 서버가 보낸 뚜껑 상태 읽기
                        int coverInt = jsonObject.getInt("cover");
                        boolean cover = (coverInt == 1);
                        int openReason  = jsonObject.getInt("openReason");

                        if (cover && !lastLidState) {
                            if (openReason == 2) {
                                LogHelper.saveLog(MainActivity.this, "BUTTON", "Opened");
                            } else if (openReason == 1) {
                                LogHelper.saveLog(MainActivity.this, "MANUAL", "Opened");
                            } else {
                                LogHelper.saveLog(MainActivity.this, "AUTO", "Opened");
                            }
                        }
                        lastLidState = cover; // 상태 업데이트

                        // 거리로 퍼센트 환산 공식
                        int percent;
                        if (distance == 0 || distance == 10 || distance == 7 || distance == 5) {
                            percent = distance;
                        } else {
                            if (distance <= 0 || distance >= EMPTY_DISTANCE) {
                                // 거리가 0이거나(예외값), EMPTY_DISTANCE(13cm)보다 멀면 0%로 처리
                                percent = 0;
                            } else if (distance <= FULL_DISTANCE) {
                                percent = 100;
                            } else {
                                double ratio = (double) (EMPTY_DISTANCE - distance) / (EMPTY_DISTANCE - FULL_DISTANCE);
                                percent = (int) (ratio * 100);
                            }
                        }

                        // UI 업데이트
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setTrashLevel(percent);
                            }
                        });
                    }
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setTrashLevel(int percent) {
        trashCanView.setFillPercent(percent);
        tvPercent.setText(percent + "%");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isPolling = false; // 앱 종료 시 타이머 루프 안전하게 정지
    }
}