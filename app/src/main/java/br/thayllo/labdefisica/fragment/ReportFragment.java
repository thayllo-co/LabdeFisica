package br.thayllo.labdefisica.fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.thayllo.labdefisica.R;
import br.thayllo.labdefisica.activity.ReportEditor;
import br.thayllo.labdefisica.adapter.ReportAdapter;
import br.thayllo.labdefisica.model.Report;
import br.thayllo.labdefisica.model.User;
import br.thayllo.labdefisica.settings.FirebasePreferences;
import br.thayllo.labdefisica.settings.Preferences;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReportFragment extends Fragment {

    private static final String TAG = "ERRR-ReportList";
    private ListView repostsListView;
    private ArrayAdapter<Report> reportsAdapter;
    private User currentUser;
    private ArrayList<Report> reportList;
    private Report newReport;
    private Preferences preferences;
    private FloatingActionButton addReportFAB;
    private TextView emptyReportListTextView;
    private Report selectedReport;

    private CollectionReference reportsReference = FirebasePreferences.getFirebaseFirestore()
            .collection("reports");
    private CollectionReference usersReference = FirebasePreferences.getFirebaseFirestore()
            .collection("users");
    private CollectionReference myContactsReference;
    private CollectionReference myReportsReference;
    private DocumentReference selectedReportReference;

    public ReportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_report, container, false);

        repostsListView = view.findViewById(R.id.reportsListView);
        addReportFAB = view.findViewById(R.id.addRepoortFloatingActionButton);
        emptyReportListTextView = view.findViewById(R.id.emptyReportListTextView);

        // Pega os dados do usuario das preferencias
        preferences = new Preferences(getActivity());
        currentUser = preferences.getUser();

        myContactsReference = FirebasePreferences.getFirebaseFirestore()
                .collection("users").document(currentUser.getId()).collection("contacts");
        myReportsReference = FirebasePreferences.getFirebaseFirestore()
                .collection("users").document(currentUser.getId()).collection("reports");

        reportList = new ArrayList<>();
        reportsAdapter = new ReportAdapter( getActivity() , reportList);
        repostsListView.setAdapter( reportsAdapter );

        myReportsReference
                .orderBy("reportAddedAt", Query.Direction.ASCENDING)
                .addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Log.w(TAG, "Listen failed.", e);
                                    return;
                                }
                                Report report;
                                for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){
                                    switch (doc.getType()) {
                                        case ADDED:
                                            reportList.add(doc.getDocument().toObject(Report.class));
                                            break;
                                        case MODIFIED:
                                            report = doc.getDocument().toObject(Report.class);
                                            for (Report r : reportList){
                                                if(r.getReportId().equals(report.getReportId())){
                                                    reportList.get(reportList.indexOf(r)).setReportTitle(report.getreportTitle());
                                                }
                                            }
                                            break;
                                        case REMOVED:
                                            report = doc.getDocument().toObject(Report.class);
                                            for (Report r : reportList){
                                                if(r.getReportId().equals(report.getReportId())){
                                                    reportList.remove(r);
                                                }
                                            }
                                            break;
                                    }
                                }
                                reportsAdapter.notifyDataSetChanged();
                                if(reportList.size() < 1 )
                                    emptyReportListTextView.setVisibility(View.VISIBLE);
                                else
                                    emptyReportListTextView.setVisibility(View.GONE);
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
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                selectedReport = reportList.get(position);
                selectedReportReference = FirebasePreferences.getFirebaseFirestore()
                        .collection("reports").document(selectedReport.getReportId());

                final CharSequence[] reportTypes = {"Renomear relatório", "Excluir relatório"};
                new AlertDialog.Builder(getActivity())
                        .setTitle("Opções")
                        .setItems(reportTypes,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 0:
                                                updateReportTitle();
                                                break;
                                            case 1:
                                                deleteReport();
                                                break;
                                        }
                                    }
                                })
                        .create()
                        .show();
                return true;
            }
        });

        addReportFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { setReportTitle();
            }
        });
        return view;
    }

    private void setReportTitle() {
        final EditText editText = new EditText(getActivity());
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.new_report)
                .setMessage(R.string.new_experiment_hint)
                .setCancelable(false)
                .setView( editText )
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Pega o email digitado pelo usuario
                        final String reportTitle = editText.getText().toString();
                        //Valida se o e-mail foi digitado
                        if( reportTitle.isEmpty() ){
                            Toast.makeText(getActivity(), R.string.type_something, Toast.LENGTH_LONG).show();
                        }else{
                            reportTypeChooser(reportTitle);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();
    }

    private void reportTypeChooser(final String reportTitle){

        final CharSequence[] reportTypes = {"Individual", "Grupo"};
        new AlertDialog.Builder(getActivity())
                .setTitle("Tipo de Relatório")
                .setItems(reportTypes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        addIndividualReport(reportTitle);
                                        break;
                                    case 1:
                                        addGroupReport(reportTitle);
                                        break;
                                }
                            }
                        })
                .create()
                .show();
    }

    private void addIndividualReport(String reportTitle) {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss.SSS");
        String stringDate = dateFormat.format(calendar.getTime());

        final ArrayList<User> myArray = new ArrayList<>();
        myArray.add(currentUser);

        DocumentReference documentReference = reportsReference.document();
        newReport = new Report(reportTitle, currentUser.getName(), documentReference.getId(), stringDate);
        newReport.setReportMembers(myArray);

        final DocumentReference myReportReference = usersReference.document(currentUser.getId())
                .collection("reports").document(newReport.getReportId());

        documentReference.set(newReport)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
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

    private void addGroupReport(final String reportTitle){
        final List<User> friendsList = new ArrayList<>();
        myContactsReference
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG);
                            return;
                        }
                        List<String> friendsDialogList = new ArrayList<>();
                        for(DocumentSnapshot doc : queryDocumentSnapshots){
                            User u  = doc.toObject(User.class);
                            friendsList.add(u);
                            friendsDialogList.add(u.getName());
                        }
                        pickFriendsDialog(reportTitle, friendsDialogList.toArray(new CharSequence[friendsDialogList.size()]),
                                friendsList);
                    }
                });
    }

    private void pickFriendsDialog(final String reportTitle, CharSequence[] charSequence, final List<User> users){
        final ArrayList<Integer> seletedItems = new ArrayList();
        new AlertDialog.Builder(getActivity())
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
                                newReport = new Report(reportTitle , currentUser.getName(), documentReference.getId(), stringDate);
                                final ArrayList<User> reportMembers = new ArrayList<>();

                                //ADD A SI PROPRIO NO GRUPO
                                users.add(currentUser);
                                seletedItems.add(users.size()-1);
                                String s = "";
                                for (int i = 0 ; i < seletedItems.size() ; i++){
                                    //AMIGOS QUE FORMAM SELECIONADOS
                                    reportMembers.add(users.get(seletedItems.get(i)));

                                }
                                newReport.setReportMembers(reportMembers);
                                for(int i=0 ; i < reportMembers.size() ; i++){
                                    s += reportMembers.get(i).getName();
                                    if(i == reportMembers.size()-1)
                                        s+=".";
                                    else
                                        s+=", ";
                                }
                                newReport.setReportSubtitle(s);
                                documentReference.set(newReport).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        for(final User u : reportMembers){
                                            DocumentReference userReportReference = FirebasePreferences.getFirebaseFirestore()
                                                    .collection("users").document(u.getId()).collection("reports")
                                                    .document(newReport.getReportId());
                                            userReportReference.set(newReport)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(getActivity(), u.getName() + " adicionado(a)" , Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(getActivity(),  "Falha ao adicionar " + u.getName(), Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });
                                        }
                                        openReport(newReport);
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

    private void openReport(Report report){
        Intent intent = new Intent( getActivity(), ReportEditor.class);
        preferences.saveReport(report);
        startActivity(intent);
    }

    private void deleteReport() {
        // AlertDialog para confirmar a exclusão
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete)
                .setIcon( R.drawable.ic_warning)
                .setMessage(R.string.sure_to_delete_report)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        selectedReportReference.delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        for(User u : selectedReport.getReportMembers()){
                                            DocumentReference userReportReference = FirebasePreferences.getFirebaseFirestore()
                                                    .collection("users").document(u.getId()).collection("reports")
                                                    .document(selectedReport.getReportId());
                                            userReportReference.delete();
                                        }
                                        Toast.makeText(getActivity(), R.string.deleted, Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getActivity(),R.string.delete_error, Toast.LENGTH_SHORT).show();
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

    private void updateReportTitle() {
        final EditText editText = new EditText(getActivity());
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.rename_report)
                .setMessage(R.string.new_experiment_hint)
                .setCancelable(false)
                .setView( editText )
                .setPositiveButton(R.string.rename, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Pega o email digitado pelo usuario
                        final String reportTitle = editText.getText().toString();
                        //Valida se o e-mail foi digitado
                        if( reportTitle.isEmpty() ){
                            Toast.makeText(getActivity(), R.string.type_something, Toast.LENGTH_LONG).show();
                        }else{
                            selectedReportReference
                                    .update("reportTitle", reportTitle)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            for(User u : selectedReport.getReportMembers()){
                                                DocumentReference userReportReference = FirebasePreferences.getFirebaseFirestore()
                                                        .collection("users").document(u.getId()).collection("reports")
                                                        .document(selectedReport.getReportId());
                                                userReportReference.update("reportTitle", reportTitle);
                                            }
                                            Toast.makeText(getActivity(), R.string.renamed, Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();
    }

}