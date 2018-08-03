package br.thayllo.labdefisica.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.thayllo.labdefisica.R;
import br.thayllo.labdefisica.model.Report;
import br.thayllo.labdefisica.model.User;

public class ReportAdapter extends ArrayAdapter<Report> {

    private List<Report> reports;
    private Activity context;

    public ReportAdapter(Activity c, ArrayList<Report> objects) {
        super(c, 0, objects);
        this.reports = objects;
        this.context = c;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = null;

        if( reports != null ){
            view = context.getLayoutInflater().inflate( R.layout.item_report , parent, false);

            // recupera elemento para exibição
            TextView reportTitleTextView = view.findViewById(R.id.reportTitleTextView);
            TextView areportSubtitleTextView = view.findViewById(R.id.reportSubtitleTextView);

            Report report = reports.get( position );
            reportTitleTextView.setText( report.getreportTitle() );
            areportSubtitleTextView.setText( report.getreportSubtitle() );
        }

        return view;
    }
}
