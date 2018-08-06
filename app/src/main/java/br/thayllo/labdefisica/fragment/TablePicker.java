package br.thayllo.labdefisica.fragment;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

import br.thayllo.labdefisica.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class TablePicker extends Fragment implements View.OnClickListener {

    public static final int TABLE_LIMIT_MIN = 1;
    public static final int TABLE_LIMIT_MAX = 10;

    private int columns = 2;
    private int rows = 2;
    private TextView rowsTextView;
    private TextView columnsTextView;
    private ImageView addRowImageView;
    private ImageView removeRowImageView;
    private ImageView addColumnImageView;
    private ImageView removeColumnImageView;
    private FloatingActionButton attachTableButton;
    private TableLayout tableLayoutContainer;
    private NestedScrollView tableScrollView;

    public TablePicker() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_table_picker, container, false);

        rowsTextView = view.findViewById(R.id.tableRowsTextView);
        columnsTextView = view.findViewById(R.id.tableColumnsTextView);
        addRowImageView = view.findViewById(R.id.tableAddRowImageView);
        addColumnImageView = view.findViewById(R.id.tableAddColumnImageView);
        removeRowImageView = view.findViewById(R.id.tableRemoveRowImageView);
        removeColumnImageView = view.findViewById(R.id.tableRemoveColumnImageView);
        attachTableButton = view.findViewById(R.id.uploadTableFloatingActionButton);
        tableLayoutContainer = view.findViewById(R.id.tableInputsTableLayout);
        tableScrollView = view.findViewById(R.id.tableScrollView);

        // formata em dois caractetes o numero de linhas e colunas
        rowsTextView.setText( String.format("%02d", rows));
        columnsTextView.setText( String.format("%02d", columns));

        // define o OnClickListener implementado no metodo sobrescrito onClick
        addRowImageView.setOnClickListener( this );
        removeRowImageView.setOnClickListener( this );
        addColumnImageView.setOnClickListener( this );
        removeColumnImageView.setOnClickListener( this );
        attachTableButton.setOnClickListener( this );

        // constroi a primeira tabela
        buildTable(rows, columns);

        // linha requerida para não abrir o teclado assim que inflar o fragmento
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // esconde a FloatingActionButton quando desliza a NestedScrollView para baixo
        tableScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) {
                    attachTableButton.hide();
                } else {
                    attachTableButton.show();
                }
            }
        });

        return view;
    }

    // metodo que trata os clicks dos botoes do frament
    @Override
    public void onClick(View v) {
        switch ( v.getId() ){
            // OnClickListener do botão que incrementa o numero de linhas
            case R.id.tableAddRowImageView:
                if( rows < TABLE_LIMIT_MAX ){
                    ++rows;
                    rowsTextView.setText( String.format("%02d", rows) );
                    buildTable(rows, columns);
                }
                else{
                    Toast.makeText(getContext(), R.string.max_limit,Toast.LENGTH_SHORT).show();
                }
                break;
            // OnClickListener do botão que decrementa o numero de linhas
            case R.id.tableRemoveRowImageView:
                if( rows > TABLE_LIMIT_MIN ){
                    --rows;
                    rowsTextView.setText( String.format("%02d", rows) );
                    buildTable(rows, columns);
                }
                else{
                    Toast.makeText(getContext(), R.string.min_limit,Toast.LENGTH_SHORT).show();
                }
                break;
            // OnClickListener do botão que incrementa o numero de colunas
            case R.id.tableAddColumnImageView:
                if( columns < TABLE_LIMIT_MAX ){
                    ++columns;
                    columnsTextView.setText( String.format("%02d", columns) );
                    buildTable(rows, columns);
                }
                else{
                    Toast.makeText(getContext(), R.string.max_limit,Toast.LENGTH_SHORT).show();
                }
                break;
            // OnClickListener do botão que decrementa o numero de colunas
            case R.id.tableRemoveColumnImageView:
                if( columns > TABLE_LIMIT_MIN ){
                    --columns;
                    columnsTextView.setText( String.format("%02d", columns) );
                    buildTable(rows, columns);
                }
                else{
                    Toast.makeText(getContext(), R.string.min_limit,Toast.LENGTH_SHORT).show();
                }
                break;
            // OnClickListener do botão que faz o upload da tabela
            case R.id.uploadTableFloatingActionButton:
                // esconde o teclado
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                View view = getActivity().getCurrentFocus();
                if (view == null) {
                    view = new View(getActivity());
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                // controi a AlertDialog de confirmação
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.sure_to_attach)
                        .setTitle(R.string.confirmation)
                        .setPositiveButton(R.string.attach, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // envia a tabela para o ReportEditor
                                tableLayoutContainer.setFocusable( false );
                                tableLayoutContainer.setClickable( false );
                                tableLayoutContainer.setFocusableInTouchMode( false );
                                // width measure spec
                                int widthSpec = View.MeasureSpec.makeMeasureSpec(
                                        tableLayoutContainer.getMeasuredWidth(), View.MeasureSpec.AT_MOST);
                                // height measure spec
                                int heightSpec = View.MeasureSpec.makeMeasureSpec(
                                        tableLayoutContainer.getMeasuredHeight(), View.MeasureSpec.AT_MOST);
                                // measure the view
                                tableLayoutContainer.measure(widthSpec, heightSpec);
                                // set the layout sizes
                                tableLayoutContainer.layout(tableLayoutContainer.getLeft(), tableLayoutContainer.getTop(),
                                        tableLayoutContainer.getMeasuredWidth() + tableLayoutContainer.getLeft(),
                                        tableLayoutContainer.getMeasuredHeight() + tableLayoutContainer.getTop());
                                // create the bitmap
                                Bitmap bitmap = Bitmap.createBitmap(tableLayoutContainer.getWidth(), tableLayoutContainer.getHeight(), Bitmap.Config.ARGB_8888);
                                // create a canvas used to get the view's image and draw it on the bitmap
                                Canvas c = new Canvas(bitmap);
                                // position the image inside the canvas
                                c.translate(-tableLayoutContainer.getScrollX(), -tableLayoutContainer.getScrollY());
                                // get the canvas
                                tableLayoutContainer.draw(c);
                                // depois desta linha o bitmep esta pronto para ser anexado

                                // constroi um array de bytes bara retortar ao editoe de relatorios
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                                Intent input = new Intent();
                                input.putExtra("result", baos.toByteArray() );
                                getActivity().setResult(Activity.RESULT_OK, input);
                                getActivity().finish();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                        builder.create().show();

                break;
        }
    }

    // metodo responsavel por construir a tabela
    public void buildTable(int rows, int columns) {

        tableLayoutContainer.removeAllViews();

        for( int i = 0 ; i <= rows ; i++ ){

            tableLayoutContainer.setStretchAllColumns(true);
            tableLayoutContainer.setBackgroundColor(Color.WHITE);
            TableRow tableRow = new TableRow(getContext());
            EditText inputEditText;

            for( int j=0; j <= columns ; j++ ){

                inputEditText = (EditText) getActivity().getLayoutInflater().inflate(R.layout.item_cell, null);

                if( i == 0 && j == 0 ){
                    // primeira celula que não serve pra nada
                    inputEditText.setBackgroundColor(Color.WHITE);
                    inputEditText.setClickable( false );
                    inputEditText.setCursorVisible( false );
                    inputEditText.setFocusable( false );
                    inputEditText.setFocusableInTouchMode( false );
                } else if( i == 0 && j > 0){
                    // configuração dos rotulos das colunas
                    inputEditText.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.table_label_columns));
                    inputEditText.setHint(String.format("Categoria %d", j));
                } else if( j== 0 && i > 0 ){
                    // configuração dosrotulos das linhas
                    inputEditText.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.table_label_rows));
                    inputEditText.setHint(String.format("Série %d", i));
                } else {
                    // configuração das células
                    // inputEditText.setHint(String.format("R:%d C:%d", i, j));
                }
                tableRow.addView( inputEditText );

            }

            tableLayoutContainer.addView( tableRow );

        }
    }

}

