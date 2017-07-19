package com.dsw.calendar.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.dsw.calendar.entity.CalendarInfo;
import com.dsw.calendar.theme.DefaultDayTheme;

/**
 * 继承MonthView, 自定义日历控件
 * <p>
 * drawLines(Canvas canvas,int rowsCount);绘制格网线
 * drawBG(Canvas canvas,int column,int row,int day);绘制选中背景色
 * drawDecor(Canvas canvas,int column,int row,int day);绘制事务标识符号
 * drawRest(Canvas canvas,int column,int row,int day);绘制‘班’、‘休’
 * drawText(Canvas canvas,int column,int row,int day);绘制日期
 */
public class GridMonthView extends MonthView {

    public GridMonthView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 绘制网格线
     *
     * @param canvas 画布
     * @param rowsCount 行数
     */
    @Override
    protected void drawLines(Canvas canvas, int rowsCount) {
        /** 1. 获取长宽(坐标) */
        int rightX = getWidth();
        int BottomY = getHeight();
        int rowCount = rowsCount;
        int columnCount = 7;

        /** 2. 初始化画笔 */
        Path path;
        float startX = 0;
        float endX = rightX;
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(theme.colorLine());

        /** 3. 一行行绘制 */
        for (int row = 1; row <= rowCount; row++) {
            float startY = row * rowSize;
            path = new Path();
            path.moveTo(startX, startY);
            path.lineTo(endX, startY);
            canvas.drawPath(path, paint);
        }

        /** 4. 一列列绘制 */
        float startY = 0;
        float endY = BottomY;
        for (int column = 1; column < columnCount; column++) {
            startX = column * columnSize;
            path = new Path();
            path.moveTo(startX, startY);
            path.lineTo(startX, endY);
            canvas.drawPath(path, paint);
        }
    }

    /**
     * 绘制选中背景色
     *
     * @param canvas 画布
     * @param column 列
     * @param row 行
     * @param day 选中天
     */
    @Override
    protected void drawBG(Canvas canvas, int column, int row, int day) {
        if (day == selDay) {
            //绘制背景色矩形
            float startRecX = columnSize * column + 1;
            float startRecY = rowSize * row + 1;
            float endRecX = startRecX + columnSize - 2 * 1;
            float endRecY = startRecY + rowSize - 2 * 1;
            paint.setColor(theme.colorSelectBG());
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(startRecX, startRecY, endRecX, endRecY, paint);
        }
    }

    /**
     * 绘制事务标识符号
     *
     * @param canvas 画布
     * @param column 列
     * @param row 行
     * @param year 年
     * @param month 月
     * @param day 日
     */
    @Override
    protected void drawDecor(Canvas canvas, int column, int row, int year, int month, int day) {
        if (calendarInfos != null && calendarInfos.size() > 0) {
            if (TextUtils.isEmpty(iscalendarInfo(year, month, day))) {
                // 没有事务, 不绘制
                return;
            }
            // 有事务, 在右上角绘制原点
            paint.setColor(theme.colorDecor());
            paint.setStyle(Paint.Style.FILL);
            float circleX = (float) (columnSize * column + columnSize * 0.8);
            float circleY = (float) (rowSize * row + rowSize * 0.2);
            canvas.drawCircle(circleX, circleY, theme.sizeDecor(), paint);
        }
    }

