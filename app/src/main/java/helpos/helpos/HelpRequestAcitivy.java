package helpos.helpos;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import helpos.helpos.models.HelpRequest;
import helpos.helpos.models.StoredUser;
import helpos.helpos.utils.Error;

public class HelpRequestAcitivy extends AppCompatActivity {

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.author)
    TextView author;
    @BindView(R.id.price)
    TextView price;
    @BindView(R.id.ability)
    TextView ability;
    @BindView(R.id.person)
    View person;
    @BindView(R.id.remove)
    Button remove;
    @BindView(R.id.cancel)
    Button cancel;
    @BindView(R.id.fulfilled)
    Button fulfilled;
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.karma)
    TextView karma;
    @BindView(R.id.roadto)
    Button roadTo;
    @BindView(R.id.call)
    Button call;

    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_request);
        setTitle("Help Request");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        HelpRequest helpRequest = (HelpRequest) getIntent().getSerializableExtra("helpRequest");
        title.setText(helpRequest.getTitle() + " :");
        description.setText(helpRequest.getDescription());
        author.setText("-"+ helpRequest.getuName());
        price.setText(String.valueOf(helpRequest.getPrice()) + "dt");
        if(helpRequest.isPay()) {
            ability.setText("yes");
            ability.setTextColor(Color.parseColor("#4DB6AC"));
        } else {
            ability.setText("none");
            ability.setTextColor(Color.parseColor("#B71C1C"));
        }

        if(!currentUserID.equals(helpRequest.getUid())) {
            //Requests I am fulfilling
            fulfilled.setVisibility(View.GONE);
            person.setVisibility(View.GONE);
            roadTo.setVisibility(View.VISIBLE);
            call.setVisibility(View.VISIBLE);
            reference.child("Users").child(helpRequest.getUid()).child("phoneNumber")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String num = dataSnapshot.getValue().toString();
                    call.setOnClickListener(view -> {
                        Intent intent = new Intent(Intent.ACTION_DIAL,
                                Uri.fromParts("tel", num, null));
                        startActivity(intent);
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            cancel.setOnClickListener(v -> {
                if (Error.isNetworkAvailable(HelpRequestAcitivy.this)) {
                    reference.child("Users")
                            .child(currentUserID)
                            .child("CurrentRequests")
                            .child(helpRequest.getId())
                            .removeValue();
                    if(!helpRequest.isOrg()) {
                        reference.child("HelpRequests")
                                .child(helpRequest.getId())
                                .child("personHelping")
                                .removeValue();

                        reference.child("Users")
                                .child(helpRequest.getUid())
                                .child("HelpRequests")
                                .child(helpRequest.getId())
                                .child("personHelping")
                                .removeValue();
                    } else {
                        reference.child("HelpRequests")
                                .child(helpRequest.getId())
                                .child("peopleHelping")
                                .child(currentUserID)
                                .removeValue();

                        reference.child("Users")
                                .child(helpRequest.getUid())
                                .child("HelpRequests")
                                .child(helpRequest.getId())
                                .child("peopleHelping")
                                .child(currentUserID)
                                .removeValue();
                    }
                    finish();
                }
            });
            roadTo.setOnClickListener(v -> {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr="
                                +helpRequest.getLatlong().get(0).toString()
                                +","
                                +helpRequest.getLatlong().get(1).toString()));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(intent);
            });
        } else {
            // My requests
            roadTo.setVisibility(View.GONE);
            call.setVisibility(View.GONE);
            if (helpRequest.getPersonHelping() != null) {
                fulfilled.setVisibility(View.VISIBLE);
                person.setVisibility(View.VISIBLE);
                reference.child("Users").child(helpRequest.getPersonHelping())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        StoredUser storedUser = dataSnapshot.getValue(StoredUser.class);
                        name.setText(storedUser.getUserName());
                        call.setVisibility(View.VISIBLE);
                        call.setOnClickListener(view -> {
                            Intent intent = new Intent(Intent.ACTION_DIAL,
                                    Uri.fromParts("tel", storedUser.getPhoneNumber(), null));
                            startActivity(intent);
                        });
                        karma.setText(String.valueOf(storedUser.getKarma()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                remove.setOnClickListener(v -> {
                    if (Error.isNetworkAvailable(HelpRequestAcitivy.this)) {
                        reference.child("Users")
                                .child(helpRequest.getPersonHelping())
                                .child("CurrentRequests")
                                .child(helpRequest.getId())
                                .removeValue();

                        reference.child("Users")
                                .child(currentUserID)
                                .child("HelpRequests")
                                .child(helpRequest.getId())
                                .child("personHelping")
                                .removeValue();

                        reference.child("HelpRequests")
                                .child(helpRequest.getId())
                                .child("personHelping")
                                .removeValue();
                        finish();
                    }
                });
            }
            cancel.setOnClickListener(v -> {
                deleteRequest(currentUserID, helpRequest);
                finish();
            });

            fulfilled.setOnClickListener(v -> {
                if (Error.isNetworkAvailable(HelpRequestAcitivy.this)) {
                    if (helpRequest.getPersonHelping() != null) {
                        reference.child("Users")
                                .child(helpRequest.getPersonHelping())
                                .child("karma")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        long value = (long) dataSnapshot.getValue();
                                        dataSnapshot.getRef().setValue(value + 10);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }
                    deleteRequest(currentUserID, helpRequest);
                    reference.child("Users")
                            .child(helpRequest.getPersonHelping())
                            .child("PeopleHelped")
                            .push()
                            .setValue(helpRequest.getuName());
                    finish();
                }
            });
        }
    }

    private void deleteRequest(String currentUserID, HelpRequest helpRequest) {
        if (Error.isNetworkAvailable(HelpRequestAcitivy.this)) {
            reference.child("Users")
                    .child(currentUserID)
                    .child("HelpRequests")
                    .child(helpRequest.getId())
                    .removeValue();

            reference.child("HelpRequests")
                    .child(helpRequest.getId())
                    .removeValue();

            if (helpRequest.getPersonHelping() != null) {
                reference.child("Users")
                        .child(helpRequest.getPersonHelping())
                        .child("CurrentRequests")
                        .removeValue();
            }
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(HelpRequestAcitivy.this, Profile.class));
        }
        return true;
    }
}
