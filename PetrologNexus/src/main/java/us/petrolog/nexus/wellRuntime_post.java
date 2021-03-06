package us.petrolog.nexus;


import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * Created by Cesar on 7/22/13.
 */
public class wellRuntime_post {

    DetailActivity myAct;
    TextView todayRuntime;
    TextView todayRuntimePercent;
    TextView yesterdayRuntime;
    TextView yesterdayRuntimePercent;

    ProgressBar TodayRuntimePB;
    ProgressBar YesterdayRuntimePB;


    public wellRuntime_post(DetailActivity myActivity) {

        myAct = myActivity;

        yesterdayRuntime = (TextView) myAct.findViewById(R.id.yesterday_runtime_time);
        yesterdayRuntimePercent = (TextView) myAct.findViewById(R.id.yesterday_runtime_percentTV);
        todayRuntime = (TextView) myAct.findViewById(R.id.today_runtime_time);
        todayRuntimePercent = (TextView) myAct.findViewById(R.id.today_runtime_percentTV);

        TodayRuntimePB = (ProgressBar) myAct.findViewById(R.id.runtime_today);
        YesterdayRuntimePB = (ProgressBar) myAct.findViewById(R.id.runtime_yesterday);

        Animation in = AnimationUtils.loadAnimation(myAct, android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(myAct, android.R.anim.fade_out);

    }

    public void post() {

        /* Yesterday's Runtime */
        int secYesterday = DetailActivity.PetrologSerialCom.getYesterdayRuntime();
        if (secYesterday > 0) {
            /* Time */
            String data = getDurationString(secYesterday);
            yesterdayRuntime.setText(StringFormatValue.format(myAct, data, myAct.getResources().getColor(R.color.mainBlue), 1.2f, false));
            /* % */
            YesterdayRuntimePB.setProgress((int) secYesterday / 86400);
            data = String.valueOf((secYesterday * 100) / 86400) + "%";
            yesterdayRuntimePercent.setText(StringFormatValue.format(myAct, data, myAct.getResources().getColor(R.color.mainBlue), 1f, false));
        } else {
            yesterdayRuntime.setText(StringFormatValue.format(myAct, "", myAct.getResources().getColor(R.color.mainGray), 1.2f, true));
            YesterdayRuntimePB.setProgress(0);
            yesterdayRuntimePercent.setText(StringFormatValue.format(myAct, "", myAct.getResources().getColor(R.color.mainGray), 1f, true));
        }


        /* Today's Runtime */
        int secToday = DetailActivity.PetrologSerialCom.getTodayRuntime();
        if (secToday > 0) {
            /* Time */
            String data = getDurationString(secToday);
            todayRuntime.setText(StringFormatValue.format(myAct, data, myAct.getResources().getColor(R.color.mainBlue), 1.2f, false));
            /* % */
            String InternalClock = DetailActivity.PetrologSerialCom.getPetrologClock();
            int totalSecToday = 0;
            try {
                totalSecToday = Integer.valueOf(InternalClock.substring(0, 2)) * 3600 +
                        Integer.valueOf(InternalClock.substring(3, 5)) * 60 +
                        Integer.valueOf(InternalClock.substring(6, 8));
                data = String.valueOf((secToday * 100) / totalSecToday) + "%";
                todayRuntimePercent.setText(StringFormatValue.format(myAct, data, myAct.getResources().getColor(R.color.mainBlue), 1f, false));
                TodayRuntimePB.setProgress((int) secToday / totalSecToday);
            } catch (NumberFormatException e) {
                Log.i("PN - H", "Empty - Number Format - " + totalSecToday);
            } catch (ArithmeticException e) {
                Log.i("PN - H", "Divide by Zero - " + totalSecToday);
            }
        } else {
            todayRuntime.setText(StringFormatValue.format(myAct, "", myAct.getResources().getColor(R.color.mainGray), 1.2f, true));
            TodayRuntimePB.setProgress(0);
            todayRuntimePercent.setText(StringFormatValue.format(myAct, "", myAct.getResources().getColor(R.color.mainGray), 1f, true));
        }

    }

    private String getDurationString(int seconds) {

        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return twoDigitString(hours) + " : " + twoDigitString(minutes) + " : " + twoDigitString(seconds);
    }

    private String twoDigitString(int number) {

        if (number == 0) {
            return "00";
        }

        if (number / 10 == 0) {
            return "0" + number;
        }

        return String.valueOf(number);
    }
}
