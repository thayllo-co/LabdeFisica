package br.thayllo.labdefisica.fragment;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import br.thayllo.labdefisica.R;
import br.thayllo.labdefisica.adapter.AttachmentAdapter;
import br.thayllo.labdefisica.model.Attachment;
import br.thayllo.labdefisica.model.Report;
import br.thayllo.labdefisica.settings.FirebasePreferences;
import br.thayllo.labdefisica.settings.Preferences;

/**
 * A simple {@link Fragment} subclass.
 */
public class AttachmentList extends Fragment {

    private static final String TAG = "ERRR-AttachmentList";
    private int tab;
    private Report currentReport;
    private ListView attachmentsListView;
    private ArrayAdapter<Attachment> attachmentsAdapter;
    private ArrayList<Attachment> tabContentList;
    private Preferences preferences;
    private TextView emptyAttachmentListTextView;

    private DocumentReference attachment;
    private StorageReference storageRef;

    private CollectionReference currentTabFirebaseFirestore;

    public AttachmentList() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public AttachmentList(int tab ) {
        this.tab = tab;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attachment_list, container, false);

        attachmentsListView = view.findViewById(R.id.attachmentsListView);
        emptyAttachmentListTextView = view.findViewById(R.id.emptyAttachmentListTextView);

        // recupera o ID do relatorio
        preferences = new Preferences( getActivity() );
        currentReport = preferences.getReport();

        currentTabFirebaseFirestore = FirebasePreferences.getFirebaseFirestore()
                .collection("reports").document(currentReport.getReportId()).collection("tab"+ tab);

        tabContentList = new ArrayList<>();
        attachmentsAdapter = new AttachmentAdapter(getActivity(), tabContentList);
        attachmentsListView.setAdapter( attachmentsAdapter );

        currentTabFirebaseFirestore
                .orderBy("attachedAt", Query.Direction.ASCENDING)
                .addSnapshotListener( new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        Attachment attachment;
                        for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){
                            switch (doc.getType()) {
                                case ADDED:
                                    tabContentList.add(doc.getDocument().toObject(Attachment.class));
                                    break;
                                case MODIFIED:
                                    /*attachment = doc.getDocument().toObject(Attachment.class);
                                    for (Attachment a : tabContentList){
                                        if(a.getId().equals(attachment.getId())){
                                            tabContentList.get(tabContentList.indexOf(a));
                                        }
                                    }*/
                                    break;
                                case REMOVED:
                                    attachment = doc.getDocument().toObject(Attachment.class);
                                    for (Attachment a : tabContentList){
                                        if(a.getId().equals(attachment.getId())){
                                            tabContentList.remove(a);
                                        }
                                    }
                                    break;
                            }
                            attachmentsAdapter.notifyDataSetChanged();
                        }
                        if(tabContentList.size() < 1 )
                            emptyAttachmentListTextView.setVisibility(View.VISIBLE);
                        else
                            emptyAttachmentListTextView.setVisibility(View.GONE);
                    }
                }
        );

        //Adicionar evento de clique longo na lista para excluir item
        attachmentsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                // referencia para remover do banco
                attachment = currentTabFirebaseFirestore.document(tabContentList.get(position).getId());
                // referencia para remover do armazenamento(se houver)
                storageRef = FirebasePreferences.getFirebaseStorage().child("reports_photos")
                        .child(currentReport.getReportId()).child(tabContentList.get(position).getId());

                deleteAttachment();

                return true;
            }
        });

        return view;
    }

    public void deleteAttachment(){
        // AlertDialog para confirmar a exclusão
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete)
                .setIcon( R.drawable.ic_warning)
                .setMessage(R.string.sure_to_delete_content)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // remove o anexo da lista
                        attachment.delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        storageRef.delete();
                                        Toast.makeText(getContext(), R.string.deleted, Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(),R.string.delete_error, Toast.LENGTH_SHORT).show();
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
}
