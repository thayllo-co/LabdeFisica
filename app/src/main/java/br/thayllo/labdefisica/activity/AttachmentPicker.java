package br.thayllo.labdefisica.activity;

import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import br.thayllo.labdefisica.R;
import br.thayllo.labdefisica.fragment.BarChartPicker;
import br.thayllo.labdefisica.fragment.LineChartPicker;
import br.thayllo.labdefisica.fragment.PieChartPicker;
import br.thayllo.labdefisica.fragment.TablePicker;
import br.thayllo.labdefisica.fragment.TextPicker;
import br.thayllo.labdefisica.helper.NetworkChangeReceiver;
import br.thayllo.labdefisica.model.AttachmentType;

// Activity que trata como proceder com o tipo de anexo selecionado
public class AttachmentPicker extends AppCompatActivity {

    private AttachmentType attachmentType;
    private Toolbar toolbar;
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attachment_picker);

        toolbar = findViewById(R.id.toolbarPicker);
        setSupportActionBar(toolbar);

        // recupera o tipo de anexo que foi selecionado para tomar a ação pertinente
        Bundle extra = getIntent().getExtras();
        if( extra != null){
            attachmentType = (AttachmentType) extra.get("AttachmentType");
            setFragmentView( attachmentType );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // configura o verificador de conexão com a internet
        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_attachment_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // icone na barra de ações que fecha a activity
        int id = item.getItemId();
        if (id == R.id.closePicker) {

            // AlertDialog para confirmar a exclusão
            AlertDialog.Builder builder = new AlertDialog.Builder(AttachmentPicker.this);
            builder
                    .setTitle(R.string.exit_editor)
                    .setMessage(R.string.sure_to_exit)
                    .setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            builder.create().show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void setContent(Fragment content) {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.attachmentContent, content);
        ft.commit();
    }

    private void setFragmentView(AttachmentType type){
        // define que ação deve ser tomada de acordo com o tipo de anexo selecionado pelo usuario
        switch (type){
            case TEXT:
                setContent( new TextPicker());
                toolbar.setTitle(R.string.text_editor);
                break;
            case TABLE:
                setContent( new TablePicker() );
                toolbar.setTitle(R.string.table_editor);
                break;
            case LINE_CHART:
                setContent( new LineChartPicker() );
                toolbar.setTitle(R.string.line_chart_editor);
                break;
            case BAR_CHART:
                setContent( new BarChartPicker() );
                toolbar.setTitle(R.string.bar_chart_editor);
                break;
            case PIE_CHART:
                setContent( new PieChartPicker() );
                toolbar.setTitle(R.string.pie_chart_editor);
                break;
        }
    }

}
