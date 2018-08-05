package br.thayllo.labdefisica.settings;

import android.content.Context;
import android.content.SharedPreferences;

import br.thayllo.labdefisica.model.Report;
import br.thayllo.labdefisica.model.User;

public class Preferences{

    private Context context;
    private SharedPreferences preferences;
    private final String FILE_NAME = "labdefisica.preferencias";
    private final int MODE = 0;
    private SharedPreferences.Editor editor;

    private final String KEY_USER_ID = "userId";
    private final String KEY_USER_NAME= "userName";
    private final String KEY_USER_EMAIL = "userEmail";
    private final String KEY_USER_PHOTOURL = "userPhotoUrl";
    private final String KEY_REPORT_ID = "reportId";
    private final String KEY_REPORT_TITLE = "reportTitle";
    private final String KEY_REPORT_SUBTITLE = "reportSubtitle";
    private final String KEY_REPORT_ADDED_AT = "reportAddedAt";
    private final String KEY_PERMISSIONS = "permissions";

    public Preferences(Context context){
        this.context = context;
        preferences = context.getSharedPreferences(FILE_NAME, MODE );
        editor = preferences.edit();

    }

    public void saveUser(User user){
        editor.putString(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_PHOTOURL, user.getPhotoUrl());
        editor.commit();

    }
    public void saveReport(Report report){
        editor.putString(KEY_REPORT_ID, report.getReportId());
        editor.putString(KEY_REPORT_TITLE, report.getreportTitle());
        editor.putString(KEY_REPORT_SUBTITLE, report.getreportSubtitle());
        editor.putString(KEY_REPORT_ADDED_AT, report.getReportAddedAt());
        editor.commit();
    }
    public void savePermissions(boolean permission){
        editor.putBoolean(KEY_PERMISSIONS, permission);
        editor.commit();
    }

    public User getUser(){
        User user = new User();
        user.setEmail( preferences.getString(KEY_USER_EMAIL, null) );
        user.setId( preferences.getString(KEY_USER_ID, null) );
        user.setName( preferences.getString(KEY_USER_NAME, null) );
        user.setPhotoUrl( preferences.getString(KEY_USER_PHOTOURL, null) );
        return user;
    }
    public Report getReport(){
        Report report = new Report();
        report.setReportId(preferences.getString(KEY_REPORT_ID, null));
        report.setReportTitle(preferences.getString(KEY_REPORT_TITLE, null));
        report.setReportSubtitle(preferences.getString(KEY_REPORT_SUBTITLE, null));
        report.setReportAddedAt(preferences.getString(KEY_REPORT_ADDED_AT, null));
        return report;
    }
    public boolean getPermissions(){
        return preferences.getBoolean(KEY_PERMISSIONS, false);
    }

    public void limpar(){
        editor.clear();
        editor.commit();
    }

}
