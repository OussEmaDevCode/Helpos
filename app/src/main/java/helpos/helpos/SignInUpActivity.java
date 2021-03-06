package helpos.helpos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import helpos.helpos.models.StoredUser;
import helpos.helpos.utils.Error;

public class SignInUpActivity extends AppCompatActivity {

    @BindView(R.id.login_user)
    EditText user;
    @BindView(R.id.login_phone)
    EditText phone;
    @BindView(R.id.login_mail)
    EditText email;
    @BindView(R.id.login_password)
    EditText password;
    @BindView(R.id.login_phone_wrapper)
    View phoneWrap;
    @BindView(R.id.login_user_wrapper)
    View userWrap;
    @BindView(R.id.create)
    Button createBtn;
    @BindView(R.id.signInBtn)
    Button signInBtn;
    @BindView(R.id.parent)
    View parent;
    @BindView(R.id.org_wrapper)
    View orgWrap;
    @BindView(R.id.org)
    CheckBox org;

    Boolean signIn = true;

    DatabaseReference users = FirebaseDatabase.getInstance().getReference().child("Users");
    FirebaseAuth mAuth;

    @OnClick(R.id.create)
    void create() {
        if (signIn) {
            createBtn.setText("Already have an account");
            signInBtn.setText("Sign Up");
            phoneWrap.setVisibility(View.VISIBLE);
            userWrap.setVisibility(View.VISIBLE);
            orgWrap.setVisibility(View.VISIBLE);
        } else {
            createBtn.setText("Create an account");
            signInBtn.setText("Sign In");
            phoneWrap.setVisibility(View.GONE);
            userWrap.setVisibility(View.GONE);
            orgWrap.setVisibility(View.GONE);
        }
        signIn = !signIn;
    }

    @OnClick(R.id.signInBtn)
    void signIn() {
        if (signIn && checkSignIn()) {
            new Error(SignInUpActivity.this, "Signing you in");
            mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(SignInUpActivity.this, MainActivity.class));
                            finish();
                        } else {
                            new Error(parent, task.getException().getLocalizedMessage());
                        }
                    });
        } else if (!signIn && checkSignUp()) {
            new Error(SignInUpActivity.this, "Setting up your account");
            mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            createUser(user);
                        } else {
                            new Error(parent, task.getException().getLocalizedMessage());
                        }
                    });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_up);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(SignInUpActivity.this, MainActivity.class));
            finish();
        }
    }

    private Boolean checkSignIn() {
        boolean status = true;
        if (password.getText() == null || password.getText().toString().trim().isEmpty()) {
            status = false;
            password.setError("Password can't be empty");
        } else if (password.getText().toString().trim().length() < 6) {
            password.setError("Password must be longer");
            status = false;
        }
        if (email.getText() == null || email.getText().toString().isEmpty()) {
            status = false;
            email.setError("Password can't be empty");
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            status = false;
            email.setError("email format isn't correct");
        }
        return status;
    }

    private Boolean checkSignUp() {
        boolean status = true;
        if (password.getText() == null || password.getText().toString().trim().isEmpty()) {
            status = false;
            password.setError("Password can't be empty");
        } else if (password.getText().toString().trim().length() < 6) {
            password.setError("Password must be longer");
            status = false;
        } else if (!password.getText().toString().matches(".*\\d.*")) {
            status = false;
            password.setError("Password must contain at least a number");
        } else if (!password.getText().toString().matches("(?s).*[A-Z].*")) {
            status = false;
            password.setError("Password must contain a capital letter");
        }
        if (user.getText() == null || user.getText().toString().trim().isEmpty()) {
            status = false;
            user.setError("User name can't be empty");
        } else if (user.getText().toString().trim().length() < 4) {
            user.setError("User name must be longer");
            status = false;
        } else if (user.getText().toString().trim().length() > 25) {
            user.setError("User name must be shorter");
            status = false;
        }
        if (email.getText() == null || email.getText().toString().isEmpty()) {
            status = false;
            email.setError("email can't be empty");
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            status = false;
            email.setError("email format isn't correct");
        }
        if (phone.getText() == null || phone.getText().toString().isEmpty()) {
            status = false;
            phone.setError("phone number can't be empty");
        }
        return status;
    }

    private void createUser(FirebaseUser Fuser) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(user.getText().toString())
                .build();

        Fuser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                StoredUser storedUser = org.isChecked() ?
                        new StoredUser(user.getText().toString(), Fuser.getUid(), phone.getText().toString(), null, org.isChecked()) :
                        new StoredUser(user.getText().toString(), Fuser.getUid(), phone.getText().toString(), 0, org.isChecked());

                users.child(Fuser.getUid())
                        .setValue(storedUser)
                        .addOnCompleteListener(task1 -> {
                            startActivity(new Intent(SignInUpActivity.this, MainActivity.class));
                            finish();
                        });
            }
        });
    }

    @Override
    public void onBackPressed() {

    }
}
