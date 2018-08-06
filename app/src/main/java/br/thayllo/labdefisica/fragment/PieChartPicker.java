package br.thayllo.labdefisica.fragment;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.thayllo.labdefisica.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PieChartPicker extends Fragment implements View.OnClickListener{

    public static final int TABLE_LIMIT_MIN = 1;
    public static final int TABLE_LIMIT_MAX = 10;
    public static final int CELL_LENGTH_LIMIT = 50;
    public static final int LABEL_LENGTH_LIMIT = 10;

    private int items = 3;
    private TextView itemsTextView;
    private ImageView addItemImageView;
    private ImageView removeItemImageView;
    private FloatingActionButton attachPieChartButton;
    private TableLayout tableLayoutContainer;
    private PieChart reportPieChart;
    private List<EditText> editTextsList;
    private List<EditText> itemsLabelsEditTextList;
    private NestedScrollView pieChartScrollView;

    private FrameLayout chartFrameLayout;

    private boolean uploadChart = false;

    public PieChartPicker() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_pie_chart_picker, container, false);

        itemsTextView = view.findViewById(R.id.pieCharItemTextView);
        addItemImageView = view.findViewById(R.id.pieChartAddItemImageView);
        removeItemImageView = view.findViewById(R.id.pieChartRemoveItemImageView);
        attachPieChartButton = view.findViewById(R.id.uploadPieChartFloatingActionButton);
        tableLayoutContainer = view.findViewById(R.id.pieChartInputsTableLayout);
        chartFrameLayout = view.findViewById(R.id.pieChartFrameLayout);
        pieChartScrollView = view.findViewById(R.id.pieChartScrollView);


        // formata em dois caractetes o numero de itens
        itemsTextView.setText( String.format("%02d", items));

        // define o OnClickListener implementado no metodo sobrescrito onClick
        addItemImageView.setOnClickListener( this );
        removeItemImageView.setOnClickListener( this );
        attachPieChartButton.setOnClickListener( this );

        // constroi a primeira tabela
        buildTable();

        // linha requerida para não abrir o teclado assim que inflar o fragmento
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // constroi o gráfico com os dados padrões
        chartSetUp();

        // esconde a FloatingActionButton quando desliza a NestedScrollView para baixo
        pieChartScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) {
                    attachPieChartButton.hide();
                } else {
                    attachPieChartButton.show();
                }
            }
        });

        return view;
    }

    // metodo que trata os clicks dos botoes do frament
    @Override
    public void onClick(View v) {
        switch ( v.getId() ){
            // OnClickListener do botão que incrementa o numero de itens
            case R.id.pieChartAddItemImageView:
                if( items < TABLE_LIMIT_MAX ){
                    ++items;
                    itemsTextView.setText( String.format("%02d", items) );
                    buildTable();
                    chartSetUp();
                }
                else{
                    Toast.makeText(getContext(), R.string.max_limit,Toast.LENGTH_SHORT).show();
                }
                break;
            // OnClickListener do botão que decrementa o numero de itens
            case R.id.pieChartRemoveItemImageView:
                if( items > TABLE_LIMIT_MIN ){
                    --items;
                    itemsTextView.setText( String.format("%02d", items) );
                    buildTable();
                    chartSetUp();
                }
                else{
                    Toast.makeText(getContext(), R.string.min_limit,Toast.LENGTH_SHORT).show();
                }
                break;
            // OnClickListener do botão que faz o upload da tabela
            case R.id.uploadPieChartFloatingActionButton:
                // esconde o teclado
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                View view = getActivity().getCurrentFocus();
                if (view == null) {
                    view = new View(getActivity());
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                // verifica se é para atualizar ou enviar o gráfico
                if( uploadChart == true ){
                    // controi a AlertDialog de confirmação
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.sure_to_attach)
                            .setTitle(R.string.confirmation)
                            .setPositiveButton(R.string.attach, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // envia o gráfico para ReportEditor
                                    Bitmap bitmap = reportPieChart.getChartBitmap();
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
                } else {
                    // prepara o grávico para ser enviado
                    uploadChart = true;
                    attachPieChartButton.setImageResource(R.drawable.ic_upload);
                    chartSetUp();
                    Toast.makeText(getContext(), R.string.chart_updated, Toast.LENGTH_SHORT).show();
                    pieChartScrollView.scrollTo(0, pieChartScrollView.getTop());
                }

                break;
        }
    }

    // metodo responsavel por construir a tabela com os dados que preencherão o grafico
    private void buildTable() {

        editTextsList = new ArrayList<>();
        itemsLabelsEditTextList = new ArrayList<>();
        tableLayoutContainer.removeAllViews();
        Random z = new Random();
        EditText inputEditText;

        for( int i = 0 ; i < 2 ; i++ ){

            tableLayoutContainer.setStretchAllColumns(true);
            tableLayoutContainer.setBackgroundColor(Color.WHITE);
            TableRow tableRow = new TableRow(getContext());

            for( int j=0; j <= items ; j++ ){
                inputEditText = (EditText) getActivity().getLayoutInflater().inflate(R.layout.item_cell, null);
                inputEditText.setSelectAllOnFocus(true);
                inputEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        uploadChart = false;
                        attachPieChartButton.setImageResource(R.drawable.ic_refresh);
                    }
                    @Override
                    public void afterTextChanged(Editable s) {}
                });
                if( i == 0 && j > 0){
                    // configuração dos rotulos dos itens
                    inputEditText.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.table_label_columns));
                    inputEditText.setHint(String.format(getString(R.string.column_label) +" %d", j));
                    inputEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(LABEL_LENGTH_LIMIT)});
                    itemsLabelsEditTextList.add( inputEditText );
                    tableRow.addView( inputEditText );
                } else if( j > 0 && i > 0 ){
                    // configuração das células
                    inputEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    inputEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT );
                    inputEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(CELL_LENGTH_LIMIT)});
                    Integer num = z.nextInt(100);
                    inputEditText.setText( num.toString() );
                    editTextsList.add( inputEditText );
                    tableRow.addView( inputEditText );
                }
            }

            tableLayoutContainer.addView( tableRow );

        }
    }

    // metodo que configura o grafico
    private void chartSetUp() {

        reportPieChart = new PieChart( getContext() );
        reportPieChart.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.edit_text_border));

        chartFrameLayout.addView( reportPieChart );

        int[] myColors = new int[]{
                getResources().getColor(R.color.DAZZLING_BLUE),
                getResources().getColor(R.color.CELOSIA_ORANGE),
                getResources().getColor(R.color.FREESIA),
                getResources().getColor(R.color.CAYENNE),
                getResources().getColor(R.color.PLACID_BLUE),
                getResources().getColor(R.color.HEMLOCK),
                getResources().getColor(R.color.RADIANT_ORCHID),
                getResources().getColor(R.color.PALOMA),
                getResources().getColor(R.color.SAND),
                getResources().getColor(R.color.VIOLET_TULIP)};

        List<PieEntry> pieEntries = new ArrayList<>();
        float ref = 0;

        // preenchimento dos dados
        for( int i = 0 ; i < items ; i++ ){

            String s = editTextsList.get(i).getText().toString();
            float v = Float.valueOf(s);
            PieEntry pieEntry;

            if(itemsLabelsEditTextList.get(i).getText().toString().equals("")) {
                pieEntry = new PieEntry(v, itemsLabelsEditTextList.get(i).getHint().toString());
            } else {
                pieEntry = new PieEntry(v, itemsLabelsEditTextList.get(i).getText().toString());
            }

            pieEntries.add( pieEntry );
            ref += v;
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Ref: " + ref + "%");
        pieDataSet.setColors( myColors );
        PieData pieData = new PieData( pieDataSet );
        reportPieChart.setData( pieData );

        // PERSONALIZAÇÕES
        reportPieChart.getDescription().setEnabled(false);
        reportPieChart.setDragDecelerationEnabled(false); //on/off a rolagem do grafico após soltar
        reportPieChart.setHighlightPerTapEnabled(false);// sem destaque por interações com o grafico
        reportPieChart.setRotationEnabled(false);

        reportPieChart.getLegend().setWordWrapEnabled( true );

        reportPieChart.animateY(2000);

        reportPieChart.invalidate(); //o gráfico é atualizado e os dados fornecidos são desenhados
    }
}