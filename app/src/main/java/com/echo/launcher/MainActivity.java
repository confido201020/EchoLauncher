package com.echo.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        webView.setBackgroundColor(0x00000000);
        webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        webView.setWebViewClient(new WebViewClient());

        webView.addJavascriptInterface(
                new AndroidBridge(),
                "Android"
        );

        setContentView(webView);

        webView.loadUrl("file:///android_asset/index.html");
    }

    public class AndroidBridge {

        @JavascriptInterface
        public void getApps() {

            runOnUiThread(() -> {

                try {

                    PackageManager pm = getPackageManager();

                    List<ApplicationInfo> apps =
                            pm.getInstalledApplications(
                                    PackageManager.GET_META_DATA
                            );

                    Collections.sort(
                            apps,
                            new Comparator<ApplicationInfo>() {
                                @Override
                                public int compare(
                                        ApplicationInfo a,
                                        ApplicationInfo b
                                ) {

                                    String nameA =
                                            pm.getApplicationLabel(a)
                                                    .toString();

                                    String nameB =
                                            pm.getApplicationLabel(b)
                                                    .toString();

                                    return nameA.compareToIgnoreCase(
                                            nameB
                                    );
                                }
                            }
                    );

                    JSONArray array = new JSONArray();

                    for (ApplicationInfo app : apps) {

                        Intent launch =
                                pm.getLaunchIntentForPackage(
                                        app.packageName
                                );

                        if (launch == null) {
                            continue;
                        }

                        JSONObject object = new JSONObject();

                        String name =
                                pm.getApplicationLabel(app)
                                        .toString();

                        object.put(
                                "name",
                                name
                        );

                        object.put(
                                "package",
                                app.packageName
                        );

                        Bitmap icon =
                                app.loadIcon(pm)
                                        .getBitmap();

                        ByteArrayOutputStream stream =
                                new ByteArrayOutputStream();

                        icon.compress(
                                Bitmap.CompressFormat.PNG,
                                100,
                                stream
                        );

                        String encoded =
                                Base64.encodeToString(
                                        stream.toByteArray(),
                                        Base64.NO_WRAP
                                );

                        object.put(
                                "icon",
                                encoded
                        );

                        array.put(object);
                    }

                    String json =
                            array.toString();

                    webView.evaluateJavascript(
                            "receiveApps(" +
                                    JSONObject.quote(json) +
                                    ");",
                            null
                    );

                } catch (Exception e) {

                    e.printStackTrace();
                }
            });
        }

        @JavascriptInterface
        public void launchApp(String packageName) {

            try {

                PackageManager pm =
                        getPackageManager();

                Intent launch =
                        pm.getLaunchIntentForPackage(
                                packageName
                        );

                if (launch != null) {

                    launch.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                    );

                    startActivity(launch);
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {

        if (webView != null) {

            webView.evaluateJavascript(
                    "closeDrawer();",
                    null
            );

        } else {

            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {

        if (webView != null) {
            webView.destroy();
        }

        super.onDestroy();
    }
}
