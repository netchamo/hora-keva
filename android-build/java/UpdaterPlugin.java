package com.netchamo.horakeva;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.core.content.FileProvider;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/** מוריד את גרסת האפליקציה החדשה ופותח את מסך ההתקנה של אנדרואיד */
@CapacitorPlugin(name = "Updater")
public class UpdaterPlugin extends Plugin {

    @PluginMethod
    public void install(PluginCall call) {
        String url = call.getString("url");
        if (url == null) { call.reject("no url"); return; }
        new Thread(() -> {
            try {
                File f = new File(getContext().getCacheDir(), "update.apk");
                URL u = new URL(url);
                HttpURLConnection c = null;
                for (int i = 0; i < 6; i++) {
                    c = (HttpURLConnection) u.openConnection();
                    c.setInstanceFollowRedirects(false);
                    c.setRequestProperty("User-Agent", "hora-keva-app");
                    int code = c.getResponseCode();
                    if (code >= 300 && code < 400) { u = new URL(u, c.getHeaderField("Location")); c.disconnect(); continue; }
                    break;
                }
                int total = c.getContentLength();
                try (InputStream in = c.getInputStream(); OutputStream out = new FileOutputStream(f)) {
                    byte[] b = new byte[65536]; int n; long done = 0; long last = 0;
                    while ((n = in.read(b)) > 0) {
                        out.write(b, 0, n); done += n;
                        if (total > 0 && done - last > 200000) {
                            last = done;
                            JSObject p = new JSObject(); p.put("progress", (double) done / total);
                            notifyListeners("progress", p);
                        }
                    }
                }
                if (Build.VERSION.SDK_INT >= 26 && !getContext().getPackageManager().canRequestPackageInstalls()) {
                    Intent s = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getContext().getPackageName()));
                    s.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(s);
                    JSObject r = new JSObject(); r.put("needsPermission", true); call.resolve(r);
                    return;
                }
                Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", f);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setDataAndType(uri, "application/vnd.android.package-archive");
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(i);
                call.resolve(new JSObject());
            } catch (Exception e) {
                call.reject(String.valueOf(e.getMessage()));
            }
        }).start();
    }
}
