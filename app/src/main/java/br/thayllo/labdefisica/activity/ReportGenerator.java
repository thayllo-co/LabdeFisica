package br.thayllo.labdefisica.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import br.thayllo.labdefisica.R;
import br.thayllo.labdefisica.adapter.AttachmentAdapter;
import br.thayllo.labdefisica.helper.GeneratePdfTask;
import br.thayllo.labdefisica.model.Attachment;
import br.thayllo.labdefisica.model.Report;
import br.thayllo.labdefisica.model.User;
import br.thayllo.labdefisica.settings.FirebasePreferences;
import br.thayllo.labdefisica.settings.Preferences;

public class ReportGenerator extends AppCompatActivity {

    private static final String TAG = "ReportGeneratorActivity";
    private static final String[] tabs = new String[]{
            "OBJETIVOS",
            "RESUMO",
            "INTRODUÇÃO TEÓRICA",
            "PROCEDIMENTO EXPERIMENTAL",
            "RESULTADOS E DISCUSSÕES",
            "CONCLUSÃO",
            "BIBLIOGRAFIA"};

    private ListView pdfListView;
    private ArrayAdapter<Attachment> attachmentsAdapter;
    private ArrayList<Attachment> pdfContent;
    private Report currentReport;
    private FloatingActionButton generatepdfFAB;

    private CollectionReference tabContentCollectionReference;
    private DocumentReference tabsDocumentReference;
    private Preferences preferences;
    private DocumentReference currentReportReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_generator);

        pdfListView = findViewById(R.id.pdfListView);
        generatepdfFAB = findViewById(R.id.generatepdfFAB);

        // recupera o id e titulo do relatorio e
        preferences = new Preferences(ReportGenerator.this);
        currentReport = preferences.getReport();

        currentReportReference = FirebasePreferences.getFirebaseFirestore()
                .collection("reports").document(currentReport.getReportId());
        currentReportReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    currentReport = task.getResult().toObject(Report.class);
                } else {
                    Toast.makeText(ReportGenerator.this,  task.getException().toString() , Toast.LENGTH_LONG).show();
                }
            }
        });

        getSupportActionBar().setTitle("Geração de PDF");

        pdfContent = new ArrayList<>();
        attachmentsAdapter = new AttachmentAdapter(ReportGenerator.this, pdfContent );
        pdfListView.setAdapter(attachmentsAdapter);

        tabsDocumentReference = FirebasePreferences.getFirebaseFirestore()
                .collection("reports").document(currentReport.getReportId());

        // thread executada em paralelo a Thread UI (principal)
        new Thread(){
            // caso haja grande fluxo de dados não estourar os limites de tempo e momoria da Thread UI
            public void run(){
                for (int i = 0 ; i < tabs.length ; i++ ) {
                    final int tab = i;
                    tabContentCollectionReference = tabsDocumentReference.collection("tab" + tab);
                    tabContentCollectionReference
                            .orderBy("attachedAt", Query.Direction.ASCENDING)
                            .addSnapshotListener(ReportGenerator.this,
                                    new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                            if (e != null) {
                                                Log.w(TAG, "Listen failed.", e);
                                                return;
                                            }
                                            pdfContent.add(new Attachment(null, tab + 1 +". " + tabs[tab], null, null, null));
                                            for(DocumentSnapshot doc : queryDocumentSnapshots){
                                                Attachment a  = doc.toObject(Attachment.class);
                                                pdfContent.add(a);
                                                attachmentsAdapter.notifyDataSetChanged();
                                            }
                                        }

                                    });
                }
            }
        }.start();

        generatepdfFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Reports");
                File pdfFile = new File(docsFolder.getAbsolutePath(),
                        currentReport.getreportTitle() +"-"+ currentReport.getReportId() + ".pdf");
                final Uri uri = Uri.fromFile(pdfFile);

                if (pdfFile.exists()) {
                    new AlertDialog.Builder(ReportGenerator.this)
                            .setTitle("Arquivo já existe")
                            .setMessage("Já existe um PDF gerado para esse relatório. Deseja abrir existente ou gerar um novo PDF?")
                            .setPositiveButton("Abrir existente", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                    Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                    viewIntent.setDataAndType(uri, "application/pdf");
                                    sendIntent.setType("plain/text");
                                    sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"thayllo.co@gmail.com"}); // recipients
                                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Relatório: " + currentReport.getreportTitle());
                                    sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                    String s = "Edição:";
                                    for(User u : currentReport.getReportMembers()){
                                        s += "\n" + u.getName() + " - Matricula: " + u.getId();
                                    }
                                    sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, s);

                                    Intent chooserIntent = Intent.createChooser(sendIntent, "Abrir usando...");
                                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { viewIntent });
                                    startActivity(chooserIntent);
                                }
                            })
                            .setNegativeButton("Gerar novo", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    new GeneratePdfTask(ReportGenerator.this, currentReport).execute(pdfContent);
                                }
                            })
                            .create()
                            .show();
                } else{
                    new AlertDialog.Builder(ReportGenerator.this)
                            .setTitle("Gerar PDF?")
                            .setMessage("Isso pode levar alguns minutos")
                            .setPositiveButton("Gerar", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    new GeneratePdfTask(ReportGenerator.this, currentReport).execute(pdfContent);
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                }
                            })
                            .create()
                            .show();
                }
            }
        });
    }
}
