package br.thayllo.labdefisica.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import br.thayllo.labdefisica.R;

public class HelpReport extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_report);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Ajuda nos Relatórios");
    }
}
