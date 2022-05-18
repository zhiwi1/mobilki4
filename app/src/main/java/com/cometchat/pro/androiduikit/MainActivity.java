package com.cometchat.pro.androiduikit;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.User;
import com.cometchat.pro.androiduikit.constants.AppConfig;
import com.cometchat.pro.uikit.ui_components.cometchat_ui.CometChatUI;
/*import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;*/
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {



    private DatePickerDialog datePickerDialog;

    private GifImageView loadGif;

    private Timer timer;
    private final Handler handler = new Handler();
    private GoogleSignInClient mGoogleSignInClient;
    private static int RESULT_LOAD_IMAGE = 1;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadGif = findViewById(R.id.gif_load);
        ((GifDrawable)loadGif.getDrawable()).setLoopCount(1);
        timer = new Timer();


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        if (GoogleSignIn.getLastSignedInAccount(this) != null){
            mGoogleSignInClient.signOut();
        }


        timer.schedule(new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                handler.post(() -> checkAnimationEnding());
            }
        }, 0, 10);

        //LoadLogInFragm();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkAnimationEnding(){
        if ( ((GifDrawable)loadGif.getDrawable()).isAnimationCompleted() ){
            if (timer != null) timer.cancel(); else{
                loadGif.setVisibility(View.INVISIBLE);
            }
            timer = null;

            if (CometChat.getLoggedInUser()!=null)
            {
                startActivity(new Intent(MainActivity.this,CometChatUI.class));
                overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
            }else {

                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                if (account != null) {

                    if (CometChatUI.LOGGED_OUT_INDIAN_CHAT){

                        CometChatUI.LOGGED_OUT_INDIAN_CHAT = false;
                        loadGif.setVisibility(View.INVISIBLE);
                        mGoogleSignInClient.signOut();
                        LoadLogInFragm();
                    } else {

                        login(getRidOfSpecialCharacters(account.getEmail()), "");
                    }
                } else {

                    loadGif.setVisibility(View.INVISIBLE);
                    LoadLogInFragm();
                }
            }
        }
    }

    private void logUser(){
        TextInputEditText email=findViewById(R.id.etEmail);
        TextInputEditText pass=findViewById(R.id.etPass);
        if (email.getText().toString().isEmpty())
            email.setError(getResources().getString(R.string.fill_this_field));
        else if (pass.getText().toString().isEmpty())
            pass.setError(getResources().getString(R.string.fill_this_field));
        else {
            login(getRidOfSpecialCharacters(email.getText().toString()),pass.getText().toString());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void LoadLogInFragm(){
        View logInFr=findViewById(R.id.log_in_fragm);
        logInFr.setVisibility(View.VISIBLE);
        findViewById(R.id.log_in_btn).setOnClickListener((e)->{
            logUser();
        });
        findViewById(R.id.reg_link).setOnClickListener((e)->{
            Log.e("DIE", "sucks");
            logInFr.setVisibility(View.INVISIBLE);
            LoadRegFragm();
        });

    }

    int RC_SIGN_IN = 0,RC_SING_UP=1;
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    private void regIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SING_UP);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }else{
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            login(getRidOfSpecialCharacters(account.getEmail()),"");
        } catch (ApiException e) {
            Toast.makeText(MainActivity.this, "Can't log in", Toast.LENGTH_LONG).show();
        }
    }





    private void RegisteUser(){
        try {
            String email = ((TextInputEditText) findViewById(R.id.etEmail_reg)).getText().toString();
            String name = ((TextInputEditText) findViewById(R.id.etName_reg)).getText().toString();
            String pass = ((TextInputEditText) findViewById(R.id.etPass_reg)).getText().toString();
            String date = ((TextInputEditText) findViewById(R.id.etDate_reg)).getText().toString();

            if (pass.isEmpty())
                ((TextInputEditText) findViewById(R.id.etPass_reg)).setError(getResources().getString(R.string.fill_this_field));
            else if (date.isEmpty())
                ((TextInputEditText) findViewById(R.id.etDate_reg)).setError(getResources().getString(R.string.fill_this_field));
            else if (email.isEmpty())
                ((TextInputEditText) findViewById(R.id.etEmail_reg)).setError(getResources().getString(R.string.fill_this_field));
            else if (name.isEmpty())
                ((TextInputEditText) findViewById(R.id.etName_reg)).setError(getResources().getString(R.string.fill_this_field));
            else {
                register(email,name,pass,date);
            }
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Failed to create user", Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void LoadRegFragm(){
        View regFr=findViewById(R.id.reg_frag);
        regFr.setVisibility(View.VISIBLE);
        initDatePicker();
        findViewById(R.id.etDate_reg).setOnClickListener((e)->datePickerDialog.show());
        findViewById(R.id.reg_btn).setOnClickListener((e)->{
            RegisteUser();
        });
        findViewById(R.id.log_in_link).setOnClickListener((e)->{
            regFr.setVisibility(View.INVISIBLE);
            LoadLogInFragm();
        });
    }

    private String birthday;

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initDatePicker()
    {
        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, day) -> {
            month = month + 1;
            birthday = makeDateString(day, month, year);
            ((TextInputEditText)findViewById(R.id.etDate_reg)).setText(birthday);
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog = new DatePickerDialog(getBaseContext(), style, dateSetListener, year, month, day);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String makeDateString(int day, int month, int year)
    {
        LocalDate date
                = LocalDate.of(year,month,day);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM uuuu").withLocale(Locale.getDefault());
        return date.format(formatter);
    }


    private void login(String uid,String pass) {
        CometChat.login(uid, AppConfig.AppDetails.AUTH_KEY, new CometChat.CallbackListener<User>() {
            @Override
            public void onSuccess(User user) {
                try {
                    String passUsers=user.getMetadata().getString("password");
                    if (!passUsers.equals(pass)){
                        Toast.makeText(MainActivity.this, "Error in password", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startActivity(new Intent(MainActivity.this, CometChatUI.class));
                    overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
                    finish();
                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, "Error in password", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onError(CometChatException e) {
                Toast.makeText(MainActivity.this, "User doesn't exist", Toast.LENGTH_SHORT).show();
                loadGif.setVisibility(View.INVISIBLE);
                LoadLogInFragm();
            }
        });
    }

    private String getRidOfSpecialCharacters(String email){
        return email.replaceFirst("@", "").replaceFirst("\\.", "");
    }

    private void register(String email,String name,String pass,String date) throws JSONException {
        User user = new User();
        String uidLikeEmail = getRidOfSpecialCharacters(email);
        user.setUid(uidLikeEmail);
        user.setName(name);
        user.setMetadata(
                new JSONObject(
                        String.format("{email:\"%s\",password:\"%s\",date:\"%s\"}", email, pass, date)));
        CometChat.createUser(user, AppConfig.AppDetails.AUTH_KEY, new CometChat.CallbackListener<User>() {
            @Override
            public void onSuccess(User user) {
                login(user.getUid(),pass);
            }

            @Override
            public void onError(CometChatException e) {
                Toast.makeText(MainActivity.this, "Failed to create user", Toast.LENGTH_LONG).show();
            }
        });
    }


 /*   @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {

        }
    }*/
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
