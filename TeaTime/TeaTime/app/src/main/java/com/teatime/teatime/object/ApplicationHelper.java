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

    /**
     * Créer l'objet Room à partir de la version en cache
     * @param ctx Contexte de l'application
     * @return objet Room
     */
    public static AppDatabase getDB(Context ctx) {
        return getDB(ctx,false);
    }

    /**
     * Créer l'objet Room à partir de la version en cache
     * @param ctx Contexte de l'application
     * @param forTest So oui ou non l'application est utilisé par une série de tests.
     * @return objet Room
     */
    public static AppDatabase getDB(Context ctx, boolean forTest) {
        if (db == null) {
            if (forTest)
                db = Room.inMemoryDatabaseBuilder(ctx,AppDatabase.class).build();
            else
                db = Room.databaseBuilder(ctx,AppDatabase.class,"TeaTime").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        }
        return db;
    }

    /**
     * Ferme la BD Room (à utiliser si la BD est chargé en mémoire pour les test unitaires)
     * @throws IOException
     */
    public static void CloseDB() throws IOException{
        if(db != null) {
            db.close();
            db = null;
        }
    }

    /**
     * Format-er le temps en secondes vers une chaîne mm:ss
     * @param seconds secondes
     * @return String des secondes formattés en mm:ss
     */
    public static String formatInfusionTime(long seconds) {
        long min = seconds/60;
        long sec = seconds%60;

        return String.format("%02d:%02d",min,sec);
    }

    /**
     * Crée le canal pour les notifications (nécessaire pour API 26+)
     * @param ctx
     * @param vibrate
     */
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
