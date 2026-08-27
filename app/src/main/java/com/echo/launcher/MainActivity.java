package com.echo.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    LinearLayout root;
    LinearLayout appsContainer;
    TextView clock;
    TextView date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.BLACK);

        buildHome();
    }

    private void buildHome() {

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(32, 70, 32, 30);
        root.setBackgroundColor(Color.rgb(10, 10, 14));

        // CLOCK
        clock = new TextView(this);
        clock.setTextColor(Color.WHITE);
        clock.setTextSize(52);
        clock.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        clock.setGravity(Gravity.CENTER);

        root.addView(clock, new LinearLayout.LayoutParams(
                -1, 100
        ));

        // DATE
        date = new TextView(this);
        date.setTextColor(Color.LTGRAY);
        date.setTextSize(17);
        date.setGravity(Gravity.CENTER);

        root.addView(date, new LinearLayout.LayoutParams(
                -1, 50
        ));

        // SEARCH BAR
        EditText search = new EditText(this);
        search.setHint("Search apps");
        search.setHintTextColor(Color.GRAY);
        search.setTextColor(Color.WHITE);
        search.setSingleLine(true);
        search.setPadding(30, 0, 30, 0);
        search.setBackgroundColor(Color.rgb(30, 30, 38));

        LinearLayout.LayoutParams searchParams =
                new LinearLayout.LayoutParams(-1, 60);

        searchParams.setMargins(0, 35, 0, 25);
        root.addView(search, searchParams);

        // APP AREA
        ScrollView scroll = new ScrollView(this);

        appsContainer = new LinearLayout(this);
        appsContainer.setOrientation(LinearLayout.VERTICAL);
        appsContainer.setGravity(Gravity.CENTER_HORIZONTAL);

        scroll.addView(appsContainer);

        root.addView(scroll, new LinearLayout.LayoutParams(
                -1, 0, 1
        ));

        setContentView(root);

        updateClock();
        loadApps();

        search.setOnEditorActionListener((v, actionId, event) -> {
            filterApps(search.getText().toString());
            return false;
        });
    }

    private void updateClock() {

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                runOnUiThread(() -> {

                    Date now = new Date();

                    clock.setText(
                            new SimpleDateFormat(
                                    "HH:mm",
                                    Locale.getDefault()
                            ).format(now)
                    );

                    date.setText(
                            new SimpleDateFormat(
                                    "EEEE, d MMMM",
                                    Locale.getDefault()
                            ).format(now)
                    );
                });

            }
        }, 0, 1000);
    }

    private void loadApps() {

        PackageManager pm = getPackageManager();

        List<ApplicationInfo> apps =
                pm.getInstalledApplications(
                        PackageManager.GET_META_DATA
                );

        Collections.sort(apps, (a, b) ->
                pm.getApplicationLabel(a)
                        .toString()
                        .compareToIgnoreCase(
                                pm.getApplicationLabel(b).toString()
                        )
        );

        for (ApplicationInfo app : apps) {

            if (pm.getLaunchIntentForPackage(
                    app.packageName) != null) {

                addApp(app);
            }
        }
    }

    private void addApp(ApplicationInfo app) {

        PackageManager pm = getPackageManager();

        TextView button = new TextView(this);

        button.setText(
                pm.getApplicationLabel(app).toString()
        );

        button.setTextColor(Color.WHITE);
        button.setTextSize(18);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setPadding(30, 0, 30, 0);

        button.setBackgroundColor(
                Color.rgb(24, 24, 30)
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1, 70
                );

        params.setMargins(0, 5, 0, 5);

        appsContainer.addView(button, params);

        button.setOnClickListener(v -> {

            Intent launch =
                    pm.getLaunchIntentForPackage(
                            app.packageName
                    );

            if (launch != null) {
                startActivity(launch);
            }
        });
    }

    private void filterApps(String text) {

        appsContainer.removeAllViews();

        PackageManager pm = getPackageManager();

        List<ApplicationInfo> apps =
                pm.getInstalledApplications(
                        PackageManager.GET_META_DATA
                );

        for (ApplicationInfo app : apps) {

            String name =
                    pm.getApplicationLabel(app)
                            .toString();

            if (name.toLowerCase(Locale.getDefault())
                    .contains(text.toLowerCase(Locale.getDefault()))) {

                if (pm.getLaunchIntentForPackage(
                        app.packageName) != null) {

                    addApp(app);
                }
            }
        }
    }
}