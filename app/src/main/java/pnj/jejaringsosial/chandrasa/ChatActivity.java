package pnj.jejaringsosial.chandrasa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pnj.jejaringsosial.chandrasa.adapters.AdapterChat;
import pnj.jejaringsosial.chandrasa.models.ModelChat;
import pnj.jejaringsosial.chandrasa.models.ModelUser;
import pnj.jejaringsosial.chandrasa.notifications.Data;
import pnj.jejaringsosial.chandrasa.notifications.Sender;
import pnj.jejaringsosial.chandrasa.notifications.Token;

public class ChatActivity extends AppCompatActivity {

    //views
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv;
    TextView nameTv, userStatusTv, emailTv;
    EditText messageEt;
    ImageButton sendBtn, attachBtn;

    private ActionBar actionBar;

    SwipeRefreshLayout swiperefreshlayout;

    FirebaseAuth firebaseAuth;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;

    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    List<ModelChat> chatList;
    AdapterChat adapterChat;

    String hisUid;
    String myUid;
    String hisImage;
    String hisEmail;


    //notif
    private RequestQueue requestQueue;

    private boolean notify = false;

    //perms constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    //image pick constant
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    //perms array
    private String[] cameraPermissions;
    private String[] storagePermissions;
    //picked image uri
    private Uri image_rui = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        //init view
        Toolbar toolbar = findViewById(R.id.toolbar) ;
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView = findViewById(R.id.chat_recyclerView) ;
        profileIv = findViewById(R.id.profileIv) ;
        nameTv = findViewById(R.id.nameTv) ;
        emailTv = findViewById(R.id.emailTv) ;
        userStatusTv = findViewById(R.id.userStatusTv) ;
        messageEt = findViewById(R.id.messageEt) ;
        sendBtn = findViewById(R.id.sendBtn) ;
        attachBtn = findViewById(R.id.attachBtn) ;

        //init perms array
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init actionbar
        actionBar = getSupportActionBar();
        //add back btn
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //layout recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        //recyclerview properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");

        //API
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        firebaseAuth = firebaseAuth.getInstance();

        firebaseDatabase = firebaseDatabase.getInstance();
        usersDbRef = firebaseDatabase.getReference("Users");

        //search user get info
        Query userQuery = usersDbRef.orderByChild("uid").equalTo(hisUid);
        //get user pic and name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //check until info received
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    //get data
                    String name =""+ ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();

                    hisImage =""+ ds.child("image").getValue();


                    //get value onlinestatus
                    String onlineStatus = ""+ ds.child("onlineStatus").getValue();
                    if (onlineStatus.equals("online")) {
                        userStatusTv.setText(onlineStatus);
                    }
                    else {
                        //convert timestamp to proper time
                        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                        cal.setTimeInMillis(Long.parseLong(onlineStatus));
                        String dateTime = DateFormat.format("HH:mm, E, MMM d", cal).toString();
                        userStatusTv.setText("Last seen at "+ dateTime);


                    }

                    //set data
                    if (name.equals("")){
                        try {
                            emailTv.setText(email);
                            emailTv.setVisibility(View.VISIBLE);
                            nameTv.setVisibility(View.GONE);
                        }
                        catch (Exception e) {
                        }
                    }
                    else {
                        nameTv.setText(name);
                        nameTv.setVisibility(View.VISIBLE);
                        emailTv.setVisibility(View.GONE);
                    }

