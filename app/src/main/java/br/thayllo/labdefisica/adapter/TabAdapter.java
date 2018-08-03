package br.thayllo.labdefisica.adapter;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import br.thayllo.labdefisica.fragment.AttachmentList;

public class TabAdapter extends FragmentStatePagerAdapter {

    private String[] tabs = new String[]{
            "OBJETIVOS",
            "RESUMO",
            "INTRODUÇÃO TEÓRICA",
            "PROCEDIMENTO EXPERIMENTAL",
            "RESULTADOS E DISCUSSÕES",
            "CONCLUSÃO",
            "BIBLIOGRAFIA"};

    public TabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch ( position ){
            case 0:
                fragment = new AttachmentList(position);
                break;
            case 1:
                fragment = new AttachmentList(position);
                break;
            case 2:
                fragment = new AttachmentList(position);
                break;
            case 3:
                fragment = new AttachmentList(position);
                break;
            case 4:
                fragment = new AttachmentList(position);
                break;
            case 5:
                fragment = new AttachmentList(position);
                break;
            case 6:
                fragment = new AttachmentList(position);
                break;
        }
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs[ position ];
    }

    @Override
    public int getCount() {
        return tabs.length;
    }

}
