package com.echo.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    LinearLayout root;
    LinearLayout appGrid;
    EditText searchBox;
    TextView clock;
    TextView date;
    TextView drawerTitle;

    ArrayList<ApplicationInfo> allApps = new ArrayList<>();
    PackageManager pm;

    int white = Color.WHITE;
    int light = Color.rgb(190, 190, 200);
    int panel = Color.rgb(28, 28, 36);
    int panel2 = Color.rgb(36, 36, 46);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.BLACK);

        pm = getPackageManager();

        buildHome();
        loadApps();
        startClock();
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radius);
        return g;
    }

    private TextView text(String value, float size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    private void buildHome() {

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 65, 28, 25);
        root.setBackgroundColor(Color.rgb(9, 9, 14));

        // TOP CLOCK
        clock = text("00:00", 54, white);
        clock.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        root.addView(clock, new LinearLayout.LayoutParams(
                -1, 85
        ));

        // DATE
        date = text("Loading...", 16, light);

        root.addView(date, new LinearLayout.LayoutParams(
                -1, 38
        ));

        // SEARCH
        searchBox = new EditText(this);
        searchBox.setSingleLine(true);
        searchBox.setHint("🔍  Search apps");
        searchBox.setHintTextColor(Color.rgb(145,145,155));
        searchBox.setTextColor(white);
        searchBox.setTextSize(16);
        searchBox.setPadding(25, 0, 25, 0);
        searchBox.setBackground(rounded(panel, 45));

        LinearLayout.LayoutParams searchParams =
                new LinearLayout.LayoutParams(-1, 62);

        searchParams.setMargins(0, 25, 0, 22);

        root.addView(searchBox, searchParams);

        // QUICK FAVORITES
        LinearLayout favorites = new LinearLayout(this);
        favorites.setOrientation(LinearLayout.HORIZONTAL);
        favorites.setGravity(Gravity.CENTER);

        String[] favoriteNames = {
                "Phone",
                "Messages",
                "Camera",
                "Settings"
        };

        for (String name : favoriteNames) {

            TextView fav = text(name, 12, white);
            fav.setBackground(rounded(panel2, 28));
            fav.setPadding(12, 0, 12, 0);

            LinearLayout.LayoutParams fp =
                    new LinearLayout.LayoutParams(0, 62, 1);

            fp.setMargins(4, 0, 4, 0);

            favorites.addView(fav, fp);

            fav.setOnClickListener(v -> launchByName(name));
        }

        root.addView(favorites);

        // DRAWER TITLE
        drawerTitle = text("YOUR APPS", 13, light);
        drawerTitle.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        drawerTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(-1, 50);

        titleParams.setMargins(5, 15, 5, 0);

        root.addView(drawerTitle, titleParams);

        // APP SCROLL AREA
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        appGrid = new LinearLayout(this);
        appGrid.setOrientation(LinearLayout.VERTICAL);

        scroll.addView(appGrid);

        root.addView(scroll, new LinearLayout.LayoutParams(
                -1, 0, 1
        ));

        // SWIPE HINT
        TextView hint = text("↑  Swipe / scroll for all apps", 13, light);

        root.addView(hint, new LinearLayout.LayoutParams(
                -1, 35
        ));

        setContentView(root);

        searchBox.addTextChangedListener(
                new android.text.TextWatcher() {

                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {}

                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        filterApps(s.toString());
                    }

                    public void afterTextChanged(
                            android.text.Editable e) {}
                }
        );

        // SIMPLE PRESS ANIMATION
        searchBox.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate()
                        .scaleX(0.98f)
                        .scaleY(0.98f)
                        .setDuration(80)
                        .start();
            }

            if (event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_CANCEL) {

                v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(80)
                        .start();
            }

            return false;
        });
    }

    private void startClock() {

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

        List<ApplicationInfo> apps =
                pm.getInstalledApplications(
                        PackageManager.GET_META_DATA
                );

        for (ApplicationInfo app : apps) {

            if (pm.getLaunchIntentForPackage(
                    app.packageName) != null) {

                allApps.add(app);
            }
        }

        Collections.sort(
                allApps,
                (a, b) -> getName(a).compareToIgnoreCase(
                        getName(b)
                )
        );

        displayApps(allApps);
    }

    private String getName(ApplicationInfo app) {

        return pm.getApplicationLabel(app)
                .toString();
    }

    private void displayApps(
            List<ApplicationInfo> apps) {

        appGrid.removeAllViews();

        String currentLetter = "";

        for (ApplicationInfo app : apps) {

            String name = getName(app);

            String first =
                    name.substring(0, 1)
                            .toUpperCase(Locale.getDefault());

            if (!first.equals(currentLetter)) {

                currentLetter = first;

                TextView letter =
                        text(first, 14, Color.rgb(120, 190, 255));

                letter.setGravity(
                        Gravity.LEFT |
                        Gravity.CENTER_VERTICAL
                );

                letter.setTypeface(
                        Typeface.DEFAULT,
                        Typeface.BOLD
                );

                LinearLayout.LayoutParams lp =
                        new LinearLayout.LayoutParams(
                                -1, 38
                        );

                lp.setMargins(8, 8, 8, 0);

                appGrid.addView(letter, lp);
            }

            addApp(app);
        }
    }

    private void addApp(ApplicationInfo app) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(15, 0, 15, 0);
        row.setBackground(rounded(panel, 24));

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        -1, 68
                );

        rowParams.setMargins(0, 4, 0, 4);

        // ICON
        ImageView icon = new ImageView(this);

        try {
            icon.setImageDrawable(
                    pm.getApplicationIcon(app)
            );
        } catch (Exception ignored) {}

        row.addView(icon, new LinearLayout.LayoutParams(
                45, 45
        ));

        // NAME
        TextView name = text(
                getName(app),
                16,
                white
        );

        name.setGravity(
                Gravity.LEFT |
                Gravity.CENTER_VERTICAL
        );

        name.setPadding(18, 0, 0, 0);

        row.addView(name, new LinearLayout.LayoutParams(
                0, -1, 1
        ));

        appGrid.addView(row, rowParams);

        // OPEN ANIMATION
        row.setOnClickListener(v -> {

            v.animate()
                    .scaleX(0.96f)
                    .scaleY(0.96f)
                    .setDuration(70)
                    .withEndAction(() -> {

                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(70)
                                .start();

                        Intent intent =
                                pm.getLaunchIntentForPackage(
                                        app.packageName
                                );

                        if (intent != null) {
                            startActivity(intent);
                        }

                    }).start();
        });
    }

    private void filterApps(String query) {

        if (query.trim().isEmpty()) {

            drawerTitle.setText("YOUR APPS");

            displayApps(allApps);

            return;
        }

        ArrayList<ApplicationInfo> results =
                new ArrayList<>();

        for (ApplicationInfo app : allApps) {

            String name = getName(app);

            if (name.toLowerCase(
                    Locale.getDefault()
            ).contains(
                    query.toLowerCase(
                            Locale.getDefault()
                    ))) {

                results.add(app);
            }
        }

        drawerTitle.setText(
                results.size() + " RESULTS"
        );

        displayApps(results);
    }

    private void launchByName(String wanted) {

        for (ApplicationInfo app : allApps) {

            String name = getName(app);

            if (name.toLowerCase(Locale.getDefault())
                    .contains(
                            wanted.toLowerCase(
                                    Locale.getDefault()
                            ))) {

                Intent intent =
                        pm.getLaunchIntentForPackage(
                                app.packageName
                        );

                if (intent != null) {
                    startActivity(intent);
                    return;
                }
            }
        }
    }
}        root.setOrientation(LinearLayout.VERTICAL);
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
