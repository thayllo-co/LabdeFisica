package br.thayllo.labdefisica.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.thayllo.labdefisica.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LaboratoryFragment extends Fragment {

    public LaboratoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_laboratory, container, false);

        return view;
    }

    public void LaboratorUnavailable(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("ALERTA!");
        builder.setPositiveButton("Entendi :)", null);
        builder.setMessage("Este modulo não esta disponível\n" +
                "Estamos trabalhando para que você possa acessá-lo breve");
        AlertDialog msg = builder.create();
        msg.show();
    }

}
