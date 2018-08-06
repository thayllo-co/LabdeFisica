package br.thayllo.labdefisica.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

        getSupportActionBar().setTitle("Gerador de PDF");

        pdfContent = new ArrayList<>();
        attachmentsAdapter = new AttachmentAdapter(ReportGenerator.this, pdfContent );
        pdfListView.setAdapter(attachmentsAdapter);

        tabsDocumentReference = FirebasePreferences.getFirebaseFirestore()
                .collection("reports").document(currentReport.getReportId());

        for (int i = 0 ; i < tabs.length ; i++ ) {
            final int finalI = i;
            tabContentCollectionReference = tabsDocumentReference.collection("tab" + finalI);
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
                            pdfContent.add(new Attachment(null, finalI + 1 +". " + tabs[finalI], null, null, null));
                            for(DocumentSnapshot doc : queryDocumentSnapshots){
                                Attachment a  = doc.toObject(Attachment.class);
                                pdfContent.add(a);
                            }
                            attachmentsAdapter.notifyDataSetChanged();
                        }

                    });
        }

        generatepdfFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // AlertDialog para confirmar a exclusão
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
        });
    }
}
