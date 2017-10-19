package fiszki.xyz.fiszkiapp.source;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Functions {
    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
