package com.teatime.teatime;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.LocaleList;

import java.util.Locale;

/**
 * Created by Kevin on 2018-03-08.
 * https://stackoverflow.com/questions/44554626/android-n-change-locale-in-runtime
 */

public class LanguageContextWrapper extends android.content.ContextWrapper {
    public LanguageContextWrapper(Context base) {
        super(base);
    }


    @SuppressWarnings("deprecation")
    public static ContextWrapper wrap(Context context, Locale newLocale) {

        Resources res = context.getResources();
        Configuration configuration = res.getConfiguration();

        if (android.os.Build.VERSION.SDK_INT >= 24) {
            configuration.setLocale(newLocale);

            LocaleList localeList = new LocaleList(newLocale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);

            context = context.createConfigurationContext(configuration);


        } else if (android.os.Build.VERSION.SDK_INT >= 17) {
            configuration.setLocale(newLocale);
            context = context.createConfigurationContext(configuration);

        } else {
            configuration.locale = newLocale;
            res.updateConfiguration(configuration, res.getDisplayMetrics());
        }

        return new ContextWrapper(context);
    }
}

