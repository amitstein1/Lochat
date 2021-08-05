package com.example.stein.client_1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class TabbedMainActivity extends AppCompatActivity {


    private static final String TAG = "TabbedMainActivity";//logt

    private SectionsPageAdapter mSectionsPageAdapter;

    private ViewPager mViewPager;

    //vars for location updates
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Boolean mLocationPermissionsGranted;
    ;
    private final int sleepTimeSeconds = 1;
    private final int sleepTimeMiliSeconds = sleepTimeSeconds * 1000;
    public Boolean toUpdateLocation;
    //static Queue<Location> liveLocationChangesToMap;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    //dictionary of group ids and marker objects
    public static Map<String, Marker> markers = new HashMap<String, Marker>();
    public static Map<String, Circle> circles = new HashMap<String, Circle>();

    //public static Boolean toMakeChangesInMap = false;
    //public static Boolean toInitMap = false;
    Serialization serialize;

    Boolean checkForRecv;

    /*public TabbedMainActivity(){
        Log.i(TAG, "TabbedMainActivity: constractor - once");
        checkForRecv = true;
        handleServerMessages();
        Log.d(TAG, "handleServerMessages start");
    }*/



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabbed_activity_main);
        Log.d(TAG, "onCreate starting...");
         toUpdateLocation = true;
        serialize = new Serialization();


        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        mViewPager.setKeepScreenOn(true);

        // Attach the page change listener inside the activity
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {
                Log.d(TAG,"getPageTitle: got page title in position:"+String.valueOf(position));
                if(position==0){
                    //MyMapFragment.toInitMap = false;
                    Log.d(TAG,"groups");
                    MyMapFragment.inMap = false;
                    MyMapFragment.afterInitMap = false;
                    //MyMapFragment.toMakeChangesInMap = false;
                }
                else if(position==1){
                    MyMapFragment.toInitMap = false;
                    Log.d(TAG,"profile");
                    MyMapFragment.inMap = false;
                    MyMapFragment.afterInitMap = false;
                    //MyMapFragment.toMakeChangesInMap = false;

                }
                else if(position==2){
                        //map
                        //because unless the pager adapter will not init the map if we will go
                        //just to the profile fragment because oncreate is called only when pressing
                        //on the privious tab: press on 1- oncreate 2 is called but if then we go
                        // to 2 oncreate will not be called and initmap will not be called - so we call it here
                        Log.d(TAG,"map");
                        MyMapFragment.toInitMap = true;
                        MyMapFragment.inMap = true;
                        MyMapFragment.afterInitMap = false;
                        MyMapFragment.toMakeChangesInMap = true;


                }
            }

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Code goes here
            }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state) {
                // Code goes here
            }
        });


        //here the function of location every second
        //now call the function that gave a location every sec and send to queue
        mLocationPermissionsGranted = false;
        getLocationPermission();
        if (mLocationPermissionsGranted) {
            updateLocationThreaad();//updata and send to que - and then to the server
        } else {
            Log.d(TAG, "did not get location permissions - location live request are not working");
        }
        //this activity will add every time to the que the curent location of
        //liveLocationChangesToMap = new LinkedList<Location>();

        checkForRecv = true;
        String caller = getIntent().getStringExtra("caller");
        if (caller.equals("LoginActivity")) {
            Log.d(TAG, "the caller was login activity");
            handleServerMessages();
        }


    }


    //log out!!!!
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "clicked on Key Down ");
            checkForRecv = false;
            toUpdateLocation = false;
            String action = "log_out";
            String to_send = action + "%-";
            ClientTask.queToSend.add(to_send);

            Intent loginIntent = new Intent(TabbedMainActivity.this, LoginActivity.class);
            TabbedMainActivity.this.startActivity(loginIntent);


            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                //initMap();

            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

    }


    //set the adapter
    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());

        adapter.addFragment(new GroupsFragment(), getString(R.string.tab_text_1));//first
        adapter.addFragment(new ProfileFragment(), getString(R.string.tab_text_2));//second
        adapter.addFragment(new MyMapFragment(), getString(R.string.tab_text_3)); //third
        viewPager.setAdapter(adapter);
    }


    //get one answer of location from google server
    private synchronized void getAndSendDeviceLocation() {
        //Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        try {
            if (mLocationPermissionsGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        //Log.d(TAG, "inside on complete");
                        if (task.getResult() != null) {//isSuccessful()) {
                            //Log.d(TAG, "onComplete: found location!");
                            android.location.Location currentLocation = (android.location.Location) task.getResult();
                            Location loc = new Location(currentLocation.getLatitude(), currentLocation.getLongitude());
                            //Log.d(TAG, "new location obj=" + loc);
                            User.setUserLocation(loc);
                            //now every activity will be able to get to the location by user object!!!!!!!!!


                            //Log.d(TAG, "send location to server");
                            String to_send = "new_loc%" + User.getUserLocation().toString();
                            //Log.d(TAG, "before adding to que:" + to_send);
                            if (toUpdateLocation)
                                ClientTask.queToSend.add(to_send);
                        } else {
                            //Log.d(TAG, "onComplete: current location is null");
                            //Toast.makeText(getApplicationContext(), "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }


    //ask every few seconds (declares on top) for a location request from google server
    //then the method will add the location to the que of the map activity() and the client activity (to send to the server)
    public void updateLocationThreaad() {
        Thread t = new Thread() {
            @Override
            public void run() {
                while (toUpdateLocation) {
                    try {
                        Thread.sleep(sleepTimeMiliSeconds);  //1000ms = 1 sec
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //do somthing every 5 sec
                                getAndSendDeviceLocation();
                                //Log.d(TAG, "location changed, set in user and sent");


                                //if (MyMapFragment.toMakeChangesInMap){
                                //   //not always - some times the user is not in the mao fragment
                                //    // so we do not want to do changes...
                                //add to que
                                //    liveLocationChangesToMap.add()
                                //}

                            }
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }
            }
        };
        t.start();
    }


    public void handleServerMessages() {
        Log.d(TAG, "handleServerMessages");
        Thread t = new Thread() {
            @Override
            public void run() {
                while (checkForRecv) {
                    try {
                        //Log.d("client_sock_register", "loop recv:"+ String.valueOf(ClientTask.queRecv));
                        Thread.sleep(1000);  //1000ms = 1 sec
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (!ClientTask.queRecv.isEmpty()) {
                                    String received_data = ClientTask.queRecv.remove();
                                    //Log.d(TAG, "que received: " + received_data);
                                    List<String> listMassage = Arrays.asList(received_data.split("%"));
                                    //Log.d(TAG, "listMassage=" + listMassage);
                                    String action = listMassage.get(0);
                                    List<String> params = listMassage.subList(1, listMassage.size());
                                    //Log.d(TAG, "action=" + action);
                                   // Log.d(TAG, "params=" + params);
                                    if (action.equals("user_obj")) {
                                     //   Log.d(TAG, "before updating user");
                                        List<String> listUserParams = Arrays.asList(params.get(0).split("~"));
                                        //Log.d(TAG, "listUserParams=" + listUserParams.toString());
                                        serialize.updatedStaticUser(listUserParams);
                                       // Log.d(TAG, "after updating user");
                                    }
                                    //else if(action.equals("other ptopery")){
                                    //}
                                    else {
                                        Log.d(TAG, "something wrong got into wrong else statement");
                                    }
                                }
                            }
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        t.start();
    }
}
