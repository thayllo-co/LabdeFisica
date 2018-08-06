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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.thayllo.labdefisica.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LineChartPicker extends Fragment implements View.OnClickListener {

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
    private FloatingActionButton attachLineChartButton;
    private TableLayout tableLayoutContainer;
    private LineChart reportLineChart;
    private List<List<EditText>> editTextsDoubleList;
    private List<EditText> rowsLabelsEditTextList;
    private List<EditText> columnsLabelsEditTextList;
    private NestedScrollView lineChartScrollView;

    private FrameLayout chartFrameLayout;

    private boolean uploadChart = false;


    public LineChartPicker() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_line_chart_picker, container, false);

        rowsTextView = view.findViewById(R.id.lineChartRowsTextView);
        columnsTextView = view.findViewById(R.id.lineChartColumnsTextView);
        addRowImageView = view.findViewById(R.id.lineChartAddRowImageView);
        addColumnImageView = view.findViewById(R.id.lineChartAddColumnImageView);
        removeRowImageView = view.findViewById(R.id.lineChartRemoveRowImageView);
        removeColumnImageView = view.findViewById(R.id.lineChartRemoveColumnImageView);
        attachLineChartButton = view.findViewById(R.id.uploadLineChartFloatingActionButton);
        tableLayoutContainer = view.findViewById(R.id.lineChartInputsTableLayout);
        chartFrameLayout = view.findViewById(R.id.lineChartFrameLayout);
        lineChartScrollView = view.findViewById(R.id.lineChartScrollView);


        // formata em dois caractetes o numero de linhas e colunas
        rowsTextView.setText( String.format("%02d", rows));
        columnsTextView.setText( String.format("%02d", columns));

        // define o OnClickListener implementado no metodo sobrescrito onClick
        addRowImageView.setOnClickListener( this );
        removeRowImageView.setOnClickListener( this );
        addColumnImageView.setOnClickListener( this );
        removeColumnImageView.setOnClickListener( this );
        attachLineChartButton.setOnClickListener( this );

        // constroi a primeira tabela
        buildTable();

        // linha requerida para não abrir o teclado assim que inflar o fragmento
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // constroi o gráfico com os dados padrões
        chartSetUp();

        // esconde a FloatingActionButton quando desliza a NestedScrollView para baixo
        lineChartScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) {
                    attachLineChartButton.hide();
                } else {
                    attachLineChartButton.show();
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
            case R.id.lineChartAddRowImageView:
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
            case R.id.lineChartRemoveRowImageView:
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
            case R.id.lineChartAddColumnImageView:
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
            case R.id.lineChartRemoveColumnImageView:
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
            case R.id.uploadLineChartFloatingActionButton:
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
                                    Bitmap bitmap = reportLineChart.getChartBitmap();
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
                    attachLineChartButton.setImageResource(R.drawable.ic_upload);
                    chartSetUp();
                    Toast.makeText(getContext(), R.string.chart_updated, Toast.LENGTH_SHORT).show();
                    lineChartScrollView.scrollTo(0, lineChartScrollView.getTop());
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

                inputEditText = (EditText) getActivity().getLayoutInflater().inflate(R.layout.item_cell, null);
                inputEditText.setSelectAllOnFocus(true);
                inputEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        uploadChart = false;
                        attachLineChartButton.setImageResource(R.drawable.ic_refresh);
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
    public void chartSetUp(){

        reportLineChart = new LineChart( getContext() );
        reportLineChart.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.edit_text_border));

        chartFrameLayout.removeAllViews();
        if( (columns*100) < 450)
            chartFrameLayout.getLayoutParams().width = 450;
        else
            chartFrameLayout.getLayoutParams().width = (columns*100);

        chartFrameLayout.addView( reportLineChart );

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

        List<ILineDataSet> dataSetList = new ArrayList<>();

        // preenchimento dos dados
        for (int i = 0; i < rows; i++) {

            List<Entry> entries = new ArrayList<>();
            List<EditText> editTextList = editTextsDoubleList.get(i);

            for (int j = 0; j < columns; j++) {

                EditText editText = editTextList.get(j);
                String text = editText.getText().toString();
                entries.add(new Entry(j,Float.valueOf(text)));
            }

            LineDataSet dataset;
            if(rowsLabelsEditTextList.get(i).getText().toString().equals("")) {
                dataset = new LineDataSet(entries, rowsLabelsEditTextList.get(i).getHint().toString());
            } else {
                dataset = new LineDataSet(entries, rowsLabelsEditTextList.get(i).getText().toString());
            }

            dataset.setAxisDependency(YAxis.AxisDependency.LEFT);
            dataset.setColor(myColors[i]);
            dataset.setCircleColor(myColors[i]);
            dataSetList.add(dataset);
        }

        LineData data = new LineData(dataSetList);
        reportLineChart.setData( data );

        // rotulos
        reportLineChart.getXAxis().setGranularityEnabled(true);
        reportLineChart.getXAxis().setGranularity(1f);
        reportLineChart.getXAxis().setDrawLabels(true);

        reportLineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if( value >= 0 && value < columnsLabelsEditTextList.size()){
                    if(columnsLabelsEditTextList.get((int) value).getText().toString().equals(""))
                        return columnsLabelsEditTextList.get((int) value).getHint().toString();
                    else
                        return columnsLabelsEditTextList.get((int) value).getText().toString();
                }
                return "";
            }
        });

        //PERSONALIZAÇÕES
        reportLineChart.getXAxis().setAxisMinimum(-0.25f);
        reportLineChart.getXAxis().setAxisMaximum(columns-0.75f);
        reportLineChart.getAxisRight().setEnabled( false );
        reportLineChart.getAxisLeft().setAxisMinimum(0f);
        reportLineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        reportLineChart.getXAxis().setDrawGridLines(false);
        reportLineChart.getDescription().setEnabled(false);
        reportLineChart.setDragEnabled(false); //on/off o arrastar (panorâmica) para o gráfico
        reportLineChart.setScaleEnabled(false); //on/off a escala para o gráfico em ambos os eixos
        reportLineChart.setPinchZoom(false); //on/off o zoom de pinça está ativado
        reportLineChart.setDoubleTapToZoomEnabled(false); //on/off o duplo toque
        reportLineChart.setDragDecelerationEnabled(false); //on/off a rolagem do grafico após soltar
        reportLineChart.setHighlightPerDragEnabled( false ); // sem destaque por interações com o grafico
        reportLineChart.setHighlightPerTapEnabled( false );

        reportLineChart.getLegend().setForm(Legend.LegendForm.CIRCLE);
        reportLineChart.getLegend().setWordWrapEnabled( true );

        reportLineChart.animateY(2000);

        reportLineChart.invalidate(); //o gráfico é atualizado e os dados fornecidos são desenhados
    }
}
