package com.echo.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();

        window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
        window.setNavigationBarColor(android.graphics.Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);

            window.getInsetsController().hide(
                    WindowInsets.Type.statusBars()
            );

            window.getInsetsController().setSystemBarsBehavior(
                    WindowInsets.Controller.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        } else {

            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }

        webView = new WebView(this);

        WebSettings settings =
                webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        webView.setBackgroundColor(
                android.graphics.Color.rgb(5, 6, 10)
        );

        webView.setWebViewClient(
                new WebViewClient()
        );

        webView.addJavascriptInterface(
                new AndroidBridge(),
                "Android"
        );

        webView.loadUrl(
                "file:///android_asset/index.html"
        );

        setContentView(webView);
    }


    public class AndroidBridge {

        @JavascriptInterface
        public void getApps() {

            runOnUiThread(() -> {

                String json =
                        getInstalledApps();

                webView.evaluateJavascript(
                        "receiveApps(" +
                                JSONObject.quote(json) +
                                ")",
                        null
                );

            });
        }


        @JavascriptInterface
        public void launchApp(String packageName) {

            try {

                PackageManager pm =
                        getPackageManager();

                Intent launchIntent =
                        pm.getLaunchIntentForPackage(
                                packageName
                        );

                if (launchIntent != null) {

                    launchIntent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                    );

                    startActivity(launchIntent);
                }

            } catch (Exception e) {

                e.printStackTrace();

            }
        }
    }


    private String getInstalledApps() {

        JSONArray array =
                new JSONArray();

        PackageManager pm =
                getPackageManager();

        Intent intent =
                new Intent(
                        Intent.ACTION_MAIN,
                        null
                );

        intent.addCategory(
                Intent.CATEGORY_LAUNCHER
        );

        List<ResolveInfo> results =
                pm.queryIntentActivities(
                        intent,
                        PackageManager.MATCH_ALL
                );

        List<AppItem> apps =
                new ArrayList<>();

        for (ResolveInfo info : results) {

            try {

                String packageName =
                        info.activityInfo.packageName;

                ApplicationInfo appInfo =
                        info.activityInfo.applicationInfo;

                String name =
                        info.loadLabel(pm).toString();

                Drawable icon =
                        info.loadIcon(pm);

                String encodedIcon =
                        drawableToBase64(icon);

                apps.add(
                        new AppItem(
                                name,
                                packageName,
                                encodedIcon
                        )
                );

            } catch (Exception ignored) {
            }
        }

        Collections.sort(
                apps,
                new Comparator<AppItem>() {

                    @Override
                    public int compare(
                            AppItem a,
                            AppItem b
                    ) {

                        return a.name.compareToIgnoreCase(
                                b.name
                        );
                    }
                }
        );

        for (AppItem app : apps) {

            try {

                JSONObject object =
                        new JSONObject();

                object.put(
                        "name",
                        app.name
                );

                object.put(
                        "package",
                        app.packageName
                );

                object.put(
                        "icon",
                        app.icon
                );

                array.put(object);

            } catch (Exception ignored) {
            }
        }

        return array.toString();
    }


    private String drawableToBase64(
            Drawable drawable
    ) {

        try {

            int width =
                    Math.max(
                            1,
                            drawable.getIntrinsicWidth()
                    );

            int height =
                    Math.max(
                            1,
                            drawable.getIntrinsicHeight()
                    );

            width = Math.min(width,192);
            height = Math.min(height,192);

            Bitmap bitmap =
                    Bitmap.createBitmap(
                            width,
                            height,
                            Bitmap.Config.ARGB_8888
                    );

            Canvas canvas =
                    new Canvas(bitmap);

            drawable.setBounds(
                    0,
                    0,
                    canvas.getWidth(),
                    canvas.getHeight()
            );

            drawable.draw(canvas);

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            bitmap.compress(
                    Bitmap.CompressFormat.PNG,
                    90,
                    output
            );

            return Base64.encodeToString(
                    output.toByteArray(),
                    Base64.NO_WRAP
            );

        } catch (Exception e) {

            return "";

        }
    }


    private static class AppItem {

        String name;
        String packageName;
        String icon;

        AppItem(
                String name,
                String packageName,
                String icon
        ) {

            this.name =
                    name;

            this.packageName =
                    packageName;

            this.icon =
                    icon;
        }
    }


    @Override
    public void onBackPressed() {

        if (webView != null) {

            webView.evaluateJavascript(
                    "closeDrawer()",
                    null
            );

        } else {

            super.onBackPressed();

        }
    }
}
