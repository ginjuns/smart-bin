package com.example.myapplication;

import android.content.Context;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class LogHelper {

    private static final String FILE_NAME = "trash_log.txt";
    private static final int KEEP_DAYS = 7; // 보관 기간
    private static final String MANUAL_TAG = "[MANUAL] "; // 9글자
    private static final String BUTTON_TAG = "[BUTTON] "; // 9글자

    // 로그 저장
    public static void saveLog(Context context, String mode, String action) {
        try {
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date());
            String line;
            if (mode.equals("MANUAL")) {
                line = MANUAL_TAG + date + "  " + action;
            } else if (mode.equals("BUTTON")) {

                line = BUTTON_TAG + date + "  " + action;

            } else {
                line = date + "  " + action;
            }

            try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_APPEND)) {
                fos.write((line + "\n").getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 로그 불러오기
    public static List<String> loadLogs(Context context) {
        List<String> logs = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(context.openFileInput(FILE_NAME), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                logs.add(line);
            }
        } catch (IOException e) {
            // 파일이 없을 때는 에러를 뿜지 않고 빈 리스트를 안전하게 반환
            return logs;
        }

        Collections.reverse(logs); // 최신 순으로 뒤집기
        return logs;
    }

    // 7일 지난 로그 자동 삭제
    public static void deleteOldLogs(Context context) {
        List<String> allLogs = new ArrayList<>();

        // 기존 로그 전부 안전하게 읽기
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(context.openFileInput(FILE_NAME), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                allLogs.add(line);
            }
        } catch (IOException e) {
            return; // 파일이 없으면 청소할 것도 없으니 바로 종료
        }

        // 7일 기준 날짜 계산
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -KEEP_DAYS);
        Date cutoff = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        // 7일 이내 로그만 필터링
        List<String> filtered = new ArrayList<>();
        for (String log : allLogs) {
            try {
                String datePart = "";

                // 1. MANUAL 태그나 BUTTON 태그가 있고, 전체 글자가 28자 이상일 때
                if ((log.contains(MANUAL_TAG) || log.contains(BUTTON_TAG)) && log.length() >= 28) {
                    // 둘 다 9글자이므로 MANUAL_TAG.length()를 그대로 기준으로 삼아 잘라냅니다!
                    datePart = log.substring(MANUAL_TAG.length(), MANUAL_TAG.length() + 19);
                }
                // 2. 머리말이 없는 순수 자동 모드일 때
                else if (!log.contains(MANUAL_TAG) && !log.contains(BUTTON_TAG) && log.length() >= 19) {
                    datePart = log.substring(0, 19);
                }

                if (!datePart.isEmpty()) {
                    Date logDate = sdf.parse(datePart);
                    if (logDate != null && logDate.after(cutoff)) {
                        filtered.add(log);
                    }
                } else {
                    filtered.add(log); // 길이가 안 맞는 이상한 줄도 일단 보존
                }
            } catch (Exception e) {
                filtered.add(log); // 파싱 실패한 줄은 안전하게 보존
            }
        }

        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            for (String log : filtered) {
                fos.write((log + "\n").getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}