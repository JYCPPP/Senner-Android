package com.example.senner.UI;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import java.util.ArrayList;
import java.util.List;

public class ChartView {

    // 限制图表的显示范围为最新的 N 个数据点
    private final float MAX_VISIBLE_RANGE = 500;
    /**
     *
     * @param lineChart 需要初始化的LineChart
     * @param MaxValue 纵轴初始最大值
     * @param MinValue 纵轴初始最小值
     * @param legendEntries 图例集合
     */
    public void InitChartView(LineChart lineChart, float MaxValue, float MinValue,  List<LegendEntry> legendEntries){

        //设置表样式
        //绘制区域边框、设置边框颜色、边框宽度
        lineChart.setDrawBorders(true);
        lineChart.setBorderColor(Color.BLACK);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.setBorderWidth(2);

        //滑动触摸相关
        lineChart.setTouchEnabled(true); // 所有触摸事件,默认true
        lineChart.setDragEnabled(true);    // 可拖动,默认true
        lineChart.setScaleEnabled(true);   // 两个轴上的缩放,X,Y分别默认为true
        lineChart.setScaleXEnabled(true);  // X轴上的缩放,默认true
        lineChart.setScaleYEnabled(true);  // Y轴上的缩放,默认true
        lineChart.setPinchZoom(true);  // X,Y轴同时缩放，false则X,Y轴单独缩放,默认false
        lineChart.setDoubleTapToZoomEnabled(true); // 双击缩放,默认true
        lineChart.setDragDecelerationEnabled(true);    // 抬起手指，继续滑动,默认true
        lineChart.setDragDecelerationFrictionCoef(0.9f);   // 摩擦系数,[0-1]，较大值速度会缓慢下降，0，立即停止;1,无效值，并转换为0.9999.默认0.9f.
        lineChart.setOnChartGestureListener (new OnChartGestureListener() { // 手势监听器
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                // 按下
            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                // 抬起,取消
            }

            @Override
            public void onChartLongPressed(MotionEvent me) {
                // 长按
            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {
                // 双击
            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {
                // 单击
            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
                // 甩动
            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
                // 缩放
            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
                // 移动
            }
        });

        //轴相关
        // 由四个元素组成：
        // 标签：即刻度值。也可以自定义，比如时间，距离等等，下面会说一下；
        // 轴线：坐标轴；
        // 网格线：垂直于轴线对应每个值画的轴线；
        // 限制线：最值等线。
        XAxis xAxis = lineChart.getXAxis();    // 获取X轴
        xAxis.setAxisLineColor(Color.BLACK); // 坐标轴颜色，默认GRAY
        xAxis.setTextColor(Color.BLACK); //刻度文字颜色
        xAxis.setGridColor(Color.BLACK);   // 网格线颜色，默认GRAY
        xAxis.setTextSize(12f);
        xAxis.setGridLineWidth(1); // 网格线宽度，dp，默认1dp
        xAxis.setAxisLineWidth(2);
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.enableGridDashedLine(20, 10, 1);

        YAxis yAxis = lineChart.getAxisLeft(); // 获取Y轴,mLineChart.getAxis(YAxis.AxisDependency.LEFT);也可以获取Y轴
        yAxis.setTextColor(Color.BLACK);  // 标签字体颜色
        yAxis.setTextSize(12f);    // 标签字体大小，dp，6-24之间，默认为10dp
        yAxis.setGridColor(Color.BLACK);    // 网格线颜色，默认GRAY
        yAxis.setGridLineWidth(1f);    // 网格线宽度，dp，默认1dp
        yAxis.setAxisLineColor(Color.BLACK);  // 坐标轴颜色，默认GRAY.测试到一个bug，假如左侧线只有1dp，
        yAxis.setAxisLineWidth(2f);  // 坐标轴线宽度，dp，默认1dp
        yAxis.setAxisMaximum(MaxValue);
        yAxis.setAxisMinimum(MinValue);
        yAxis.enableGridDashedLine(20, 10, 1);    // 网格线为虚线，lineLength，每段实线长度,spaceLength,虚线间隔长度，phase，起始点（进过测试，最后这个参数也没看出来干啥的）

        //图例相关
        Legend legend = lineChart.getLegend(); // 获取图例，但是在数据设置给chart之前是不可获取的
        legend.setEnabled(true);    // 是否绘制图例
        legend.setTextColor(Color.BLACK);    // 图例标签字体颜色，默认BLACK
        legend.setTextSize(10); // 图例标签字体大小[6,24]dp,默认10dp
        legend.setTypeface(null);   // 图例标签字体
        legend.setWordWrapEnabled(false);    // 当图例超出时是否换行适配，这个配置会降低性能，且只有图例在底部时才可以适配。默认false
        legend.setMaxSizePercent(1f); // 设置，默认0.95f,图例最大尺寸区域占图表区域之外的比例
        legend.setForm(Legend.LegendForm.SQUARE);   // 设置图例的形状，SQUARE, CIRCLE 或者 LINE
        legend.setXEntrySpace(6);  // 设置水平图例间间距，默认6dp
        legend.setYEntrySpace(0);  // 设置垂直图例间间距，默认0
        legend.setDrawInside(true);
        legend.setFormToTextSpace(5);    // 设置图例的标签与图形之间的距离，默认5dp
        legend.setCustom(legendEntries);

        lineChart.invalidate();


    }

    public void InitWhiteChartView(LineChart lineChart, float MaxValue, float MinValue,  List<LegendEntry> legendEntries){

        //设置表样式
        //绘制区域边框、设置边框颜色、边框宽度
        lineChart.setDrawBorders(true);
        lineChart.setBorderColor(Color.WHITE);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.setBorderWidth(2);

        //滑动触摸相关
        lineChart.setTouchEnabled(true); // 所有触摸事件,默认true
        lineChart.setDragEnabled(true);    // 可拖动,默认true
        lineChart.setScaleEnabled(true);   // 两个轴上的缩放,X,Y分别默认为true
        lineChart.setScaleXEnabled(true);  // X轴上的缩放,默认true
        lineChart.setScaleYEnabled(true);  // Y轴上的缩放,默认true
        lineChart.setPinchZoom(true);  // X,Y轴同时缩放，false则X,Y轴单独缩放,默认false
        lineChart.setDoubleTapToZoomEnabled(true); // 双击缩放,默认true
        lineChart.setDragDecelerationEnabled(true);    // 抬起手指，继续滑动,默认true
        lineChart.setDragDecelerationFrictionCoef(0.9f);   // 摩擦系数,[0-1]，较大值速度会缓慢下降，0，立即停止;1,无效值，并转换为0.9999.默认0.9f.
        lineChart.setOnChartGestureListener (new OnChartGestureListener() { // 手势监听器
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                // 按下
            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                // 抬起,取消
            }

            @Override
            public void onChartLongPressed(MotionEvent me) {
                // 长按
            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {
                // 双击
            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {
                // 单击
            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
                // 甩动
            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
                // 缩放
            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
                // 移动
            }
        });

        //轴相关
        // 由四个元素组成：
        // 标签：即刻度值。也可以自定义，比如时间，距离等等，下面会说一下；
        // 轴线：坐标轴；
        // 网格线：垂直于轴线对应每个值画的轴线；
        // 限制线：最值等线。
        XAxis xAxis = lineChart.getXAxis();    // 获取X轴
        xAxis.setAxisLineColor(Color.WHITE); // 坐标轴颜色，默认GRAY
        xAxis.setTextColor(Color.WHITE); //刻度文字颜色
        xAxis.setGridColor(Color.WHITE);   // 网格线颜色，默认GRAY
        xAxis.setTextSize(12f);
        xAxis.setGridLineWidth(1); // 网格线宽度，dp，默认1dp
        xAxis.setAxisLineWidth(2);
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.enableGridDashedLine(20, 10, 1);

        YAxis yAxis = lineChart.getAxisLeft(); // 获取Y轴,mLineChart.getAxis(YAxis.AxisDependency.LEFT);也可以获取Y轴
        yAxis.setTextColor(Color.WHITE);  // 标签字体颜色
        yAxis.setTextSize(12f);    // 标签字体大小，dp，6-24之间，默认为10dp
        yAxis.setGridColor(Color.WHITE);    // 网格线颜色，默认GRAY
        yAxis.setGridLineWidth(1f);    // 网格线宽度，dp，默认1dp
        yAxis.setAxisLineColor(Color.WHITE);  // 坐标轴颜色，默认GRAY.测试到一个bug，假如左侧线只有1dp，
        yAxis.setAxisLineWidth(2f);  // 坐标轴线宽度，dp，默认1dp
        yAxis.setAxisMaximum(MaxValue);
        yAxis.setAxisMinimum(MinValue);
        yAxis.enableGridDashedLine(20, 10, 1);    // 网格线为虚线，lineLength，每段实线长度,spaceLength,虚线间隔长度，phase，起始点（进过测试，最后这个参数也没看出来干啥的）

        //图例相关
        Legend legend = lineChart.getLegend(); // 获取图例，但是在数据设置给chart之前是不可获取的
        legend.setEnabled(true);    // 是否绘制图例
        legend.setTextColor(Color.WHITE);    // 图例标签字体颜色，默认BLACK
        legend.setTextSize(10); // 图例标签字体大小[6,24]dp,默认10dp
        legend.setTypeface(null);   // 图例标签字体
        legend.setWordWrapEnabled(false);    // 当图例超出时是否换行适配，这个配置会降低性能，且只有图例在底部时才可以适配。默认false
        legend.setMaxSizePercent(1f); // 设置，默认0.95f,图例最大尺寸区域占图表区域之外的比例
        legend.setForm(Legend.LegendForm.SQUARE);   // 设置图例的形状，SQUARE, CIRCLE 或者 LINE
        legend.setXEntrySpace(6);  // 设置水平图例间间距，默认6dp
        legend.setYEntrySpace(0);  // 设置垂直图例间间距，默认0
        legend.setDrawInside(true);
        legend.setFormToTextSpace(5);    // 设置图例的标签与图形之间的距离，默认5dp
        legend.setCustom(legendEntries);

        lineChart.invalidate();


    }
    public void SetThreshold(LineChart lineChart, float threshold){

        YAxis yAxis = lineChart.getAxisLeft();
        //阈值线
        LimitLine limitLine = new LimitLine(threshold, "Threshold");
        limitLine.setLineColor(Color.RED);
        limitLine.setLineWidth(2f);
        limitLine.setTextSize(10f);
        limitLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        yAxis.removeAllLimitLines();
        yAxis.addLimitLine(limitLine);
        lineChart.invalidate();
    }

    public void SetThreshold(LineChart lineChart, float threshold1, float threshold2){

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.removeAllLimitLines();
        //阈值线
        LimitLine limitLine1 = new LimitLine(threshold1, "Threshold");
        limitLine1.setLineColor(Color.RED);
        limitLine1.setLineWidth(2f);
        limitLine1.setTextSize(10f);
        limitLine1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        yAxis.addLimitLine(limitLine1);
        //阈值线
        LimitLine limitLine2 = new LimitLine(threshold2, "Threshold");
        limitLine2.setLineColor(Color.RED);
        limitLine2.setLineWidth(2f);
        limitLine2.setTextSize(10f);
        limitLine2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        yAxis.addLimitLine(limitLine2);
        lineChart.invalidate();
    }

    /**
     *
     * @param chart 所需传数据折线图
     * @param values0 储存第一组数据的列表
     * @param values1 储存第二组数据的列表
     * @param values2 储存第三组数据的列表
     * @param value0 第一组数据
     * @param value1 第二组数据
     * @param value2 第三组数据
     * @param string0 第一组数据名称
     * @param string1 第二组数据名称
     * @param string2 第三组数据名称
     * @param color0 第一组折线颜色
     * @param color1 第二组折线颜色
     * @param color2 第三组折线颜色
     * @param isShowValueText 是否显示数据
     */
    public void SetLineChartData(Activity activity, LineChart chart,
                                 ArrayList<Entry> values0, ArrayList<Entry> values1, ArrayList<Entry> values2,
                                 float value0, float value1, float value2,
                                 String string0, String string1, String string2,
                                 int color0, int color1, int color2,
                                 boolean isShowValueText) {

        activity.runOnUiThread(() ->  {

            values0.add(new Entry(values0.size(), value0));
            values1.add(new Entry(values1.size(), value1));
            values2.add(new Entry(values2.size(), value2));

            LineData lineData = chart.getData();

            if (lineData == null) {

                LineDataSet set0 = new LineDataSet(values0, string0);
                LineDataSet set1 = new LineDataSet(values1, string1);
                LineDataSet set2 = new LineDataSet(values2, string2);

                setLineDataSet(set0, color0, isShowValueText);
                setLineDataSet(set1, color1, isShowValueText);
                setLineDataSet(set2, color2, isShowValueText);

                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(set0);
                dataSets.add(set1);
                dataSets.add(set2);

                lineData = new LineData(dataSets);
                chart.setData(lineData);

            } else {

                setLineDataSet((LineDataSet) lineData.getDataSetByIndex(0), color0, isShowValueText);
                setLineDataSet((LineDataSet) lineData.getDataSetByIndex(1), color1, isShowValueText);
                setLineDataSet((LineDataSet) lineData.getDataSetByIndex(2), color2, isShowValueText);

                //移动图表到最新的数据点位置

                float lastX  = Math.max(Math.max(lineData.getDataSetByIndex(0).getEntryCount() - 1, lineData.getDataSetByIndex(1).getEntryCount() - 1), lineData.getDataSetByIndex(2).getEntryCount() - 1);
                if (lastX > MAX_VISIBLE_RANGE) {
                    chart.getXAxis().setAxisMinimum(lastX - MAX_VISIBLE_RANGE);

                }

                lineData.notifyDataChanged();
                chart.notifyDataSetChanged();
            }

            YAxis yAxis = chart.getAxisLeft();
            float maxY = yAxis.getAxisMaximum();
            float minY = yAxis.getAxisMinimum();

            float valueMax = Math.max(Math.max(value0, value1), value2);
            float valueMin = Math.min(Math.min(value0, value1), value2);
            float nExpend = 1;

            if (maxY == 0 && minY == 0) {
                yAxis.setAxisMaximum(valueMax + nExpend);
                yAxis.setAxisMinimum(valueMin - nExpend);
            } else {
                if (valueMax > maxY) {
                    yAxis.setAxisMaximum(valueMax + nExpend);
                }
                if (valueMin < minY) {
                    yAxis.setAxisMinimum(valueMin - nExpend);
                }
            }
            chart.invalidate();

        });
    }

    private void setLineDataSet(LineDataSet set, int color, boolean isShowValueText) {
        set.setValueTextSize(3f);
        set.setColor(color);
        set.setValueTextColor(color);
        set.setCircleColor(color);
        set.setLineWidth(2f);
        set.setCircleRadius(1f);

        if (!isShowValueText) {
            set.setValueTextSize(0);
        }

        set.notifyDataSetChanged();
    }
    /**
     *
     * @param chart 所需传数据折线图
     * @param values0 储存第一组数据的列表
     * @param values1 储存第二组数据的列表
     * @param string0 第一组数据名称
     * @param string1 第二组数据名称
     * @param color0 第一组折线颜色
     * @param color1 第二组折线颜色
     * @param isShowValueText 是否显示数据
     */
    public void SetDisplacementChartData(Activity activity, LineChart chart,
                                 ArrayList<Entry> values0, ArrayList<Entry> values1,
                                 String string0, String string1,
                                 int color0, int color1,
                                 boolean isShowValueText)
    {

        activity.runOnUiThread(() -> {

            LineData lineData = chart.getData();

            if (lineData == null) {
                LineDataSet set0 = new LineDataSet(values0, string0);
                LineDataSet set1 = new LineDataSet(values1, string1);

                setLineDataSet(set0, color0, isShowValueText);
                setLineDataSet(set1, color1, isShowValueText);

                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(set0);
                dataSets.add(set1);

                lineData = new LineData(dataSets);
                chart.setData(lineData);

            } else {
                setLineDataSet((LineDataSet) lineData.getDataSetByIndex(0), color0, isShowValueText);
                setLineDataSet((LineDataSet) lineData.getDataSetByIndex(1), color1, isShowValueText);
                // 移动图表到最新的数据点位置
                float lastX  = Math.max(chart.getData().getDataSetByIndex(0).getEntryCount() - 1, chart.getData().getDataSetByIndex(1).getEntryCount() - 1);
                if (lastX > MAX_VISIBLE_RANGE) {
                    chart.getXAxis().setAxisMinimum(lastX - MAX_VISIBLE_RANGE);
                }
                lineData.notifyDataChanged();
                chart.notifyDataSetChanged();
            }


            YAxis y = chart.getAxisLeft();
            XAxis x = chart.getXAxis();

            float XMax = x.getAxisMinimum();
            float XMin = x.getAxisMaximum();
            float YMax = y.getAxisMaximum();
            float YMin = y.getAxisMinimum();

            //遍历找到
            float maxCenterX = 0;
            float maxTimeX = 0;
            float minCenterX = 0;
            float minTimeX = 0;
            for (Entry entry : values0) {
                if (entry.getY() > maxCenterX) {
                    maxCenterX = entry.getY();
                }
                if(entry.getX() > maxTimeX){
                    maxTimeX = entry.getX();
                }
                if(entry.getY() < minCenterX){
                    minCenterX = entry.getY();
                }
                if(entry.getX() < minTimeX){
                    minTimeX = entry.getX();
                }
            }

            //遍历找到
            float maxCenterY = 0;
            float maxTimeY = 0;
            float minCenterY = 0;
            float minTimeY = 0;
            for (Entry entry : values1) {
                if (entry.getY() > maxCenterY) {
                    maxCenterY = entry.getY();
                }
                if(entry.getX() > maxTimeY){
                    maxTimeY = entry.getX();
                }
                if(entry.getY() < minCenterY){
                    minCenterY = entry.getY();
                }
                if(entry.getX() < minTimeY){
                    minTimeY = entry.getX();
                }
            }

            float CenterMax = Math.max(maxCenterX, maxCenterY);
            float CenterMin = Math.min(minCenterX, minCenterY);
            float TimeMax = Math.max(maxTimeX, maxTimeY);
            float TimeMin = Math.min(minTimeX, minTimeY);

            float nExpend = 1;//保证曲线始终全部显示

            if(YMax == 0 && YMin == 0 && XMax == 0 && XMin == 0){
                x.setAxisMinimum(TimeMin - nExpend);
                x.setAxisMaximum(TimeMax + nExpend);
                y.setAxisMaximum(CenterMax + nExpend);
                y.setAxisMinimum(CenterMin - nExpend);
            }else {
                if(TimeMax > XMax){
                    x.setAxisMaximum(TimeMax + nExpend);
                }
                if(TimeMin < XMin){
                    x.setAxisMinimum(TimeMin - nExpend);
                }
                if (CenterMax > YMax) {
                    y.setAxisMaximum(CenterMax + nExpend);
                }
                if (CenterMin < YMin) {
                    y.setAxisMinimum(CenterMin - nExpend);
                }
            }
            // 在另一个线程中调用postInvalidate()方法
            chart.invalidate();
        });

    }
    /**
     *
     * @param chart 所需传入数据折线图
     * @param values 储存数据的列表，必须是全局的
     * @param value 数据
     * @param string 数据名称
     * @param color 折线颜色
     * @param isShowValueText 是否显示数据
     */
    public void SetLineChartData(Activity activity, LineChart chart,
                                 ArrayList<Entry> values,
                                 float value,
                                 String string,
                                 int color,
                                 boolean isShowValueText)
    {
        // 在另一个线程中修改数据
        activity.runOnUiThread(()-> {
            // 增加或删除数据点等操作
            values.add(new Entry(values.size(), value));
            LineData lineData = chart.getData();

            if (lineData == null) {
                LineDataSet set0 = new LineDataSet(values, string);
                setLineDataSet(set0, color, isShowValueText);

                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(set0);

                lineData = new LineData(dataSets);
                chart.setData(lineData);

            } else {
                setLineDataSet((LineDataSet) lineData.getDataSetByIndex(0), color, isShowValueText);
                // 移动图表到最新的数据点位置
                float lastX  = chart.getData().getDataSetByIndex(0).getEntryCount() - 1;
                if (lastX > MAX_VISIBLE_RANGE) {
                    chart.getXAxis().setAxisMinimum(lastX - MAX_VISIBLE_RANGE);
                }
                lineData.notifyDataChanged();
                chart.notifyDataSetChanged();
            }

            //set Max and Min Y value
            YAxis y = chart.getAxisLeft();
            float nMax = y.getAxisMaximum();
            float nMin = y.getAxisMinimum();
            float nExpend = 1;//保证曲线始终全部显示
            if (nMax == 0 && nMin == 0) {
                y.setAxisMaximum(value + nExpend);
                y.setAxisMinimum(value - nExpend);
            } else {
                if (value > nMax) {
                    y.setAxisMaximum(value + nExpend);
                }
                if (value < nMin) {
                    y.setAxisMinimum(value - nExpend);
                }
            }
            // 在另一个线程中调用postInvalidate()方法
            chart.invalidate();
        });
    }


}
