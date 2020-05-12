package helpos.helpos.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class Error {

    public Error(View root, String errorMsg) {
        Snackbar.make(root, errorMsg,Snackbar.LENGTH_SHORT).setAction("ok", v -> {}).show();
    }

    public Error (Context context, String errorMsg) {
        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
    }

    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isNetworkAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        if (!isNetworkAvailable) {
            Toast.makeText(c, "no Internet connection", Toast.LENGTH_SHORT).show();
        }
        return isNetworkAvailable;
    }
}
