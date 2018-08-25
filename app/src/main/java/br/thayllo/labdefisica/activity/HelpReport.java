package br.thayllo.labdefisica.activity;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import br.thayllo.labdefisica.R;
import br.thayllo.labdefisica.helper.NetworkChangeReceiver;

public class HelpReport extends AppCompatActivity {

    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_report);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Ajuda nos Relatórios");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // configura o verificador de conexão com a internet
        networkChangeReceiver = new NetworkChangeReceiver(HelpReport.this);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
    }
}
