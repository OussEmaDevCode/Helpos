package helpos.helpos;

import androidx.fragment.app.FragmentActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Picker extends FragmentActivity implements OnMapReadyCallback {
    @BindView(R.id.current_position)
    FloatingActionButton currentPositionFab;
    @BindView(R.id.done)
    FloatingActionButton done;
    @BindView(R.id.remove)
    FloatingActionButton remove;

    private LocationManager mLocationManager;
    public static final int LOCATION_UPDATE_MIN_DISTANCE = 10;
    public static final int LOCATION_UPDATE_MIN_TIME = 5000;
    Location mLocation = null;
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null && mMap != null) {
                mLocation = location;
                LatLng gps = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 15));
            }
        }
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {}

        @Override
        public void onProviderEnabled(String s) {}

        @Override
        public void onProviderDisabled(String s) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker);
        ButterKnife.bind(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getCurrentLocation();

    }

    private GoogleMap mMap;
    Marker marker;

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(latlng -> {
            mMap.clear();
            marker = mMap.addMarker(new MarkerOptions().position(latlng));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 600, null);
        });

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        currentPositionFab.bringToFront();
        done.bringToFront();
        remove.bringToFront();

        currentPositionFab.setOnClickListener(v -> {
            if (mLocation != null) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("lat", mLocation.getLatitude());
                returnIntent.putExtra("long", mLocation.getLongitude());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "We couldn't define your location", Toast.LENGTH_SHORT).show();
            }
        });

        done.setOnClickListener(v -> {
            if (marker != null) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("lat", marker.getPosition().latitude);
                returnIntent.putExtra("long", marker.getPosition().longitude);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Please choose a location", Toast.LENGTH_SHORT).show();
            }
        });

        remove.setOnClickListener(v -> {
            mMap.clear();
            marker = null;
        });

        if (mLocation != null) {
            LatLng gps = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 15));
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!(isGPSEnabled || isNetworkEnabled)) {
            new AlertDialog.Builder(Picker.this).setTitle("Enable location")
                    .setMessage("Please enable location or network to make the app function properly")
                    .setIcon(R.drawable.ic_location_on_black_24dp)
                    .setCancelable(false)
                    .setPositiveButton("enable", (dialog, which) -> startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))).show();
        } else {
            if (isGPSEnabled) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
                mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } else {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
                mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

        }
    }
}
