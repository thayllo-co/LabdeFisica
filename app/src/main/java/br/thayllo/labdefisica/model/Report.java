package br.thayllo.labdefisica.model;

import java.util.ArrayList;

public class Report {

    private String reportId;
    private String reportTitle;
    private String reportSubtitle;
    private String reportAddedAt;
    private ArrayList<User> reportMembers;

    public Report() {

    }

    public Report(String reportTitle, String reportSubtitle, String reportId, String reportAddedAt) {
        this.reportTitle = reportTitle;
        this.reportSubtitle = reportSubtitle;
        this.reportId = reportId;
        this.reportAddedAt = reportAddedAt;
    }

    public String getreportTitle() {
        return reportTitle;
    }

    public String getreportSubtitle() {
        return reportSubtitle;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    public void setReportSubtitle(String reportSubtitle) {
        this.reportSubtitle = reportSubtitle;
    }

    public String getReportAddedAt() {
        return reportAddedAt;
    }

    public void setReportAddedAt(String reportAddedAt) {
        this.reportAddedAt = reportAddedAt;
    }

    public ArrayList<User> getReportMembers() {
        return reportMembers;
    }

    public void setReportMembers(ArrayList<User> reportMembers) {
        this.reportMembers = reportMembers;
    }

    @Override
    public String toString() {
        return "reportId=" + reportId +
                "\nreportTitle=" + reportTitle +
                "\nreportSubtitle=" + reportSubtitle +
                "\nreportAddedAt=" + reportAddedAt;
    }
}
