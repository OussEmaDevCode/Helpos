package helpos.helpos;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import helpos.helpos.models.HelpRequest;
import helpos.helpos.viewHolder.ViewHolder;

public class Profile extends AppCompatActivity {

    @BindView(R.id.personal_requests)
    RecyclerView personalRequests;
    @BindView(R.id.current_requests)
    RecyclerView currentRequests;
    @BindView(R.id.people_helped)
    RecyclerView people_helped;
    @BindView(R.id.title)
    TextView title;

    @OnClick(R.id.sign_out)
    void signOut() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(Profile.this, SignInUpActivity.class));
        finish();
    }

    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        userId = current.getUid();
        title.setText(current.getDisplayName() + " :");
        setUp(personalRequests, currentRequests, people_helped);
        personal();
        current();
        people();
    }

    private void setUp(RecyclerView... recyclerViews) {
        for (RecyclerView recycler : recyclerViews) {
            recycler.setHasFixedSize(true);
            recycler.setLayoutManager(new LinearLayoutManager(this));
            recycler.addItemDecoration(new DividerItemDecoration(this,
                    DividerItemDecoration.VERTICAL));
        }
    }

    private void personal() {
        FirebaseRecyclerOptions<HelpRequest> options = new FirebaseRecyclerOptions.Builder<HelpRequest>()
                .setQuery(reference.child("Users").child(userId).child("HelpRequests"), HelpRequest.class)
                .setLifecycleOwner(this)
                .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<HelpRequest, ViewHolder>(options) {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(ViewHolder holder, final int position, HelpRequest helpRequest) {
                holder.title.setText(helpRequest.getTitle());
                holder.root.setOnClickListener(view -> {
                    Intent i = helpRequest.isOrg() ? new Intent(Profile.this, OrgHelpRequestActivty.class)
                            : new Intent(Profile.this, HelpRequestAcitivy.class);
                    i.putExtra("helpRequest", helpRequest);
                    startActivity(i);
                });
            }
        };
        personalRequests.setAdapter(adapter);
    }

    private void current() {
        FirebaseRecyclerOptions<HelpRequest> options = new FirebaseRecyclerOptions.Builder<HelpRequest>()
                .setQuery(reference.child("Users").child(userId).child("CurrentRequests"), HelpRequest.class)
                .setLifecycleOwner(this)
                .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<HelpRequest, ViewHolder>(options) {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(ViewHolder holder, final int position, HelpRequest helpRequest) {
                holder.title.setText(helpRequest.getTitle());
                holder.root.setOnClickListener(view -> {
                    Intent i = new Intent(Profile.this, HelpRequestAcitivy.class);
                    i.putExtra("helpRequest", helpRequest);
                    startActivity(i);
                });
            }
        };
        currentRequests.setAdapter(adapter);
    }

    private void people() {
        FirebaseRecyclerOptions<String> options = new FirebaseRecyclerOptions.Builder<String>()
                .setQuery(reference.child("Users").child(userId).child("PeopleHelped"), String.class)
                .setLifecycleOwner(this)
                .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<String, ViewHolder>(options) {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(ViewHolder holder, final int position, String person) {
                holder.title.setText(person);
                holder.root.setOnClickListener(view -> {
                    // TODO add on click
                });
            }
        };
        people_helped.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(Profile.this, MainActivity.class));
        }
        return true;
    }
}