                    try {
                        //image received, set to iv toolbar
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_default_img_white).into(profileIv);
                    }
                    catch (Exception e) {
                        //error getting pic, set default pic
                        Picasso.get().load(R.drawable.ic_default_img_white).into(profileIv);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //click button to send msg
        sendBtn.setOnClickListener((v) -> {
                notify = true;
                //get text from et
                String message = messageEt.getText().toString().trim();
                //check text empty or not
                if (TextUtils.isEmpty(message)) {
                    //text empty
                    Toast.makeText(ChatActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                }
                else {
                    //text not empty
                    sendMessage(message);
                }
                //reset et after send msg
                messageEt.setText("");

        });

        //click btn to import img
        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show image pick dialog
                showImagePickDialog();
            }
        });

        readMessages();

        seenMessage();
    }

    private void showImagePickDialog() {
        //options(camera, gallery) in dialog
        String[] options = {"Camera", "Gallery"};

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this) ;
        builder.setTitle("Choose Image From");
        //set options dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //item click handle
                if (which==0) {
                    //camera click
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }
                if (which==1) {
                    //gallery click
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }
                }
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void pickFromGallery() {
        //intent pick img from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        //intent pick img from camera
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Desc");
        image_rui = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_rui);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);

    }

    private boolean checkStoragePermission(){
        //check storage permission enabled or not
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        //request runtime permission storage
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        //check camera permission enabled or not
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) ==(PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        //request runtime permission camera
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)) {
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessages() {
        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid) ||
                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)) {
                        chatList.add(chat);
                    }
                    adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage);
                    adapterChat.notifyDataSetChanged();
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterChat);

                    //scroll to latest chat
                    recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String message) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis()) ;

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", false);
        hashMap.put("type", "text");
        databaseReference.child("Chats").push().setValue(hashMap);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                ModelUser user = dataSnapshot.getValue(ModelUser.class);

                if (notify) {
                    senNotification(hisUid, user.getName(), message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

            }
        });

        //create chatlist child in database
        DatabaseReference chatRef1  = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(myUid)
                .child(hisUid);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef1.child("id").setValue(hisUid);
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

            }
        });

        DatabaseReference chatRef2  = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(hisUid)
                .child(myUid);
        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef2.child("id").setValue(myUid);
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

            }
        });

    }

    private void sendImageMessage(Uri image_rui) throws IOException {
        notify = true;

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Sending image...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String timeStamp = ""+System.currentTimeMillis();
        String fileNameAndPath = "ChatImages/"+"post_"+timeStamp;

        //get bitmap from image uri
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_rui);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        byte[] data = baos.toByteArray(); //conv image to bytes
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()) {
                            //add img uri info to db
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                            //setup data
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", myUid);
                            hashMap.put("receiver", hisUid);
                            hashMap.put("message", downloadUri);
                            hashMap.put("timestamp", timeStamp);
                            hashMap.put("type", "image");
                            hashMap.put("isSeen", false);

                            //put
                            databaseReference.child("Chats").push().setValue(hashMap);

                            //send notif
                            DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
                            database.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                                    ModelUser user = dataSnapshot.getValue(ModelUser.class);

                                    if (notify) {
                                        senNotification(hisUid, user.getName(), "sent you a photo");
                                    }
                                    notify = false;
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

                                }
                            });

                            DatabaseReference chatRef2  = FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(hisUid)
                                    .child(myUid);
                            chatRef2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()){
                                        chatRef2.child("id").setValue(myUid);
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        progressDialog.dismiss();
                    }
                });
    }

    private void senNotification(final String hisUid, String name, String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(myUid, name + ": " + message, "New Message", hisUid, R.mipmap.ic_launcher_foreground);

                    Sender sender = new Sender(data, token.getToken());

                    //fcm jsom object request
                    try {
                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        //response of request
                                        Log.d("JSON_RESPONSE", "onResponse: "+response.toString());
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON_RESPONSE", "onResponse: "+error.toString());

                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                //put params
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAAECobNuE:APA91bF9X2yQTyrME0doharJ9tFxS4vDFp_BO7O-Qr_BLU7VT0TBSqAMcZ83AqyR2LKycHgqZ5w2SeSKAoi6kWQ7gRhornTCmB-GO_-SipMTSvEUCQSQmahopLVV1HwYY2IR-bCdp5eM");

                                return headers;
                            }
                        };

                        requestQueue.add(jsonObjectRequest);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //signed user stay here
            myUid = user.getUid(); //current user id
        }
        else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus (String status) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid) ;
        HashMap<String, Object> hashMap = new HashMap<>() ;
        hashMap.put("onlineStatus", status);
        //update value onlineStatus current user
        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        //set online
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //get timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());

        //set offline with last seen timestamp
        checkOnlineStatus(timestamp);
        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        //set online
        checkOnlineStatus("online");
        super.onResume();
    }


    //handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //this method called when allow or deny permission, CASE

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length>0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        //both permission granted
                        pickFromCamera();
                    }
                    else {
                        //permission granted
                        Toast.makeText(this, "Camera & Storage permissions are required...", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length>0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        //storage permission granted
                        pickFromGallery();
                    }
                    else {
                        //permission granted
                        Toast.makeText(this, "Storage permissions required...", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        //this method called after pick img gallery or camera
        if (resultCode == RESULT_OK) {

            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //image picked from gallery, get uri
                image_rui = data.getData();

                //set to iV
                try {
                    sendImageMessage(image_rui);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //image picked from camera, get uri
                try {
                    sendImageMessage(image_rui);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //hide searchview, add post
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_add_participant_group).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_video).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}