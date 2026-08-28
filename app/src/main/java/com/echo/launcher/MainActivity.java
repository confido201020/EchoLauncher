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

```
private LinearLayout root;
private LinearLayout appContainer;
private TextView clock;
private TextView date;
private EditText searchBox;

private final Handler handler = new Handler();
private final List<ApplicationInfo> allApps = new ArrayList<>();

private float downY;

private static final int BG = Color.rgb(7, 9, 15);
private static final int CARD = Color.rgb(20, 24, 34);
private static final int CARD_LIGHT = Color.rgb(28, 33, 46);
private static final int TEXT = Color.WHITE;
private static final int MUTED = Color.rgb(155, 162, 178);

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Window window = getWindow();
    window.setStatusBarColor(BG);
    window.setNavigationBarColor(BG);

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

private TextView makeText(
        String text,
        float size,
        int color
) {
    TextView tv = new TextView(this);

    tv.setText(text);
    tv.setTextSize(size);
    tv.setTextColor(color);
    tv.setGravity(Gravity.CENTER_VERTICAL);

    return tv;
}

private void buildHome() {

    root = new LinearLayout(this);

    root.setOrientation(
            LinearLayout.VERTICAL
    );

    root.setPadding(
            dp(22),
            dp(30),
            dp(22),
            dp(18)
    );

    root.setBackgroundColor(BG);

    TextView brand =
            makeText(
                    "ECHO",
                    26,
                    TEXT
            );

    brand.setTypeface(
            Typeface.DEFAULT,
            Typeface.BOLD
    );

    brand.setGravity(Gravity.CENTER);

    root.addView(
            brand,
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(48)
            )
    );

    clock =
            makeText(
                    "",
                    56,
                    TEXT
            );

    clock.setTypeface(
            Typeface.DEFAULT,
            Typeface.BOLD
    );

    clock.setGravity(Gravity.CENTER);

    root.addView(
            clock,
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(76)
            )
    );

    date =
            makeText(
                    "",
                    15,
                    MUTED
            );

    date.setGravity(Gravity.CENTER);

    root.addView(
            date,
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(32)
            )
    );

    searchBox =
            new EditText(this);

    searchBox.setSingleLine(true);
    searchBox.setTextColor(TEXT);
    searchBox.setHintTextColor(MUTED);
    searchBox.setHint("Search apps");
    searchBox.setTextSize(16);

    searchBox.setPadding(
            dp(20),
            0,
            dp(20),
            0
    );

    searchBox.setBackground(
            roundedBackground(
                    CARD,
                    60
            )
    );

    LinearLayout.LayoutParams searchParams =
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(56)
            );

    searchParams.setMargins(
            0,
            dp(20),
            0,
            dp(18)
    );

    root.addView(
            searchBox,
            searchParams
    );

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

    LinearLayout bottom =
            new LinearLayout(this);

    bottom.setGravity(Gravity.CENTER);
    bottom.setPadding(
            dp(8),
            dp(7),
            dp(8),
            dp(7)
    );

    bottom.setBackground(
            roundedBackground(
                    CARD,
                    35
            )
    );

    TextView homeButton =
            makeText(
                    "⌂   HOME",
                    14,
                    TEXT
            );

    homeButton.setGravity(
            Gravity.CENTER
    );

    bottom.addView(
            homeButton,
            new LinearLayout.LayoutParams(
                    0,
                    dp(50),
                    1
            )
    );

    TextView appsButton =
            makeText(
                    "▦   APPS",
                    14,
                    MUTED
            );

    appsButton.setGravity(
            Gravity.CENTER
    );

    bottom.addView(
            appsButton,
            new LinearLayout.LayoutParams(
                    0,
                    dp(50),
                    1
            )
    );

    root.addView(
            bottom,
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(64)
            )
    );

    setContentView(root);

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

    searchBox.setOnKeyListener(
            (v, keyCode, event) -> {

                filterApps(
                        searchBox
                                .getText()
                                .toString()
                );

                return false;
            }
    );

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
                            distance > dp(100)
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

                    SimpleDateFormat
                            timeFormat =
                            new SimpleDateFormat(
                                    "HH:mm",
                                    Locale.getDefault()
                            );

                    SimpleDateFormat
                            dateFormat =
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

        if (
                pm.getLaunchIntentForPackage(
                        app.packageName
                ) != null
        ) {

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

        TextView item =
                makeText(
                        "   " + name,
                        18,
                        TEXT
                );

        item.setTypeface(
                Typeface.DEFAULT,
                Typeface.NORMAL
        );

        item.setPadding(
                dp(12),
                0,
                dp(12),
                0
        );

        item.setBackground(
                roundedBackground(
                        CARD_LIGHT,
                        24
                )
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(58)
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
                        startActivity(launch);
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
                                .scaleX(0.97f)
                                .scaleY(0.97f)
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

        displayApps(allApps);

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
            dp(80)
    );

    appContainer.animate()
            .alpha(1f)
            .translationY(0)
            .setDuration(350)
            .start();
}

@Override
protected void onDestroy() {

    super.onDestroy();

    handler.removeCallbacksAndMessages(
            null
    );
}
```

}
