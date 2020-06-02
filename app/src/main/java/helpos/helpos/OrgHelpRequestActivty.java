package helpos.helpos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import helpos.helpos.models.HelpRequest;
import helpos.helpos.models.StoredUser;
import helpos.helpos.viewHolder.PersonHelpingViewHolder;
import helpos.helpos.viewHolder.ViewHolder;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OrgHelpRequestActivty extends AppCompatActivity {
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
    @BindView(R.id.cancel)
    Button cancel;
    @BindView(R.id.fulfilled)
    Button fulfilled;
    @BindView(R.id.people_helping)
    RecyclerView peopleHelping;

    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_org_help_request_activty);
        setTitle("Help Request");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        String currentUserId = FirebaseAuth.getInstance().getUid();
        HelpRequest helpRequest = (HelpRequest) getIntent().getSerializableExtra("helpRequest");
        title.setText(helpRequest.getTitle() + " :");
        description.setText(helpRequest.getDescription());
        author.setText("-" + helpRequest.getuName());
        price.setText(String.valueOf(helpRequest.getPrice()) + "dt");
        if (helpRequest.isPay()) {
            ability.setText("yes");
            ability.setTextColor(Color.parseColor("#4DB6AC"));
        } else {
            ability.setText("none");
            ability.setTextColor(Color.parseColor("#B71C1C"));
        }
        peopleHelping.setHasFixedSize(true);
        peopleHelping.setLayoutManager(new LinearLayoutManager(this));
        peopleHelping.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        ArrayList<StoredUser> storedUsers = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference()
                .child("HelpRequests")
                .child(helpRequest.getId())
                .child("peopleHelping").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                storedUsers.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    storedUsers.add(ds.getValue(StoredUser.class));
                }
                RecyclerView.Adapter<PersonHelpingViewHolder> adapter = new RecyclerView.Adapter<PersonHelpingViewHolder>() {
                    @NonNull
                    @Override
                    public PersonHelpingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_person_helping, parent, false);
                        return new PersonHelpingViewHolder(view);
                    }

                    @Override
                    public void onBindViewHolder(@NonNull PersonHelpingViewHolder holder, int position) {
                        StoredUser user = storedUsers.get(position);
                        holder.title.setText(user.getUserName());
                        holder.call.setOnClickListener(v -> {
                            Intent intent = new Intent(Intent.ACTION_DIAL,
                                    Uri.fromParts("tel", user.getPhoneNumber(), null));
                            startActivity(intent);
                        });
                        holder.remove.setOnClickListener(v -> {
                            ref.child("Users")
                                    .child(user.getUserId())
                                    .child("CurrentRequests")
                                    .child(helpRequest.getId())
                                    .removeValue();

                            ref.child("HelpRequests")
                                    .child(helpRequest.getId())
                                    .child("peopleHelping")
                                    .child(user.getUserId())
                                    .removeValue();

                            ref.child("Users")
                                    .child(currentUserId)
                                    .child("HelpRequests")
                                    .child(helpRequest.getId())
                                    .child("peopleHelping")
                                    .child(user.getUserId())
                                    .removeValue();
                        });
                    }

                    @Override
                    public int getItemCount() {
                        return storedUsers.size();
                    }
                };
                peopleHelping.setAdapter(adapter);

                if (!storedUsers.isEmpty()) {
                    fulfilled.setVisibility(View.VISIBLE);
                    fulfilled.setOnClickListener(v -> {
                        ref.child("HelpRequests")
                                .child(helpRequest.getId())
                                .removeValue();

                        ref.child("Users")
                                .child(currentUserId)
                                .child("HelpRequests")
                                .child(helpRequest.getId())
                                .removeValue();

                        for (StoredUser user : storedUsers) {
                            ref.child("Users")
                                    .child(user.getUserId())
                                    .child("CurrentRequests")
                                    .child(helpRequest.getId())
                                    .removeValue();
                            ref.child("Users")
                                    .child(user.getUserId())
                                    .child("karma").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                    ref.child("Users")
                                            .child(user.getUserId())
                                            .child("karma")
                                            .setValue(dataSnapshot1.getValue(Integer.class)+ 10);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        finish();
                    });
                }
                cancel.setOnClickListener(v -> {
                    ref.child("HelpRequests")
                            .child(helpRequest.getId())
                            .removeValue();

                    ref.child("Users")
                            .child(currentUserId)
                            .child("HelpRequests")
                            .child(helpRequest.getId())
                            .removeValue();
                    if(!storedUsers.isEmpty()) {
                        for (StoredUser user : storedUsers) {
                            ref.child("Users")
                                    .child(user.getUserId())
                                    .child("CurrentRequests")
                                    .child(helpRequest.getId())
                                    .removeValue();
                        }
                    }
                    finish();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(OrgHelpRequestActivty.this, Profile.class));
        }
        return true;
    }

}