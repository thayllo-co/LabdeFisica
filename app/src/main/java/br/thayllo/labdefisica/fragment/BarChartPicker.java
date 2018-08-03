package br.thayllo.labdefisica.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.FloatingActionButton.Behavior;
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
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.thayllo.labdefisica.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class BarChartPicker extends Fragment implements View.OnClickListener {

    public static final int TABLE_LIMIT_MIN = 1;
    public static final int TABLE_LIMIT_MAX = 10;
    public static final int CELL_LENGTH_LIMIT = 50;
    public static final int LABEL_LENGTH_LIMIT = 10;

    private int columns = 2;
    private int rows = 2;
    private TextView rowsTextView;
    private TextView columnsTextView;
    private ImageView addRowImageView;
    private ImageView removeRowImageView;
    private ImageView addColumnImageView;
    private ImageView removeColumnImageView;
    private FloatingActionButton attachBarChartButton;
    private TableLayout tableLayoutContainer;
    private BarChart reportBarChart;
    private List<List<EditText>> editTextsDoubleList;
    private List<EditText> rowsLabelsEditTextList;
    private List<EditText> columnsLabelsEditTextList;
    private NestedScrollView barChartScrollView;

    private FrameLayout chartFrameLayout;

    private boolean uploadChart = false;

    public BarChartPicker() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_bar_chart_picker, container, false);


        rowsTextView = view.findViewById(R.id.barChartRowsTextView);
        columnsTextView = view.findViewById(R.id.barChartColumnsTextView);
        addRowImageView = view.findViewById(R.id.barChartAddRowImageView);
        addColumnImageView = view.findViewById(R.id.barChartAddColumnImageView);
        removeRowImageView = view.findViewById(R.id.barChartRemoveRowImageView);
        removeColumnImageView = view.findViewById(R.id.barChartRemoveColumnImageView);
        attachBarChartButton = view.findViewById(R.id.uploadBarChartFloatingActionButton);
        tableLayoutContainer = view.findViewById(R.id.barChartInputsTableLayout);
        chartFrameLayout = view.findViewById(R.id.barChartFrameLayout);
        barChartScrollView = view.findViewById(R.id.barChartScrollView);


        // formata em dois caractetes o numero de linhas e colunas
        rowsTextView.setText( String.format("%02d", rows));
        columnsTextView.setText( String.format("%02d", columns));

        // define o OnClickListener implementado no metodo sobrescrito onClick
        addRowImageView.setOnClickListener( this );
        removeRowImageView.setOnClickListener( this );
        addColumnImageView.setOnClickListener( this );
        removeColumnImageView.setOnClickListener( this );
        attachBarChartButton.setOnClickListener( this );

        // constroi a primeira tabela
        buildTable();

        // linha requerida para não abrir o teclado assim que inflar o fragmento
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // constroi o gráfico com os dados padrões
        chartSetUp();

        // esconde a FloatingActionButton quando desliza a NestedScrollView para baixo
        barChartScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) {
                    attachBarChartButton.hide();
                } else {
                    attachBarChartButton.show();
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
            case R.id.barChartAddRowImageView:
                if( rows < TABLE_LIMIT_MAX ){
                    ++rows;
                    rowsTextView.setText( String.format("%02d", rows) );
                    buildTable();
                    chartSetUp();
                }
                else{
                    Toast.makeText(getContext(), R.string.max_limit,Toast.LENGTH_SHORT).show();
                }
                break;
            // OnClickListener do botão que decrementa o numero de linhas
            case R.id.barChartRemoveRowImageView:
                if( rows > TABLE_LIMIT_MIN ){
                    --rows;
                    rowsTextView.setText( String.format("%02d", rows) );
                    buildTable();
                    chartSetUp();
                }
                else{
                    Toast.makeText(getContext(), R.string.min_limit,Toast.LENGTH_SHORT).show();
                }
                break;
            // OnClickListener do botão que incrementa o numero de colunas
            case R.id.barChartAddColumnImageView:
                if( columns < TABLE_LIMIT_MAX ){
                    ++columns;
                    columnsTextView.setText( String.format("%02d", columns) );
                    buildTable();
                    chartSetUp();
                }
                else{
                    Toast.makeText(getContext(), R.string.max_limit,Toast.LENGTH_SHORT).show();
                }
                break;
            // OnClickListener do botão que decrementa o numero de colunas
            case R.id.barChartRemoveColumnImageView:
                if( columns > TABLE_LIMIT_MIN ){
                    --columns;
                    columnsTextView.setText( String.format("%02d", columns) );
                    buildTable();
                    chartSetUp();
                }
                else{
                    Toast.makeText(getContext(), R.string.min_limit,Toast.LENGTH_SHORT).show();
                }
                break;
            // OnClickListener do botão que faz o upload da tabela
            case R.id.uploadBarChartFloatingActionButton:
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
                                    Bitmap bitmap = reportBarChart.getChartBitmap();
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
                    attachBarChartButton.setImageResource(R.drawable.ic_upload);
                    chartSetUp();
                    Toast.makeText(getContext(), R.string.chart_updated, Toast.LENGTH_SHORT).show();
                    barChartScrollView.scrollTo(0, barChartScrollView.getTop());
                }
                break;
        }
    }

    // metodo responsavel por construir a tabela com os dados que preencherão o grafico
    public void buildTable() {

        editTextsDoubleList = new ArrayList<>();
        rowsLabelsEditTextList = new ArrayList<>();
        columnsLabelsEditTextList = new ArrayList<>();
        tableLayoutContainer.removeAllViews();
        Random z = new Random();

        for( int i = 0 ; i <= rows ; i++ ){

            ArrayList<EditText> cellEditTexts = new ArrayList<>();
            tableLayoutContainer.setStretchAllColumns(true);
            tableLayoutContainer.setBackgroundColor(Color.WHITE);
            TableRow tableRow = new TableRow(getContext());
            EditText inputEditText;

            for( int j=0; j <= columns ; j++ ){

                inputEditText = (EditText) getActivity().getLayoutInflater().inflate(R.layout.item_cell_editable, null);
                inputEditText.setSelectAllOnFocus(true);
                inputEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        uploadChart = false;
                        attachBarChartButton.setImageResource(R.drawable.ic_refresh);
                    }
                    @Override
                    public void afterTextChanged(Editable s) {}
                });

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
                    inputEditText.setHint(String.format(getString(R.string.column_label) +" %d", j));
                    inputEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(LABEL_LENGTH_LIMIT)});
                    columnsLabelsEditTextList.add( inputEditText );
                } else if( j== 0 && i > 0 ){
                    // configuração dos rotulos das linhas
                    inputEditText.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.table_label_rows));
                    inputEditText.setHint(String.format(getString(R.string.row_label) +" %d", i));
                    inputEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(LABEL_LENGTH_LIMIT)});
                    rowsLabelsEditTextList.add( inputEditText );
                } else {
                    // configuração das células
                    inputEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    inputEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT );
                    inputEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(CELL_LENGTH_LIMIT)});
                    Integer num = z.nextInt(100);
                    inputEditText.setText( num.toString() );
                    cellEditTexts.add( inputEditText );
                }
                tableRow.addView( inputEditText );

            }

            tableLayoutContainer.addView( tableRow );
            if( cellEditTexts.size() != 0 )
                editTextsDoubleList.add( cellEditTexts );

        }
    }

    // metodo que configura o grafico
    public void chartSetUp() {

        reportBarChart = new BarChart( getContext() );
        reportBarChart.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.edit_text_border));

        chartFrameLayout.removeAllViews();
        if( (rows*columns*50) < 450)
            chartFrameLayout.getLayoutParams().width = 450;
        else
            chartFrameLayout.getLayoutParams().width = (rows*columns*50);
        chartFrameLayout.addView( reportBarChart );

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

        List<BarDataSet> dataSetList = new ArrayList<>();

        // preenchimento dos dados
        for (int i = 0; i < rows; i++) {

            List<BarEntry> barEntries = new ArrayList<>();
            List<EditText> editTextList = editTextsDoubleList.get(i);

            for (int j = 0; j < columns; j++) {

                EditText editText = editTextList.get(j);
                String text = editText.getText().toString();
                if( rows == 1 )
                    barEntries.add(new BarEntry( j + 0.5f , Float.parseFloat( text )));
                else
                    barEntries.add(new BarEntry( j , Float.parseFloat( text )));
            }

            BarDataSet dataset;
            if(rowsLabelsEditTextList.get(i).getText().toString().equals("")) {
                dataset = new BarDataSet(barEntries, rowsLabelsEditTextList.get(i).getHint().toString());
            } else {
                dataset = new BarDataSet(barEntries, rowsLabelsEditTextList.get(i).getText().toString());
            }
            dataset.setColor(myColors[i]);
            dataSetList.add(dataset);
        }

        //
        float groupSpace = (0.05f * rows);
        float barSpace = 0.0f;
        float barWidth = 0.95f;
        BarData data = null;
        switch ( rows ){
            case 1:
                data = new BarData(dataSetList.get(0));
                break;
            case 2:
                data = new BarData(dataSetList.get(0), dataSetList.get(1));
                break;
            case 3:
                data = new BarData(dataSetList.get(0), dataSetList.get(1), dataSetList.get(2));
                break;
            case 4:
                data = new BarData(dataSetList.get(0), dataSetList.get(1), dataSetList.get(2), dataSetList.get(3));
                break;
            case 5:
                data = new BarData(dataSetList.get(0), dataSetList.get(1), dataSetList.get(2), dataSetList.get(3),
                        dataSetList.get(4));
                break;
            case 6:
                data = new BarData(dataSetList.get(0), dataSetList.get(1), dataSetList.get(2), dataSetList.get(3),
                        dataSetList.get(4), dataSetList.get(5));
                break;
            case 7:
                data = new BarData(dataSetList.get(0), dataSetList.get(1), dataSetList.get(2), dataSetList.get(3),
                        dataSetList.get(4), dataSetList.get(5), dataSetList.get(6));
                break;
            case 8:
                data = new BarData(dataSetList.get(0), dataSetList.get(1), dataSetList.get(2), dataSetList.get(3),
                        dataSetList.get(4), dataSetList.get(5), dataSetList.get(6), dataSetList.get(7));
                break;
            case 9:
                data = new BarData(dataSetList.get(0), dataSetList.get(1), dataSetList.get(2), dataSetList.get(3),
                        dataSetList.get(4), dataSetList.get(5), dataSetList.get(6), dataSetList.get(7), dataSetList.get(8));
                break;
            case 10:
                data = new BarData(dataSetList.get(0), dataSetList.get(1), dataSetList.get(2), dataSetList.get(3),
                        dataSetList.get(4), dataSetList.get(5), dataSetList.get(6), dataSetList.get(7), dataSetList.get(8),
                        dataSetList.get(9));
                break;

        }
        data.setBarWidth(barWidth); // set the width of each bar
        if( rows > 1){
            reportBarChart.setData(data);
            reportBarChart.groupBars(0f, groupSpace, barSpace);
        } else {
            reportBarChart.setData(data);
            reportBarChart.setFitBars( true );
        }

        // rotulos
        reportBarChart.getXAxis().setGranularityEnabled(true);
        reportBarChart.getXAxis().setGranularity((float)rows);
        reportBarChart.getXAxis().setDrawLabels(true);

        //BUG TO FIX!!!!!!!!!!!!!!!!!!!
        reportBarChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if( rows > 5 || columns > 5 ){ //PROBLEMA COM OS LABELS DAS COLUNAS QUANDO > 5 (CONCERTAR)!!!!
                    return "";
                } else if( value % rows == 0 ){
                    int index = (int) (value/rows);

                    if( index >= 0 && index < columnsLabelsEditTextList.size()){
                        if(columnsLabelsEditTextList.get( index ).getText().toString().equals(""))
                            return columnsLabelsEditTextList.get( index ).getHint().toString();
                        else
                            return columnsLabelsEditTextList.get( index ).getText().toString();
                    }
                    //return String.valueOf(value);
                    return "";
                }
                else {
                    //return String.valueOf(value);//
                    return "";
                }}
        });

        // ALGUMAS PERSONALIZAÇÕES

        reportBarChart.getXAxis().setAxisMinimum(0f);
        reportBarChart.getXAxis().setAxisMaximum(columns*rows);
        reportBarChart.getAxisRight().setEnabled( false );
        reportBarChart.getAxisLeft().setAxisMinimum(0f);
        reportBarChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        reportBarChart.getXAxis().setDrawGridLines(false);
        reportBarChart.getXAxis().setCenterAxisLabels(true);
        reportBarChart.getDescription().setEnabled(false);
        reportBarChart.setDragEnabled(false); //on/off o arrastar (panorâmica) para o gráfico
        reportBarChart.setScaleEnabled(false); //on/off a escala para o gráfico em ambos os eixos
        reportBarChart.setPinchZoom(false); //on/off o zoom de pinça está ativado
        reportBarChart.setDoubleTapToZoomEnabled(false); //on/off o duplo toque
        reportBarChart.setDragDecelerationEnabled(false); //on/off a rolagem do grafico após soltar
        reportBarChart.setHighlightPerDragEnabled( false ); // sem destaque por interações com o grafico
        reportBarChart.setHighlightFullBarEnabled( false);
        reportBarChart.setHighlightPerTapEnabled( false );

        reportBarChart.getLegend().setForm(Legend.LegendForm.CIRCLE);
        reportBarChart.getLegend().setWordWrapEnabled( true );

        reportBarChart.animateY(2000);

        reportBarChart.invalidate(); //o gráfico é atualizado e os dados fornecidos são desenhados
    }
}
