package kr.foryou.varopetcompany;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kakao.kakaonavi.KakaoNaviParams;
import com.kakao.kakaonavi.KakaoNaviService;
import com.kakao.kakaonavi.Location;
import com.kakao.kakaonavi.NaviOptions;
import com.kakao.kakaonavi.options.CoordType;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.foryou.varopetcompany.menu.MenuListAdapter;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import util.BackPressCloseHandler;
import util.Common;
import util.LocationPosition;
import util.NetworkCheck;
import util.retrofit.MenuList;
import util.retrofit.RetrofitService;
import util.retrofit.ServerPost;
import util.retrofit.SetRetrofit;
import util.retrofit.TaxiPosition;

import static com.kakao.util.helper.Utility.getPackageInfo;


public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ImageView closeBtn;
    private View drawerView;
    ArrayList<MenuList> menuList;
    ArrayList<TaxiPosition> positionList;

    private final int REQUEST_VIEWER=1000;
    final int FILECHOOSER_NORMAL_REQ_CODE = 1200,FILECHOOSER_LOLLIPOP_REQ_CODE=1300;
    ValueCallback<Uri> filePathCallbackNormal;
    ValueCallback<Uri[]> filePathCallbackLollipop;
    Uri mCapturedImageURI;

    LinearLayout webLayout;
    RelativeLayout networkLayout;
    WebView webView;
    //NetworkCheck netCheck;
    Button replayBtn;
    ProgressBar loadingProgress;
    public static boolean execBoolean = true;
    private BackPressCloseHandler backPressCloseHandler;
    boolean isIndex = true;
    double lat,lng;
    String firstUrl = "";
    ListView menuListView;
    TextView joinBtn,loginBtn;
    String currentPlace="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String key=getKeyHash(this);
        Log.d("hash-key",key);
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        FirebaseApp.initializeApp(this);
        FirebaseMessaging.getInstance().subscribeToTopic("varopet");
        FirebaseInstanceId.getInstance().getToken();


        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        firstUrl = getString(R.string.url);
        try{
            if(!intent.getExtras().getString("goUrl").equals("")){
                firstUrl =intent.getExtras().getString("goUrl");
            }
        }catch(Exception e){

        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(this);
        }
        try {
            if (Common.TOKEN.equals("") || Common.TOKEN.equals(null)) {
                refreshToken();
            } else {

            }
        }catch (Exception e){
            refreshToken();
        }
        Log.d("push-token",Common.TOKEN);
        setLayout();
    }


    //레이아웃 설정
    public void setLayout() {





        networkLayout = (RelativeLayout) findViewById(R.id.networkLayout);//네트워크 연결이 끊겼을 때 레이아웃 가져오기
        webLayout = (LinearLayout) findViewById(R.id.webLayout);//웹뷰 레이아웃 가져오기
        /*webLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("url", webView.getUrl());
                webView.reload();

            }
        });*/
        loadingProgress = (ProgressBar)findViewById(R.id.loadingProgress);
        webView = (WebView) findViewById(R.id.webView);//웹뷰 가져오기




        webViewSetting();

        //getMenu();
    }

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR_MR1)
    public void webViewSetting() {
        /*webView.addJavascriptInterface(new AppShare(), "appshare");
        webView.addJavascriptInterface(new AppShares(), "appshares");
        webView.addJavascriptInterface(new PostEmail(), "postemail");*/
        WebSettings setting = webView.getSettings();//웹뷰 세팅용

        setting.setAllowFileAccess(true);//웹에서 파일 접근 여부
        setting.setAppCacheEnabled(true);//캐쉬 사용여부
        setting.setGeolocationEnabled(true);//위치 정보 사용여부
        setting.setDatabaseEnabled(true);//HTML5에서 db 사용여부
        setting.setDomStorageEnabled(true);//HTML5에서 DOM 사용여부
        setting.setCacheMode(WebSettings.LOAD_DEFAULT);//캐시 사용모드 LOAD_NO_CACHE는 캐시를 사용않는다는 뜻
        setting.setJavaScriptEnabled(true);//자바스크립트 사용여부
        setting.setSupportMultipleWindows(false);//윈도우 창 여러개를 사용할 것인지의 여부 무조건 false로 하는 게 좋음
        setting.setUseWideViewPort(true);//웹에서 view port 사용여부
        webView.setWebChromeClient(chrome);//웹에서 경고창이나 또는 컴펌창을 띄우기 위한 메서드
        webView.setWebViewClient(client);//웹페이지 관련된 메서드 페이지 이동할 때 또는 페이지가 로딩이 끝날 때 주로 쓰임
        String userAgent = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(userAgent+" VaroPet");
        webView.addJavascriptInterface(new WebJavascriptEvent(), "Android");

        //현재 안드로이드 버전이 허니콤(3.0) 보다 높으면 줌 컨트롤 사용여부 체킹
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setting.setBuiltInZoomControls(true);
            setting.setDisplayZoomControls(false);
        }

        //네트워크 체킹을 할 때 쓰임
        /*netCheck = new NetworkCheck(this, this);
        netCheck.setNetworkLayout(networkLayout);
        netCheck.setWebLayout(webLayout);
        netCheck.networkCheck();*/
        //뒤로가기 버튼을 눌렀을 때 클래스로 제어함
        backPressCloseHandler = new BackPressCloseHandler(this);

        replayBtn=(Button)findViewById(R.id.replayBtn);
        replayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //netCheck.networkCheck();
            }
        });
        LocationPosition.act=MainActivity.this;
        LocationPosition.setPosition(MainActivity.this);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); // 혼합된 컨텐츠 허용//
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        webView.loadUrl(firstUrl);
    }

    WebChromeClient chrome;
    {
        chrome = new WebChromeClient() {
            //새창 띄우기 여부
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                return false;
            }

            //경고창 띄우기
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("\n" + message + "\n")
                        .setPositiveButton("확인",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        result.confirm();
                                    }
                                }).create().show();
                return true;
            }

            //컴펌 띄우기
            @Override
            public boolean onJsConfirm(WebView view, String url, String message,
                                       final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("\n" + message + "\n")
                        .setPositiveButton("확인",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        result.confirm();
                                    }
                                })
                        .setNegativeButton("취소",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        result.cancel();
                                    }
                                }).create().show();
                return true;
            }

            //현재 위치 정보 사용여부 묻기
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                // Should implement this function.
                final String myOrigin = origin;
                final GeolocationPermissions.Callback myCallback = callback;
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Request message");
                builder.setMessage("Allow current location?");
                builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        myCallback.invoke(myOrigin, true, false);
                    }

                });
                builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        myCallback.invoke(myOrigin, false, false);
                    }

                });
                AlertDialog alert = builder.create();
                alert.show();
            }
            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                openFileChooser(uploadMsg, "");
            }

            // For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                filePathCallbackNormal = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_NORMAL_REQ_CODE);
            }

            // For Android 4.1+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                openFileChooser(uploadMsg, acceptType);
            }


            // For Android 5.0+
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {
                if (filePathCallbackLollipop != null) {
//                    filePathCallbackLollipop.onReceiveValue(null);
                    filePathCallbackLollipop = null;
                }
                filePathCallbackLollipop = filePathCallback;


                // Create AndroidExampleFolder at sdcard
                File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "AndroidExampleFolder");
                if (!imageStorageDir.exists()) {
                    // Create AndroidExampleFolder at sdcard
                    imageStorageDir.mkdirs();
                }

                // Create camera captured image file path and name
                File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                mCapturedImageURI = Uri.fromFile(file);

                Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);

                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");

                // Create file chooser intent
                Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
                // Set camera intent to file chooser
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});

                // On select image call onActivityResult method of activity
                startActivityForResult(chooserIntent, FILECHOOSER_LOLLIPOP_REQ_CODE);
                return true;

            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILECHOOSER_NORMAL_REQ_CODE) {
            if (filePathCallbackNormal == null) return;
            Uri result = (data == null || resultCode != RESULT_OK) ? null : data.getData();
            filePathCallbackNormal.onReceiveValue(result);
            filePathCallbackNormal = null;

        } else if (requestCode == FILECHOOSER_LOLLIPOP_REQ_CODE) {
            Uri[] result = new Uri[0];
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if(resultCode == RESULT_OK){
                    result = (data == null) ? new Uri[]{mCapturedImageURI} : WebChromeClient.FileChooserParams.parseResult(resultCode, data);
                }

                filePathCallbackLollipop.onReceiveValue(result);

            }
        }
    }

    WebViewClient client;
    {
        client = new WebViewClient() {
            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url=request.getUrl().toString();
                loadingProgress.setVisibility(View.VISIBLE);

                Log.d("url",url);

                if (url.startsWith("tel")) {


                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.


                        }
                        Log.d("tel","tel");
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        return true;
                    } catch (Exception e) {
                        Log.d("error",e.toString());
                        e.printStackTrace();
                    }

                    return false;


                }
                if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("javascript:")) {
                    Intent intent = null;

                    try {
                        intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME); //IntentURI처리
                        Uri uri = Uri.parse(intent.getDataString());

                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        return true;
                    } catch (URISyntaxException ex) {
                        return false;
                    } catch (ActivityNotFoundException e) {
                        if (intent == null) return false;



                        String packageName = intent.getPackage();
                        if (packageName != null) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                            return true;
                        }

                        return false;
                    }
                }
               /* if(!currentPlace.equals("")){
                    if(0<url.lastIndexOf("?")) {
                        url = url + "&currentPlace=" + currentPlace;
                    }else{
                        url = url + "?currentPlace=" + currentPlace;
                    }

                    webView.loadUrl(url);
                }*/
                return false;
            }

           
            //페이지 로딩이 다 끝났을 때
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                //webLayout.setRefreshing(false);
                loadingProgress.setVisibility(View.GONE);
                Log.d("url",url);
                Log.d("ss_mb_id", Common.getPref(getApplicationContext(),"ss_mb_id",""));

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    CookieSyncManager.getInstance().sync();
                } else {
                    CookieManager.getInstance().flush();
                }

                if (url.equals(getString(R.string.url)) || url.equals(getString(R.string.domain))) {
                    isIndex=true;
                    webView.loadUrl("javascript:setToken('"+ Common.TOKEN+"');");
                } else {
                    isIndex=false;
                }



                Log.d("currentPlace",currentPlace);
            }
            //페이지 오류가 났을 때 6.0 이후에는 쓰이지 않음
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                webView.reload();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    //쿠키 값 삭제
    public void deleteCookie(){
        CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(webView.getContext());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeSessionCookie();
        cookieManager.removeAllCookie();
        cookieSyncManager.sync();
    }
    //다시 들어왔을 때
    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().startSync();
        }
        execBoolean=true;
        Log.d("newtork","onResume");
        //netCheck.networkCheck();
    }
    //홈버튼 눌러서 바탕화면 나갔을 때
    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().startSync();
        }
        firstUrl=webView.getUrl();

        execBoolean=false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //netCheck.stopReciver();
    }

    //뒤로가기를 눌렀을 때
    public void onBackPressed() {
        //super.onBackPressed();
        //웹뷰에서 히스토리가 남아있으면 뒤로가기 함
        if (!isIndex) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else if (webView.canGoBack() == false) {
                backPressCloseHandler.onBackPressed();
            }
        } else {
            backPressCloseHandler.onBackPressed();
        }

    }
    //드로우 메뉴 실행하기
    class WebJavascriptEvent{
        @JavascriptInterface
        public void openMenu(){
            try{
                Log.d("javascript","openMenu");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        drawerLayout.openDrawer(drawerView);
                    }
                });

            }catch (Exception e){
                Log.d("javascript",e.toString());
            }
        }
        @JavascriptInterface
        public void setLogin(String mb_id,String mb_name){
            Common.savePref(MainActivity.this,"mb_id",mb_id);
            Common.savePref(MainActivity.this,"mb_name",mb_name);
            loginBtn.setText("로그아웃");
            joinBtn.setText("마이페이지");
        }
        @JavascriptInterface
        public void setLogout(){
            Common.savePref(MainActivity.this,"mb_id","");
            Common.savePref(MainActivity.this,"mb_name","");
            loginBtn.setText("로그인");
            joinBtn.setText("회원가입");
        }
        @JavascriptInterface
        public void getLocation(){
            lng = LocationPosition.lng;
            lat = LocationPosition.lat;
            Log.d("위치",lat+"");
            String place;
            place= LocationPosition.getAddress(lat,lng);
            Log.d("address",place);
            if(place.equals("")){
                LocationPosition.act=MainActivity.this;
                LocationPosition.setPosition(MainActivity.this);
                Toast.makeText(getApplicationContext(),"GPS 신호가 잡히지 않고 있습니다.\n GPS를 활성화하시거나 잠시 후에 이용하시길 바랍니다.",Toast.LENGTH_SHORT).show();
            }
            final String addr=place;


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:currentPositionChoice('"+addr+"','"+lng+"','"+lat+"');");
                }
            });
        }
        @JavascriptInterface
        public void setNavi(String i,String place,String lat,String lng){
            Log.d("navi-lat",lat+"");
            Log.d("navi-lng",lng+"");
            switch (Integer.parseInt(i)){
                case 0:
                    LocationPosition.startPointLat=Double.parseDouble(lat);
                    LocationPosition.startPointLng=Double.parseDouble(lng);
                    LocationPosition.startPlace=place;
                    break;
                case 1:
                    LocationPosition.way1Place=place;
                    LocationPosition.way1PointLat=Double.parseDouble(lat);
                    LocationPosition.way1PointLng=Double.parseDouble(lng);
                    break;
                case 2:
                    LocationPosition.way2Place=place;
                    LocationPosition.way2PointLat=Double.parseDouble(lat);;
                    LocationPosition.way2PointLng=Double.parseDouble(lng);
                    break;
                case 3:
                    LocationPosition.desPlace=place;
                    LocationPosition.desPointLat=Double.parseDouble(lat);;
                    LocationPosition.desPointLng=Double.parseDouble(lng);
                    break;
            }

        }

        @JavascriptInterface
        public void viewPosition(String wr_id){
            //httpok 로그 보기
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            //클라이언트 설정
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(httpLoggingInterceptor)
                    .build();
            //레트로핏 설정
            Retrofit retrofit=new Retrofit.Builder()
                    .baseUrl("http://www.barobaropet.com")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            RetrofitService retrofitService=retrofit.create(RetrofitService.class);

            Map map = new HashMap();
            map.put("division","taxi_position");
            map.put("wr_id",wr_id);
            Call<ServerPost> call=retrofitService.getPosition(map);
            call.enqueue(new Callback<ServerPost>() {
                @Override
                public void onResponse(Call<ServerPost> call, Response<ServerPost> response) {
                    if(response.isSuccessful()){
                        ServerPost repo=response.body();
                        Log.d("response",response+"");
                        positionList=repo.getPosition();
                        LocationPosition.startPlace=positionList.get(0).getWr_subject();
                        LocationPosition.startPointLat=Double.parseDouble(positionList.get(0).getWr_subject_lat());
                        Log.d("position",positionList.get(0).getWr_subject_lat()+"");
                        LocationPosition.startPointLng=Double.parseDouble(positionList.get(0).getWr_subject_lng());

                        LocationPosition.way1Place=positionList.get(0).getWr_1();
                        if(positionList.get(0).getWr_1_lat().equals("")){
                            LocationPosition.way1PointLat = 0;
                            LocationPosition.way1PointLng = 0;
                        }else {
                            LocationPosition.way1PointLat = Double.parseDouble(positionList.get(0).getWr_1_lat());
                            LocationPosition.way1PointLng = Double.parseDouble(positionList.get(0).getWr_1_lng());
                        }

                        LocationPosition.way2Place=positionList.get(0).getWr_2();
                        if(positionList.get(0).getWr_2_lat().equals("")){
                            LocationPosition.way2PointLat = 0;
                            LocationPosition.way2PointLng = 0;
                        }else {
                            LocationPosition.way2PointLat = Double.parseDouble(positionList.get(0).getWr_2_lat());
                            LocationPosition.way2PointLng = Double.parseDouble(positionList.get(0).getWr_2_lng());
                        }

                        LocationPosition.desPlace=positionList.get(0).getWr_3();
                        LocationPosition.desPointLat=Double.parseDouble(positionList.get(0).getWr_3_lat());
                        LocationPosition.desPointLng=Double.parseDouble(positionList.get(0).getWr_3_lng());

                    }else{

                    }
                }

                @Override
                public void onFailure(Call<ServerPost> call, Throwable t) {

                }
            });
        }
        @JavascriptInterface
        public void execNavi(){
            Location location= Location.newBuilder(LocationPosition.desPlace,LocationPosition.desPointLat,LocationPosition.desPointLng).build();
            NaviOptions options = NaviOptions.newBuilder().setCoordType(CoordType.WGS84).setStartX(LocationPosition.startPointLat).setStartY(LocationPosition.startPointLng).build();
            //KakaoNaviParams.Builder builder = KakaoNaviParams.newBuilder(location).setNaviOptions(options);
            Log.d("way1",LocationPosition.way1PointLat+"");
            // 경유지를 1개 포함하는 KakaoNaviParams.Builder 객체
            List<Location> viaList = new ArrayList<Location>();
            if(!LocationPosition.way1Place.equals("")) {
                viaList.add(Location.newBuilder(LocationPosition.way1Place, LocationPosition.way1PointLat, LocationPosition.way1PointLng).build());
            }
            if(!LocationPosition.way2Place.equals("")){
                viaList.add(Location.newBuilder(LocationPosition.way2Place, LocationPosition.way2PointLat, LocationPosition.way2PointLng).build());
            }
            KakaoNaviParams.Builder builder = KakaoNaviParams.newBuilder(location).setNaviOptions(options).setViaList(viaList);
            KakaoNaviParams params = builder.build();
            Log.d("lat",LocationPosition.way1PointLat+"");
            KakaoNaviService.getInstance().navigate(MainActivity.this, builder.build());
        }
        @JavascriptInterface
        public void startExecNavi(){
            Location location= Location.newBuilder(LocationPosition.startPlace,LocationPosition.startPointLat,LocationPosition.startPointLng).build();
            NaviOptions options = NaviOptions.newBuilder().setCoordType(CoordType.WGS84).setStartX(LocationPosition.lat).setStartY(LocationPosition.lng).build();
            //KakaoNaviParams.Builder builder = KakaoNaviParams.newBuilder(location).setNaviOptions(options);
            Log.d("way1",LocationPosition.way1PointLat+"");
            // 경유지를 1개 포함하는 KakaoNaviParams.Builder 객체
            List<Location> viaList = new ArrayList<Location>();
            KakaoNaviParams.Builder builder = KakaoNaviParams.newBuilder(location).setNaviOptions(options).setViaList(viaList);
            KakaoNaviParams params = builder.build();
            KakaoNaviService.getInstance().navigate(MainActivity.this, builder.build());
        }

    }
    public static String getKeyHash(final Context context) {
        PackageInfo packageInfo = getPackageInfo(context, PackageManager.GET_SIGNATURES);
        if (packageInfo == null)
            return null;

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                return Base64.encodeToString(md.digest(), Base64.NO_WRAP);
            } catch (NoSuchAlgorithmException e) {
                Log.w("signed", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
        return null;
    }


    private void refreshToken(){
        FirebaseMessaging.getInstance().subscribeToTopic("varopet");
        Common.TOKEN= FirebaseInstanceId.getInstance().getToken();

    }
}
