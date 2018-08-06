package br.thayllo.labdefisica.fragment;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import br.thayllo.labdefisica.R;

/**
 * Fragmento que recebe anexos do tipo texto e o anexa no relatorio
 */
public class TextPicker extends Fragment {

    private EditText inputTextEditText;
    private FloatingActionButton attachTextButton;
    private NestedScrollView textScrollView;


    public TextPicker() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text_picker, container, false);

        inputTextEditText = view.findViewById(R.id.pickTextEditText);
        attachTextButton = view.findViewById(R.id.uploadTextFloatingActionButton);
        textScrollView = view.findViewById(R.id.textScrollView);

        // linha requerida para não abrir o teclado assim que inflar o fragmento
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        attachTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadText();
            }
        });

        textScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) {
                    attachTextButton.hide();
                } else {
                    attachTextButton.show();
                }
            }
        });

        return view;
    }

    private void uploadText() {

        // esconde o teclado
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getActivity().getCurrentFocus();
        if (view == null) {
            view = new View(getActivity());
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        // controi a AlertDialog de confirmação
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());

        //Configurações do Dialog
        alertDialog.setTitle(R.string.confirmation);
        alertDialog.setMessage(R.string.sure_to_attach);
        alertDialog.setCancelable(false);

        //Configura botões
        alertDialog.setPositiveButton(R.string.attach, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if( inputTextEditText.getText().toString().isEmpty() ){
                    Toast.makeText( getActivity(), R.string.type_something, Toast.LENGTH_SHORT).show();
                } else {
                    // envia o conteudo do texto para o ReportEditor
                    String s = inputTextEditText.getText().toString();
                    Intent input = new Intent();
                    input.putExtra("result", s );
                    getActivity().setResult(Activity.RESULT_OK, input);
                    getActivity().finish();
                }
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialog.create();
        alertDialog.show();
    }

}
