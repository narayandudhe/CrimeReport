package com.example.crime.missingcrime;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.crime.missingcrime.Model.UserModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.RECEIVE_SMS;
import static android.Manifest.permission.SEND_SMS;

public class Admin extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    public UserModel userModel;
    Button send;
    TextView textView;
    public FirebaseDatabase database;
    CardView crimeGo,missingGo,logoutGo;
    private FusedLocationProviderClient client;
    private LocationManager locationManager;
    private com.google.android.gms.location.LocationListener listener;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(checkAndRequestPermissions()) {
            // carry on the normal flow, as the case of  permissions  granted.
        }
        textView = findViewById(R.id.location);
        database = FirebaseDatabase.getInstance();
        client = LocationServices.getFusedLocationProviderClient(this);
        cardView();


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (user != null) {
            // User is signed in
            userModel = new UserModel();
            DatabaseReference userDB = database.getReference().child("users").child(user.getUid());
            userModel.setId(user.getUid());
            userDB.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        dataSnapshot.getValue();
                        userModel = dataSnapshot.getValue(UserModel.class);
                        textView.setText("Hello "+userModel.getName());
                        Toast.makeText(Admin.this, userModel.getName()+user.getUid(), Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });
        } else {
            Intent intentLogin= new Intent(this, SignInActivity.class );
            startActivity(intentLogin);
        }
    }

    public boolean isServicesOK(){
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(Admin.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Toast.makeText(this, "isServicesOK: Google Play Services is working", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Toast.makeText(this, "isServicesOK: an error occured but we can fix it", Toast.LENGTH_SHORT).show();
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(Admin.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void cardView()
    {
        crimeGo=(CardView)findViewById(R.id.crimeGo);
        missingGo=(CardView)findViewById(R.id.personGo);
        logoutGo=(CardView)findViewById(R.id.logoutGo);
        ;
//        on click
        crimeGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                crimeIntent();
            }
        });
        missingGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                missingIntent();
            }
        });
        logoutGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

    }

    void signOut()
    {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Admin.this, SignInActivity.class);
        startActivity(intent);
        finish();
        return;
    }
    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED)
        {

// Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS))
            {

                Toast.makeText(getBaseContext(),
                        "App requires permission to send SMS",
                        Toast.LENGTH_SHORT).show();

            }

            else
            {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        1);

            }
        }    }
    private  boolean checkAndRequestPermissions() {
        int permissionSendMessage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS);
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.SEND_SMS);
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }
    public void addReport()
    {
        Intent intentAddCrime = new Intent(this, AddReportActivity.class);
        startActivity(intentAddCrime);
    }
    public void reportList()
    {
        Intent intentReportList = new Intent(this, ReportsActivity.class);
        startActivity(intentReportList);
    }
    public void myReport()
    {
        Intent myReports = new Intent(this, MyReportActivity.class);
        startActivity(myReports);
    }
    public void emergencyContact()
    {
        Intent intentContacts = new Intent(this, ContactsActvity.class);
        startActivity(intentContacts);
    }
    public void crimeIntent()
    {
        Intent crimeIntent = new Intent(this, AdminHome.class);
        startActivity(crimeIntent);
    }
    public void missingIntent()
    {
        Intent missingIntent = new Intent(this, ReportMenuActivity.class);
        startActivity(missingIntent);
    }

}
