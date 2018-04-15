package com.teatime.teatime;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.ColorRes;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.teatime.teatime.object.ApplicationHelper;
import com.teatime.teatime.object.Filter;
import com.teatime.teatime.object.GoodWithType;
import com.teatime.teatime.object.HealthPropertyType;
import com.teatime.teatime.object.Tea;
import com.teatime.teatime.object.TeaType;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private boolean mustNotify;


    @Override
    protected void attachBaseContext(Context newBase) {

        SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(newBase);

        String lang = pref.getBoolean(newBase.getString(R.string.enable_fr_key), false) ? "fr" : "en";

        Locale locale = new Locale(lang);

        Context context = LanguageContextWrapper.wrap(newBase, locale);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Check If First time
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean firstime = sp.getBoolean("first_time",true);
        if(firstime) {
            SharedPreferences.Editor edit = sp.edit();
            edit.putBoolean("first_time",false);
            edit.commit();
            FillDBForFirstUse();
        }
        ApplicationHelper.createNotificationChannel(this,sp.getBoolean(getString(R.string.notification_vibrate_key),true));
        mustNotify = sp.getBoolean(getString(R.string.notification_allow_key),true);
        //Build Activity
        setContentView(R.layout.activity_main);
        //Build Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                Menu m = navigationView.getMenu();
                Filter f = loadFilter();
                EnumSet<TeaType> teaTypes = f.getTeaType();
                EnumSet<GoodWithType> goodWithTypes = f.getGoodWith();
                EnumSet<HealthPropertyType> healthPropertyTypes = f.getHealthProperty();

                if(!teaTypes.contains(TeaType.ALL)){
                    if(teaTypes.contains(TeaType.BLACK))
                        m.findItem(R.id.nav_type_rouge).setChecked(true);
                    if(teaTypes.contains(TeaType.GREEN))
                        m.findItem(R.id.nav_type_vert).setChecked(true);
                    if(teaTypes.contains(TeaType.OOLONG))
                        m.findItem(R.id.nav_type_bleu).setChecked(true);
                    if(teaTypes.contains(TeaType.WHITE))
                        m.findItem(R.id.nav_type_blanc).setChecked(true);
                    if(teaTypes.contains(TeaType.FERMENTED))
                        m.findItem(R.id.nav_type_sombre).setChecked(true);
                }
                else {
                    m.findItem(R.id.nav_type_rouge).setChecked(false);
                    m.findItem(R.id.nav_type_vert).setChecked(false);
                    m.findItem(R.id.nav_type_bleu).setChecked(false);
                    m.findItem(R.id.nav_type_blanc).setChecked(false);
                    m.findItem(R.id.nav_type_sombre).setChecked(false);
                }

                if(!goodWithTypes.contains(GoodWithType.ALL)) {
                    if(goodWithTypes.contains(GoodWithType.SUGAR))
                        m.findItem(R.id.nav_good_sucre).setChecked(true);
                    if(goodWithTypes.contains(GoodWithType.MILK))
                        m.findItem(R.id.nav_good_lait).setChecked(true);
                    if(goodWithTypes.contains(GoodWithType.OTHER))
                        m.findItem(R.id.nav_good_autre).setChecked(true);
                }
                else {
                    m.findItem(R.id.nav_good_sucre).setChecked(false);
                    m.findItem(R.id.nav_good_lait).setChecked(false);
                    m.findItem(R.id.nav_good_autre).setChecked(false);
                }

                if(!healthPropertyTypes.contains(HealthPropertyType.ALL)) {
                    if(healthPropertyTypes.contains(HealthPropertyType.ANTI_INFLAMMATORY))
                        m.findItem(R.id.nav_prop_flame).setChecked(true);
                    if(healthPropertyTypes.contains(HealthPropertyType.CAFFEINE))
                        m.findItem(R.id.nav_prop_caffeine).setChecked(true);
                }
                else {
                    m.findItem(R.id.nav_prop_flame).setChecked(false);
                    m.findItem(R.id.nav_prop_caffeine).setChecked(false);
                }

                updateSideMenuState();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                // Called when a drawer has settled in a completely closed state.
                // Called when a drawer has settled in a completely closed state.
                updateSideMenuState();
                saveFilter();
                FilterAndBindData();
                ScrollView sv = findViewById(R.id.scrollView);
                sv.scrollTo(0,0);
                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();
        ActionBar actionBar = getSupportActionBar();
        Drawable normalDrawable = ContextCompat.getDrawable(this,R.drawable.ic_menu_black_24dp);
        Drawable wrapDrawable = DrawableCompat.wrap(normalDrawable);
        DrawableCompat.setTint(wrapDrawable, this.getResources().getColor(android.R.color.holo_green_dark,null));
        actionBar.setHomeAsUpIndicator(wrapDrawable);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //Build Side Menu
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        TextView txtusername = (TextView)headerView.findViewById(R.id.txtUsername);
        txtusername.setText(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("username_text",null));
        //Check if search
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Filter f = loadFilter();
            //Check for injection
            if (query.contains("\"") || query.contains("'") ||
                    query.contains(";") || query.contains("=="))
                f.setName(null);
            else
                f.setName(query);
            saveFilter(f);
        }
        //Bind Data
        FilterAndBindData();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_actionbar, menu);

        MenuItem searchItem = menu.findItem(R.id.search_actionbar) ;
        MenuItem settingItem = menu.findItem(R.id.setting_actionbar);
        if(searchItem != null) {
            Filter f = loadFilter();
            if (f.getName() == null)
                searchItem.setIcon(R.drawable.ic_search_black_24dp);
            else
                searchItem.setIcon(R.drawable.ic_clear_black_24dp);
            tintMenuIcon(searchItem,android.R.color.holo_green_dark);
        }
        if(settingItem != null){
            tintMenuIcon(settingItem,android.R.color.holo_green_dark);
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        Menu m = navigationView.getMenu();
        int id = item.getItemId();
        if (id == R.id.nav_all){
            m.findItem(R.id.nav_type_blanc).setChecked(false);
            m.findItem(R.id.nav_type_bleu).setChecked(false);
            m.findItem(R.id.nav_type_rouge).setChecked(false);
            m.findItem(R.id.nav_type_vert).setChecked(false);
            m.findItem(R.id.nav_type_sombre).setChecked(false);
            m.findItem(R.id.nav_prop_flame).setChecked(false);
            m.findItem(R.id.nav_prop_caffeine).setChecked(false);
            m.findItem(R.id.nav_good_sucre).setChecked(false);
            m.findItem(R.id.nav_good_lait).setChecked(false);
            m.findItem(R.id.nav_good_autre).setChecked(false);
        }
        else if(id == R.id.nav_type_the){
            boolean newState = !m.findItem(R.id.nav_type_blanc).isVisible();
            m.findItem(R.id.nav_type_blanc).setVisible(newState);
            m.findItem(R.id.nav_type_bleu).setVisible(newState);
            m.findItem(R.id.nav_type_rouge).setVisible(newState);
            m.findItem(R.id.nav_type_vert).setVisible(newState);
            m.findItem(R.id.nav_type_sombre).setVisible(newState);
        }
        else if(id == R.id.nav_prop_the) {
            boolean newState = !m.findItem(R.id.nav_prop_flame).isVisible();
            m.findItem(R.id.nav_prop_flame).setVisible(newState);
            m.findItem(R.id.nav_prop_caffeine).setVisible(newState);
        }
        else if(id == R.id.nav_bon_avec) {
            boolean newState = !m.findItem(R.id.nav_good_sucre).isVisible();
            m.findItem(R.id.nav_good_sucre).setVisible(newState);
            m.findItem(R.id.nav_good_lait).setVisible(newState);
            m.findItem(R.id.nav_good_autre).setVisible(newState);
        }
        else {
            if(item.isChecked()){
                item.setChecked(false);
            }
            else {
                item.setChecked(true);
            }
            updateSideMenuState();
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Menu m = navigationView.getMenu();
        if (id == R.id.setting_actionbar) {
            //Settings onClick
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.search_actionbar) {
            //Search onClick
            //TODO : Go to Search Activity/Start Search Widget
            Filter f = loadFilter();
            if (f.getName() == null)
                onSearchRequested();
            else {
                f.setName(null);
                saveFilter(f);
                item.setIcon(R.drawable.ic_search_black_24dp);
                tintMenuIcon(item,android.R.color.holo_green_dark);
                FilterAndBindData();
            }
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }

    //Database
    /*
     * Cette méthode devrait être remplacée par
     * une BD à distance ou un API.
     * Les définitions sont prises du site Wikipedia en visitant
     * les pages à partir des noms des thés
     * */
    /**
     * Rempli la base de donnée avec des donées par défaut.
     */
    public void FillDBForFirstUse() {
        Tea t1 = new Tea("Lapsang souchong", "Lapsang souchong",
                "From China (translated from jīnjùnméi), one of the highest grade red teas in mainland China. \n\nMake sure you steep the infusion process as follow : 60-40-60 seconds at 90-95 degrees Celsius with 4g of tea per 200ml of water .",
                "De la Chine (traduit de jīnjùnméi), une des feuilles de plus hautes qualités provenant de Chine. \n\nAssurez-vous de bien infuser le thé selon le procédé d'infusion suviant: en escalier avec des tranches de 60-40-60 secondes à une température de 90-95 degrés Celsius avec 4g de thé par 200ml d'eau.",
                false, EnumSet.of(TeaType.BLACK),
                EnumSet.of(HealthPropertyType.NONE),
                EnumSet.of(GoodWithType.NONE),
                160,
                "lapsang");
        Tea t2 = new Tea("Yingdehong", "Yingdehong",
                "From China, this tea has a cocoa-like aroma and a sweet aftertaste, one can find a peppery note. \n\nMake sure you steep the infusion process as follow : 60-40-60 seconds at 90-95 degrees Celsius with 4g of tea per 200ml of water .",
                "De la Chine, ce thé a un arôme de cocoa et un arrière goût sucré, on peut aussi y trouvé une note poivrée. \n\nAssurez-vous de bien infuser le thé selon le procédé d'infusion suviant: en escalier avec des tranches de 60-40-60 secondes à une température de 90-95 degrés Celsius avec 4g de thé par 200ml d'eau.",
                false, EnumSet.of(TeaType.BLACK),
                EnumSet.of(HealthPropertyType.NONE),
                EnumSet.of(GoodWithType.MILK),
                160,
                "yingdehong");
        Tea t3 = new Tea("Sun Moon Lake", "Sun Moon Lake",
                "From Taiwan (translated from Rìyuè-tán-hóngchá), this tea has honey rich tones, sweet osmanthus, cinnamon and peppermint.\n\nMake sure you steep the infusion process as follow : 60-40-60 seconds at 90-95 degrees Celsius with 4g of tea per 200ml of water .",
                "De la Taïwan (traduit de Rìyuè-tán-hóngchá), ce thé a un riche arôme de miel, osmanthus sucré et de menthe poivrée.\n\nAssurez-vous de bien infuser le thé selon le procédé d'infusion suviant: en escalier avec des tranches de 60-40-60 secondes à une température de 90-95 degrés Celsius avec 4g de thé par 200ml d'eau.",
                false,
                EnumSet.of(TeaType.BLACK),
                EnumSet.of(HealthPropertyType.NONE),
                EnumSet.of(GoodWithType.NONE),160,
                "sunmoonlake");
        Tea t4 = new Tea("Munnar", "Munnar",
                "From India (translated from Mūnnār cāya), This variety produces a strong bodied golden yellow liquor with refreshing briskness and a hint of fruit. It has a medium toned fragrance, that is akin to malted biscuits.\n\nMake sure you steep the infusion process as follow : 60-40-60 seconds at 90-95 degrees Celsius with 4g of tea per 200ml of water .",
                "De l'Inde (traduit de Mūnnār cāya), cette variété produit un breuvage jaune très corsé avec une dynamique rafraîchissante et une note de fruit, qui est similaire à un biscuit malté.\n\nAssurez-vous de bien infuser le thé selon le procédé d'infusion suviant: en escalier avec des tranches de 60-40-60 secondes à une température de 90-95 degrés Celsius avec 4g de thé par 200ml d'eau.",
                false, EnumSet.of(TeaType.BLACK),
                EnumSet.of(HealthPropertyType.CAFFEINE),
                EnumSet.of(GoodWithType.MILK),
                160,
                "munnar");
        Tea t5 = new Tea("Jaekseol", "Jaekseol",
                "From Korea (translated from jaekseol-cha), Jaekseol tea is golden, light scarlet in color and has a sweet, clean taste.\n\nMake sure you steep the infusion process as follow : 60-40-60 seconds at 90-95 degrees Celsius with 4g of tea per 200ml of water .",
                "De la Corée (traduit de jaekseol-cha), Jaekseol est un thé de couleur doré, écarlate avec un goût pure et sucrée.\n\nAssurez-vous de bien infuser le thé selon le procédé d'infusion suviant: en escalier avec des tranches de 60-40-60 secondes à une température de 90-95 degrés Celsius avec 4g de thé par 200ml d'eau.",
                false,
                EnumSet.of(TeaType.BLACK),
                EnumSet.of(HealthPropertyType.NONE),
                EnumSet.of(GoodWithType.MILK),
                160,
                "jaekseol");
        Tea t6 = new Tea("Rize", "Rize",
                "From Turkey (translated from Rize çayı), this tea is characterised by its strong taste, when brewed it is mahogany in color. Traditionally served with beet sugar crystals.\n\nMake sure you steep the infusion process as follow : 60-40-60 seconds at 90-95 degrees Celsius with 4g of tea per 200ml of water .",
                "De la Turquie (traduit de Rize çayı), ce thé est caractérisé par son goût corsé et une fois infusé, il a une couleur acajou.\n\nAssurez-vous de bien infuser le thé selon le procédé d'infusion suviant: en escalier avec des tranches de 60-40-60 secondes à une température de 90-95 degrés Celsius avec 4g de thé par 200ml d'eau.",
                false,
                EnumSet.of(TeaType.BLACK),
                EnumSet.of(HealthPropertyType.CAFFEINE),
                EnumSet.of(GoodWithType.SUGAR),
                160,
                "rize");
        Tea t7 = new Tea("Huang Shan Mao Feng", "Huang Shan Mao Feng",
                "Huang Shan Mao Feng is a green tea produced in south eastern interior Anhui province of China. The tea is one of the most famous teas in China and can almost always be found on the China Famous Tea list.\n\nRecommended Steeping : 3 times for 30s at 61-87 degrés Celsius with 2g of tea per 100ml of water.",
                "Le Huángshān máofēng est un des thés les plus réputés de Chine, inclus dans la liste des dix thés chinois les plus célèbres. Il provient des monts jaunes (Huángshān), où il est fabriqué depuis plus de 300 ans. Thé de montagne, il est parfois cultivé à près de 1 000 mètres d'altitude. Le Huángshān máofēng a une astringence plus grande que la plupart des thés chinois de qualité. Celle-ci reste néanmoins discrète et structure une infusion où dominent des saveurs végétales fraiches et minérales accompagnées de notes d'iode et de châtaigne.\n\nInfusion en trois étapes de 30s chaque à 61-87 degrés Celsius avec 2g de thé par 100ml d'eau.",
                false,
                EnumSet.of(TeaType.GREEN),
                EnumSet.of(HealthPropertyType.CAFFEINE,HealthPropertyType.ANTI_INFLAMMATORY),
                EnumSet.of(GoodWithType.NONE),
                30,
                "huangshan");
        Tea t8 = new Tea("Pingshui Gunpowder", "Le Gunpowder",
                "The original and most common variety of gunpowder tea with larger pearls, better color, and a more aromatic infusion, which is commonly sold as Temple of Heaven Gunpowder or Pinhead Gunpowder, the former, a common brand of this tea variety. The flavor of brewed gunpowder tea is often described as thick and strong like a soft honey, but with a smokey flavor and an aftertaste that is slightly coppery.\n\nRecommended Steeping : 3 times for 20s at 70-80 degrees Celsius with 2g of tea per 100ml of water.",
                "Le Gunpowder est un thé vert chinois originaire de la province du Zhejiang. Des feuilles brillantes indiquent un thé jeune et témoignent d'une bonne qualité. Le goût du Gunpowder est défini par sa forte saveur de miel avec une note de fumée et un arrière-goût de cuivre.\n\nInfusion en trois étapes de 20s chaque à 70-80 degrés Celsius avec 2g de thé par 100ml d'eau",
                false,
                EnumSet.of(TeaType.GREEN),
                EnumSet.of(HealthPropertyType.CAFFEINE,HealthPropertyType.ANTI_INFLAMMATORY),
                EnumSet.of(GoodWithType.SUGAR),
                20,
                "gunpowder");
        Tea t9 = new Tea("Sencha", "Sencha",
                "Sencha is the most popular japanese green tea. The resulting taste is more 'green' than a chinese tea, almost has a taste of herb or algae. The infusion is green and very bitter.\n\nInfusion time of 60s with 1g per 125ml of water at +/- 80 degrees Celsius",
                "Le Sencha est un thé vert japonais dont le nom signifie littéralement « thé infusé ». Il est le thé le plus courant au Japon. Le goût résultant est plus vert que les thés chinois, presque un goût d'herbe ou d'algue. L'infusion est très verte et assez amère.\n\nTemps d'infusion: 60s avec 1g par 125ml d'eau à +/- 80 degrés Celsius",
                false,
                EnumSet.of(TeaType.GREEN),
                EnumSet.of(HealthPropertyType.CAFFEINE,HealthPropertyType.ANTI_INFLAMMATORY),
                EnumSet.of(GoodWithType.NONE),
                60,
                "sencha");
        Tea t10 = new Tea("Bancha", "Bancha",
                "Bancha is harvested from the same tree as sencha grade, but it is plucked later than sencha is. Its flavour is unique, it has a stronger organic straw smell.\n\nInfusion Time : 30s at 80 degrees Celsius with 1g of tea per 100ml of water",
                "Le Bancha est un thé vert japonais qui est issu de la dernière récolte du thé, qui a lieu en octobre. Le bancha est le thé vert commun (ordinaire) au Japon. Il est récolté à partir du même arbre que la qualité sencha, mais il est cueilli plus tard.\n\nTemps d'infusion : 30s à 80 degrés Celsius avec 1g de thé par 100ml d'eau",
                false,
                EnumSet.of(TeaType.GREEN),
                EnumSet.of(HealthPropertyType.CAFFEINE),
                EnumSet.of(GoodWithType.NONE),
                30,
                "bancha");
        Tea t11 = new Tea("Kukicha", "Kukicha",
                "Kukicha has a mildly nutty, and slightly creamy sweet flavour. It is made of four sorts of stems, stalks and twigs of Camellia sinensis. Green varieties are best steeped for less than one minute.\n\nInfusion Time : 30s at 80 degrees Celsius with 1g of tea per 100ml of water",
                "Le kukicha est un mélange traditionnel de thé vert japonais avec les tiges du thé, le moins riche en caféine des thés traditionnels. Le kukicha possède un goût puissant et rafraîchissant, avec un arrière-goût rappelant l'algue.\n\nTemps d'infusion : 30s à 80 degrés Celsius avec 1g de thé par 100ml d'eau",
                false,
                EnumSet.of(TeaType.GREEN),
                EnumSet.of(HealthPropertyType.ANTI_INFLAMMATORY),
                EnumSet.of(GoodWithType.NONE),
                30,
                "kukicha");
        Tea t12 = new Tea("Ceylon", "Ceylan",
                "Ceylon green teas generally have the fuller body and the more pungent, rather malty, nutty flavour characteristic of the teas originating from Assamese seed stock.\n\nInfusion time : 60s at 80 degrees Celsius with 1g of tea per 100ml of water",
                "Le thé de Ceylan satisfait les papilles des plus grands dégustateurs de thé partout dans le monde entier. Caractérisé par un goût fort et authentique, les petites feuilles donnent essentiellement une saveur légère tandis que les grandes feuilles donnent une saveur accentuée et corsée.\n\nTemps d'infusion : 60s à 80 degrés Celsius avec 1g de thé par 100ml d'eau",
                false,
                EnumSet.of(TeaType.GREEN),
                EnumSet.of(HealthPropertyType.CAFFEINE,HealthPropertyType.ANTI_INFLAMMATORY),
                EnumSet.of(GoodWithType.NONE),
                60,
                "ceylon_tea");
        Tea t13 = new Tea("Tieguanyin", "Tieguanyin",
                "Tieguanyin is a oolong chinese tea, which originated from the province of Fujian (China) and in Taiwan. The low oxidation gives it a very floral taste with fresh and vegetal notes.\n\nInfusion time : 120s at 90 degrees Celsius with 1g of tea per 200ml of water",
                "Le Tieguanyin est un thé chinois de type oolong cultivé, à l'origine, dans la province du Fujian (Chine) et à Taïwan. La faible oxydation donne un thé très floral, aux notes fraiches et végétales.\n\nTemps d'infusion : 120s à 90 degrés Celsius avec 1g de thé par 200ml d'eau",
                false,
                EnumSet.of(TeaType.OOLONG),
                EnumSet.of(HealthPropertyType.CAFFEINE),
                EnumSet.of(GoodWithType.NONE),
                120,
                "tieguanyin");
        Tea t14 = new Tea("Da Hong Pao", "Da Hong Pao",
                "Da Hong Pao (Big Red Robe) is a Wuyi rock tea grown in the Wuyi Mountains. It is a heavily oxidized, dark oolong tea.\n\nInfusion time : 120s at 90 degrees Celsius with 1g of tea per 200ml of water",
                "Dàhóngpáo est un prestigieux thé oolong des monts Wuyi et dont le nom signifierait « Grande robe rouge ». C'est un thé de première qualité de la variété Wu Yi Yan Cha des thés oolong.\n\nTemps d'infusion : 120s à 90 degrés Celsius avec 1g de thé par 200ml d'eau",
                false,
                EnumSet.of(TeaType.OOLONG),
                EnumSet.of(HealthPropertyType.CAFFEINE,HealthPropertyType.ANTI_INFLAMMATORY),
                EnumSet.of(GoodWithType.SUGAR),
                120,
                "dahongpao");
        Tea t15 = new Tea("Bai Mu Dan",
                "Bai Mu Dan",
                "Bai Mu Dan is a type of white tea made from plucks each with one leaf shoot and two immediate young leaves. A very mild peony aroma and a floral aroma are noticed when brewing the tea.\n\nInfusion time : 120s at 70 degrees Celsius with 1g of tea per 100ml of water",
                "Le Bai Mu Dan ou Pai Mu Tan est un thé blanc, à longues feuilles blanches. Le Bai Mu Dan est souvent préféré par les buveurs de thé blanc pour son goût plus entier et sa plus grande force comparé au Bai Hao Yinzhen qui lui est fabriqué à partir de feuilles non développées, et donne donc une infusion plus pâle.\n\nTemps d'infusion : 120s à 70 degrés Celsius avec 1g de thé par 100ml d'eau",
                false,
                EnumSet.of(TeaType.WHITE),
                EnumSet.of(HealthPropertyType.CAFFEINE),
                EnumSet.of(GoodWithType.NONE),
                120,
                "baimudan");
        Tea t16 = new Tea("Pu'Er", "Pu'Er",
                "Pu'er or Pu-Erh is a variety of fermented tea produced in Yunnan province, China. Because of the prolonged fermentation in ripened pu'erh and slow oxidization of aged raw pu'erh, these teas often lack the bitter, astringent properties of other teas, and can be brewed much stronger and repeatedly, with some claiming 20 or more infusions of tea from one pot of leaves.\n\nInfusion time : ~10min at 95 degrees Celsius with 1g per 200ml of water",
                "Le thé Pu'Er est fabriqué à partir de feuilles d'une variété de théier nommé Camellia sinensis poussant dans le Yunnan.\n\nTemps d'infusion : ~10min à 95 degrés Celsius avec 1g de thé par 200ml d'eau",
                false,
                EnumSet.of(TeaType.FERMENTED),
                EnumSet.of(HealthPropertyType.CAFFEINE,HealthPropertyType.ANTI_INFLAMMATORY),
                EnumSet.of(GoodWithType.NONE),
                600,
                "puer"); //Yunnan H:C + T:F + G:N


        long[] ids = ApplicationHelper.getDB(getBaseContext()).teaDao().insertAll(
                t1,t2,t3,t4,t5,t6,t7,t8,t9,t10,t11,t12,t13,t14,t15,t16
        );
    }

    //Data Binding
    /**
     * Récupère les thés de la base de donnée
     * et instancie les View nécessaire à l'affichage
     */
    public void FilterAndBindData(){
        LinearLayout container = (LinearLayout) findViewById(R.id.tea_container);
        container.removeAllViews();

        //Get Teas
        Filter f = loadFilter();
        String lang = getResources().getConfiguration().getLocales().get(0).getLanguage();
        List<Tea> teas = f.getTeasFromFilter(getBaseContext(),lang);

        if(teas.size() == 0) {
            //Show Empty State
            TextView txtEmpty = new TextView(this);
            txtEmpty.setText(getString(R.string.no_result));
            LinearLayout.LayoutParams empty_lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            empty_lp.setMargins((int)getResources().getDimension(R.dimen.txtempty_leftMargin),(int)getResources().getDimension(R.dimen.txtempty_topMargin),0,0);
            txtEmpty.setLayoutParams(empty_lp);
            container.addView(txtEmpty);
            return;
        }
        for (Tea t : teas) {
            final long tea_id = t.getId();
            //Create Empty Row
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setBackgroundColor(Color.parseColor("#FAFAFA"));//#FAFAFA default android background
            rowLayout.setLayoutParams(new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            rowLayout.setOrientation(LinearLayout.VERTICAL);
            //Header Row
            LinearLayout headerLayout = new LinearLayout(this);
            headerLayout.setLayoutParams(new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,1));
            headerLayout.setOrientation(LinearLayout.HORIZONTAL);
            //Fill Header Row
            //Tea Picture
            ImageView imgTeaPicture = new ImageView(this);
            imgTeaPicture.setLayoutParams(new LinearLayout.LayoutParams(
                    (int)getResources().getDimension(R.dimen.teaPicture_width),
                    (int)getResources().getDimension(R.dimen.teaPicture_height),1));
            imgTeaPicture.setBackgroundColor(getHexCodeFromTea(t));
            Drawable teaPictureDrawable;
            if(t.getImgFileName() == null){
                teaPictureDrawable = DrawableCompat.wrap(getDrawable(R.drawable.ic_local_cafe_black_24dp));
                DrawableCompat.setTint(teaPictureDrawable,getResources().getColor(android.R.color.background_light,null));
            }
            else {
                teaPictureDrawable = DrawableCompat.wrap(getDrawable(getResources().getIdentifier(t.getImgFileName(), "drawable", getPackageName())));
            }

            imgTeaPicture.setImageDrawable(teaPictureDrawable);
            //TxtButton
            Button txtButton = new Button(this);
            txtButton.setLayoutParams(new LinearLayout.LayoutParams(
                    (int)getResources().getDimension(R.dimen.teaButton_width),
                    (int)getResources().getDimension(R.dimen.teaButton_height),1));
            txtButton.setBackgroundColor(getHexCodeFromTea(t));
            txtButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    teaTextClick(v);
                }
            });
            final String tea_name = t.getName(lang);
            txtButton.setText(tea_name);
            txtButton.setTextSize(20);
            txtButton.setAllCaps(false);
            txtButton.setTextColor(getTextColorFromTea(t));
            txtButton.setTypeface(txtButton.getTypeface(), Typeface.BOLD);
            //Image View imgStar
            ImageView imgStar = new ImageView(this);
            imgStar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    (int)getResources().getDimension(R.dimen.teaStar_height),1));
            imgStar.setBackgroundColor(getHexCodeFromTea(t));
            imgStar.setClickable(true);
            imgStar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final long id = tea_id;
                    Tea t = ApplicationHelper.getDB(getBaseContext()).teaDao().getById(id);
                    ApplicationHelper.getDB(getBaseContext()).teaDao().setFavourite(id,!t.getFavourite());
                    t.setFavourite(!t.getFavourite());
                    ImageView imgStar = (ImageView)v;
                    if(t.getFavourite()) {
                        final Drawable starDrawable = DrawableCompat.wrap(getDrawable(R.drawable.ic_star_black_24dp));
                        DrawableCompat.setTint(starDrawable,getResources().getColor(android.R.color.background_light,null));
                        imgStar.setImageDrawable(starDrawable);
                    }
                    else {
                        final Drawable starDrawable = DrawableCompat.wrap(getDrawable(R.drawable.ic_star_border_black_24dp));
                        DrawableCompat.setTint(starDrawable,getResources().getColor(android.R.color.background_light,null));
                        imgStar.setImageDrawable(starDrawable);
                    }
                }
            });
            if(t.getFavourite()) {
                final Drawable starDrawable = DrawableCompat.wrap(getDrawable(R.drawable.ic_star_black_24dp));
                DrawableCompat.setTint(starDrawable,getResources().getColor(android.R.color.background_light,null));
                imgStar.setImageDrawable(starDrawable);
            }
            else {
                final Drawable starDrawable = DrawableCompat.wrap(getDrawable(R.drawable.ic_star_border_black_24dp));
                DrawableCompat.setTint(starDrawable,getResources().getColor(android.R.color.background_light,null));
                imgStar.setImageDrawable(starDrawable);
            }
            headerLayout.addView(imgTeaPicture,0);
            headerLayout.addView(txtButton,1);
            headerLayout.addView(imgStar,2);
            //TextArea
            LinearLayout layoutDescription = new LinearLayout(this);
            layoutDescription.setLayoutParams(new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,1));
            layoutDescription.setOrientation(LinearLayout.HORIZONTAL);
            layoutDescription.setVisibility(View.GONE);
            layoutDescription.setBackgroundColor(Color.parseColor("#FAFAFA"));//#FAFAFA default android background
            TextView txtDescription = new TextView(this);
            LinearLayout.LayoutParams txtDescription_lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            txtDescription_lp.setMargins((int)getResources().getDimension(R.dimen.txtDescription_marginLeft),0,(int)getResources().getDimension(R.dimen.txtDescription_marginRight),0);
            txtDescription.setLayoutParams(txtDescription_lp);
            int txtDescription_padding = (int)getResources().getDimension(R.dimen.txtDescription_padding);
            txtDescription.setPadding(txtDescription_padding,txtDescription_padding,txtDescription_padding,txtDescription_padding);
            txtDescription.setBackground(getDrawable(R.drawable.borders));
            txtDescription.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            txtDescription.setMaxLines(10);
            txtDescription.setTextSize(16);
            txtDescription.setText(t.getDescription(lang));
            layoutDescription.addView(txtDescription);
            //Chronometer Layout
            LinearLayout chronoLayout = new LinearLayout(this);
            LinearLayout.LayoutParams chronoLayout_lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,1);
            chronoLayout_lp.setMargins(
                    (int)getResources().getDimension(R.dimen.layoutChrono_marginLeft),
                    0,
                    (int)getResources().getDimension(R.dimen.layoutChrono_marginRight),
                    (int)getResources().getDimension(R.dimen.layoutChrono_marginBottom));
            chronoLayout.setLayoutParams(chronoLayout_lp);
            chronoLayout.setBackground(getButtonRadiusFromTea(t));
            chronoLayout.setOrientation(LinearLayout.HORIZONTAL);
            chronoLayout.setVisibility(View.GONE);
            //txtView BrewTimer
            TextView txtBrewTimer = new TextView(this);
            LinearLayout.LayoutParams txtBrewTimer_lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,1);
            int txtBrewerTimer_margin = (int)getResources().getDimension(R.dimen.txtBrew_margin);
            txtBrewTimer_lp.setMargins(txtBrewerTimer_margin,txtBrewerTimer_margin,txtBrewerTimer_margin,txtBrewerTimer_margin);
            txtBrewTimer.setLayoutParams(txtBrewTimer_lp);
            txtBrewTimer.setText(getResources().getString(R.string.brew_timer));
            txtBrewTimer.setTextColor(getTextColorFromTea(t));
            txtBrewTimer.setTypeface(txtBrewTimer.getTypeface(),Typeface.BOLD);
            //Chronometer
            final TextView txtChronometer = new TextView(this);
            LinearLayout.LayoutParams chronometer_lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,1);
            int chronometer_margin = (int)getResources().getDimension(R.dimen.chronoBrew_margin);
            chronometer_lp.setMargins(0,chronometer_margin,chronometer_margin,chronometer_margin);
            txtChronometer.setLayoutParams(chronometer_lp);
            txtChronometer.setTextColor(getTextColorFromTea(t));
            txtChronometer.setTypeface(txtChronometer.getTypeface(),Typeface.BOLD);
            txtChronometer.setText(ApplicationHelper.formatInfusionTime(t.getInfusionTime()));
            //Chronometer button
            final ImageView imgChrono = new ImageView(this);
            LinearLayout.LayoutParams imgChrono_lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    (int)getResources().getDimension(R.dimen.imgChrono_height),1);
            int imgChrono_margin = (int)getResources().getDimension(R.dimen.imgChrono_margin);
            imgChrono_lp.setMargins(imgChrono_margin,imgChrono_margin,imgChrono_margin,imgChrono_margin);
            imgChrono.setLayoutParams(imgChrono_lp);
            imgChrono.setClickable(true);
            final Drawable chronoDrawable = DrawableCompat.wrap(getDrawable(R.drawable.ic_timer_black_24dp));
            DrawableCompat.setTint(chronoDrawable,getResources().getColor(android.R.color.background_light,null));
            final long infusionTimeInMS = t.getInfusionTime() * 1000;
            imgChrono.setImageDrawable(chronoDrawable);
            imgChrono.setActivated(true);
            final CountDownTimer chrono = new CountDownTimer(infusionTimeInMS,1000){
                public void onTick(long millisUntilFinished) {
                    txtChronometer.setText(ApplicationHelper.formatInfusionTime(millisUntilFinished/1000));
                }

                public void onFinish() {
                    imgChrono.setActivated(true);
                    imgChrono.setClickable(true);
                    txtChronometer.setText(ApplicationHelper.formatInfusionTime(infusionTimeInMS / 1000));

                    if(!mustNotify)
                        return;

                    NotificationChannel channel = new NotificationChannel(getString(R.string.channel_name), "Teatime", NotificationManager.IMPORTANCE_HIGH);
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getBaseContext(), getString(R.string.channel_name))
                            .setSmallIcon(R.drawable.ic_local_cafe_black_24dp)
                            .setContentTitle(getResources().getString(R.string.notification_title))
                            .setContentText(String.format(getResources().getString(R.string.notification_text),tea_name))
                            .setChannelId(channel.getId())
                            .setDefaults(Notification.DEFAULT_ALL);


                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getBaseContext());
                    notificationManager.notify((int)tea_id,notificationBuilder.build());
                }

            };
            imgChrono.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View imgB) {

                    if(imgChrono.isActivated()){
                        chrono.start();
                        imgChrono.setActivated(false);
                    }
                    else{
                        chrono.cancel();
                        imgChrono.setActivated(true);
                    }
                    txtChronometer.setText(ApplicationHelper.formatInfusionTime((infusionTimeInMS/1000)));
                }
            });
            chronoLayout.addView(txtBrewTimer,0);
            chronoLayout.addView(txtChronometer,1);
            chronoLayout.addView(imgChrono,2);

            rowLayout.addView(headerLayout,0);
            rowLayout.addView(layoutDescription,1);
            rowLayout.addView(chronoLayout,2);

            container.addView(rowLayout);
        }
    }

    /**
     * Retoune le Drawable avec les coins ronds de la partie du temps d'infusion
     * @param t objet Tea
     * @return Drawable avec les coins ronds
     */
    public Drawable getButtonRadiusFromTea(Tea t) {
        switch (TeaType.getStatusValue(t.getTeaType())) {
            case 1:
                return getResources().getDrawable(R.drawable.buttonradius_white,null);
            case 2:
                return getResources().getDrawable(R.drawable.buttonradius_green,null);
            case 4:
                return getResources().getDrawable(R.drawable.buttonradius_blue,null);
            case 8:
                return getResources().getDrawable(R.drawable.buttonradius_red,null);
            case 16:
                return getResources().getDrawable(R.drawable.buttonradius_purple,null);
            default:
                return getResources().getDrawable(R.drawable.buttonradius_white,null);
        }
    }

    /**
     * Retourne la couleur associé au type du thé
     * @param t objet Tea
     * @return La couleur associé au type de thé
     */
    public int getHexCodeFromTea(Tea t) {
        switch (TeaType.getStatusValue(t.getTeaType())) {
            case 1:
                return getResources().getColor(R.color.whiteTea,null);
            case 2:
                return getResources().getColor(R.color.greenTea,null);
            case 4:
                return getResources().getColor(R.color.blueTea,null);
            case 8:
                return getResources().getColor(R.color.blackTea,null);
            case 16:
                return getResources().getColor(R.color.purpleTea,null);
            default:
                return Color.BLACK;
        }
    }

    /**
     * Retourne la couleur du texte en fonction du type de thé
     * @param t objet Tea
     * @return Color ID
     */
    public int getTextColorFromTea(Tea t) {
        return Color.WHITE;
    }

    //Filter
    /**
     * Désérialise le Filtre
     * @return Filtre désérialisé
     */
    public Filter loadFilter(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String filterJSON = sp.getString(getString(R.string.filter_preferences_key), null);
        Filter f;
        if (filterJSON == null) {
            f = new Filter(null, EnumSet.of(TeaType.ALL), EnumSet.of(GoodWithType.ALL),EnumSet.of(HealthPropertyType.ALL));
        }
        else {
            f = (Filter) new Gson().fromJson(filterJSON,Filter.class);
        }
        return f;
    }

    /**
     * Force la création d'un nouvel objet Filter
     */
    public void saveFilter() {saveFilter(null);}

    /**
     * Créer ou met à jour l'objet Filtre et le sérialise dans les Preferences en JSON
     * @param f Le Filtre quit doit être mis à jour
     */
    public void saveFilter(Filter f){
        Menu m = navigationView.getMenu();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //TeaType
        if (f == null)
        {
            EnumSet<TeaType> teaTypes = EnumSet.noneOf(TeaType.class);
            if(m.findItem(R.id.nav_type_vert).isChecked()) teaTypes.add(TeaType.GREEN);
            if(m.findItem(R.id.nav_type_bleu).isChecked()) teaTypes.add(TeaType.OOLONG);
            if(m.findItem(R.id.nav_type_rouge).isChecked()) teaTypes.add(TeaType.BLACK);
            if(m.findItem(R.id.nav_type_blanc).isChecked()) teaTypes.add(TeaType.WHITE);
            if(m.findItem(R.id.nav_type_sombre).isChecked()) teaTypes.add(TeaType.FERMENTED);
            if(!m.findItem(R.id.nav_type_vert).isVisible()) teaTypes = EnumSet.of(TeaType.ALL);
            //GoodWith
            EnumSet<GoodWithType> goodWithTypes = EnumSet.noneOf(GoodWithType.class);
            if(m.findItem(R.id.nav_good_sucre).isChecked()) goodWithTypes.add(GoodWithType.SUGAR);
            if(m.findItem(R.id.nav_good_lait).isChecked()) goodWithTypes.add(GoodWithType.MILK);
            if(m.findItem(R.id.nav_good_autre).isChecked()) goodWithTypes.add(GoodWithType.OTHER);
            if(!m.findItem(R.id.nav_good_autre).isVisible()) goodWithTypes = EnumSet.of(GoodWithType.ALL);
            //HealthProperty
            EnumSet<HealthPropertyType> healthPropertyTypes = EnumSet.noneOf(HealthPropertyType.class);
            if(m.findItem(R.id.nav_prop_caffeine).isChecked()) healthPropertyTypes.add(HealthPropertyType.CAFFEINE);
            if(m.findItem(R.id.nav_prop_flame).isChecked()) healthPropertyTypes.add(HealthPropertyType.ANTI_INFLAMMATORY);
            if(!m.findItem(R.id.nav_prop_flame).isVisible()) healthPropertyTypes = EnumSet.of(HealthPropertyType.ALL);

            String filterJSON = sp.getString(getString(R.string.filter_preferences_key), null);
            if(filterJSON == null) {
                f = new Filter(null, teaTypes, goodWithTypes, healthPropertyTypes);
            }
            else {
                f = (Filter) new Gson().fromJson(filterJSON,Filter.class);
                f.setTeaType(teaTypes);
                f.setGoodWith(goodWithTypes);
                f.setHealthProperty(healthPropertyTypes);
            }
        }
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(getString(R.string.filter_preferences_key),new Gson().toJson(f));
        edit.commit();
    }

    //UI
    /**
     * Met à jour l'état du menu à défilement horizontal
     */
    private void updateSideMenuState() {
        Menu m = navigationView.getMenu();
        if (m.findItem(R.id.nav_type_vert).isChecked() ||
            m.findItem(R.id.nav_type_bleu).isChecked() ||
            m.findItem(R.id.nav_type_rouge).isChecked() ||
            m.findItem(R.id.nav_type_blanc).isChecked() ||
            m.findItem(R.id.nav_type_sombre).isChecked()){
            m.findItem(R.id.nav_type_blanc).setVisible(true);
            m.findItem(R.id.nav_type_bleu).setVisible(true);
            m.findItem(R.id.nav_type_rouge).setVisible(true);
            m.findItem(R.id.nav_type_vert).setVisible(true);
            m.findItem(R.id.nav_type_sombre).setVisible(true);
        }
        else{
            m.findItem(R.id.nav_type_blanc).setVisible(false);
            m.findItem(R.id.nav_type_bleu).setVisible(false);
            m.findItem(R.id.nav_type_rouge).setVisible(false);
            m.findItem(R.id.nav_type_vert).setVisible(false);
            m.findItem(R.id.nav_type_sombre).setVisible(false);
        }
        if( m.findItem(R.id.nav_prop_caffeine).isChecked() ||
            m.findItem(R.id.nav_prop_flame).isChecked()) {
            m.findItem(R.id.nav_prop_flame).setVisible(true);
            m.findItem(R.id.nav_prop_caffeine).setVisible(true);
        }
        else {
            m.findItem(R.id.nav_prop_flame).setVisible(false);
            m.findItem(R.id.nav_prop_caffeine).setVisible(false);
        }
        if( m.findItem(R.id.nav_good_sucre).isChecked() ||
            m.findItem(R.id.nav_good_lait).isChecked() ||
            m.findItem(R.id.nav_good_autre).isChecked()) {
            m.findItem(R.id.nav_good_sucre).setVisible(true);
            m.findItem(R.id.nav_good_lait).setVisible(true);
            m.findItem(R.id.nav_good_autre).setVisible(true);
        }
        else {
            m.findItem(R.id.nav_good_sucre).setVisible(false);
            m.findItem(R.id.nav_good_lait).setVisible(false);
            m.findItem(R.id.nav_good_autre).setVisible(false);
        }
    }

    /**
     * Affiche ou cache le détail d'un thé
     * @param v La view associé au thé
     */
    public void teaTextClick(View v) {
        //BUTTON STUB
        LinearLayout parentLayout = (LinearLayout)v.getParent().getParent();
        LinearLayout descriptionLayout = (LinearLayout) parentLayout.getChildAt(1);
        LinearLayout linearLayout = (LinearLayout)parentLayout.getChildAt(2);
        if(descriptionLayout.getVisibility() == View.GONE){
            descriptionLayout.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.VISIBLE);
        }
        else{
            descriptionLayout.setVisibility(View.GONE);
            linearLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Set Tint for a menu icon
     * @param item MenuItem qui contient un Drawable
     * @param color ID de la couleur que l'on veut appliquer au Drawable
     */
    private void tintMenuIcon(MenuItem item, @ColorRes int color) {
        Drawable normalDrawable = item.getIcon();
        Drawable wrapDrawable = DrawableCompat.wrap(normalDrawable);
        DrawableCompat.setTint(wrapDrawable, this.getResources().getColor(color,null));

        item.setIcon(wrapDrawable);
    }












}
