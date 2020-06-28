package helpos.helpos;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import helpos.helpos.models.HelpRequest;
import helpos.helpos.models.StoredUser;
import helpos.helpos.utils.Error;

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

    @OnClick(R.id.add)
    void create() {
        Intent i = new Intent(MainActivity.this, HelpRequester.class);
        startActivityForResult(i, 1);
    }

    BottomSheetBehavior behavior;

    StoredUser currentUser;

    Location mLocation = null;

    String userId;

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null && mMap != null) {
                mLocation = location;
                LatLng gps = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 17));
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

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //getCurrentLocation();
                if (mMap != null && mLocation != null) {
                    LatLng gps = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 17));
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    mMap.getUiSettings().setAllGesturesEnabled(true);
                }

            } else {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Please enable location")
                        .setMessage("Please let us access your location to make the app function properly")
                        .setIcon(R.drawable.ic_warning_black_24dp)
                        .setCancelable(false)
                        .setPositiveButton("enable", (dialog, which) -> {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            dialog.dismiss();
                        })
                        .create()
                        .show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setTitle("Home");
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            startActivity(new Intent(MainActivity.this, SignInUpActivity.class));
        }

        FirebaseDatabase.getInstance().getReference().child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(StoredUser.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        behavior = BottomSheetBehavior.from(bottomSheet);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setAllGesturesEnabled(true);
            //getCurrentLocation();
        }
        if (mLocation != null) {
            LatLng gps = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 17));
        }

        FirebaseDatabase.getInstance().getReference().child("HelpRequests")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mMap.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            HelpRequest helpRequest = snapshot.getValue(HelpRequest.class);
                            if (helpRequest.getPersonHelping() == null) {
                                LatLng position = new LatLng(helpRequest.getLatlong().get(0), helpRequest.getLatlong().get(1));
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(position)
                                        .title(helpRequest.getTitle());
                                Marker marker = mMap.addMarker(markerOptions);
                                marker.setTag(helpRequest.getId());
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        new Error(bottomSheet, "Failed retrieving help requests");
                    }
                });

        mMap.setOnMarkerClickListener(marker -> {
            emptyText.setVisibility(View.GONE);
            content.setVisibility(View.VISIBLE);
            help.setVisibility(View.INVISIBLE);
            bottomSheet.setPadding(0, 0, 0, 0);
            FirebaseDatabase.getInstance().getReference().child("HelpRequests")
                    .child(marker.getTag().toString())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                            HelpRequest helpRequest = dataSnapshot.getValue(HelpRequest.class);
                            title.setText(helpRequest.getTitle() + " :");
                            description.setText(helpRequest.getDescription());
                            if (helpRequest.getPrice() >= 0) {
                                price.setText(String.valueOf(helpRequest.getPrice()) + "dt");
                            } else {
                                price.setText("Unknown");
                            }
                            if (helpRequest.isPay()) {
                                isPay.setText(R.string.yes);
                                isPay.setTextColor(Color.parseColor("#4DB6AC"));
                            } else {
                                isPay.setText(R.string.none);
                                isPay.setTextColor(Color.parseColor("#B71C1C"));
                            }
                            if (helpRequest.getUid().equals(userId)) {
                                author.setText("-" + "You");
                            } else if (helpRequest.isOrg()) {
                                databaseReference.child("HelpRequests")
                                        .child(helpRequest.getId())
                                        .child("peopleHelping")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (!dataSnapshot.hasChild(userId)) {
                                                    author.setText("-" + helpRequest.getuName());
                                                    help.setVisibility(View.VISIBLE);
                                                    bottomSheet.setPadding(0, 16, 0, 0);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                            } else {
                                author.setText("-" + helpRequest.getuName());
                                help.setVisibility(View.VISIBLE);
                                bottomSheet.setPadding(0, 16, 0, 0);
                            }
                            help.setOnClickListener(v -> {
                                if (Error.isNetworkAvailable(MainActivity.this)) {
                                    databaseReference.child("Users")
                                            .child(userId)
                                            .child("CurrentRequests")
                                            .child(helpRequest.getId())
                                            .setValue(helpRequest);

                                    if (!helpRequest.isOrg()) {
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
                                    } else {
                                        databaseReference.child("HelpRequests")
                                                .child(helpRequest.getId())
                                                .child("peopleHelping")
                                                .child(userId)
                                                .setValue(currentUser);

                                        databaseReference.child("Users")
                                                .child(helpRequest.getUid())
                                                .child("HelpRequests")
                                                .child(helpRequest.getId())
                                                .child("peopleHelping")
                                                .child(userId)
                                                .setValue(currentUser);
                                    }
                                    marker.remove();
                                    emptyText.setVisibility(View.VISIBLE);
                                    content.setVisibility(View.GONE);
                                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                                    Intent i = new Intent(MainActivity.this, HelpRequestAcitivy.class);
                                    i.putExtra("helpRequest", helpRequest);
                                    startActivity(i);
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            new Error(bottomSheet, "Failed retrieving help request information");
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
        if (item.getItemId() == R.id.profile) {
            startActivity(new Intent(MainActivity.this, Profile.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!(isGPSEnabled || isNetworkEnabled)) {
            new AlertDialog.Builder(MainActivity.this).setTitle("Enable location")
                    .setMessage("Please enable location or network to make the app function properly")
                    .setIcon(R.drawable.ic_location_on_black_24dp)
                    .setCancelable(false)
                    .setPositiveButton("enable", (dialog, which) ->
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .show();
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

    @Override
    protected void onResume() {
        super.onResume();
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        content.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                LatLng gps = new LatLng(data.getDoubleExtra("lat", 0), data.getDoubleExtra("long", 0));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 18));
            }
        }
    }
}
