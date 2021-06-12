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
import androidx.recyclerview.widget.RecyclerView;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import pnj.jejaringsosial.chandrasa.adapters.AdapterGroupChat;
import pnj.jejaringsosial.chandrasa.models.ModelGroupChat;
import pnj.jejaringsosial.chandrasa.models.ModelUser;

public class GroupChatActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private String groupId, myGroupRole="";

    private ActionBar actionBar;

    private Toolbar toolbar;
    private ImageView groupIconIv;
    private ImageButton attachBtn, sendBtn;
    private TextView groupTitleTv;
    private EditText messageEt;
    private RecyclerView chatRv;

    private ArrayList<ModelGroupChat> groupChatList;
    private AdapterGroupChat adapterGroupChat;

    //perms constant
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    //img pick constant
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    //request
    private String[] cameraPermission;
    private String[] storagePermission;

    private Uri image_uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);



        //init views
        toolbar = findViewById(R.id.toolbar);
        groupIconIv = findViewById(R.id.groupIconIv);
        groupTitleTv = findViewById(R.id.groupTitleTv);
        attachBtn = findViewById(R.id.attachBtn);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);
        chatRv = findViewById(R.id.chatRv);

        setSupportActionBar(toolbar);

        //init actionbar
        actionBar = getSupportActionBar();
        //add back btn
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init req permission
        cameraPermission = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        storagePermission = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        //get id clicked group
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();
        loadGroupInfo();
        loadGroupMessages();
        loadMyGroupRole();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input data
                String message = messageEt.getText().toString().trim();
                //validate
                if (TextUtils.isEmpty(message)) {
                    //empty, dont send
                    Toast.makeText(GroupChatActivity.this, "Please enter a message...", Toast.LENGTH_SHORT).show();
                }
                else {
                    sendMessage(message);
                }
            }
        });

        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });
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
        cv.put(MediaStore.Images.Media.TITLE,"GroupImageTitle");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"GroupImageDescription");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
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
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
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
                image_uri = data.getData();
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //image picked from camera, get uri
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendImageMessage(Uri image_uri) throws IOException {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Sending image...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String timeStamp = ""+System.currentTimeMillis();
        String fileNameAndPath = "ChatImages/"+""+System.currentTimeMillis();
        StorageReference ref = FirebaseStorage.getInstance().getReference(fileNameAndPath);
        //get bitmap from image uri
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        byte[] data = baos.toByteArray(); //conv image to bytes
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
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");

                            //setup data
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", firebaseAuth.getUid());
                            hashMap.put("message", downloadUri);
                            hashMap.put("timestamp", "" + timeStamp);
                            hashMap.put("type", "" + "image");

                            //put in db
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                            ref.child(groupId).child("Messages").child(timeStamp)
                                    .setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //messaage sent, clear et
                                            messageEt.setText("");
                                            progressDialog.dismiss();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull @NotNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //signed user stay here
            user.getUid(); //current user id
        }
        else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants")
                .orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                            myGroupRole = ""+ds.child("role").getValue();
                            //refresh menu item
                            invalidateOptionsMenu();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadGroupMessages() {
        //init list
        groupChatList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                groupChatList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    ModelGroupChat model = ds.getValue(ModelGroupChat.class);
                    groupChatList.add(model);
                }
                //adapter
                adapterGroupChat = new AdapterGroupChat(GroupChatActivity.this, groupChatList);
                //set to rv
                chatRv.setAdapter(adapterGroupChat);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String message) {
        String timestamp = ""+System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", "" + firebaseAuth.getUid());
        hashMap.put("message", "" + message);
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("type", "" + "text");

        //add in db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //message sent
                        messageEt.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            String groupTitle = ""+ds.child("groupTitle").getValue();
                            String groupDescription = ""+ds.child("groupDescription").getValue();
                            String groupIcon = ""+ds.child("groupIcon").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String createdBy = ""+ds.child("createdBy").getValue();

                            groupTitleTv.setText(groupTitle);
                            try {
                                Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_white).into(groupIconIv);
                            }
                            catch (Exception e) {
                                groupIconIv.setImageResource(R.drawable.ic_group_white);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_add_video).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(true);

        if (myGroupRole.equals("Creator") || myGroupRole.equals("Admin")){
            //im admin/creator, show add person option
            menu.findItem(R.id.action_add_participant_group).setVisible(true);
        }
        else {
            menu.findItem(R.id.action_add_participant_group).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_participant_group) {
            Intent intent = new Intent(this, GroupAddParticipantActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        }

        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }

        if (id == R.id.action_groupinfo) {
            Intent intent = new Intent(this, GroupInfoActivity.class) ;
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}