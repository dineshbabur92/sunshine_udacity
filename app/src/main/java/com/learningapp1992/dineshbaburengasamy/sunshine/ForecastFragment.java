package com.learningapp1992.dineshbaburengasamy.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private List<String> weatherData;
    private ArrayAdapter<String> listFiller;
    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater){
        menuInflater.inflate(R.menu.forecastfragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id==R.id.action_refresh){
            String preferenceValue = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.location_key),getString(R.string.location_pref_default));
            FetchWeatherTask fetchWeatherTask= new FetchWeatherTask();
            fetchWeatherTask.execute(preferenceValue);
            Log.v(LOG_TAG,"Preference Value: " + preferenceValue);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            /* myself
            ArrayList<String> weatherData=new ArrayList<>();

            weatherData.add("Today - Sunny - 88/63");
            weatherData.add("Tommorrow - Foggy - 70/46");
            weatherData.add("Wednesday - Cloudy - 74/63");
            weatherData.add("Thursday - Rainy - 64/51");
            weatherData.add("Friday - Foggy- 70/46");
            weatherData.add("Saturday - Sunny - 76/68");
            */
        String[] weatherData_them={"Today - Sunny - 88/63",
                "Tommorrow - Foggy - 70/46",
                "Wednesday - Cloudy - 74/63",
                "Thursday - Rainy - 64/51",
                "Friday - Foggy- 70/46",
                "Saturday - Sunny - 76/68"
        };
        weatherData=new ArrayList<String>(Arrays.asList(weatherData_them));

        listFiller = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast, R.id.list_item_forecast_textview, weatherData);

        ListView listView = (ListView)rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(listFiller);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*Context context = getActivity().getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                CharSequence toastDescription = "Item Clicked!";

                Toast toast = Toast.makeText(context, toastDescription, duration);*/

                String forecast = listFiller.getItem(position);
               // Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent( getActivity(), DetailedActivity.class).putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }
        });

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String,Void,String[]>{

        private final String LOG_TAG=FetchWeatherTask.class.getSimpleName();

        public String getReadableDataTime(long time){
            SimpleDateFormat simpleDateFormat= new SimpleDateFormat("EEE MMM dd");
            return simpleDateFormat.format(time);
        }

        public String getRoundOffTemp(Double min, Double max){
            return (long)Math.round(min) + "/" + (long)Math.round(max);
        }

        public String[] getJsonResultAsList(String inputJsonString, int numDays) throws JSONException{


            final String WEATHER_CONST = "weather";
            final String LIST_CONST = "list";
            final String TEMP_CONST = "temp";
            final String MAX_CONST = "max";
            final String MIN_CONST = "min";
            final String DESC_CONST = "main";

            String jsonResultList[] = new String[numDays];
            JSONObject rootJsonObject = new JSONObject(inputJsonString);

            JSONArray weatherArray = rootJsonObject.getJSONArray(LIST_CONST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            for(int i=0; i < weatherArray.length(); i++){
                String julianDateValue = null;
                String description = null;
                String temperatureValues = null;

                long dateTime;

                dateTime= dayTime.setJulianDay(julianStartDay + i);
                julianDateValue = getReadableDataTime(dateTime);

                JSONObject dayObject = weatherArray.getJSONObject(i);

                JSONObject tempObject = dayObject.getJSONObject(TEMP_CONST);
                temperatureValues = getRoundOffTemp(tempObject.getDouble(MIN_CONST), tempObject.getDouble(MAX_CONST));

                JSONArray weatherInfoArray = dayObject.getJSONArray(WEATHER_CONST);
                description = weatherInfoArray.getJSONObject(0).getString(DESC_CONST);

                jsonResultList[i] = julianDateValue + " - " + description + " - " + temperatureValues;
            }
            for (String s : jsonResultList) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return jsonResultList;
        }

        protected String[] doInBackground(String... params) {

            if(params.length == 0){
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader br = null;
            String jsonString = null;

            String format = "json";
            String units = "metric";
            int numDays = 7;
            try{
                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");

                final String FORECAST_BASE_URL="http://api.openweathermap.org/data/2.5/forecast/daily?";

                final String QUERY_PARAM="q";
                final String FORMAT_PARAM="mode";
                final String UNITS_PARAM="units";
                final String DAYS_PARAM="cnt";

                Uri uri= Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM,params[0])
                        .appendQueryParameter(FORMAT_PARAM,format)
                        .appendQueryParameter(UNITS_PARAM,units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .build();

                url=new URL(uri.toString());

                Log.v(LOG_TAG, "Built URI: " + uri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                String line = null;
                StringBuffer buffer = new StringBuffer();


                InputStream inputStream = urlConnection.getInputStream();
                if(inputStream==null){
                    return null;
                }
                //br= new InputStreamReader(urlConnection.getInputStream());
                br =  new BufferedReader(new InputStreamReader(inputStream));
                while((line=br.readLine())!=null){
                    buffer.append(line + "\n");
                }
                if(buffer.length()==0){
                    return null;
                }
                jsonString = buffer.toString();
                Log.v(LOG_TAG, "Forecast JSON String: " + jsonString);
            }
            catch(IOException e){
                Log.e(LOG_TAG, "Error", e);
                return null;
            }
            finally {
                if(urlConnection!=null){
                    urlConnection.disconnect();
                }
                if(br!=null){
                    try {
                        br.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing bufferedreader reference", e);
                    }
                }
            }

            try{
                return getJsonResultAsList(jsonString, numDays);

            }
            catch(JSONException e){
                Log.e(LOG_TAG,"Error in getting jsonstrings as list",e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            //super.onPostExecute(strings);
            if(strings != null){
                listFiller.clear();
                for(String s: strings){
                    listFiller.add(s);
                }
            }
        }
    }
}