package com.example.petopiaapp.ui.home;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.petopiaapp.R;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment implements  View.OnClickListener{

    private HomeViewModel homeViewModel;
    private Animation fab_open, fab_close;
    private FloatingActionButton fab, fab_camera, fab_album;
    private Boolean isFabOpen = false;
    private Uri pickedImgUri;
    private Activity frag_home_activity = null;
    private static final int PReqCode = 2;
    private static final int REQUESCODE = 2;


    Dialog popup_add_post;
    ImageView popupUserImage, popupPostImage, popupPostButton;
    TextView popupTitle, popupDescription;
    ProgressBar popupClickProgress;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // mAuth = FirebaseAuth.getInstance();
        // currentUser = mAuth.getCurrentUser();

        inipopup();
        setupPopupImageClick();

        fab_open = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fab_close);

        fab = (FloatingActionButton) root.findViewById(R.id.fab_main);
        fab_camera = (FloatingActionButton) root.findViewById(R.id.fab_camera);
        fab_album = (FloatingActionButton) root.findViewById(R.id.fab_album);

        fab.setOnClickListener(this);
        fab_camera.setOnClickListener(this);
        fab_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup_add_post.show();
            }
        });
        return root;
    }

    private void setupPopupImageClick() {
        popupPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAndRequestForPermission();
                //showMessage("Image Clicked");
            }
        });
    }

    private void checkAndRequestForPermission(){
        if(ContextCompat.checkSelfPermission(frag_home_activity, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(frag_home_activity, Manifest.permission.READ_EXTERNAL_STORAGE)){
                Toast.makeText(frag_home_activity, "please accept for required permission", Toast.LENGTH_SHORT).show();
            }
            else
            {
                ActivityCompat.requestPermissions(frag_home_activity, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, PReqCode);
            }
        }
        else{
            //showMessage("openGallery");
            openGallery();
        }
    }

    private void openGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUESCODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUESCODE && data != null){
            pickedImgUri = data.getData();
            popupPostImage.setImageURI(pickedImgUri);
        }
    }


    private void inipopup() {
        popup_add_post.setContentView(R.layout.popup_add_post);
        popup_add_post.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup_add_post.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        popup_add_post.getWindow().getAttributes().gravity = Gravity.TOP;

        popupUserImage = popup_add_post.findViewById(R.id.popup_user_image);
        popupPostImage = popup_add_post.findViewById(R.id.popup_post_image);
        popupPostButton = popup_add_post.findViewById(R.id.add_post_button);
        popupTitle = popup_add_post.findViewById(R.id.popup_title);
        popupDescription = popup_add_post.findViewById(R.id.popup_description);
        popupClickProgress = popup_add_post.findViewById(R.id.popup_progressBar);

        // here glide user photo in popupUserImage

        popupPostButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                popupPostButton.setVisibility(View.INVISIBLE);
                popupClickProgress.setVisibility(View.VISIBLE);

                if(!popupTitle.getText().toString().isEmpty() && !popupDescription.getText().toString().isEmpty() && popupPostImage != null){
                    showMessage("posting...");
                }
                else{
                    showMessage("please verify all input fields and choose Post Image");
                    popupPostButton.setVisibility(View.VISIBLE);
                    popupClickProgress.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void showMessage(String message) {
        Toast.makeText(frag_home_activity, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.frag_home_activity = activity;
        popup_add_post = new Dialog(activity);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab_main:
                anim();
                break;
            case R.id.fab_camera:
                anim();
                break;
            case R.id.fab_album:
                anim();
                break;
        }
    }
    public void anim() {

        if (isFabOpen) {
            fab_camera.startAnimation(fab_close);
            fab_album.startAnimation(fab_close);
            fab_camera.setClickable(false);
            fab_album.setClickable(false);
            isFabOpen = false;
        } else {
            fab_camera.startAnimation(fab_open);
            fab_album.startAnimation(fab_open);
            fab_camera.setClickable(true);
            fab_album.setClickable(true);
            isFabOpen = true;
        }
    }
}