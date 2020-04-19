package helpos.helpos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import helpos.helpos.models.StoredUser;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
        } else {
            createBtn.setText("Create an account");
            signInBtn.setText("Sign In");
            phoneWrap.setVisibility(View.GONE);
            userWrap.setVisibility(View.GONE);
        }
        signIn = !signIn;
    }

    @OnClick(R.id.signInBtn)
    void signIn() {
        if (signIn && checkSignIn()) {
            Toast.makeText(getApplicationContext(), "Signing you in", Toast.LENGTH_LONG).show();
            mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(SignInUpActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Snackbar.make(parent, task.getException().getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });
        } else if (!signIn && checkSignUp()) {
            Toast.makeText(getApplicationContext(), "Setting up your account", Toast.LENGTH_LONG).show();
            mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            createUser(user);
                        } else {
                            Snackbar.make(parent, task.getException().getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
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
                users.child(Fuser.getUid())
                        .setValue(new StoredUser(user.getText().toString(), Fuser.getUid(), phone.getText().toString(), 0))
                        .addOnCompleteListener(task1 -> {
                            startActivity(new Intent(SignInUpActivity.this, MainActivity.class));
                            finish();
                        });
            }
        });
    }
}
