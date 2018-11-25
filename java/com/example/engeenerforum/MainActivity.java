package com.example.engeenerforum;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.icu.text.MessagePattern;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW;
import static android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE;
import static android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final static int FCR = 1;
    WebView webView;
    private String mCM;
    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;
    final int MENU_SAVE_ID = 1;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (Build.VERSION.SDK_INT >= 21) {
            Uri[] results = null;

            //Check if response is positive
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == FCR) {

                    if (null == mUMA) {
                        return;
                    }
                    if (intent == null) {
                        //Capture Photo if no image available
                        if (mCM != null) {
                            results = new Uri[]{Uri.parse(mCM)};
                        }
                    } else {
                        String dataString = intent.getDataString();
                        if (dataString != null) {
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }
                }
            }
            mUMA.onReceiveValue(results);
            mUMA = null;
        } else {

            if (requestCode == FCR) {
                if (null == mUM) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                mUM.onReceiveValue(result);
                mUM = null;
            }
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
        }

        webView =  findViewById(R.id.ifView);
        assert webView != null;
        registerForContextMenu(webView);//регистрируем контекстное меню
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);//Включает или отключает доступ к файлам внутри WebView.
        webSettings.setBuiltInZoomControls(true); //масштабирование
        // включаем поддержку JavaScript
        webSettings.setDisplayZoomControls(false);//откл кнопки с лупами
        if (Build.VERSION.SDK_INT >= 21) {
            webSettings.setMixedContentMode(MIXED_CONTENT_ALWAYS_ALLOW);//Настраивает поведение WebView, когда безопасный источник пытается загрузить ресурс из незащищенного происхождения.
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT < 19) {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        webView.setWebViewClient(new Callback());//дает сообщение об ошибке загрузки

        webView.setWebChromeClient(new WebChromeClient() {

            //загрузить файл на сайт
            //For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {

                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), FCR);
            }

            // For Android 3.0+, above method not supported in some android 3+ versions, in such case we use this
            public void openFileChooser(ValueCallback uploadMsg, String acceptType) {

                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);//создаем неявное намерение выбрать файл
                i.addCategory(Intent.CATEGORY_OPENABLE);//присваеваем категорю .
                i.setType("*/*");//тип откр файлов
                MainActivity.this.startActivityForResult(
                        Intent.createChooser(i, "File Browser"),//предоставляем окно выбора программы
                        FCR);

            }
            //For Android 4.1+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {

                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), MainActivity.FCR);
            }

            //For Android 5.0+
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    WebChromeClient.FileChooserParams fileChooserParams) {

                if (mUMA != null) {
                    mUMA.onReceiveValue(null);
                }

                mUMA = filePathCallback;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {

                    File photoFile = null;

                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCM);
                    } catch (IOException ex) {
                        Log.e(TAG, "Image file creation failed", ex);
                    }
                    if (photoFile != null) {
                        mCM = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }

                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("*/*");
                Intent[] intentArray;

                if (takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooserIntent, FCR);

                return true;
            }
        });
      //загрузить файл на сайт
//скачивание файлов

        webView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
Log.d("vvv",url);
                Log.d("vvv",userAgent);
                Log.d("vvv",contentDisposition);
                Log.d("vvv",mimetype);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimetype);
                //------------------------COOKIE!!------------------------
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                request.addRequestHeader("User-Agent", userAgent);
                  //------------------------COOKIE!!------------------------

               // String fileName = URLUtil.guessFileName(url,contentDisposition,mimetype);
                String contentDispositionSplit[] = contentDisposition.split("''");
                String fileName = contentDispositionSplit[1].trim();
                Log.d("vvv",fileName);
                request.allowScanningByMediaScanner();

                request.setAllowedOverRoaming(false);
                request.setTitle(fileName);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
              //  Uri destinationUri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/EngeenersForum/"+fileName);
                //request.setDestinationUri(destinationUri);
                request.setDestinationInExternalPublicDir( Environment.DIRECTORY_DOWNLOADS + "/EngeenerForum/", fileName);
               // request.setDestinationInExternalFilesDir(getApplicationContext(), null, "/EngeenerForum/");
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Загрузка", //To notify the Client that the file is being downloaded
                        Toast.LENGTH_LONG).show();

            }
        });
//скачивание файлов
        webView.loadUrl("http://liftovik.listbb.ru/index.php");//запуск сайта
        // webView.loadUrl("http://zaycev.net/pages/46408/4640896.shtml");

    }
    //контекстное меню
    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        super.onCreateContextMenu(contextMenu, view, contextMenuInfo);

        final WebView.HitTestResult webViewHitTestResult = webView.getHitTestResult();//олучает тип данных на которые нажали

        if (webViewHitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||//если тип данных картинка или ссылка на картинку
                webViewHitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {

            contextMenu.setHeaderTitle("Сохранить изображение?");//заголовок конт меню

            contextMenu.add(0, 1, 0, "Да!") //добавляем кнопку ДА в конт меню
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {//слушатель нажатия кнопки да
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {

                            String DownloadImageURL = webViewHitTestResult.getExtra(); //получает адрес от объекта на который нажимаем

                            if (URLUtil.isValidUrl(DownloadImageURL)) {//проверяем является ли адресом DownloadImageURL

                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(DownloadImageURL));
                                //------------------------COOKIE!!------------------------
                                String cookies = CookieManager.getInstance().getCookie(DownloadImageURL);
                                request.addRequestHeader("cookie", cookies);
                                
                                //------------------------COOKIE!!------------------------
                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                downloadManager.enqueue(request);

                                Toast.makeText(MainActivity.this, "Загрузка", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Не удалось загрузить", Toast.LENGTH_LONG).show();
                            }
                            return false;
                        }
                    });
            contextMenu.add(0, 2, 0, "Нет!")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            return false;

                        }
                    });
        }
    }
    // Create an image file
    private File createImageFile() throws IOException {

        @SuppressLint("SimpleDateFormat")
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_DOWN) {

            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:

                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        finish();
                    }

                    return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public class Callback extends WebViewClient {
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(getApplicationContext(), "Не возможно загрузить!", Toast.LENGTH_SHORT).show();
        }
    }


}