package com.petrologautomation.petrolognexus;


import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;

import java.util.Arrays;

/**
 * Created by Cesar on 7/22/13.
 */
public class wellHistoricalRuntime_post {

    MainActivity myAct;
    private XYPlot History;

    private SimpleXYSeries beforeToday;
    private Paint bTLinePaint;
    private Paint bTFillPaint;
    private LineAndPointFormatter bTLineFormat;

    private SimpleXYSeries today;
    private Paint TLinePaint;
    private Paint TFillPaint;
    private LineAndPointFormatter TLineFormat;

    private SimpleXYSeries afterToday;
    private Paint aTLinePaint;
    private Paint aTFillPaint;
    private LineAndPointFormatter aTLineFormat;

    private Number[] serie = new Number[2];

    private Number[] serieToday = new Number[6];

    public wellHistoricalRuntime_post(MainActivity myActivity){

        myAct = myActivity;
        beforeToday = new SimpleXYSeries(Arrays.asList(serie),SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, "");
        today = new SimpleXYSeries(Arrays.asList(serie),SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, "");
        afterToday = new SimpleXYSeries(Arrays.asList(serie),SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, "");
        History = (XYPlot) myAct.findViewById(R.id.runtimeTrend);

        XYGraphWidget myWidget = History.getGraphWidget();

        myWidget.setDrawMarkersEnabled(false);

        PointLabelFormatter label = new PointLabelFormatter(Color.BLUE);
        label.vOffset = -10f;
        label.hOffset = -6f;
        Paint textPaint = new Paint();
        textPaint.setTextSize(14);
        textPaint.setTypeface(Typeface.defaultFromStyle(0));
        textPaint.setColor(Color.BLUE);
        label.setTextPaint(textPaint);
        bTLineFormat = new LineAndPointFormatter(
                Color.BLUE,
                Color.RED,
                null,
                label);

        TLineFormat = new LineAndPointFormatter(
                Color.BLUE,
                Color.RED,
                null,
                label);

        aTLineFormat = new LineAndPointFormatter(
                Color.BLUE,
                Color.RED,
                null,
                label);

        /* beforeToday paint */
        bTLinePaint = new Paint();
        bTLinePaint.setStyle(Paint.Style.STROKE);
        bTLinePaint.setStrokeWidth(5);
        bTLinePaint.setShader(new LinearGradient(0, 0, 0, 1, Color.WHITE, Color.BLUE, Shader.TileMode.REPEAT));
        bTLineFormat.setLinePaint(bTLinePaint);

        bTFillPaint = new Paint();
        bTFillPaint.setStyle(Paint.Style.FILL);
        bTFillPaint.setAlpha(150);
        bTFillPaint.setShader(new LinearGradient(0, 600, 0, 0, Color.WHITE, Color.RED, Shader.TileMode.REPEAT));
        bTLineFormat.setFillPaint(bTFillPaint);

        History.getLayoutManager().remove(History.getLegendWidget());
        History.addSeries(beforeToday,bTLineFormat);

        /* today paint */
        TLinePaint = new Paint();
        TLinePaint.setStyle(Paint.Style.STROKE);
        TLinePaint.setStrokeWidth(5);
        TLinePaint.setShader(new LinearGradient(0, 0, 0, 1, Color.WHITE, Color.BLUE, Shader.TileMode.REPEAT));
        TLineFormat.setLinePaint(TLinePaint);

        TFillPaint = new Paint();
        TFillPaint.setStyle(Paint.Style.FILL);
        TFillPaint.setAlpha(150);
        TFillPaint.setShader(new LinearGradient(0, 600, 0, 0, Color.WHITE, Color.BLACK, Shader.TileMode.REPEAT));
        TLineFormat.setFillPaint(TFillPaint);

        History.getLayoutManager().remove(History.getLegendWidget());
        History.addSeries(today,TLineFormat);

        /* after today paint */
        aTLinePaint = new Paint();
        aTLinePaint.setStyle(Paint.Style.STROKE);
        aTLinePaint.setStrokeWidth(5);
        aTLinePaint.setShader(new LinearGradient(0, 0, 0, 1, Color.WHITE, Color.BLUE, Shader.TileMode.REPEAT));
        aTLineFormat.setLinePaint(aTLinePaint);

        aTFillPaint = new Paint();
        aTFillPaint.setStyle(Paint.Style.FILL);
        aTFillPaint.setAlpha(150);
        aTFillPaint.setShader(new LinearGradient(0, 600, 0, 0, Color.WHITE, Color.GREEN, Shader.TileMode.REPEAT));
        aTLineFormat.setFillPaint(aTFillPaint);

        History.getLayoutManager().remove(History.getLegendWidget());
        History.addSeries(afterToday,aTLineFormat);


    }

    public void post() {
        clean();
        int day = 1;
        try {
            day = Integer.valueOf(MainActivity.PetrologSerialCom.getPetrologClock().substring(9,11));
        }
        catch (NumberFormatException e){
            // TODO
        }

        for (int i=1;i<32;i++){
            if (i < day){
                beforeToday.addLast(i, (MainActivity.PetrologSerialCom.getHistoricalRuntime(i)*100)/86400);;
            }
            if (i >= day-1 && i <= day){
                today.addLast(i, (MainActivity.PetrologSerialCom.getHistoricalRuntime(i)*100)/86400);;
            }
            if (i >= day) {
                afterToday.addLast(i, (MainActivity.PetrologSerialCom.getHistoricalRuntime(i)*100)/86400);;
            }
        }
        History.addSeries(beforeToday,bTLineFormat);
        History.addSeries(today,TLineFormat);
        History.addSeries(afterToday,aTLineFormat);

        History.redraw();
    }

    public void clean() {
        History.clear();
        if(beforeToday.size()>0){
            for (int i=1;i<beforeToday.size();i++){
                beforeToday.removeLast();
            }
        }
        History.redraw();
    }
}
