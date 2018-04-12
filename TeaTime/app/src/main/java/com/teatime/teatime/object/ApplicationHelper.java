package com.teatime.teatime.object;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.arch.persistence.room.Room;
import android.content.Context;

import com.teatime.teatime.R;

import java.io.IOException;

public class ApplicationHelper {
    private static AppDatabase db;
    private static NotificationChannel nc;
    public static AppDatabase getDB(Context ctx) {
        return getDB(ctx,false);
    }
    public static AppDatabase getDB(Context ctx, boolean forTest) {
        if (db == null) {
            if (forTest)
                db = Room.inMemoryDatabaseBuilder(ctx,AppDatabase.class).build();
            else
                db = Room.databaseBuilder(ctx,AppDatabase.class,"TeaTime").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        }
        return db;
    }
    public static void CloseDB() throws IOException{
        if(db != null)
        {
            db.close();
            db = null;
        }
    }

    public static String formatInfusionTime(long seconds) {
        long min = seconds/60;
        long sec = seconds%60;

        return String.format("%02d:%02d",min,sec);
    }

    public static void createNotificationChannel(Context ctx, boolean vibrate) {
        if(nc != null)
            return;

        NotificationManager notificationManager =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = ctx.getResources().getString(R.string.channel_name);
        CharSequence channelName = ctx.getResources().getString(R.string.channel_name);
        int importance = NotificationManager.IMPORTANCE_LOW;
        nc = new NotificationChannel(channelId, channelName, importance);
        nc.enableVibration(vibrate);
        if(vibrate)
            nc.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        notificationManager.createNotificationChannel(nc);
    }
}
