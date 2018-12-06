package com.example.engeenerforum;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

import android.support.annotation.Nullable;



public class RemoteFetch {

    private static final String ENGEENER_FORUM_API = "http://192.168.31.140/site/api.php?api_key=lifter&login=";

   // private static final String LOG_TAG ="myLogs" ;






    @Nullable
    public static JSONObject getJSON(String login){

        try {

            URL url = new URL(String.format(ENGEENER_FORUM_API+login));
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();
         //   Log.d(LOG_TAG, "читаем "+login);


            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

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
      //      Log.d(LOG_TAG, "читаем в буф"+login);
            return data;
        }catch(Exception e){

            return null;
        }
    }

}
