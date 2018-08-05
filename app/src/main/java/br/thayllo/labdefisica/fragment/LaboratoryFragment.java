package br.thayllo.labdefisica.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import br.thayllo.labdefisica.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LaboratoryFragment extends Fragment implements View.OnClickListener{

    public LaboratoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_laboratory, container, false);

        LinearLayout lab0Layout = view.findViewById(R.id.lab0Layout);
        LinearLayout lab1Layout = view.findViewById(R.id.lab1Layout);
        LinearLayout lab2Layout = view.findViewById(R.id.lab2Layout);
        LinearLayout lab3Layout = view.findViewById(R.id.lab3Layout);
        LinearLayout lab4Layout = view.findViewById(R.id.lab4Layout);

        lab0Layout.setOnClickListener(this);
        lab1Layout.setOnClickListener(this);
        lab2Layout.setOnClickListener(this);
        lab3Layout.setOnClickListener(this);
        lab4Layout.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        LaboratoryUnavailable();
    }

    public void LaboratoryUnavailable() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("ALERTA!");
        builder.setPositiveButton("Entendi :)", null);
        builder.setMessage("Este modulo não esta disponível\n" +
                "Estamos trabalhando para que você possa acessá-lo breve");
        AlertDialog msg = builder.create();
        msg.show();
    }


}
