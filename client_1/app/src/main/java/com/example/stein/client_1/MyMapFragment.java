package com.example.stein.client_1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by stein on 25/01/2018.
 */

public class MyMapFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "MyMapFragment"; //"MyMapFragment";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 16f;
    private Boolean mLocationPermissionsGranted = false;
    public static GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private final int sleepTimeSeconds = 1;
    private final int sleepTimeMiliSeconds = sleepTimeSeconds* 1000;

    public static Boolean is_first_time_for_map_fragment = true;

    Thread t;
    public static Boolean rrr = true;
    Marker m =null;

    public static Boolean toMakeChangesInMap = true;
    public static Boolean toInitMap = true;
    public static Boolean inMap = true;
    public static boolean afterInitMap = false;



    //widgets
    private EditText mSearchText;
    private ImageView mGps;
    //private Marker myLocationMarker;


    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Toast.makeText(this.getActivity(), "Map is _ Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;
        Log.d(TAG, "onMapReady: map is not null now!!!!!");
        //mMap.setClustering(new ClusteringSettings().enabled(false).addMarkersDynamically(true));

        if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMyLocationEnabled(true);
        Location loc = User.getUserLocation();
        LatLng latlangUsertLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
        moveCamera(latlangUsertLocation, DEFAULT_ZOOM);

        MarkerOptions markerO = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(latlangUsertLocation).title("My Location");
        //myLocationMarker = mMap.addMarker(markerO);
        //myLocationMarker.setVisible(true);
        //myLocationMarker.showInfoWindow();//will cause the title to show even without clicking on the marker
        Log.d(TAG, "addMarker: Marker added:" + String.valueOf(latlangUsertLocation.latitude) + "," + String.valueOf(latlangUsertLocation.longitude) + ", My Location");

        Log.d(TAG, "after onMapReady to true");
        afterInitMap = true;


        //addMarker(latlang,"My Location");

        //addCircle(latlang,100);
        //putAllNearbyGroupsOnMap


    }



    /*@Override
    public void onDestroy()
    {
        super.onDestroy();
        //we want the thread to join
        Log.d(TAG, "onDestroyView");
        toMakeChangesInMap = false;
        Log.d(TAG, "changes in map are unavailable");
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "thread of changing live map was joined");
    }*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "before inflate");
        View view = inflater.inflate(R.layout.map_fragment,container,false);
        Log.d(TAG, "after inflate");
        //toMakeChangesInMap = false;
        //Log.d(TAG, "changes in map are unvailable");
        //view.setContentView(R.layout.map_fragment);
        //mSearchText = (EditText) view.findViewById(R.id.input_search);
        mGps = (ImageView) view.findViewById(R.id.ic_gps);
        Log.d(TAG, "toInitMap="+toInitMap.toString());
        if (inMap && toInitMap && !afterInitMap){
            Log.d(TAG, "initiating: map on create view");
            initMap();
            toInitMap =false;
        }

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                //getDeviceLocation();
                //Log.d(TAG, "my location:"+User.getUserLocation().toString());
                LatLng latlang = new LatLng(User.getUserLocation().getLatitude(), User.getUserLocation().getLongitude());
                moveCamera(latlang, DEFAULT_ZOOM);


                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(latlang));

/*
                if (rrr){
                    m = addMarker(latlang,"My Location","bkbkb");
                    rrr = false;
                    Log.d(TAG, "add!!!!!!!!");
                }
                else{
                    m.remove();
                    rrr = true;
                    Log.d(TAG, "delete!!!!!!!!");
                }*/


            }
        });




        if(is_first_time_for_map_fragment){
            is_first_time_for_map_fragment = false;
            makeLiveChangesInMap();
            Log.d(TAG, "makeLiveChangesInMap is called");
        }
        //makeLiveChangesInMap();


        return view;
    }





    /*private void geoLocate(){
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(getActivity());
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));
        }
    }*/

    private void hideSoftKeyboard(){
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void  updateMyMangerDynamicGroupsMarkersCircles(){
        for ( Group group : User.manage_groups.values()){
            if (group.getType()==groupType.Dynamic){
                //set position of the dynamic group to the current location
                LatLng latlangUsertLocation = new LatLng(User.getUserLocation().getLatitude(),User.getUserLocation().getLongitude());
                try{
                TabbedMainActivity.markers.get(group.getId()).setPosition(latlangUsertLocation);
                TabbedMainActivity.circles.get(group.getId()).setCenter(latlangUsertLocation);}
                catch (Exception e){
                    Log.d(TAG, "update marker dynamics - markers are null: ");
                }
            }
        }
    }

    /*public void  removeGroupsUserNotInAnymoreFromMarkersDict(){
        List<String> markersIds = new ArrayList<>();
        markersIds.addAll(TabbedMainActivity.markers.keySet());//convert the set to a list
        for (String groupId : markersIds){
            if (!User.isExist(groupId, markersIds)){
                    TabbedMainActivity.markers.get(groupId).remove();
                }
        }
    }*/


    public Boolean isExist(String name1, List<String> listOfNames){
        for (String name2 : listOfNames ) {
            if (name1.equals(name2)) {
                return true;
            }
        }
        return false;
    }

    public void addUserGroupsMarkersCircles(){
        List<String> markersIds = new ArrayList<>(TabbedMainActivity.markers.keySet());
        Log.d(TAG,"markers ids:" + markersIds.toString());
        //add missed markers to the map
        Map<String, Marker> new_markers = new HashMap<String, Marker>();
        Map<String, Circle> new_circles = new HashMap<String, Circle>();


        for (Group group : User.userGroups.values()) {
            Boolean b = !isExist(group.getId(),markersIds);
            Log.i(TAG,"b="+b.toString());
            if (b) {
               /*LatLng latlang = new LatLng(32.186523,34.891408);
                if (mMap!=null){
                    mMap.addMarker(new MarkerOptions().position(latlang).title("grgrgrgrgrgrr"));
                    m = addMarker(latlang,"4545454545");
                    if(m==null){
                        Log.d(TAG, "ryfjmcjdyjdfjfgjfgh");
                    }
                    else
                        Log.d(TAG, "000000000000000000000000000000");
                }
                else{
                    Log.d(TAG, "nullllllllllllllllllllllllllllllll");
                }*/

                //groups is in userGroups but not in markers - add to markers
                LatLng latlangMidLoc = new LatLng(group.getMidLoc().getLatitude(),group.getMidLoc().getLongitude());
                Log.i(TAG,"exist!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ");
                Log.d(TAG, "after onMapReady before marking=" +afterInitMap);
                Marker marker = addMarker(latlangMidLoc,group.getName(),group.getType().toString());//add the marker and save it in a variable
                Circle circle = addCircle(latlangMidLoc,group.getRadius());
                if (marker == null){
                    Log.i(TAG,"marker=null!!!!!!!!!!!!!!!!!!!!!1111");
                }
                else{
                    Log.i(TAG,"marker != null");
                }
                Log.i(TAG,"marker added to map:" + group.getId());

                new_markers.put(group.getId(),marker);//add a marker to the new markers dictionary - local variable
                new_circles.put(group.getId(),circle);
            }
        }
        //put the missed markers in the markers dictionary
        List<String> newMarkersIds = new ArrayList<>(new_markers.keySet());
        for ( String markerId : newMarkersIds){
            TabbedMainActivity.markers.put(markerId,new_markers.get(markerId));//add a marker to the markers dictionary
            Log.i(TAG,"marker added to dict");
            TabbedMainActivity.circles.put(markerId,new_circles.get(markerId));//remove circle from circles dictionary

        }
        //LatLng latlang = new LatLng(32.186523,34.891408);
        //m = addMarker(latlang,"4545454545");
    }

    public void removeNonUserGroupsMarkersCircles(){
        List<String> markersIds = new ArrayList<>(TabbedMainActivity.markers.keySet());
        List<String> circlesIds = new ArrayList<>(TabbedMainActivity.circles.keySet());
        List<String> user_groups_ids = new ArrayList<>(User.userGroups.keySet());
        //removed markers the user is not in their groups location
        Map<String, Marker> markersToRemove = new HashMap<String, Marker>();
        Map<String, Circle> circlesToRemove = new HashMap<String, Circle>();
        for (String groupId : markersIds){
            if (!User.isExist(groupId, user_groups_ids)){
                Log.d(TAG,"delete marker");
                //groups is in markers but not in userGroups - remove from markers
                TabbedMainActivity.markers.get(groupId).remove();//remove marker from googlemap
                markersToRemove.put(groupId,TabbedMainActivity.markers.get(groupId));//add a marker to remove it after the loop

                TabbedMainActivity.circles.get(groupId).remove();//remove circle from googlemap
                //circlesToRemove.put(groupId,TabbedMainActivity.circles.get(groupId));//add a cicle to remove it after the loop
            }
        }
        //finally delete the needed markers from the markers dictionary
        List<String> markersToRemoveIds = new ArrayList<>(markersToRemove.keySet());
        for ( String markerId : markersToRemoveIds){
            TabbedMainActivity.markers.remove(markerId);//remove marker from markers dictionary
            TabbedMainActivity.circles.remove(markerId);//remove circle from circles dictionary
        }
    }





   /* public void updateUserGroupsMarkers(){
        List<String> markersIds = new ArrayList<>(TabbedMainActivity.markers.keySet());
        //Log.d(TAG,"markers ids:" + markersIds.toString());
        //add missed markers to the map
        Map<String, Marker> new_markers = new HashMap<String, Marker>();
        for (Group group : User.userGroups.values()) {
            if (!User.isExist(group.getId(),markersIds)) {

                //groups is in userGroups but not in markers - add to markers
                LatLng latlangMidLoc = new LatLng(group.getMidLoc().getLatitude(),group.getMidLoc().getLongitude());
                Log.i(TAG,"exist!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");
                Marker marker = addMarker(latlangMidLoc,group.getName());//add the marker and save it in a variable
                Log.i(TAG,"marker added to map");
                new_markers.put(group.getId(),marker);//add a marker to the new markers dictionary - local variable

            }
        }
        //put the missed markers in the markers dictionary
        List<String> newMarkersIds = new ArrayList<>(new_markers.keySet());
        for ( String markerId : newMarkersIds){
            TabbedMainActivity.markers.put(markerId,new_markers.get(markerId));//add a marker to the markers dictionary
            Log.i(TAG,"marker added to dict");
        }


        List<String> user_groups_ids = new ArrayList<>(User.userGroups.keySet());
        //removed markers the user is not in their groups location
        Map<String, Marker> markersToRemove = new HashMap<String, Marker>();
        for (String groupId : markersIds){
            if (!User.isExist(groupId, user_groups_ids)){
                Log.d(TAG,"delete marker");
                //groups is in markers but not in userGroups - remove from markers
                TabbedMainActivity.markers.get(groupId).remove();//remove marker fron googlemap
                markersToRemove.put(groupId,TabbedMainActivity.markers.get(groupId));//add a marker to remove it after the loop
            }
        }
        //finally delete the needed markers from the markers dictionary
        List<String> markersToRemoveIds = new ArrayList<>(markersToRemove.keySet());
        for ( String markerId : markersToRemoveIds){
            TabbedMainActivity.markers.remove(markerId);//remove marker from markers dictionary
        }

    }*/


    public void makeLiveChangesInMap(){
        t = new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "toMakeChangesInMap="+toMakeChangesInMap.toString());
                Log.d(TAG, "got inside while of makeLiveChangesInMap");
                while (toMakeChangesInMap) {

                    try {
                        Thread.sleep(sleepTimeMiliSeconds);  //1000ms = 1 sec
                        if(getActivity() == null){
                            Log.d(TAG, "getActivity() == null!!!!!!!!!!");
                            return;
                        }
                        getActivity().runOnUiThread(new Runnable() {//because this is a fragment and not activity
                            @Override
                            public void run() {
                                //do somthing every 1 sec

                                if (toInitMap){
                                    Log.d(TAG, "toInitMap is: true - now we init map");
                                    initMap();
                                    toInitMap = false;
                                }

                                Log.d(TAG, "toInitMap is:"+toInitMap.toString());
                                Log.d(TAG, "toMakeChangesInMap is:"+toMakeChangesInMap.toString());
                                if (mMap == null){
                                    Log.d(TAG, "mMap = null!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                                }
                                else{
                                    Log.d(TAG, "mMap != null!!!!!!!!!!!");
                                }

                                if(inMap) {
                                    if(mMap!=null){
                                        if(afterInitMap){
                                            Log.d(TAG, "we are in map after init and map not null:)");
                                            LatLng latlangUsertLocation = new LatLng(User.getUserLocation().getLatitude(),User.getUserLocation().getLongitude());
                                            //myLocationMarker.setPosition(latlangUsertLocation);
                                            addUserGroupsMarkersCircles();
                                            removeNonUserGroupsMarkersCircles();

                                            updateMyMangerDynamicGroupsMarkersCircles();
                                            Log.d(TAG,"done update MyManger Dynamic Groups Markers");
                                        }
                                        else
                                            Log.d(TAG, "still before init...");
                                    }
                                    else
                                        Log.d(TAG, "map = nullllllll1234123123412351");
                                }
                                else
                                    Log.d(TAG, "not in map :(");





                                //updateUserGroupsMarkers();
                                //Log.d(TAG,"done update User Groups Markers");
                                //removeGroupsUserNotInAnymoreFromMarkersDict(); this is already done in updateUserGroupsMarkers

                                //now we can do what ever we want
                                //Log.d(TAG, "zooming camera sec - not good - no need!!");
                                //moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),DEFAULT_ZOOM);

                            }
                        });


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.d(TAG, "catch of makeLiveChangesInMap: "+ e.toString());

                    }

                }
            }
        };
        t.start();
        Log.d(TAG, "thread of changing live map started");

    }


    /*public void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        try{
            if(mLocationPermissionsGranted){

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location!");
                            android.location.Location currentLocation = (android.location.Location) task.getResult();
                            if (currentLocation!=null) {
                                Location loc = new Location(currentLocation.getLatitude(), currentLocation.getLongitude());
                                Log.d(TAG, "new loc obj=" + loc.toString());
                                User.setUserLocation(loc);
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                        DEFAULT_ZOOM);
                            }
                            else{
                                Log.d(TAG,"wired!!!!! - received null but task.isSuccessful() = true");
                            }

                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(getActivity(), "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }*/

    public void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        //hideSoftKeyboard();
    }

    public final Marker addMarker(LatLng latLng, String title, String snippedStr){
        MarkerOptions markerO = new MarkerOptions().position(latLng).title(title).snippet(snippedStr);
        if(mMap!= null){
            Marker marker = mMap.addMarker(markerO);
            marker.setVisible(true);
            marker.showInfoWindow();//will cause the title to show even without clicking on the marker
            Log.d(TAG, "addMarker: Marker added:"+String.valueOf(latLng.latitude) + ","+String.valueOf(latLng.longitude) +", "+ title);
            return marker;
        }
        Log.d(TAG, "addMarker: Marker not added");
        return  null;
    }

    public Circle  addCircle(LatLng latLng, double radius){
        radius*=1000;
        CircleOptions circleO = new CircleOptions().center(latLng).radius(radius).strokeWidth(3);
        if(mMap!= null) {
            Circle circle = mMap.addCircle(circleO);
            circle.setVisible(true);
            Log.d(TAG, "addCircle: circle added:"+String.valueOf(latLng.latitude) + ","+String.valueOf(latLng.longitude) +", "+ String.valueOf(radius));
            return circle;
        }
        Log.d(TAG, "addCircle: Circle not added");
        return  null;

    }




   @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
       //toMakeChangesInMap = false;
       //
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
       //toMakeChangesInMap = true;
       // setContentView(R.layout.activity_map);
       // mSearchText = (EditText) findViewById(R.id.input_search);
       // mGps = (ImageView) findViewById(R.id.ic_gps);

      //  getLocationPermission();//already did it in tabbed activity

    }





    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MyMapFragment.this);
        Log.d(TAG, "now map is ready for any thing");

        //addUserGroupsMarkers();
        //removeNonUserGroupsMarkers();

        //toMakeChangesInMap = true;
        //Log.d(TAG, "toMakeChangesInMap = true");
    }

    /*private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(getActivity(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //mMap.setMyLocationEnabled(true);
            if(ContextCompat.checkSelfPermission(getActivity(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();

            }else{
                ActivityCompat.requestPermissions(getActivity(),
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(getActivity(),
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

    }*/



    //if the user not have permissions - enters his first time for example
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    Log.d(TAG, "before init map first time");
                    initMap();
                    Log.d(TAG, "after init map first time");
                }
            }
        }
    }
}