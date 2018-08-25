package br.thayllo.labdefisica.helper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.androidadvance.topsnackbar.TSnackbar;

import br.thayllo.labdefisica.R;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        final TSnackbar snackbar = TSnackbar
                .make(((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content), "SEM CONEXÃO INTERNET", TSnackbar.LENGTH_INDEFINITE)
                        .setAction( R.string.close, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(context, R.string.connection_info, Toast.LENGTH_LONG).show();
                            }
                        });
        snackbar.setActionTextColor(Color.GRAY);
        snackbar.getView().setBackgroundColor(Color.RED);
        TextView textView = (TextView) snackbar.getView().findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setTextColor(Color.WHITE);

        if(!isConnected){
            snackbar.show();
        } else {
            snackbar.dismiss();
        }
    }
}