package br.thayllo.labdefisica.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import br.thayllo.labdefisica.R;
import br.thayllo.labdefisica.adapter.TabAdapter;
import br.thayllo.labdefisica.model.Attachment;
import br.thayllo.labdefisica.model.AttachmentType;
import br.thayllo.labdefisica.model.Report;
import br.thayllo.labdefisica.model.User;
import br.thayllo.labdefisica.settings.FirebasePreferences;
import br.thayllo.labdefisica.settings.Preferences;
import br.thayllo.labdefisica.helper.SlidingTabLayout;

public class ReportEditor extends AppCompatActivity implements OnSuccessListener<UploadTask.TaskSnapshot>,
        OnProgressListener<UploadTask.TaskSnapshot>, OnFailureListener{

    private static final int RC_TEXT_PICKER = 10;
    private static final int RC_TABLE_PICKER = 30;
    private static final int RC_BAR_CHART_PICKER = 40;
    private static final int RC_LINE_CHART_PICKER = 50;
    private static final int RC_PIE_CHART_PICKER = 60;

    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;
    private Toolbar toolbar;
    private TabAdapter tabAdapter;
    private Report currentReport;
    private int currentTab;
    private User currentUser;
    private FloatingActionButton attachmentPickerFAB;
    private Attachment attach;
    private Preferences preferences;
    private ProgressDialog progressDialog;

    private StorageReference mReportPicsStorageReference;
    private CollectionReference currentTabFirebaseFirestore;
    private DocumentReference attachmentReference;
    private DocumentReference currentReportReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_editor);

        // recupera o id e titulo do relatorio e
        preferences = new Preferences(ReportEditor.this);
        currentReport = preferences.getReport();

        currentReportReference = FirebasePreferences.getFirebaseFirestore()
                .collection("reports").document(currentReport.getReportId());
        currentReportReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    currentReport = task.getResult().toObject(Report.class);
                } else {
                    Toast.makeText(ReportEditor.this,  task.getException().toString() , Toast.LENGTH_LONG).show();
                }
            }
        });

        toolbar = findViewById(R.id.toolbarReportEditor);
        slidingTabLayout = findViewById(R.id.slidingTabReport);
        viewPager = findViewById(R.id.viewPagerReport);
        attachmentPickerFAB = findViewById(R.id.attachFloatingActionButton);

        progressDialog = new ProgressDialog(ReportEditor.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle(getResources().getString(R.string.plese_wait));
        progressDialog.setMessage(getResources().getString(R.string.uploading_attachment));

        // configura toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(currentReport.getreportTitle());

        // configura a SlidingTabLayout e a ViewPager
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setSelectedIndicatorColors(ContextCompat.getColor(this, R.color.colorAccent));
        slidingTabLayout.setClickable(true);
        tabAdapter = new TabAdapter( getSupportFragmentManager() );
        viewPager.setAdapter(tabAdapter);
        slidingTabLayout.setViewPager( viewPager);

        currentUser = preferences.getUser();
        currentReport = preferences.getReport();

        // configura referencias do BD que correspondem a esse relatorio
        mReportPicsStorageReference = FirebasePreferences.getFirebaseStorage()
                .child("reports_photos").child(currentReport.getReportId());

        // configura FloatingActionButton para seleção do tipo de anexo
        attachmentPickerFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                currentTab = viewPager.getCurrentItem();
                currentTabFirebaseFirestore = FirebasePreferences.getFirebaseFirestore()
                        .collection("reports").document(currentReport.getReportId()).collection("tab"+ currentTab);
                attachmentChooser();
            }
        });
    }

    // set up do menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_report_editor, menu);
        return true;
    }

    // configuração das ações do menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.generateReport:
                Intent intent = new Intent( ReportEditor.this, ReportGenerator.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // recebe e trata os conteudos devolvidos de outras activitys
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        attachmentReference = currentTabFirebaseFirestore.document();
        attach = new Attachment();
        attach.setId( attachmentReference.getId() );

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss.SSS");
        String stringDate = dateFormat.format(calendar.getTime());
        attach.setAttachedAt( stringDate );

        if(resultCode == RESULT_OK){
            if (requestCode == RC_TEXT_PICKER ) {

                attach.setName( currentUser.getName() );
                attach.setText( data.getStringExtra("result") );
                addAttachment();

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ){

                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri selectedImageUri = result.getUri();
                StorageReference photoRef = mReportPicsStorageReference.child(attach.getId());
                photoRef.putFile(selectedImageUri)
                        .addOnSuccessListener(this, this)
                        .addOnProgressListener(this)
                        .addOnFailureListener(this);

            } else if ( requestCode == RC_TABLE_PICKER || requestCode == RC_LINE_CHART_PICKER
                    || requestCode == RC_BAR_CHART_PICKER || requestCode == RC_PIE_CHART_PICKER ){

                byte[] bytes = data.getByteArrayExtra("result");
                StorageReference photoRef = mReportPicsStorageReference.child( attach.getId() );
                photoRef.putBytes( bytes )
                        .addOnSuccessListener(this)
                        .addOnProgressListener(this)
                        .addOnFailureListener(this);

            }
        }
    }

    // metodo que confirma de o arquivo foi armazenado no FirebaseStorage
    @Override
    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
        Uri downloadUrl = taskSnapshot.getDownloadUrl();
        attach.setName( currentUser.getName() );
        attach.setPhotoUrl( downloadUrl.toString() );
        addAttachment();
        progressDialog.dismiss();
    }

    // metodo exibe e esxonde a ProgressBar
    @Override
    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
        progressDialog.show();
        /*long transferred = taskSnapshot.getBytesTransferred();
        long total = taskSnapshot.getTotalByteCount();
        viewPagerProgressBar.setVisibility( transferred < total ? View.VISIBLE : View.GONE);*/
    }

    // caso ocorra alguma falha desabilita a ProgressBar
    @Override
    public void onFailure(@NonNull Exception e) {
        progressDialog.dismiss();
        Toast.makeText(ReportEditor.this, R.string.upload_error, Toast.LENGTH_SHORT).show();
    }

    // metodo que carrega o anexo no BD
    private void addAttachment(){
        attachmentReference.set(attach)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if( task.isSuccessful()){
                            Toast.makeText(ReportEditor.this, R.string.successfully_attached, Toast.LENGTH_SHORT).show();
                        }
                        if( task.isCanceled() ){
                            Toast.makeText(ReportEditor.this, R.string.attachment_failure, Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    // cria uma AlertDialog para selecionar o tipo de anexo
    private void attachmentChooser(){

        AlertDialog.Builder choicesBuilder = new AlertDialog.Builder(this);
        choicesBuilder.setTitle(R.string.available_attachments);

        final Intent intent = new Intent(ReportEditor.this, AttachmentPicker.class);
        final String extra = "AttachmentType";

        choicesBuilder.setItems(R.array.attachments_types,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which)
                        {
                            case 0:
                                intent.putExtra(extra, AttachmentType.TEXT);
                                startActivityForResult(intent, RC_TEXT_PICKER);
                                break;
                            case 1:
                                CropImage.activity()
                                        .setGuidelines(CropImageView.Guidelines.ON)
                                        .start(ReportEditor.this);
                                break;
                            case 2:
                                intent.putExtra(extra, AttachmentType.TABLE);
                                startActivityForResult(intent, RC_TABLE_PICKER);
                                break;
                            case 3:
                                intent.putExtra(extra, AttachmentType.LINE_CHART);
                                startActivityForResult(intent, RC_LINE_CHART_PICKER);
                                break;
                            case 4:
                                intent.putExtra(extra, AttachmentType.BAR_CHART);
                                startActivityForResult(intent, RC_BAR_CHART_PICKER);
                                break;
                            case 5:
                                intent.putExtra(extra, AttachmentType.PIE_CHART);
                                startActivityForResult(intent, RC_PIE_CHART_PICKER);
                                break;
                        }

                    }
                });
        AlertDialog choicesDialog = choicesBuilder.create();
        choicesDialog.show();
    }

}
