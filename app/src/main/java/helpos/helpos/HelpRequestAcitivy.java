package helpos.helpos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import helpos.helpos.models.HelpRequest;
import helpos.helpos.models.StoredUser;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HelpRequestAcitivy extends AppCompatActivity {

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.author)
    TextView author;
    @BindView(R.id.authorPhone)
    TextView authorPhone;
    @BindView(R.id.authorPhoneWrapper)
    View authorPhoneWrapper;
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
    @BindView(R.id.phone)
    TextView phone;
    @BindView(R.id.karma)
    TextView karma;

    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_request);
        setTitle("Help Request");
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
            fulfilled.setVisibility(View.GONE);
            person.setVisibility(View.GONE);
            authorPhoneWrapper.setVisibility(View.VISIBLE);
            reference.child("Users").child(helpRequest.getUid()).child("phoneNumber").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    authorPhone.setText(dataSnapshot.getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            cancel.setOnClickListener(v -> {
                reference.child("Users")
                        .child(currentUserID)
                        .child("CurrentRequests")
                        .child(helpRequest.getId())
                        .removeValue();

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
                finish();
            });
        } else {
            if (helpRequest.getPersonHelping() != null) {
                fulfilled.setVisibility(View.VISIBLE);
                person.setVisibility(View.VISIBLE);
                reference.child("Users").child(helpRequest.getPersonHelping())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        StoredUser storedUser = dataSnapshot.getValue(StoredUser.class);
                        name.setText(storedUser.getUserName());
                        phone.setText(storedUser.getPhoneNumber());
                        karma.setText(String.valueOf(storedUser.getKarma()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                remove.setOnClickListener(v -> {
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
                });
            }
            cancel.setOnClickListener(v -> {
                deleteRequest(currentUserID, helpRequest);
                finish();
            });

            fulfilled.setOnClickListener(v -> {
                if (helpRequest.getPersonHelping() != null) {
                    reference.child("Users")
                            .child(helpRequest.getPersonHelping())
                            .child("karma")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            long value =(long) dataSnapshot.getValue();
                            dataSnapshot.getRef().setValue(value+ 10);
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
            });
        }
    }

    private void deleteRequest(String currentUserID, HelpRequest helpRequest) {
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