    /**
     * 绘制‘班’、‘休’
     * @param canvas 画布
     * @param column 列
     * @param row 行
     * @param year 年
     * @param month 月
     * @param day 日
     */
    @Override
    protected void drawRest(Canvas canvas, int column, int row, int year, int month, int day) {
        if (calendarInfos != null && calendarInfos.size() > 0) {
            for (CalendarInfo calendarInfo : calendarInfos) {
                /** 判断是否设置了事务数据 */
                if (calendarInfo.day == day && calendarInfo.year == year && calendarInfo.month == month + 1) {
                    // 1. 左上角 X坐标, Y坐标 (绘制在框内, 所以X+1, Y+1)
                    float pointX0 = columnSize * column + 1;
                    float pointY0 = rowSize * row + 1;
                    // 2. 三角形右边坐标 (X轴向右移动高度的一半)
                    float pointX1 = (float) (columnSize * column + rowSize * 0.5);
                    float pointY1 = rowSize * row + 1;
                    // 3. 三角形下面坐标 (Y轴向下移动高度一半)
                    float pointX2 = columnSize * column + 1;
                    float pointY2 = (float) (rowSize * row + rowSize * 0.5);

                    // 4. 画两边
                    Path path = new Path();
                    path.moveTo(pointX0, pointY0);
                    path.lineTo(pointX1, pointY1);
                    path.lineTo(pointX2, pointY2);
                    path.close();

                    paint.setStyle(Paint.Style.FILL);
                    if (calendarInfo.rest == 2) {//班
                        paint.setColor(theme.colorWork());
                        canvas.drawPath(path, paint);

                        paint.setTextSize(theme.sizeDesc());
                        paint.setColor(theme.colorSelectDay());
                        paint.measureText("班");
                        canvas.drawText("班", pointX0 + 5, pointY0 + paint.measureText("班"), paint);
                    } else if (calendarInfo.rest == 1) {//休息
                        paint.setColor(theme.colorRest());
                        canvas.drawPath(path, paint);
                        paint.setTextSize(theme.sizeDesc());
                        paint.setColor(theme.colorSelectDay());
                        canvas.drawText("休", pointX0 + 5, pointY0 + paint.measureText("休"), paint);
                    }
                }
            }
        }
    }

    /**
     * 绘制日期
     * @param canvas 画布
     * @param column 列
     * @param row 行
     * @param year 年
     * @param month 月
     * @param day 日
     */
    @Override
    protected void drawText(Canvas canvas, int column, int row, int year, int month, int day) {
        // 1. 字体大小
        paint.setTextSize(theme.sizeDay());
        // 2. 计算字体绘制开始坐标(中间)
        float startX = columnSize * column + (columnSize - paint.measureText(day + "")) / 2;
        float startY = rowSize * row + rowSize / 2 - (paint.ascent() + paint.descent()) / 2;
        paint.setStyle(Paint.Style.STROKE);
        // 3. 判断是否是事务数据
        String des = iscalendarInfo(year, month, day);
        if (day == selDay) {//日期为选中的日期
            if (!TextUtils.isEmpty(des)) {//desc不为空的时候
                /** 如果有事务, 字体向上移10dp */
                int dateY = (int) (startY - 10);
                paint.setColor(theme.colorSelectDay());
                canvas.drawText(day + "", startX, dateY, paint);

                /** 绘制事务 */
                paint.setTextSize(theme.sizeDesc());
                int priceX = (int) (columnSize * column + (columnSize - paint.measureText(des)) / 2);
                int priceY = (int) (startY + 15);
                canvas.drawText(des, priceX, priceY, paint);
            } else {//des为空的时候
                paint.setColor(theme.colorSelectDay());
                canvas.drawText(day + "", startX, startY, paint);
            }
        } else if (day == currDay && currDay != selDay && currMonth == selMonth) {//今日的颜色，不是选中的时候
            //正常月，选中其他日期，则今日为红色
            paint.setColor(theme.colorToday());
            canvas.drawText(day + "", startX, startY, paint);
        } else {
            if (!TextUtils.isEmpty(des)) {//没选中，但是desc不为空
                int dateY = (int) (startY - 10);
                paint.setColor(theme.colorWeekday());
                canvas.drawText(day + "", startX, dateY, paint);

                paint.setTextSize(theme.sizeDesc());
                paint.setColor(theme.colorDesc());
                int priceX = (int) (columnSize * column + Math.abs((columnSize - paint.measureText(des)) / 2));
                int priceY = (int) (startY + 15);
                canvas.drawText(des, priceX, priceY, paint);
            } else {//des为空
                paint.setColor(theme.colorWeekday());
                canvas.drawText(day + "", startX, startY, paint);
            }
        }
    }

    @Override
    protected void createTheme() {
        theme = new DefaultDayTheme();
    }
}
