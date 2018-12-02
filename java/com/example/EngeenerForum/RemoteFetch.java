package com.example.EngeenerForum;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Locale;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;


public class RemoteFetch {

    private static final String ENGEENER_FORUM_API =
            "http://192.168.31.140/site/api.php?api_key=mudofan&login=Lifter";

    public static JSONObject getJSON(Context context){
        try {
            URL url = new URL(ENGEENER_FORUM_API);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        //    connection.addRequestProperty("x-api-key", context.getString(R.string.open_weather_maps_app_id));
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            // This value will be 404 if the request was not
            // successful
            //if(data.getInt("cod") != 200){
          //      return null;
           // }

            return data;
        }catch(Exception e){
            return null;
        }
    }
  // private void renderJson(JSONObject json){
  //     try {
  //         cityField.setText(json.getString("name").toUpperCase(Locale.US) +
  //                 ", " +
  //                 json.getJSONObject("sys").getString("country"));
  //         Log.d("ППЦ", "renderWeather: " +cityField.getText());
  //         JSONObject details = json.getJSONArray("weather").getJSONObject(0);
  //         JSONObject main = json.getJSONObject("main");

  //         currentTemperatureField.setText( main.getInt("temp")+ " ℃");
  //         Log.d("ППЦ", "renderWeather: " +currentTemperatureField.getText());
  //         DateFormat df = DateFormat.getDateTimeInstance();



  //         setWeatherIcon(details.getInt("id"),
  //                 json.getJSONObject("sys").getLong("sunrise") * 1000,
  //                 json.getJSONObject("sys").getLong("sunset") * 1000);

  //     }catch(Exception e){
  //         Log.e("SimpleWeather", "One or more fields not found in the JSON data");
  //     }
  // }
}
