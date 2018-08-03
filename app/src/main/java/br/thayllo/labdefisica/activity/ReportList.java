package br.thayllo.labdefisica.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.thayllo.labdefisica.R;
import br.thayllo.labdefisica.adapter.ReportAdapter;
import br.thayllo.labdefisica.model.AttachmentType;
import br.thayllo.labdefisica.model.Report;
import br.thayllo.labdefisica.model.User;
import br.thayllo.labdefisica.settings.FirebasePreferences;
import br.thayllo.labdefisica.settings.Preferences;

public class ReportList extends AppCompatActivity{

    private static final String TAG = "ERRR-ReportList";
    private ListView repostsListView;
    private ArrayAdapter<Report> reportsAdapter;
    private User currentUser;
    private ArrayList<Report> reportList;
    private Report newReport;
    private Preferences preferences;
    private FloatingActionButton addReportFAB;
    private TextView emptyReportListTextView;

    private CollectionReference reportsReference = FirebasePreferences.getFirebaseFirestore()
            .collection("reports");
    private CollectionReference usersReference = FirebasePreferences.getFirebaseFirestore()
            .collection("users");
    private CollectionReference myContactsReference;
    private CollectionReference myReportsReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        repostsListView = findViewById(R.id.reportsListView);
        addReportFAB = findViewById(R.id.addRepoortFloatingActionButton);
        emptyReportListTextView = findViewById(R.id.emptyReportListTextView);

        // Pega os dados do usuario das preferencias
        preferences = new Preferences(ReportList.this);
        currentUser = preferences.getUser();

        myContactsReference = FirebasePreferences.getFirebaseFirestore()
                .collection("users").document(currentUser.getId()).collection("contacts");
        myReportsReference = FirebasePreferences.getFirebaseFirestore()
                .collection("users").document(currentUser.getId()).collection("reports");

        reportList = new ArrayList<>();
        reportsAdapter = new ReportAdapter( ReportList.this , reportList);
        repostsListView.setAdapter( reportsAdapter );

