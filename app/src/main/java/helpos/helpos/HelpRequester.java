package helpos.helpos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import helpos.helpos.models.HelpRequest;
import helpos.helpos.utils.Error;

public class HelpRequester extends AppCompatActivity {

    @BindView(R.id.editTitle)
    EditText title;
    @BindView(R.id.description)
    EditText description;
    @BindView(R.id.price)
    EditText price;
    @BindView(R.id.ispay)
    CheckBox ispay;

    List<Double> place = new ArrayList<>();

    FirebaseAuth mAuth;

    @OnClick(R.id.locate)
    void locate () {
        Intent i = new Intent(HelpRequester.this, Picker.class);
        startActivityForResult(i, 1);
    }

    @OnClick(R.id.submit)
    void submit () {
        if (check()) {
            String helpID = random();
            HelpRequest helpRequest = new HelpRequest(
                    title.getText().toString(),
                    description.getText().toString(),
                    Integer.parseInt(price.getText().toString()),
                    ispay.isChecked(),
                    place,
                    mAuth.getUid(),
                    helpID,
                    mAuth.getCurrentUser().getDisplayName()
                    , null);

            FirebaseDatabase.getInstance()
                    .getReference()
                    .child("HelpRequests")
                    .child(helpID)
                    .setValue(helpRequest).addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            FirebaseDatabase.getInstance()
                                    .getReference()
                                    .child("Users")
                                    .child(mAuth.getUid())
                                    .child("HelpRequests")
                                    .child(helpID)
                                    .setValue(helpRequest).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    finish();
                                }
                            });
                        } else {
                            new Error(getWindow().getDecorView().getRootView(),"Couldn't create your help request");
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                place.add(data.getDoubleExtra("lat", 0));
                place.add(data.getDoubleExtra("long", 0));
            }
        }
    }

    private boolean check() {
        String titleS = title.getText().toString();
        String descriptionS = description.getText().toString();
        if (!(titleS.length() > 4 && titleS.length() < 40)) {
            title.setError("title too long or too short");
            return false;
        }
        if (!(descriptionS.length() > 15 && descriptionS.length() < 1999 )) {
            description.setError("description too long or too short");
            return false;
        }
        if (price.getText().toString().isEmpty()) {
            price.setError("please enter a price");
            return false;
        }
        if (place.isEmpty()) {
            new Error(getApplicationContext(), "please choose a place");
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_requester);
        setTitle("Create Help Request");
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
    }

    private String random() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
