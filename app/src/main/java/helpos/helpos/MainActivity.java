package helpos.helpos;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import helpos.helpos.models.HelpRequest;
import helpos.helpos.models.StoredUser;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final int LOCATION_UPDATE_MIN_DISTANCE = 10;
    public static final int LOCATION_UPDATE_MIN_TIME = 5000;

    @BindView(R.id.bottom_sheet)
    View bottomSheet;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.author)
    TextView author;
    @BindView(R.id.price)
    TextView price;
    @BindView(R.id.ability)
    TextView isPay;
    @BindView(R.id.help)
    Button help;
    @BindView(R.id.content)
    View content;
    @BindView(R.id.emptyText)
    View emptyText;

    BottomSheetBehavior behavior;

    AlertDialog alert = null;

    Location mLocation = null;

    Boolean locked = false;

    String userId;
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null &&mMap!=null) {
                mLocation = location;
                LatLng gps = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 17));
                mLocationManager.removeUpdates(mLocationListener);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };


    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
            if (requestCode == 1) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation();
                    if(mMap!= null&& mLocation !=null){
                        LatLng gps = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 17));
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        mMap.getUiSettings().setAllGesturesEnabled(true);
                        alert.dismiss();
                    }

                }
                else {
                    alert.show();
                }
            }
        }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setTitle("Home");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        behavior = BottomSheetBehavior.from(bottomSheet);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        alert = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Please enable mLocation")
                .setMessage("Please let us access your mLocation to make the app function properly")
                .setIcon(R.drawable.ic_warning_black_24dp)
                .setCancelable(false)
                .setPositiveButton("enable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                    }
                }).create();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else {
            getCurrentLocation();
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else  {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setAllGesturesEnabled(true);
        }
        FirebaseDatabase.getInstance().getReference().child("HelpRequests")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mMap.clear();
                        int personHelping = 0;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (!snapshot.hasChild("personHelping")) {
                                HelpRequest helpRequest = snapshot.getValue(HelpRequest.class);
                                LatLng position = new LatLng(helpRequest.getLatlong().get(0), helpRequest.getLatlong().get(1));
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(position)
                                        .title(helpRequest.getTitle());
                                Marker marker = mMap.addMarker(markerOptions);
                                marker.setTag(helpRequest.getId());
                            } else {
                                personHelping++;
                            }
                        }
                        locked = (personHelping > 10);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

        if (mLocation != null) {
            LatLng gps = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 17));
        }

        mMap.setOnMarkerClickListener(marker -> {
            emptyText.setVisibility(View.GONE);
            content.setVisibility(View.VISIBLE);
            FirebaseDatabase.getInstance().getReference().child("HelpRequests")
                    .child(marker.getTag().toString())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    HelpRequest helpRequest = dataSnapshot.getValue(HelpRequest.class);
                    title.setText(helpRequest.getTitle()+ " :");
                    description.setText(helpRequest.getDescription());
                    price.setText(String.valueOf(helpRequest.getPrice()) + "dt");
                    if(helpRequest.isPay()) {
                        isPay.setText("yes");
                        isPay.setTextColor(Color.parseColor("#4DB6AC"));
                    } else {
                        isPay.setText("none");
                        isPay.setTextColor(Color.parseColor("#B71C1C"));
                    }
                    if (helpRequest.getUid().equals(userId)) {
                        author.setText("-" + "You");
                        help.setVisibility(View.INVISIBLE);
                    } else {
                        author.setText("-" + helpRequest.getuName());
                        help.setVisibility(View.VISIBLE);
                        help.setOnClickListener(v -> {
                            if (!locked) {
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                                databaseReference.child("Users")
                                        .child(userId)
                                        .child("CurrentRequests")
                                        .child(helpRequest.getId())
                                        .setValue(helpRequest);

                                databaseReference.child("HelpRequests")
                                        .child(helpRequest.getId())
                                        .child("personHelping")
                                        .setValue(userId);

                                databaseReference.child("Users")
                                        .child(helpRequest.getUid())
                                        .child("HelpRequests")
                                        .child(helpRequest.getId())
                                        .child("personHelping")
                                        .setValue(userId);
                                marker.remove();
                                emptyText.setVisibility(View.VISIBLE);
                                content.setVisibility(View.GONE);
                                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            } else {
                                Toast.makeText(getApplicationContext(), "Sorry there are already way too many people out there", Toast.LENGTH_LONG);
                            }
                        });
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            return false;
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.create) {
            startActivity(new Intent(MainActivity.this, HelpRequester.class));
        }else if(item.getItemId() == R.id.profile){
            startActivity(new Intent(MainActivity.this, Profile.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getCurrentLocation() {
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!(isGPSEnabled || isNetworkEnabled)) {
            new AlertDialog.Builder(MainActivity.this).setTitle("Enable mLocation")
                    .setMessage("Please enable mLocation to make the app function properly")
                    .setIcon(R.drawable.ic_location_on_black_24dp)
                    .setCancelable(false)
                    .setPositiveButton("enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                        }
                    }).show();
        } else {
            if (isNetworkEnabled) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    return;
                }
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
                mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            }

            if (isGPSEnabled) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
                mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            }

        }
    }
}