        myReportsReference
                .orderBy("reportAddedAt", Query.Direction.ASCENDING)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Log.w(TAG, "Listen failed.", e);
                                    return;
                                }
                                for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){
                                    if(doc.getType() == DocumentChange.Type.ADDED){
                                        Log.w(TAG, doc.getDocument().toObject(Report.class).toString() );
                                        reportList.add(doc.getDocument().toObject(Report.class));
                                        reportsAdapter.notifyDataSetChanged();
                                    }else if(doc.getType() == DocumentChange.Type.REMOVED){
                                        //reportsAdapter.notifyDataSetChanged();
                                    }
                                    if(reportList.size() < 1 )
                                        emptyReportListTextView.setVisibility(View.VISIBLE);
                                    else
                                        emptyReportListTextView.setVisibility(View.GONE);
                                }
                            }
                        }
                );

        repostsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openReport(reportList.get(position));
            }
        });

        repostsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                deleteReport(position);
                return true;
            }
        });

        addReportFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { reportTypeChooser();
            }
        });
    }

    private void deleteReport(final int position) {
        // Create a reference to remove of the database
        final DocumentReference report = FirebasePreferences.getFirebaseFirestore()
                .collection("reports").document(reportList.get(position).getReportId());
        // Create a reference to the file to delete
        final StorageReference storageReference = FirebasePreferences.getFirebaseStorage()
                .child("reports_photos").child(reportList.get(position).getReportId());

        // AlertDialog para confirmar a exclusão
        new AlertDialog.Builder(ReportList.this)
                .setTitle(R.string.delete)
                .setIcon( R.drawable.ic_warning)
                .setMessage(R.string.sure_to_delete_report)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        report.delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // DESENVOLVER METODO PARA REMOVER AS IMAGENS DO FIREBASE STORAGE CORRESPONDENTE AO RELATORIO DELETADO
                                                /*storageReference.delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(ReportEditor.this, "DEU CERTO", Toast.LENGTH_LONG).show();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(ReportEditor.this, "ERRO: " + e.toString(), Toast.LENGTH_LONG).show();
                                                            }
                                                        });*/
                                        reportList.remove(position);
                                        reportsAdapter.notifyDataSetChanged();
                                        Toast.makeText(ReportList.this, R.string.deleted, Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ReportList.this,R.string.delete_error, Toast.LENGTH_SHORT).show();
                                    }
                                });
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

    private void openReport(Report report){
        Intent intent = new Intent( ReportList.this, ReportEditor.class);
        preferences.saveReport(report);
        startActivity(intent);
    }

    private void reportTypeChooser(){

        final CharSequence[] reportTypes = {"Individual", "Grupo"};
        new AlertDialog.Builder(this)
                .setTitle("Tipo de Relatório")
                .setItems(reportTypes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        addIndividualReport();
                                        break;
                                    case 1:
                                        addGroupReport();
                                        break;
                                }
                            }
                        })
                .create()
                .show();
    }

    private void addIndividualReport() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ReportList.this);
        //Configurações do Dialog
        alertDialog.setTitle(R.string.new_report);
        alertDialog.setMessage(R.string.new_experiment_hint);
        alertDialog.setCancelable(false);
        // Configura o EditText para receber o email do novo contato
        final EditText editText = new EditText(ReportList.this);
        alertDialog.setView( editText );
        //Configura botões
        alertDialog.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Pega o email digitado pelo usuario
                final String reportTitle = editText.getText().toString();
                //Valida se o e-mail foi digitado
                if( reportTitle.isEmpty() ){
                    Toast.makeText(ReportList.this, R.string.type_something, Toast.LENGTH_LONG).show();
                }else{
                    DocumentReference documentReference = reportsReference.document();

                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss.SSS");
                    String stringDate = dateFormat.format(calendar.getTime());
                    newReport = new Report(reportTitle, currentUser.getName(), documentReference.getId(), stringDate);

                    documentReference.set(newReport)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    DocumentReference myReportReference = usersReference.document(currentUser.getId())
                                            .collection("reports").document(newReport.getReportId());
                                    myReportReference.set(newReport)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    openReport(newReport);
                                                }
                                            });
                                }
                            });
                }
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.create();
        alertDialog.show();
    }

    private void addGroupReport(){

        final List<User> friendsList = new ArrayList<>();

        myContactsReference
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener(ReportList.this, new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Toast.makeText(ReportList.this, e.getMessage(), Toast.LENGTH_LONG);
                                    return;
                                }
                                List<String> friendsDialogList = new ArrayList<>();
                                for(DocumentSnapshot doc : queryDocumentSnapshots){
                                    User u  = doc.toObject(User.class);
                                    friendsList.add(u);
                                    friendsDialogList.add(u.getName());
                                }
                                pickFriendsDialog(friendsDialogList.toArray(new CharSequence[friendsDialogList.size()]),
                                        friendsList);
                            }
                        });

    }

    private void pickFriendsDialog(CharSequence[] charSequence, final List<User> users){
        final ArrayList<Integer> seletedItems = new ArrayList();
        new AlertDialog.Builder(ReportList.this)
                .setTitle("Selecione Amigos")
                .setMultiChoiceItems(charSequence, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    seletedItems.add(which);
                                } else if (seletedItems.contains(which)) {
                                    seletedItems.remove(Integer.valueOf(which));
                                }
                            }
                        })
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Calendar calendar = Calendar.getInstance();
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss.SSS");
                                String stringDate = dateFormat.format(calendar.getTime());

                                DocumentReference documentReference = reportsReference.document();
                                newReport = new Report("Relatório em grupo", currentUser.getName(), documentReference.getId(), stringDate);

                                documentReference.set(newReport).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //ADD A SI PROPRIO NO GRUPO
                                        users.add(currentUser);
                                        seletedItems.add(users.size()-1);

                                        for (int i = 0 ; i < seletedItems.size() ; i++){
                                            //AMIGOS QUE FORMAM SELECIONADOS
                                            final User user = users.get(seletedItems.get(i));
                                            DocumentReference myReportReference = usersReference.document(user.getId())
                                                    .collection("reports").document(newReport.getReportId());

                                            myReportReference.set(newReport).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(ReportList.this, user.getName() + " adicionado(a) com sucesso" , Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(ReportList.this,  "Falha ao adicionar " + user.getName(), Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                                });
                                        }
                                    }
                                });

                            }
                        })
                .setNegativeButton("Cancelar",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                .create()
                .show();
    }

}