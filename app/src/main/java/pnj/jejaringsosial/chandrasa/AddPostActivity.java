package pnj.jejaringsosial.chandrasa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;

    ActionBar actionBar;

    //permissions constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    //image pick constant
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    //permissions array
    String[] cameraPermissions;
    String[] storagePermissions;

    //views
    EditText titleEt;
    ImageView imageIv;
    FloatingActionButton uploadBtn;

    //user info
    String name, email, uid, dp;

    //info post edited
    String editTitle, editImage;

    //image picked same uri
    Uri image_rui = null;

    //progress bar
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Photo");

        //enable back button
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init permission array
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        pd = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        //init views
        titleEt = findViewById(R.id.pTitleEt) ;
        imageIv = findViewById(R.id.pImageIv) ;
        uploadBtn = findViewById(R.id.pUploadBtn) ;

        //get data through intent from previous activity adapter
        Intent intent = getIntent();
        String isUpdateKey = ""+intent.getStringExtra("key");
        String editPostId = ""+intent.getStringExtra("editPostId");
        //validate update post
        if (isUpdateKey.equals("editPost")) {
            //update
            actionBar.setTitle("Update Post");
            loadPostData(editPostId);
        }
        else {
            //add

            actionBar.setTitle("Add New Photo");
        }



        //get image from cam/gallery on click
        imageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show image pick dialog
                showImagePickDialog();
            }
        });

        //upload btn click listener
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get title, desc from et
                String title = titleEt.getText().toString().trim();
                if (TextUtils.isEmpty(title)){
                    Toast.makeText(AddPostActivity.this, "Please enter a title...", Toast.LENGTH_SHORT).show();
                    return;
                }



                if (isUpdateKey.equals("editPost")) {
                    actionBar.setTitle("Update Post");
                    beginUpdate(title, editPostId);

                }
                else {
                    actionBar.setTitle("Add New Photo");

                    if (image_rui==null){
                        Toast.makeText(AddPostActivity.this, "Pick a photo first...", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else {
                        uploadData(title);
                    }
                }
            }
        });

        actionBar.setSubtitle(email);

        //get info current user to include in post
        userDbRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    name = ""+ ds.child("name").getValue();
                    email = ""+ ds.child("email").getValue();
                    dp = ""+ ds.child("image").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

            }
        });

    }

    private void beginUpdate(String title, String editPostId) {
        pd.setMessage("Updating Post...");
        pd.show();
        updateWithCurrentImage(title, editPostId);
    }

    private void updateWithCurrentImage(final String title, String editPostId) {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/"+ "post_"+timeStamp;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] data = baos.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            //put post info
                            hashMap.put("uid", uid);
                            hashMap.put("uName", name);
                            hashMap.put("uEmail", email);
                            hashMap.put("uDp", dp);
                            hashMap.put("pTitle", title);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                            ref.child(editPostId)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                                            finish();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull @NotNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPostData(String editPostId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        //get detail post using id post
        Query fquery = reference.orderByChild("pId").equalTo(editPostId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    //get data
                    editTitle = ""+ds.child("pTitle").getValue();
                    editImage = ""+ds.child("pImage").getValue();

                    //set data to views
                    titleEt.setText(editTitle);

                    //set image
                    try {
                        Picasso.get().load(editImage).into(imageIv);
                        imageIv.setClickable(false);
                    }
                    catch (Exception e) {

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

            }
        });
    }

    private void uploadData(String title) {
        pd.setMessage("Publishing post...");
        pd.show();

        //put post img name, post id, post time
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_" + timeStamp;

        //get img from imgview
        Bitmap bitmap = ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //image compress
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        //post with img
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded, get url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()){
                            //url received, upload post to firebase

                            HashMap<Object, String> hashMap = new HashMap<>();
                            //put post info
                            hashMap.put("uid", uid);
                            hashMap.put("uName", name);
                            hashMap.put("uEmail", email);
                            hashMap.put("uDp", dp);
                            hashMap.put("pId", timeStamp);
                            hashMap.put("pTitle", title);
                            hashMap.put("pImage", downloadUri);
                            hashMap.put("pTime", timeStamp);
                            hashMap.put("pLikes", "0");
                            hashMap.put("pComments", "0");
                            hashMap.put("type", "photo");

                            //path to store post data
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                            //put data in this ref
                            ref.child(timeStamp).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            //database added
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(AddPostActivity.this, DashboardActivity.class));
                                            finish();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull @NotNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        //failed upload
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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


    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //signed user stay here
            email = user.getEmail();
            uid = user.getUid();
        }
        else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //goto previous activity
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant_group).setVisible(false);
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

        if (id == R.id.action_add_video) {
            startActivity(new Intent(AddPostActivity.this, AddVideoActivity.class));
        }
        return super.onOptionsItemSelected(item);
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
                imageIv.setImageURI(image_rui);

            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //image picked from camera, get uri
                imageIv.setImageURI(image_rui);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}


