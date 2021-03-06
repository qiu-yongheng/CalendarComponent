package com.dsw.calendar.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import com.dsw.calendar.entity.CalendarInfo;
import com.dsw.calendar.theme.IDayTheme;
import com.dsw.calendar.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 自定义日历控件
 *
 * 暴露以下方法给子类自定义
 *
 * drawLines(Canvas canvas,int rowsCount);绘制格网线
 * drawBG(Canvas canvas,int column,int row,int day);绘制选中背景色
 * drawDecor(Canvas canvas,int column,int row,int day);绘制事务标识符号
 * drawRest(Canvas canvas,int column,int row,int day);绘制‘班’、‘休’
 * drawText(Canvas canvas,int column,int row,int day);绘制日期
 */
public abstract class MonthView extends View {
    protected int NUM_COLUMNS = 7;
    protected int NUM_ROWS = 6;
    protected Paint paint;
    protected IDayTheme theme;
    private IMonthLisener monthLisener;
    private IDateClick dateClick;
    protected int currYear, currMonth, currDay;
    protected int selYear, selMonth, selDay;
    private int leftYear, leftMonth, leftDay;
    private int rightYear, rightMonth, rightDay;
    protected int[][] daysString;
    protected float columnSize, rowSize, baseRowSize;
    private int mTouchSlop;
    protected float density;
    private int indexMonth;
    private int width;
    protected List<CalendarInfo> calendarInfos = new ArrayList<CalendarInfo>();
    private int downX = 0, downY = 0;
    private Scroller mScroller;
    private int smoothMode;

    public MonthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        /** 像素密度 */
        density = getResources().getDisplayMetrics().density;

        mScroller = new Scroller(context);
        /** 触发移动事件的最短距离，如果小于这个距离就不触发移动控件 */
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        /** 获取当前时间 */
        Calendar calendar = Calendar.getInstance();
        currYear = calendar.get(Calendar.YEAR);
        currMonth = calendar.get(Calendar.MONTH);
        currDay = calendar.get(Calendar.DATE);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        /** 设置当前选中的时间 */
        setSelectDate(currYear, currMonth, currDay);

        /** 设置上一个月时间 */
        setLeftDate();
        /** 设置下一个月数据 */
        setRightDate();
        /** 初始化主题 */
        createTheme();

