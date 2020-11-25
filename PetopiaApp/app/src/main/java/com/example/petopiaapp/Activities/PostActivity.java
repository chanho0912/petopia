package com.example.petopiaapp.Activities;
import com.example.petopiaapp.Activities.HomeActivity;
import com.example.petopiaapp.R;
import com.example.petopiaapp.models.FinalPost;
import com.example.petopiaapp.ui.home.HomeFragment;
import com.example.petopiaapp.ui.home.Slide;
import com.example.petopiaapp.ui.home.SliderAdapter;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostActivity extends FragmentActivity {

    Uri imageUri;
    StorageTask uploadTask;
    StorageReference storageReference;
    FirebaseStorage storage;

    ImageView close, go_to_al;
    TextView post;
    EditText title, description;

    private static final int PReqCode = 2;
    private static final int REQUESCODE = 2;

    private List<Slide> listslides;
    private List<Uri> picked_img_uris;
    private List<String> saved_img_uri;
    private Uri pickedImgUri;
    ViewPager2 image_added;
    private ProgressDialog progressDialog;
    private int upload_count = 0;
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        close = findViewById(R.id.close);
        image_added = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        go_to_al = findViewById(R.id.go_to_album);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Image uploading, please wait...");

        storage = FirebaseStorage.getInstance();
        listslides = new ArrayList<>();
        picked_img_uris = new ArrayList<>();
        saved_img_uri = new ArrayList<>();

        storageReference = FirebaseStorage.getInstance().getReference("post_images");

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PostActivity.this, HomeFragment.class));
                finish();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // uploadImage();
                if(pickedImgUri != null){
                    progressDialog.show();
                    final StorageReference storageReference = storage.getReference();

                    for(upload_count = 0; upload_count < listslides.size(); upload_count++){
                        final Uri _image_uri = picked_img_uris.get(upload_count);
                        final StorageReference filereference = storageReference.child("post_images").child(System.currentTimeMillis() + "." + getFileExtension(_image_uri));

                        uploadTask = filereference.putFile(_image_uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if(task.isSuccessful()){
                                    filereference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if(task.isSuccessful()){
                                                counter++;
                                                Uri downloadUri = task.getResult();
                                                saved_img_uri.add(downloadUri.toString());
                                                Toast.makeText(PostActivity.this, "image uri saved!", Toast.LENGTH_LONG).show();
                                            }
                                            else{
                                                Toast.makeText(PostActivity.this, "fail to image uri saved!", Toast.LENGTH_LONG).show();
                                            }
                                            if (counter == listslides.size()){
                                                saveImageData();
                                                counter = 0;
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }
        });

        go_to_al.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAndRequestForPermission();
            }
        });
    }

    private void saveImageData(){
        Map<String, String> dataMap = new HashMap<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        DatabaseReference reference_image = FirebaseDatabase.getInstance().getReference("Posts_image");

        String postid = reference.push().getKey();
        String postid2 = reference_image.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("postid",postid);
        hashMap.put("title",title.getText().toString());
        hashMap.put("description",description.getText().toString());
        hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());

        for(int i = 0; i < saved_img_uri.size(); i++){
            dataMap.put("image" + i, saved_img_uri.get(i));
        }

        reference.child(postid).setValue(hashMap);
        reference_image.child(postid2).setValue(dataMap);

        progressDialog.dismiss();
        startActivity(new Intent(PostActivity.this, HomeActivity.class));
        finish();
    }


    private String getFileExtension (Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void checkAndRequestForPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                Toast.makeText(this, "please accept for required permission", Toast.LENGTH_SHORT).show();
            }
            else{
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, PReqCode);
            }
        }
        else{
            OpenGallery();
        }
    }
    private void OpenGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "select image"), REQUESCODE);
    }

    private void uploadImage(){
        /*
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting");
        progressDialog.show();

        if(imageUri != null){
            final StorageReference filereference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            uploadTask = filereference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return filereference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

                        String postid = reference.push().getKey();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("postid",postid);
                        hashMap.put("postimage",myUrl);
                        hashMap.put("description",description.getText().toString());
                        hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());

                        reference.child(postid).setValue(hashMap);

                        progressDialog.dismiss();
                        startActivity(new Intent(PostActivity.this, HomeActivity.class));
                        finish();
                    }
                    else{
                        Toast.makeText(PostActivity.this,"Upload Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            Toast.makeText(this, "No Image Selected!", Toast.LENGTH_SHORT).show();
        }

         */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == REQUESCODE){
            if(data.getClipData() != null){
                int count = data.getClipData().getItemCount();
                for(int i = 0; i < count; i++){
                    pickedImgUri = data.getClipData().getItemAt(i).getUri();
                    picked_img_uris.add(pickedImgUri);
                    listslides.add(new Slide(pickedImgUri.toString()));
                }
            }
            else{
                pickedImgUri = data.getData();
                listslides.add(new Slide(pickedImgUri.toString()));
            }

            image_added.setAdapter(new SliderAdapter(listslides, image_added));

            image_added.setClipToPadding(false);
            image_added.setClipChildren(false);
            image_added.setOffscreenPageLimit(3);
            image_added.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

            CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
            compositePageTransformer.addTransformer(new MarginPageTransformer(40));
            compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
                @Override
                public void transformPage(@NonNull View page, float position) {
                    float r = 1 - Math.abs(position);
                    page.setScaleY(0.85f + r * 0.15f);
                }
            });

            image_added.setPageTransformer(compositePageTransformer);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

}