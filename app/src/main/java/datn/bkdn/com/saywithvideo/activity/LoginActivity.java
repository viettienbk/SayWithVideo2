package datn.bkdn.com.saywithvideo.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.network.Tools;
import datn.bkdn.com.saywithvideo.utils.Constant;
import datn.bkdn.com.saywithvideo.utils.Utils;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tvLogin;
    private EditText edtEmail;
    private EditText edtPass;
    private ImageView clearPass;
    private ImageView clearEmail;
    private Firebase root;
    private CallbackManager callbackManager;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Tools.isOnline(this)) {
            Firebase.setAndroidContext(this);
            FacebookSdk.sdkInitialize(getBaseContext());
            callbackManager = CallbackManager.Factory.create();

            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    loginFacebook(AccessToken.getCurrentAccessToken());
                }

                @Override
                public void onCancel() {
                    Toast.makeText(LoginActivity.this, "Login facebook cancel by user", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(FacebookException error) {
                    Toast.makeText(LoginActivity.this, "Login facebook error", Toast.LENGTH_SHORT).show();
                }
            });
            root = new Firebase(Constant.FIREBASE_ROOT);
        }

        if (!checkCurrentUser()) {
            this.finish();
            startActivity(new Intent(this, MainActivity.class));
        }
        init();
    }

    private boolean checkCurrentUser() {
        return Utils.getCurrentUserEmail(this).equals("null");
    }

    private void init() {
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPass = (EditText) findViewById(R.id.edtpass);
        tvLogin = (TextView) findViewById(R.id.tvLogin);
        TextView tvRegister = (TextView) findViewById(R.id.tvregister);
        TextView tvForgot = (TextView) findViewById(R.id.tvForgot);
        clearEmail = (ImageView) findViewById(R.id.imgClearEmail);
        clearPass = (ImageView) findViewById(R.id.imgClearPass);
        TextView tvLoginFacebook = (TextView) findViewById(R.id.tvLoginFacebook);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);

        tvLogin.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
        tvLoginFacebook.setOnClickListener(this);
        tvForgot.setOnClickListener(this);
        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (edtEmail.getText().toString().equals("")) {
                    clearEmail.setVisibility(View.GONE);
                } else {
                    clearEmail.setVisibility(View.VISIBLE);
                }
            }
        });
        edtPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("")) {
                    clearPass.setVisibility(View.GONE);
                } else {
                    if (s.toString().length() >= 6) {
                        tvLogin.setVisibility(View.VISIBLE);
                    } else {
                        tvLogin.setVisibility(View.GONE);
                    }
                    clearPass.setVisibility(View.VISIBLE);
                }
            }
        });
        clearEmail.setOnClickListener(this);
        clearPass.setOnClickListener(this);
        getEmail();
    }

    private void getEmail() {
        edtEmail.setText(Utils.getPrimaryEmail(this));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgClearEmail:
                edtEmail.setText("");
                break;
            case R.id.imgClearPass:
                edtPass.setText("");
                break;
            case R.id.tvLogin:
                if (!Tools.isOnline(getBaseContext())) {
                    Snackbar.make(findViewById(R.id.root), "Please make sure to have an internet connection.", Snackbar.LENGTH_LONG).show();
                } else {
                    mProgressDialog.show();
                    checkisValidAccount(edtEmail.getText().toString(), edtPass.getText().toString());
                }
                datn.bkdn.com.saywithvideo.utils.Tools.hideKeyboard(LoginActivity.this);
                break;
            case R.id.tvregister:
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                break;
            case R.id.tvLoginFacebook:
                if (!Tools.isOnline(getBaseContext())) {
                    Snackbar.make(findViewById(R.id.root), "Please make sure to have an internet connection.", Snackbar.LENGTH_LONG).show();
                } else {
                    mProgressDialog.show();
                    LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "user_friends", "email"));
                }
                break;
            case R.id.tvForgot:
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
                break;
        }
    }

    private void loginFacebook(final AccessToken token) {
        if (token != null) {
            root.authWithOAuthToken("facebook", token.getToken(), new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {
                    String name = authData.getProviderData().get("displayName").toString();
                    String uid = authData.getUid();
                    HashMap<String, String> map = new HashMap<>();
                    map.put("name", name);
                    root.child("users").child(authData.getUid()).setValue(map);
                    finishActivity(name, "", uid);
                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    Toast.makeText(LoginActivity.this, firebaseError.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            root.unauth();
        }
        mProgressDialog.show();
    }

    private void finishActivity(String name, String email, String uid) {
        Utils.setCurrentUsername(LoginActivity.this, name, email, uid);
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        this.finish();
        startActivity(i);
    }

    private void checkisValidAccount(final String email, String pass) {
        if (!Tools.isOnline(LoginActivity.this)) {
            Snackbar.make(findViewById(R.id.root), "Please make sure to have an internet connection.", Snackbar.LENGTH_LONG).show();
            mProgressDialog.show();
            return;
        }
        root.authWithPassword(email, pass,
                new Firebase.AuthResultHandler() {

                    @Override
                    public void onAuthenticated(final AuthData authData) {
                        Log.d("tien", "login thanh cong");
                        Firebase base = new Firebase(Constant.FIREBASE_ROOT + "users/" + authData.getUid() + "/name/");
                        final String uid = authData.getUid();
                        base.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String name = dataSnapshot.getValue().toString();
                                finishActivity(name, email, uid);
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });

                    }

                    @Override
                    public void onAuthenticationError(FirebaseError error) {
                        Toast.makeText(LoginActivity.this, "Email or password is wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
        mProgressDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
