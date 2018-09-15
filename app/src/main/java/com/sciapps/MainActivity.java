package com.sciapps;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.sciapps.model.Node;
import com.sciapps.utils.Graph;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


/*
 *
 * 1. Updated the OnClickListeners to allow for easier code reuse. Can add another button with same functionality
 *    easily.
 * 2. Moved the LoadFileAsync out of the method body and consolidated the readFile and initializeChart methods into
 *   a single readFile method.
 *
 * */
public class MainActivity extends Activity {
    private static final String REQUEST_TITLE = "Select a file.";
    private static final int PICK_FILE_REQUEST_CODE = 8778;
    private ImageView fileNavigation = null;
    private ImageView chartNavigation = null;
    private LineChart chart = null;
    private XAxis xAxis = null;
    private YAxis leftAxis = null;
    private List<Node> csvData = null;
    private Graph csvReferenceData = null;
    private Map<String, Integer> colors = null;
    private boolean run = true;
    private ListView resultsListView = null;

    private View.OnClickListener fileNavigationOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            launchFileChooser();
        }
    };

    private View.OnClickListener chartNavigationOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new LocateElementsAsync().execute();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ActionBar actionBar = getActionBar();
        actionBar.setTitle(getString(R.string.app_name));
        actionBar.setDisplayHomeAsUpEnabled(false);

        csvData = new ArrayList<Node>();
        csvReferenceData = new Graph();
        colors = new HashMap<String, Integer>();
        colors.put("Al", Color.RED);
        colors.put("Cu", Color.GREEN);
        colors.put("Fe", Color.BLUE);
        colors.put("Mg", Color.YELLOW);

        resultsListView = (ListView) findViewById(R.id.results_list_view);

        fileNavigation = (ImageView) findViewById(R.id.navigation_file);
        fileNavigation.setOnClickListener(fileNavigationOnClickListener);

        chartNavigation = (ImageView) findViewById(R.id.navigation_chart);
        chartNavigation.setOnClickListener(chartNavigationOnClickListener);

        chart = (LineChart) findViewById(R.id.chart);

        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);

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
        readFile(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            readFile(data.getData());
        }
    }

    private void launchFileChooser() {
        startActivityForResult(Intent.createChooser(new Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT), REQUEST_TITLE), PICK_FILE_REQUEST_CODE);
    }

    private void readFile(Uri uri) {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;

        try {
            if (uri == null) {
                inputStream = getResources().openRawResource(R.raw.reference_values);
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            } else {
                bufferedReader = new BufferedReader(new InputStreamReader(getContentResolver()
                        .openInputStream(uri)));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        new LoadFileAsync().execute(bufferedReader);
    }

    class LoadFileAsync extends AsyncTask<BufferedReader, Graph, List<Node>> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
            super.onPreExecute();
        }

        protected List<Node> doInBackground(BufferedReader... bufferedReaders) {
            List<Node> csvData = new ArrayList<Node>();
            Graph csvReferenceData = new Graph();
            BufferedReader bufferedReader = null;
            int option = 0;
            try {
                bufferedReader = bufferedReaders[0];
                while (run) {
                    String line = bufferedReader.readLine();
                    if (line != null) {
                        String[] data = line.split(",");
                        if (data.length == 3) {
                            option = 3;
                            csvReferenceData.buildGraph(data[0], data[1], data[2]);
                        } else if (data.length == 2) {
                            option = 2;
                            if (!data[0].equals("wavelength") || !data[1].equals("intensity")) {
                                csvData.add(new Node(Math
                                        .round(Double.parseDouble(data[0]) * 100.0) / 100.0, Math
                                        .round(Double.parseDouble(data[1]) * 100.0) / 100.0));
                            }
                        }
                    } else {
                        run = false;

                        if (option == 3)
                            publishProgress(csvReferenceData);
                        else if (option == 2)
                            return csvData;
                    }
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
            return null;
        }

        protected void onProgressUpdate(Graph... graph) {
            csvReferenceData = graph[0];
        }

        @Override
        protected void onPostExecute(List<Node> list) {
            if (list != null)
                csvData = list;

            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

    class LocateElementsAsync extends AsyncTask<Void, Void, Graph> {
        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
            super.onPreExecute();
        }

        protected Graph doInBackground(Void... arg0) {
            Graph presentDataGraph = new Graph();
            String referenceElement = null;
            double maximumIntensity = 0.0;
            double minimumIntensity = 0.0;
            boolean containsWaveLength = true;
            // <Node, Reference Intensity> ....
            SortedMap<Double, Node> matchingWavelengths = new TreeMap<Double, Node>();
            /*
            * Loops through the list containing the data referencing the wavelength value against the reference
            * wavelength value contained in the graph data structure.
            * */
            for (int i = 0; i < csvData.size(); i++) {
                containsWaveLength = csvReferenceData.containsWavelength(5.0, csvData.get(i).getWavelength());

                if (referenceElement == null)
                    referenceElement = csvReferenceData.getReferenceElement();

                /*
                * When a match is found the reference intensity id retrieved from the graph and placed in the
                * data node. The node is then placed in a sortedMap(ascending by key) with the intensity value as the key.
                * */
                if (containsWaveLength) {
                    if (referenceElement.equals(csvReferenceData.getReferenceElement())) {
                        csvData.get(i).setElement(referenceElement);
                        csvData.get(i).setReferenceIntensity(csvReferenceData.getReferenceIntensity());
                        matchingWavelengths.put(csvData.get(i).getIntensity(), csvData.get(i));
                    }
                }
            }

            /*
            * After looping through the list and seperating out the nodes with matching wavelengths the sortedMap
            * is iterated through. The max and min intensities are saved in variables and used to normalize the
            * intensity values for that particular collection of matching values.
            * */
            Iterator iterator = matchingWavelengths.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry matchedData = (Map.Entry) iterator.next();
                Node node = (Node) matchedData.getValue();
                Double referenceIntensity = node.getReferenceIntensity();
                if (maximumIntensity == 0.0) {
                    maximumIntensity = ((TreeMap<Double, Node>) matchingWavelengths).lastKey();
                    minimumIntensity = node.getIntensity();
                }

                double normalizedIntensity = presentDataGraph.normalize(node.getIntensity(), minimumIntensity, maximumIntensity);

                /*
                * Next the newly normalized intensity value percent difference is checked against
                 * the reference value stored in the current node. If it passes then the node is
                 * placed in a new graph object.
                * */
                if (presentDataGraph.percentDifference(.2, normalizedIntensity, referenceIntensity)) {
                    if (referenceElement.equals("Al")) {
                        node.setIcon(R.drawable.aluminum);
                    } else if (referenceElement.equals("Cu")) {
                        node.setIcon(R.drawable.copper);
                    } else if (referenceElement.equals("Fe")) {
                        node.setIcon(R.drawable.iron);
                    } else if (referenceElement.equals("Mg")) {
                        node.setIcon(R.drawable.magnesium);
                    }

                    node.setNormalizedIntensity(normalizedIntensity);
                    presentDataGraph.addNode(referenceElement, node);
                }
            }

            return presentDataGraph;
        }

        @Override
        protected void onPostExecute(Graph result) {
            Graph presentDataGraph = result;
            setData(presentDataGraph);
            ResultsAdapter resultsAdapter = new ResultsAdapter(MainActivity.this, R.layout.item_element, presentDataGraph.getResults());
            resultsListView.setAdapter(resultsAdapter);

            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

    private void setData(Graph presentDataGraph) {
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        LineDataSet lineDataSet = null;
        String axisLabel = "";

        leftAxis.setAxisMaximum((float) 1000f);
        leftAxis.setAxisMinimum((float) 0f);
        xAxis.setAxisMaximum((float) 1000f);

        List<Node> elementList = (List<Node>) presentDataGraph.getAllElements();
        ArrayList<Entry> yValuesAl = new ArrayList<Entry>();
        ArrayList<Entry> yValuesCu = new ArrayList<Entry>();
        ArrayList<Entry> yValuesMg = new ArrayList<Entry>();
        ArrayList<Entry> yValuesFe = new ArrayList<Entry>();

        for (int v = 0; v < elementList.size(); v++) {
            axisLabel = (String) elementList.get(v).getElement();

            if (axisLabel.equals("Al")) {
                yValuesAl.add(new Entry((float) elementList.get(v).getIntensity(), (float) elementList.get(v).getWavelength()));
                lineDataSet = new LineDataSet(yValuesAl, axisLabel);
            } else if (axisLabel.equals("Cu")) {
                yValuesCu.add(new Entry((float) elementList.get(v).getIntensity(), (float) elementList.get(v).getWavelength()));
                lineDataSet = new LineDataSet(yValuesCu, axisLabel);
            } else if (axisLabel.equals("Mg")) {
                yValuesMg.add(new Entry((float) elementList.get(v).getIntensity(), (float) elementList.get(v).getWavelength()));
                lineDataSet = new LineDataSet(yValuesMg, axisLabel);
            } else if (axisLabel.equals("Fe")) {
                yValuesFe.add(new Entry((float) elementList.get(v).getIntensity(), (float) elementList.get(v).getWavelength()));
                lineDataSet = new LineDataSet(yValuesFe, axisLabel);
            }

            if (v == elementList.size() - 1) {
                dataSets.add(lineDataSet);
                lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                lineDataSet.setColor(colors.get(axisLabel));
                lineDataSet.setCircleColor(Color.DKGRAY);
                lineDataSet.setLineWidth(2f);
                lineDataSet.setCircleRadius(3f);
                lineDataSet.setFillAlpha(65);
                lineDataSet.setDrawCircleHole(false);
            }
        }

        LineData data = new LineData(dataSets);
        chart.setData(data);
        chart.invalidate();
    }

    public class ResultsAdapter extends ArrayAdapter<Node> {
        List<Node> resultList = new ArrayList<Node>();

        public ResultsAdapter(Context context, int resourceId, List<Node> resultList) {
            super(context, resourceId, resultList);
            this.resultList = resultList;
        }

        @Override
        public int getCount() {
            return super.getCount();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_element, null);

            TextView intensityTextView = (TextView) convertView
                    .findViewById(R.id.intensity_text_view);
            TextView linesTextView = (TextView) convertView.findViewById(R.id.lines_text_view);
            ImageView iconImageView = (ImageView) convertView.findViewById(R.id.element_image_view);

            intensityTextView.setText(String.valueOf(resultList.get(position).getIntensity()));
            linesTextView.setText(String.valueOf(resultList.get(position).getLines()));

            iconImageView.setImageResource(resultList.get(position).getIcon());

            return convertView;
        }
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
