
Those **must NOT be pasted into `MainActivity.java`**. That is why Java reports `illegal character: '`'`.

Also, the earlier `Drawable.getBitmap()` problem needs to be avoided completely.

Let's make this clean: **replace the entire contents of `MainActivity.java` with the code below.** Do not add anything before `package com.echo.launcher;` and do not add anything after the final `}`.

```java
package com.echo.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private LinearLayout root;
    private LinearLayout appContainer;
    private TextView clock;
    private TextView date;
    private TextView greeting;
    private EditText searchBox;

    private final Handler handler = new Handler();
    private final List<ApplicationInfo> allApps = new ArrayList<>();

    private float downY;

    private static final int BG = Color.rgb(5, 6, 11);
    private static final int CARD = Color.rgb(19, 22, 32);
    private static final int CARD2 = Color.rgb(27, 31, 44);
    private static final int TEXT = Color.WHITE;
    private static final int MUTED = Color.rgb(155, 162, 178);
    private static final int ACCENT = Color.rgb(95, 115, 255);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();

        window.setStatusBarColor(BG);
        window.setNavigationBarColor(BG);

        window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        buildHome();
        updateClock();
        loadApps();
    }

    private int dp(float value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics()
                        .density + 0.5f
        );
    }

    private GradientDrawable roundedBackground(
            int color,
            float radius
    ) {
        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));

        return drawable;
    }

    private GradientDrawable strokeBackground(
            int color,
            int strokeColor,
            int strokeWidth,
            float radius
    ) {
        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        drawable.setStroke(
                dp(strokeWidth),
                strokeColor
        );

        return drawable;
    }

    private TextView makeText(
            String text,
            float size,
            int color
    ) {
        TextView tv =
                new TextView(this);

        tv.setText(text);
        tv.setTextSize(size);
        tv.setTextColor(color);
        tv.setGravity(Gravity.CENTER_VERTICAL);

        return tv;
    }

    private void buildHome() {

        root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                dp(20),
                dp(40),
                dp(20),
                dp(16)
        );

        root.setBackgroundColor(BG);

        /*
         * TOP HEADER
         */

        LinearLayout header =
                new LinearLayout(this);

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView logo =
                makeText(
                        "ECHO",
                        22,
                        TEXT
                );

        logo.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        header.addView(
                logo,
                new LinearLayout.LayoutParams(
                        0,
                        dp(48),
                        1
                )
        );

        TextView status =
                makeText(
                        "●  READY",
                        11,
                        Color.rgb(100, 230, 160)
                );

        status.setGravity(
                Gravity.CENTER
        );

        header.addView(
                status,
                new LinearLayout.LayoutParams(
                        dp(90),
                        dp(38)
                )
        );

        root.addView(
                header,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(55)
                )
        );

        /*
         * CLOCK
         */

        clock =
                makeText(
                        "",
                        58,
                        TEXT
                );

        clock.setTypeface(
                Typeface.DEFAULT,
                Typeface.NORMAL
        );

        clock.setGravity(
                Gravity.CENTER
        );

        root.addView(
                clock,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(76)
                )
        );

        /*
         * DATE
         */

        date =
                makeText(
                        "",
                        15,
                        MUTED
                );

        date.setGravity(
                Gravity.CENTER
        );

        root.addView(
                date,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(32)
                )
        );

        /*
         * GREETING
         */

        greeting =
                makeText(
                        "Welcome to Echo",
                        21,
                        TEXT
                );

        greeting.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        greeting.setGravity(
                Gravity.CENTER
        );

        root.addView(
                greeting,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(48)
                )
        );

        /*
         * SEARCH
         */

        searchBox =
                new EditText(this);

        searchBox.setSingleLine(true);
        searchBox.setTextColor(TEXT);
        searchBox.setHintTextColor(MUTED);
        searchBox.setHint(
                "Search your phone..."
        );

        searchBox.setTextSize(16);

        searchBox.setPadding(
                dp(20),
                0,
                dp(20),
                0
        );

        searchBox.setBackground(
                strokeBackground(
                        CARD,
                        Color.rgb(45, 50, 68),
                        1,
                        30
                )
        );

        LinearLayout.LayoutParams searchParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(56)
                );

        searchParams.setMargins(
                0,
                dp(14),
                0,
                dp(15)
        );

        root.addView(
                searchBox,
                searchParams
        );

        /*
         * SECTION TITLE
         */

        TextView section =
                makeText(
                        "YOUR APPS",
                        11,
                        MUTED
                );

        section.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        section.setPadding(
                dp(4),
                0,
                0,
                0
        );

        root.addView(
                section,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(28)
                )
        );

        /*
         * APP LIST
         */

        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(true);

        scroll.setVerticalScrollBarEnabled(false);

        appContainer =
                new LinearLayout(this);

        appContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        appContainer.setPadding(
                0,
                dp(4),
                0,
                dp(25)
        );

        scroll.addView(appContainer);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        /*
         * BOTTOM DOCK
         */

        LinearLayout dock =
                new LinearLayout(this);

        dock.setGravity(
                Gravity.CENTER
        );

        dock.setPadding(
                dp(8),
                dp(7),
                dp(8),
                dp(7)
        );

        dock.setBackground(
                strokeBackground(
                        CARD,
                        Color.rgb(42, 46, 62),
                        1,
                        32
                )
        );

        TextView home =
                makeText(
                        "⌂",
                        26,
                        TEXT
                );

        home.setGravity(
                Gravity.CENTER
        );

        dock.addView(
                home,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        TextView apps =
                makeText(
                        "◉",
                        25,
                        MUTED
                );

        apps.setGravity(
                Gravity.CENTER
        );

        dock.addView(
                apps,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        TextView settings =
                makeText(
                        "⚙",
                        23,
                        MUTED
                );

        settings.setGravity(
                Gravity.CENTER
        );

        dock.addView(
                settings,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        root.addView(
                dock,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(68)
                )
        );

        setContentView(root);

        /*
         * SEARCH
         */

        searchBox.setOnClickListener(
                v -> showAppDrawer()
        );

        searchBox.setOnEditorActionListener(
                (v, actionId, event) -> {

                    filterApps(
                            searchBox
                                    .getText()
                                    .toString()
                    );

                    return false;
                }
        );

        /*
         * SWIPE UP
         */

        root.setOnTouchListener(
                (v, event) -> {

                    if (
                            event.getAction()
                                    == MotionEvent.ACTION_DOWN
                    ) {
                        downY = event.getY();
                        return true;
                    }

                    if (
                            event.getAction()
                                    == MotionEvent.ACTION_UP
                    ) {

                        float distance =
                                downY - event.getY();

                        if (
                                distance > dp(80)
                        ) {
                            showAppDrawer();
                        }

                        return true;
                    }

                    return true;
                }
        );
    }

    private void updateClock() {

        handler.postDelayed(
                new Runnable() {

                    @Override
                    public void run() {

                        Date now =
                                new Date();

                        SimpleDateFormat timeFormat =
                                new SimpleDateFormat(
                                        "HH:mm",
                                        Locale.getDefault()
                                );

                        SimpleDateFormat dateFormat =
                                new SimpleDateFormat(
                                        "EEEE, d MMMM",
                                        Locale.getDefault()
                                );

                        if (clock != null) {
                            clock.setText(
                                    timeFormat.format(now)
                            );
                        }

                        if (date != null) {
                            date.setText(
                                    dateFormat.format(now)
                            );
                        }

                        if (greeting != null) {

                            int hour =
                                    now.getHours();

                            String message;

                            if (hour < 12) {
                                message =
                                        "Good morning";
                            } else if (hour < 18) {
                                message =
                                        "Good afternoon";
                            } else {
                                message =
                                        "Good evening";
                            }

                            greeting.setText(
                                    message + " • Echo"
                            );
                        }

                        handler.postDelayed(
                                this,
                                1000
                        );
                    }
                },
                0
        );
    }

    private void loadApps() {

        PackageManager pm =
                getPackageManager();

        List<ApplicationInfo> apps =
                pm.getInstalledApplications(
                        PackageManager.GET_META_DATA
                );

        allApps.clear();

        for (
                ApplicationInfo app :
                apps
        ) {

            Intent launch =
                    pm.getLaunchIntentForPackage(
                            app.packageName
                    );

            if (launch != null) {
                allApps.add(app);
            }
        }

        Collections.sort(
                allApps,
                (a, b) ->
                        pm.getApplicationLabel(a)
                                .toString()
                                .compareToIgnoreCase(
                                        pm.getApplicationLabel(b)
                                                .toString()
                                )
        );

        displayApps(allApps);
    }

    private void displayApps(
            List<ApplicationInfo> apps
    ) {

        if (appContainer == null) {
            return;
        }

        appContainer.removeAllViews();

        PackageManager pm =
                getPackageManager();

        for (
                ApplicationInfo app :
                apps
        ) {

            String name =
                    pm.getApplicationLabel(app)
                            .toString();

            LinearLayout item =
                    new LinearLayout(this);

            item.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            item.setGravity(
                    Gravity.CENTER_VERTICAL
            );

            item.setPadding(
                    dp(12),
                    dp(7),
                    dp(14),
                    dp(7)
            );

            item.setBackground(
                    strokeBackground(
                            CARD,
                            Color.rgb(35, 39, 54),
                            1,
                            22
                    )
            );

            /*
             * APP ICON
             */

            android.widget.ImageView icon =
                    new android.widget.ImageView(
                            this
                    );

            try {

                icon.setImageDrawable(
                        app.loadIcon(pm)
                );

            } catch (Exception ignored) {
            }

            icon.setScaleType(
                    android.widget.ImageView.ScaleType.CENTER_CROP
            );

            item.addView(
                    icon,
                    new LinearLayout.LayoutParams(
                            dp(48),
                            dp(48)
                    )
            );

            /*
             * APP NAME
             */

            TextView title =
                    makeText(
                            name,
                            16,
                            TEXT
                    );

            title.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );

            title.setPadding(
                    dp(15),
                    0,
                    dp(5),
                    0
            );

            item.addView(
                    title,
                    new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            1
                    )
            );

            /*
             * ARROW
             */

            TextView arrow =
                    makeText(
                            "›",
                            27,
                            MUTED
                    );

            arrow.setGravity(
                    Gravity.CENTER
            );

            item.addView(
                    arrow,
                    new LinearLayout.LayoutParams(
                            dp(35),
                            ViewGroup.LayoutParams.MATCH_PARENT
                    )
            );

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            dp(66)
                    );

            params.setMargins(
                    0,
                    dp(5),
                    0,
                    dp(5)
            );

            appContainer.addView(
                    item,
                    params
            );

            item.setOnClickListener(
                    v -> {

                        Intent launch =
                                pm.getLaunchIntentForPackage(
                                        app.packageName
                                );

                        if (launch != null) {

                            try {

                                startActivity(
                                        launch
                                );

                            } catch (Exception ignored) {
                            }
                        }
                    }
            );

            item.setOnTouchListener(
                    (v, event) -> {

                        if (
                                event.getAction()
                                        == MotionEvent.ACTION_DOWN
                        ) {

                            v.animate()
                                    .scaleX(.97f)
                                    .scaleY(.97f)
                                    .setDuration(80)
                                    .start();

                        } else if (
                                event.getAction()
                                        == MotionEvent.ACTION_UP
                                        ||
                                event.getAction()
                                        == MotionEvent.ACTION_CANCEL
                        ) {

                            v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .start();
                        }

                        return false;
                    }
            );
        }
    }

    private void filterApps(
            String query
    ) {

        if (query == null) {
            return;
        }

        String q =
                query.trim()
                        .toLowerCase(
                                Locale.getDefault()
                        );

        if (q.isEmpty()) {

            displayApps(
                    allApps
            );

            return;
        }

        PackageManager pm =
                getPackageManager();

        List<ApplicationInfo> filtered =
                new ArrayList<>();

        for (
                ApplicationInfo app :
                allApps
        ) {

            String name =
                    pm.getApplicationLabel(app)
                            .toString()
                            .toLowerCase(
                                    Locale.getDefault()
                            );

            if (name.contains(q)) {
                filtered.add(app);
            }
        }

        displayApps(filtered);
    }

    private void showAppDrawer() {

        if (appContainer == null) {
            return;
        }

        appContainer.setAlpha(0f);

        appContainer.setTranslationY(
                dp(60)
        );

        appContainer.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(350)
                .start();
    }

    @Override
    protected void onResume() {

        super.onResume();

        loadApps();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        handler.removeCallbacksAndMessages(
                null
        );
    }
}
