package com.sciapps;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.sciapps.model.Node;
import com.sciapps.model.NodeComparator;
import com.sciapps.utils.Graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity implements OnChartValueSelectedListener {
    private static final String REQUEST_TITLE = "Select a file.";
    private static final int PICK_FILE_REQUEST_CODE = 8778;
    private ImageView fileNavigation = null;
    private ImageView chartNavigation = null;
    private LineChart chart = null;
    private XAxis xAxis = null;
    private YAxis leftAxis = null;
    private List<Node> csvData = null;
    private Graph csvReferenceData = null;
    private Graph presentData = null;
    private Map<String, Integer> colors = null;
    private boolean run = true;
    private EditText resultsView = null;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ActionBar actionBar = getActionBar();
        actionBar.setTitle(getString(R.string.app_name));
        actionBar.setDisplayHomeAsUpEnabled(false);

        csvData = new ArrayList<Node>();
        csvReferenceData = new Graph();
        presentData = new Graph();
        colors = new HashMap<String, Integer>();
        colors.put("Al", Color.RED);
        colors.put("Cu", Color.GREEN);
        colors.put("Fe", Color.BLUE);
        colors.put("Mg", Color.YELLOW);

        resultsView = (EditText) findViewById(R.id.results_view);
        fileNavigation = (ImageView) findViewById(R.id.navigation_file);
        chartNavigation = (ImageView) findViewById(R.id.navigation_chart);
        chart = (LineChart) findViewById(R.id.chart);

        chart.setOnChartValueSelectedListener(this);
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);

        chart.setDragDecelerationFrictionCoef(0.9f);

        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setHighlightPerDragEnabled(true);

        chart.setPinchZoom(true);

        chart.setBackgroundColor(Color.WHITE);

        xAxis = chart.getXAxis();
        xAxis.setTextSize(11f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setGranularityEnabled(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        chart.getAxisRight().setEnabled(false);

        fileNavigation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startActivityForResult(Intent.createChooser(new Intent()
                        .setType("*/*")
                        .setAction(Intent.ACTION_GET_CONTENT), REQUEST_TITLE), PICK_FILE_REQUEST_CODE);
            }
        });

        chartNavigation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                new LocateElementsAsync().execute();
            }
        });

        initializeChart();
    }

    private void initializeChart() {
        InputStream inputStream = getResources().openRawResource(R.raw.reference_values);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        while (run) {
            String line = null;
            try {
                line = bufferedReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (line != null) {
                csvReferenceData.buildGraph(line);
            } else
                run = false;
        }

        run = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            class LoadFileAsync extends AsyncTask<Void, Void, String> {
                ProgressDialog progressDialog;

                @Override
                protected void onPreExecute() {
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("Please wait...");
                    progressDialog.show();
                    super.onPreExecute();
                }

                protected String doInBackground(Void... arg0) {
                    readFile(data.getData());
                    return null;
                }

                @Override
                protected void onPostExecute(String string) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                }
            }

            new LoadFileAsync().execute();
        }
    }

    private void readFile(Uri uri) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(getContentResolver()
                    .openInputStream(uri)));
            while (run) {
                String line = bufferedReader.readLine();
                if (line != null) {
                    String[] data = line.split(",");
                    if (!data[0].equals("wavelength") || !data[1].equals("intensity"))
                        csvData.add(new Node("", data[0], data[1]));
                } else
                    run = false;
            }

            run = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        chart.centerViewToAnimated(e.getX(), e.getY(), chart.getData()
                .getDataSetByIndex(h.getDataSetIndex())
                .getAxisDependency(), 500);
    }

    @Override
    public void onNothingSelected() {

    }

    class LocateElementsAsync extends AsyncTask<Void, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
            super.onPreExecute();
        }

        protected String doInBackground(Void... arg0) {
            StringBuffer stringBuffer = new StringBuffer();
            Collections.sort(csvData, new NodeComparator());
            List<Double> referenceIntensities = new ArrayList<Double>();
            double maxIntensity = 0.0;
            String referenceElement = "";
            String result = "";

            // Loop through all collected data.
            for (int i = 0; i < csvData.size(); i++) {
                maxIntensity = Math
                        .round(Double.parseDouble(csvData.get(0).getIntensity()) * 100.0) / 100.0;
                if (csvReferenceData.containsWaveLength(csvData.get(i).getWavelength())) {
                    // If yes retrieve the element.
                    referenceElement = csvReferenceData.getReferenceElement();
                    // If yes retrieve all referenceIntensities for that reference data.
                    referenceIntensities = csvReferenceData
                            .getReferenceIntensities(referenceElement);

                    for (int N = 0; N < referenceIntensities.size(); N++) {
                        double percentOfMax = (referenceIntensities.get(N) * maxIntensity);
                        double range = Math.abs(percentOfMax - Double
                                .parseDouble(csvData.get(i).getIntensity()));

                        if (range <= .3 || range >= .3) {
                            String wavelength = String
                                    .valueOf(Math.round(Double.parseDouble(csvData.get(i)
                                            .getWavelength()) * 100.0) / 100.0);
                            String intensity = String.valueOf(Math.round(Double
                                    .parseDouble(csvData.get(i).getIntensity()) * 100.0) / 100.0);
                            presentData.addNode(referenceElement, wavelength, intensity);
                            count++;
                            if (count == (
                                    referenceIntensities.size() * csvReferenceData
                                            .getElementCount(referenceElement))) {
                                stringBuffer.append(result)
                                        .append(" ").append(referenceElement)
                                        .append(" has ")
                                        .append(count * csvReferenceData
                                                .getElementCount(referenceElement))
                                        .append(" points worth of likelihood. ")
                                        .append("\n");
                            }
                        }
                    }
                }
            }
            count = 0;

            return stringBuffer.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            resultsView.setText(result.toString());
            setData();
            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

    private void setData() {
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        LineDataSet lineDataSet = null;
        String axisLabel = "";

        leftAxis.setAxisMaximum((float) presentData.getMaxIntensity());
        leftAxis.setAxisMinimum(-30f);
        xAxis.setAxisMaximum((float) presentData.getMaxWaveLength());

        for (int i = 0; i < presentData.getAdjacencyListSize(); i++) {
            LinkedList<Node> elementList = presentData.getAdjacencyList().get(i);
            ArrayList<Entry> yValues = new ArrayList<Entry>();

            for (int v = 0; v < elementList.size(); v++) {
                System.out.println("ELEMENTS " + elementList.get(v)
                        .getElement() + " WAVELENGTH " + elementList.get(v)
                        .getWavelength() + " INTENSITY " + elementList.get(v).getIntensity());
                axisLabel = elementList.get(v).getElement();
                yValues.add(new Entry(Float.parseFloat(elementList.get(v).getIntensity()), Float
                        .parseFloat(elementList.get(v).getWavelength())));
            }

            lineDataSet = new LineDataSet(yValues, axisLabel);
            lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineDataSet.setColor(colors.get(axisLabel));
            lineDataSet.setCircleColor(Color.DKGRAY);
            lineDataSet.setLineWidth(2f);
            lineDataSet.setCircleRadius(3f);
            lineDataSet.setFillAlpha(65);
            lineDataSet.setDrawCircleHole(false);
            dataSets.add(lineDataSet);
        }

        LineData data = new LineData(dataSets);
        chart.setData(data);
        chart.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_about, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.message)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });

                builder.create();
                builder.show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
