package com.example.petopiaapp.Fragment;
import com.bumptech.glide.Glide;
import com.example.petopiaapp.R;
import com.example.petopiaapp.models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.StorageTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {
    ImageView close, image_profile;
    TextView save, tv_change;
    MaterialEditText username;

    FirebaseUser firebaseUser;

    private Uri mImageUri;
    private StorageTask uploadTask;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        close = findViewById(R.id.close);
        image_profile = findViewById(R.id.image_profile);
        save = findViewById(R.id.save);
        tv_change = findViewById(R.id.tv_change);
        username = findViewById(R.id.username);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference("uploads");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                username.setText(user.getUsername());
                Glide.with(getApplicationContext()).load(user.getImageurl()).into(image_profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tv_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .setCropShape(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? CropImageView.CropShape.RECTANGLE :CropImageView.CropShape.OVAL)
                        .start(EditProfileActivity.this);
            }
        });

        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .setCropShape(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? CropImageView.CropShape.RECTANGLE :CropImageView.CropShape.OVAL)
                        .start(EditProfileActivity.this);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile(username.getText().toString());
                finish();
            }

        });
    }

    private void updateProfile(String username){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("username",username);

        reference.updateChildren(hashMap);
    }

    private  String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return  mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Updating");
        pd.show();

        if(mImageUri !=null){
            final StorageReference filereference = storageRef.child(System.currentTimeMillis()
                    +"."+ getFileExtension(mImageUri));

            uploadTask = filereference.putFile(mImageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return filereference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String myUrl = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("imageurl", ""+ myUrl);

                        reference.updateChildren(hashMap);
                        pd.dismiss();
                    }
                    else{
                        Toast.makeText(EditProfileActivity.this,"Failed",Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener(){
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfileActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            Toast.makeText(EditProfileActivity.this,"No Image Selected",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode ==RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();

            uploadImage();
        }
        else{
            Toast.makeText(EditProfileActivity.this,"Something gone wrong",Toast.LENGTH_SHORT).show();

        }
    }
}