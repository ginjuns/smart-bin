package com.example.myapplication;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

public class TrashCanView extends View {

    private int fillPercent = 0; // 기본값 0%
    private Paint paintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);

    public TrashCanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeWidth(8f);
        paintBorder.setColor(Color.parseColor("#333333"));
        paintFill.setStyle(Paint.Style.FILL);
        paintFill.setPathEffect(new CornerPathEffect(15f));
    }

    // 퍼센트 설정 메서드 (외부에서 호출)
    public void setFillPercent(int percent) {
        // 혹시 모를 에러 방지 기본 예외 처리
        if (percent < 0) percent = 0;

        // 아두이노 수치를 앱의 게이지(0, 25, 50, 100)로 1:1 매칭
        if (percent == 0) {
            this.fillPercent = 0;   // 0: 0% 빈 통
        } else if (percent == 10) {
            this.fillPercent = 33;  // 10: 33%
        } else if (percent == 7) {
            this.fillPercent = 66;  // 7: 66%
        } else if (percent == 5) {
            this.fillPercent = 100; // 5: 100%
        } else {
            // 혹시 다른 숫자가 들어올 것을 대비한 안전장치 (기존 로직 활용)
            if (percent <= 50) this.fillPercent = percent;
            else if (percent < 100) this.fillPercent = percent;
            else this.fillPercent = 100;
        }

        invalidate(); // 화면 다시 그리기
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        // 휴지통 외곽선 구조 좌표 (사다리꼴)
        float topLeft     = w * 0.15f;
        float topRight    = w * 0.85f;
        float bottomLeft  = w * 0.22f;
        float bottomRight = w * 0.78f;
        float top         = h * 0.05f;
        float bottom      = h * 0.95f;

        float padding = 12f; // 테두리와 게이지 사이의 흰색 공간 간격

        // 0% 일 때는 게이지 없이 휴지통 외곽선만
        if (fillPercent > 0) {

            // 퍼센트에 따른 색상
            if (fillPercent <= 33) {
                paintFill.setColor(Color.parseColor("#B7E36A")); // 50% 이하 초록
            } else if (fillPercent < 66) {
                paintFill.setColor(Color.parseColor("#FFC73B")); // 50% 초과부터 80% 미만 주황
            } else {
                paintFill.setColor(Color.parseColor("#F44336")); // 80%부터 빨강
            }

            // 게이지가 움직일 수 있는 가동 범위
            // 바닥도 패딩만큼 위로(bottom - padding), 천장도 패딩만큼 아래로(top + padding) 제한
            float innerBottom = bottom - padding;
            float innerTop    = top + padding;
            float totalInnerHeight = innerBottom - innerTop;

            // 실제 채워질 높이 계산
            float fillHeight = totalInnerHeight * ((float) fillPercent / 100f);
            float fillTop = innerBottom - fillHeight;

            // 사다리꼴 기울기에 맞춘 좌우 폭 계산 비율 (기존 외곽선 기준 유지)
            float ratio = (fillTop - top) / (bottom - top);
            float fillLeft  = topLeft  + (bottomLeft  - topLeft)  * ratio + padding;
            float fillRight = topRight + (bottomRight - topRight) * ratio - padding;

            // 게이지 그리기
            Path fillPath = new Path();
            fillPath.moveTo(fillLeft, fillTop);
            fillPath.lineTo(fillRight, fillTop);
            fillPath.lineTo(bottomRight - padding, innerBottom);
            fillPath.lineTo(bottomLeft  + padding, innerBottom);
            fillPath.close();
            canvas.drawPath(fillPath, paintFill);
        }

        // 휴지통 외곽선 그리기 (언제나 맨 위에 선명하게 덮어씌움)
        Path borderPath = new Path();
        borderPath.moveTo(topLeft, top);
        borderPath.lineTo(topRight, top);
        borderPath.lineTo(bottomRight, bottom);
        borderPath.lineTo(bottomLeft, bottom);
        borderPath.close();
        canvas.drawPath(borderPath, paintBorder);
    }
}