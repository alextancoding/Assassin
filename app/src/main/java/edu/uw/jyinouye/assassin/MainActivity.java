package edu.uw.jyinouye.assassin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;

import edu.uw.jyinouye.assassin.fragments.ChatFragment;
import edu.uw.jyinouye.assassin.fragments.LeaderboardFragment;
import edu.uw.jyinouye.assassin.fragments.ProfileFragment;
import edu.uw.jyinouye.assassin.fragments.ShopFragment;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "MainActivity";

    private Toolbar toolbar;
    private DrawerLayout mDrawer;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle mDrawerToggle;

    private SupportMapFragment mMapFragment;
    private ChatFragment mChatFragment;
    private LeaderboardFragment mLeaderboardFragment;
    private ProfileFragment mProfileFragment;
    private ShopFragment mShopFragment;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private static final int LOC_REQUEST_CODE = 0;

    private Assassin assassin;
    private Player player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        assassin = ((Assassin)getApplicationContext()).getInstance();
        player = assassin.getPlayer();

        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Find our drawer view
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(nvDrawer);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, R.string.nav_open, R.string.nav_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };

        mDrawer.addDrawerListener(mDrawerToggle);

        // Poll for location every 10 seconds, max 5 seconds
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // create references to fragments to add later
        mChatFragment = new ChatFragment();
        mLeaderboardFragment = new LeaderboardFragment();
        mProfileFragment = new ProfileFragment();
        mShopFragment = new ShopFragment();

        // create new mapfragment with callbacks to this activity
        mMapFragment = SupportMapFragment.newInstance();
        mMapFragment.getMapAsync(this);

        // Setup initial state where all but mapfragment is hidden
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.flContent, mMapFragment)
                .add(R.id.flContent, mChatFragment)
                .add(R.id.flContent, mLeaderboardFragment)
                .add(R.id.flContent, mProfileFragment)
                .add(R.id.flContent, mShopFragment)
                .hide(mChatFragment)
                .hide(mLeaderboardFragment)
                .hide(mProfileFragment)
                .hide(mShopFragment)
                .show(mMapFragment)
                .commit();
    }

    /**
     * All code relating to drawer layout implementaion came from
     * https://guides.codepath.com/android/Fragment-Navigation-Drawer
     */

    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE! Make sure to override the method with only a single `Bundle` argument
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        switch(menuItem.getItemId()) {
            case R.id.nav_map_fragment:
                ft.show(mMapFragment)
                        .hide(mChatFragment)
                        .hide(mLeaderboardFragment)
                        .hide(mProfileFragment)
                        .hide(mShopFragment);
                break;
            case R.id.nav_chat_fragment:
                ft.show(mChatFragment)
                        .hide(mMapFragment)
                        .hide(mLeaderboardFragment)
                        .hide(mProfileFragment)
                        .hide(mShopFragment);
                break;
            case R.id.nav_leaderboard_fragment:
                ft.show(mLeaderboardFragment)
                        .hide(mChatFragment)
                        .hide(mMapFragment)
                        .hide(mProfileFragment)
                        .hide(mShopFragment);
                break;
            case R.id.nav_shop_fragment:
                ft.show(mShopFragment)
                        .hide(mChatFragment)
                        .hide(mLeaderboardFragment)
                        .hide(mProfileFragment)
                        .hide(mMapFragment);
                break;
            case R.id.nav_profile_fragment:
                ft.show(mProfileFragment)
                        .hide(mChatFragment)
                        .hide(mLeaderboardFragment)
                        .hide(mMapFragment)
                        .hide(mShopFragment);
                break;
            default:
                ft.show(mMapFragment)
                        .hide(mChatFragment)
                        .hide(mLeaderboardFragment)
                        .hide(mProfileFragment)
                        .hide(mShopFragment);
                break;
        }

        ft.commit();

        // Highlight the selected item, update the title, and close the drawer
        // Highlight the selected item has been done by NavigationView
        // menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        mDrawer.closeDrawers();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set map ui elements
        UiSettings mapUiSettings = mMap.getUiSettings();
        mapUiSettings.setMapToolbarEnabled(true);
        mapUiSettings.setZoomControlsEnabled(true);
        mapUiSettings.setMyLocationButtonEnabled(true);

        // Show location and zoom
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permission == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(47.6097, -122.3331)));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(13));
        } else {
            requestPermission();
        }
    }

    // Handles conversion between Location and LatLng
    private LatLng toLatLng(Location l) {
        return new LatLng(l.getLatitude(), l.getLongitude());
    }

    // Requests for permission to use ACCESS_FINE_LOCATION
    private void requestPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            //TODO: Explain why you need the permission
        }
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOC_REQUEST_CODE);
    }

    // Updates location
    protected void startLocationUpdates() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permission == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        } else {
            requestPermission();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //TODO: check why location isn't being sent correctly to firebase,
        player.setLocation(location);
        Log.v(TAG, "Location:" + location.getLatitude() + ", " + location.getLongitude());
    }

    @Override
    public void onConnected(Bundle bundle) {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permission == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        } else {
            requestPermission();
        }

        // Move the camera to you
        if (mLastLocation != null) {
            LatLng lastPos = toLatLng(mLastLocation);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(lastPos));
        }

        // make sure we poll for updates
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            // Granted permission, continue
            case LOC_REQUEST_CODE:
                onConnected(null);
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }
}
