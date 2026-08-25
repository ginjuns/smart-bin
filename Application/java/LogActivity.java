package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        // 뒤로가기 → 메뉴 화면
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // X → 첫 화면(메인)으로
        ImageButton btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {
            Intent intent = new Intent(LogActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        List<String> logs = LogHelper.loadLogs(this);
        if (logs.isEmpty()) {
            logs.add("No history available");
        }

        // RecyclerView 설정
        RecyclerView recyclerLog = findViewById(R.id.recyclerLog);
        recyclerLog.setLayoutManager(new LinearLayoutManager(this));
        recyclerLog.setAdapter(new LogAdapter(logs));
    }

    // 로그 어댑터
    static class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {
        private List<String> items;

        LogAdapter(List<String> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.text.setText(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView text;
            ViewHolder(View view) {
                super(view);
                text = view.findViewById(android.R.id.text1);
            }
        }
    }
}