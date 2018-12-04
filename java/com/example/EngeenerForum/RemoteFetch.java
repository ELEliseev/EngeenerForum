package com.example.EngeenerForum;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;


public class RemoteFetch {

    private static final String ENGEENER_FORUM_API = "http://192.168.31.140/site/api.php?api_key=mudofan&login=Lifter";
    private static final String LOG_TAG ="myLogs" ;


    @Nullable
    public static JSONObject getJSON(){
        try {

            URL url = new URL(ENGEENER_FORUM_API);
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();



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
            //Log.d(LOG_TAG, "читаем в буф"+data);
            return data;
        }catch(Exception e){

            return null;
        }
    }

}
