package br.thayllo.labdefisica.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.thayllo.labdefisica.R;
import br.thayllo.labdefisica.activity.ReportGenerator;
import br.thayllo.labdefisica.model.Attachment;
import br.thayllo.labdefisica.model.Report;
import br.thayllo.labdefisica.model.User;

public class GeneratePdfTask extends AsyncTask<ArrayList<Attachment>, Integer, Uri> {

    private ArrayList<Attachment> pdfContent;
    private ProgressDialog progressDialog;
    private Report currentReport;
    private Context context;
    private PDFView pdfView;

    public GeneratePdfTask(Context context, Report currentReport) {
        super();
        this.context = context;
        this.currentReport = currentReport;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setTitle(context.getResources().getString(R.string.plese_wait));
        progressDialog.setMessage(context.getResources().getString(R.string.generating_pdf));
        progressDialog.show();
        super.onPreExecute();
    }

    /** The system calls this to perform work in a worker thread and
     * delivers it the parameters given to AsyncTask.execute()*/
    @Override
    protected Uri doInBackground(ArrayList<Attachment>... arrayLists) {
        pdfContent = arrayLists[0];
        progressDialog.setMax(pdfContent.size());
        try{
            File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Reports");
            if (!docsFolder.exists()) {
                docsFolder.mkdir();
            }

            File pdfFile = new File(docsFolder.getAbsolutePath(),
                    currentReport.getreportTitle() +"-"+ currentReport.getReportId() + ".pdf");

            OutputStream output = new FileOutputStream(pdfFile);
            Document document = new Document(PageSize.A4, 30, 25, 30, 25);
            PdfWriter.getInstance(document, output);

            Font title = new Font(Font.FontFamily.TIMES_ROMAN  , 14, Font.BOLD);
            Font paragraph = new Font(Font.FontFamily.TIMES_ROMAN  , 12, Font.NORMAL);
            Font subtitle = new Font(Font.FontFamily.TIMES_ROMAN  , 10, Font.NORMAL);

            document.open();

            Paragraph header = new Paragraph();
            header.setAlignment(Element.ALIGN_CENTER);
            header.setSpacingAfter(20);
            header.setSpacingBefore(20);
            header.add(new Chunk("Laboratório de Física", title));
            document.add(header);

            for (User u : currentReport.getReportMembers()) {
                Paragraph name = new Paragraph();
                name.setAlignment(Element.ALIGN_CENTER);
                name.add(new Chunk(u.getName() + " - " + u.getId(), title));
                name.setSpacingAfter(5);
                name.setSpacingBefore(5);
                document.add(name);

            }

            Paragraph reportTitle = new Paragraph();
            reportTitle.setAlignment(Element.ALIGN_CENTER);
            reportTitle.setSpacingAfter(270);
            reportTitle.setSpacingBefore(270);
            reportTitle.add(new Chunk(currentReport.getreportTitle().toUpperCase(), title));
            document.add(reportTitle);

            Paragraph date = new Paragraph();
            date.setAlignment(Element.ALIGN_CENTER);
            date.add(new Chunk("Araras-SP", title));
            document.add(date);
            document.newPage();

            for(int i = 0; i < pdfContent.size() ; i++){

                progressDialog.setProgress(i);
                final Attachment attachment = pdfContent.get(i);

                if(attachment.getId() == null){
                    document.newPage();
                    document.add(new Paragraph( attachment.getText(), title));
                } else if(attachment.getPhotoUrl() != null){

                    Image image = Image.getInstance(new URL(attachment.getPhotoUrl()));
                    int indentation = 0;
                    float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                            - document.rightMargin() - indentation) / image.getWidth()) * 80;
                    image.scalePercent(scaler);
                    image.setAlignment(Element.ALIGN_CENTER);

                    Phrase phrase = new Phrase(attachment.getPhotoUrl(), subtitle);

                    PdfPTable table = new PdfPTable(1);
                    table.setWidthPercentage(100);
                    table.setSpacingBefore(15);
                    table.setSpacingAfter(10);

                    PdfPCell imageCell = new PdfPCell(image, false);
                    PdfPCell subtitleCell = new PdfPCell(phrase);
                    imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    subtitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    imageCell.setBorder(Rectangle.NO_BORDER);
                    subtitleCell.setBorder(Rectangle.NO_BORDER);
                    table.addCell(imageCell);
                    table.addCell(subtitleCell);
                    table.setHorizontalAlignment(Element.ALIGN_CENTER);

                    document.add(table);
                } else {
                    Paragraph p = new Paragraph( attachment.getText(), paragraph);
                    p.setExtraParagraphSpace(15);
                    document.add(p);
                }
            }
            document.close();

            return Uri.fromFile(pdfFile);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (BadElementException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /** The system calls this to perform work in the UI thread and delivers
     * the result from doInBackground()*/
    @Override
    protected void onPostExecute(Uri uri) {
        super.onPostExecute(uri);
        progressDialog.dismiss();
        /*pdfView.fromUri(uri)
                .spacing(5)
                .load();
        pdfView.setVisibility(View.VISIBLE);*/
        PackageManager packageManager = context.getPackageManager();
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType("application/pdf");
        List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() > 0) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            context.startActivity(intent);
        }else{
            Toast.makeText(context,"PDF salvo em: " + uri.getPath() ,Toast.LENGTH_LONG).show();
        }
    }
}