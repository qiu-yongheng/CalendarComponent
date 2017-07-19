package com.dsw.calendar.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dsw.calendar.R;
import com.dsw.calendar.component.GridMonthView;
import com.dsw.calendar.component.MonthView;
import com.dsw.calendar.component.WeekView;
import com.dsw.calendar.entity.CalendarInfo;
import com.dsw.calendar.theme.IDayTheme;
import com.dsw.calendar.theme.IWeekTheme;

import java.util.List;

/**
 * Created by Administrator on 2016/7/31.
 */
public class GridCalendarView extends LinearLayout implements View.OnClickListener {
    private WeekView weekView;
    private GridMonthView gridMonthView;
    private TextView textViewYear, textViewMonth;
    private OnDateListener listener;

    public GridCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 设置排列方式
        setOrientation(LinearLayout.VERTICAL);
        // 设置控件大小
        LayoutParams llParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        // 日历头部控件, 前进后退当前月份
        View view = LayoutInflater.from(context).inflate(R.layout.display_grid_date, null);
        // 日期
        weekView = new WeekView(context, null);
        // 日历控件
        gridMonthView = new GridMonthView(context, null);

        // 添加控件到GridCalendarView
        addView(view, llParams);
        addView(weekView, llParams);
        addView(gridMonthView, llParams);

        view.findViewById(R.id.left).setOnClickListener(this);
        view.findViewById(R.id.right).setOnClickListener(this);
        textViewYear = (TextView) view.findViewById(R.id.year);
        textViewMonth = (TextView) view.findViewById(R.id.month);

        // TODO 不显示当前年月信息, 需检查源码
        gridMonthView.setMonthLisener(new MonthView.IMonthLisener() {
            @Override
            public void setTextMonth() {
                textViewYear.setText(gridMonthView.getSelYear() + "年");
                textViewMonth.setText((gridMonthView.getSelMonth() + 1) + "月");

                listener.onChange(gridMonthView.getSelYear(), gridMonthView.getSelMonth() + 1);
            }
        });
    }

    /**
     * 设置日历点击事件
     *
     * @param dateClick
     */
    public void setDateClick(MonthView.IDateClick dateClick) {
        gridMonthView.setDateClick(dateClick);
    }

    /**
     * 设置星期的形式
     *
     * @param weekString 默认值	"日","一","二","三","四","五","六"
     */
    public void setWeekString(String[] weekString) {
        weekView.setWeekString(weekString);
    }

    /**
     * @param calendarInfos
     */
    public void setCalendarInfos(List<CalendarInfo> calendarInfos) {
        gridMonthView.setCalendarInfos(calendarInfos);
        textViewYear.setText(gridMonthView.getSelYear() + "年");
        textViewMonth.setText((gridMonthView.getSelMonth() + 1) + "月");
    }

    /**
     * 设置日主题
     *
     * @param theme
     */
    public void setDayTheme(IDayTheme theme) {
        gridMonthView.setTheme(theme);
    }

    /**
     * 设置周主题
     *
     * @param weekTheme
     */
    public void setWeekTheme(IWeekTheme weekTheme) {
        weekView.setWeekTheme(weekTheme);
    }

    public interface OnDateListener {
        void onChange(int year, int month);
    }

    /**
     * 月份改变的回调
     *
     * @param listener
     */
    public void SetOnDateChangeListener(OnDateListener listener) {
        this.listener = listener;
    }

    /**
     * 点击切换月
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.left) {
            gridMonthView.onLeftClick();
        } else {
            gridMonthView.onRightClick();
        }
    }
}