        /** 从主题中获取日期item高度 : 没有设置主题, 70dp; 有设置主题, 获取主题高度 */
        baseRowSize = rowSize = theme == null ? 70 : theme.dateHeight();
        /** 滑动模式  0是渐变滑动方式，1是没有滑动方式 */
        smoothMode = theme == null ? 0 : theme.smoothMode();
    }

    /**
     * 1. 测量
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        /** 如果宽度不是match parent, 设置宽度 */
        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = (int) (300 * density);
        }

        width = widthSize;
        NUM_ROWS = 6; //本来是想根据每月的行数，动态改变控件高度，现在为了使滑动的左右两边效果相同，不适用getMonthRowNumber();
        /** 计算高度 */
        int heightSize = (int) (NUM_ROWS * baseRowSize);
        setMeasuredDimension(widthSize, heightSize);
    }

    /**
     * 2. 绘制
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        /** 主题颜色 */
        canvas.drawColor(theme.colorMonthView());

        /** 没有滑动模式 */
        if (smoothMode == 1) {
            drawDate(canvas, selYear, selMonth, indexMonth * width, 0);
            return;
        }
        //绘制上一月份
        drawDate(canvas, leftYear, leftMonth, (indexMonth - 1) * width, 0);
        //绘制下一月份
        drawDate(canvas, rightYear, rightMonth, (indexMonth + 1) * width, 0);
        //绘制当前月份
        drawDate(canvas, selYear, selMonth, indexMonth * width, 0);
    }

    /**
     * 绘制日期
     * @param canvas
     * @param year
     * @param month
     * @param startX
     * @param startY
     */
    private void drawDate(Canvas canvas, int year, int month, int startX, int startY) {
        /** 1. 保存画布 */
        canvas.save();
        /** 2. 平移画布, 默认X = 0, Y = 0 */
        canvas.translate(startX, startY);
        /** 3. 日历有多少行(%7) */
        NUM_ROWS = getMonthRowNumber(year, month);
        /** 4. 计算每列宽度 /7 */
        columnSize = getWidth() * 1.0F / NUM_COLUMNS;
        /** 5. 计算每列高度 /6 */
        rowSize = getHeight() * 1.0F / NUM_ROWS;
        /** 6.  */
        daysString = new int[6][7];
        /** 7. 获取指定月有多少天 */
        int mMonthDays = DateUtils.getMonthDays(year, month);
        /** 8. 获取指定月1号周几 */
        int weekNumber = DateUtils.getFirstDayWeek(year, month);
        int column, row;
        /** 9. 画线(子类实现) */
        drawLines(canvas, NUM_ROWS);
        /** 10.  */
        for (int day = 0; day < mMonthDays; day++) {
            /* 计算是第几列 */
            column = (day + weekNumber - 1) % 7;
            /* 计算是第几行 */
            row = (day + weekNumber - 1) / 7;
            /* 从一号开始, 保存到二维数组 */
            daysString[row][column] = day + 1;

            drawBG(canvas, column, row, daysString[row][column]);
            drawDecor(canvas, column, row, year, month, daysString[row][column]);
            drawRest(canvas, column, row, year, month, daysString[row][column]);
            drawText(canvas, column, row, year, month, daysString[row][column]);
        }
        /** 11. 还原画布 */
        canvas.restore();
    }

    /**
     * 回执格网线
     *
     * @param canvas
     */
    protected abstract void drawLines(Canvas canvas, int rowsCount);

    protected abstract void drawBG(Canvas canvas, int column, int row, int day);

    protected abstract void drawDecor(Canvas canvas, int column, int row, int year, int month, int day);

    protected abstract void drawRest(Canvas canvas, int column, int row, int year, int month, int day);

    protected abstract void drawText(Canvas canvas, int column, int row, int year, int month, int day);

    /**
     * 实例化Theme
     */
    protected abstract void createTheme();

    private int lastMoveX;

    /**
     * 滑动监听
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int eventCode = event.getAction();
        switch (eventCode) {
            case MotionEvent.ACTION_DOWN:
                /** 按下, 记录开始的x, y */
                downX = (int) event.getX();
                downY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                /** 移动 */
                if (smoothMode == 1)
                    /* 没有滑动特效 */
                    break;

                int dx = (int) (downX - event.getX());

                /* 判断是否可用平移 */
                if (Math.abs(dx) > mTouchSlop) {

                    int moveX = dx + lastMoveX;
                    /* 移动控件 */
                    smoothScrollTo(moveX, 0);
                }
                break;
            case MotionEvent.ACTION_UP:
                /** 抬起, 判断是左滑还是右滑, 重新设置当前月前后3个月数据, 调用invalidate重绘 */
                int upX = (int) event.getX();
                int upY = (int) event.getY();
                if (upX - downX > 0 && Math.abs(upX - downX) > mTouchSlop * 10) {//左滑
                    if (smoothMode == 0) {
                        setLeftDate();
                        indexMonth--;
                    } else {
                        /* 没有移动效果, 跟点击一样 */
                        onLeftClick();
                    }
                } else if (upX - downX < 0 && Math.abs(upX - downX) > mTouchSlop * 10) {//右滑
                    if (smoothMode == 0) {
                        setRightDate();
                        indexMonth++;
                    } else {
                        onRightClick();
                    }
                } else if (Math.abs(upX - downX) < 10 && Math.abs(upY - downY) < 10) {//点击事件
                    /** 在滑动事件中处理点击事件 */
                    performClick();
                    /* 计算点击中心点坐标, 判断是在哪一列, 哪一行 */
                    doClickAction((upX + downX) / 2, (upY + downY) / 2);
                }

                if (smoothMode == 0) {
                    // 记录控件移动后的坐标
                    lastMoveX = indexMonth * width;
                    smoothScrollTo(width * indexMonth, 0);
                }
                break;
        }
        return true;
    }

    //调用此方法滚动到目标位置
    public void smoothScrollTo(int fx, int fy) {
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        smoothScrollBy(dx, dy);
    }

    //调用此方法设置滚动的相对偏移
    public void smoothScrollBy(int dx, int dy) {
        //设置mScroller的滚动偏移量
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy, 500);
        invalidate();//这里必须调用invalidate()才能保证computeScroll()会被调用，否则不一定会刷新界面，看不到滚动效果
    }

    /**
     *
     */
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    /**
     * 设置选中的月份
     *
     * @param year
     * @param month
     */
    protected void setSelectDate(int year, int month, int day) {
        selYear = year;
        selMonth = month;
        selDay = day;
    }

    /**
     * 计算需要多少行来显示日期
     * @param year
     * @param month
     * @return
     */
    protected int getMonthRowNumber(int year, int month) {
        /** 通过年月, 计算当月有多少天 */
        int monthDays = DateUtils.getMonthDays(year, month);
        /** 通过年月, 计算当月第一天是星期几 */
        int weekNumber = DateUtils.getFirstDayWeek(year, month);
        /** 计算需要多少行来显示日期 */
        return (monthDays + weekNumber - 1) % 7 == 0 ? (monthDays + weekNumber - 1) / 7 : (monthDays + weekNumber - 1) / 7 + 1;
    }

    /**
     * 获取外界传进来的事务数据
     * @param calendarInfos
     */
    public void setCalendarInfos(List<CalendarInfo> calendarInfos) {
        this.calendarInfos = calendarInfos;
        invalidate();
    }

    /**
     * 判断是否为事务天数,通过获取desc来辨别
     *
     * @param day
     * @return
     */
    protected String iscalendarInfo(int year, int month, int day) {
        if (calendarInfos == null || calendarInfos.size() == 0)
            return "";
        for (CalendarInfo calendarInfo : calendarInfos) {
            if (calendarInfo.day == day && calendarInfo.month == month + 1 && calendarInfo.year == year) {
                // 返回选择日期的事务
                return calendarInfo.des;
            }
        }
        return "";
    }

    /**
     * 执行点击事件
     * 在滑动事件中初始化
     * @param x
     * @param y
     */
    private void doClickAction(int x, int y) {
        /** 计算行列 */
        int row = (int) (y / rowSize);
        int column = (int) (x / columnSize);
        /** 设置选中的日期 */
        setSelectDate(selYear, selMonth, daysString[row][column]);
        /** 重绘 */
        invalidate();
        //执行activity发送过来的点击处理事件
        if (dateClick != null) {
            /** 给子类回调处理 */
            dateClick.onClickOnDate(selYear, selMonth + 1, selDay);
        }
    }

    /**
     * 左点击，日历向后翻页
     */
    public void onLeftClick() {
        setLeftDate();
        invalidate();

        if (monthLisener != null) {
            monthLisener.setTextMonth();
        }
    }

    /**
     * 右点击，日历向前翻页
     */
    public void onRightClick() {
        setRightDate();
        invalidate();

        if (monthLisener != null) {
            monthLisener.setTextMonth();
        }
    }

    /**
     * 设置当前显示月份上一个月的数据, 设置默认选中日期
     */
    private void setLeftDate() {
        int year = selYear;
        int month = selMonth;
        int day = selDay;
        if (month == 0) {//如果是1月份，则变成12月份
            year = selYear - 1;
            month = 11;
        } else if (DateUtils.getMonthDays(year, month - 1) < day) {//向左滑动，当前月天数小于左边的
            //如果当前日期为该月最后一点，当向前推的时候，就需要改变选中的日期
            /** 如果当前选中的日期, 上一个月没有, 例如3月31号, 2月没有31号, 这时在月份减一后, 获取2月最后一天默认选中 */
            month = month - 1;
            day = DateUtils.getMonthDays(year, month);
        } else {
            /** 正常情况下, 直接月份减一就行了 */
            month = month - 1;
        }
        setSelectDate(year, month, day);
        computeDate();
    }

    /**
     * 设置当前显示月份下一个月的数据
     */
    private void setRightDate() {
        int year = selYear;
        int month = selMonth;
        int day = selDay;
        if (month == 11) {//若果是12月份，则变成1月份
            year = selYear + 1;
            month = 0;
        } else if (DateUtils.getMonthDays(year, month + 1) < day) {//向右滑动，当前月天数小于左边的
            //如果当前日期为该月最后一点，当向前推的时候，就需要改变选中的日期
            month = month + 1;
            day = DateUtils.getMonthDays(year, month);
        } else {
            month = month + 1;
        }
        setSelectDate(year, month, day);
        computeDate();
    }

    /**
     *
     */
    private void computeDate() {
        if (selMonth == 0) {
            /** 上一年 */
            leftYear = selYear - 1;
            leftMonth = 11;
            rightYear = selYear;
            rightMonth = selMonth + 1;
        } else if (selMonth == 11) {
            /** 下一年 */
            leftYear = selYear;
            leftMonth = selMonth - 1;
            rightYear = selYear + 1;
            rightMonth = 0;
        } else {
            /** 当年 */
            leftYear = selYear;
            leftMonth = selMonth - 1;
            rightYear = selYear;
            rightMonth = selMonth + 1;
        }
        /**  */
        if (monthLisener != null) {
            monthLisener.setTextMonth();
        }
    }

    /**
     * 日期item的点击事件
     */
    public void setDateClick(IDateClick dateClick) {
        this.dateClick = dateClick;
    }

    public interface IDateClick {
        void onClickOnDate(int year, int month, int day);
    }

    public interface IMonthLisener {
        void setTextMonth();
    }

    /**
     * 设置时间改变监听
     * @param monthLisener
     */
    public void setMonthLisener(IMonthLisener monthLisener) {
        this.monthLisener = monthLisener;
    }

    /**
     * 设置样式
     *
     * @param theme
     */
    public void setTheme(IDayTheme theme) {
        this.theme = theme;
        invalidate();
    }

    public int getSelYear() {
        return selYear;
    }

    public int getSelMonth() {
        return selMonth;
    }
}
