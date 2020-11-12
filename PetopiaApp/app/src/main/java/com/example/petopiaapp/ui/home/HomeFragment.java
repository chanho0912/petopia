package com.example.petopiaapp.ui.home;
import com.example.petopiaapp.R;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.MessagePattern;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

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

    private List<Slide> listslides;

    Dialog  popup_add_post;
    ImageView popupUserImage, popupPostButton, popupAlbumButton;
    ViewPager2 popupPostImage;
    TextView popupTitle, popupDescription;
    ProgressBar popupClickProgress;

    RecyclerView PostRecyclerView ;
    RecyclerView.LayoutManager layoutManager;
    PostAdapter postAdapter ;
    List<Post> PostList ;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        // mAuth = FirebaseAuth.getInstance();
        // currentUser = mAuth.getCurrentUser();

        listslides = new ArrayList<>();


        PostRecyclerView = root.findViewById(R.id.postRV);

        //layoutManager = new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.HORIZONTAL);
        //PostRecyclerView.setLayoutManager(layoutManager);

        PostRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        PostRecyclerView.setHasFixedSize(true);

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

    @Override
    public void onStart() {
        super.onStart();
        // get database reference
        // on data change
        PostList = new ArrayList<>() ;

        //Uri uri_1 = Uri.parse("android.resource://com.worldbright.puppycam.ui.home/drawable/dog1.jpg");

        Uri uri_1 = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getContext().getResources().getResourcePackageName(R.drawable.dog1)
                + '/' + getContext().getResources().getResourceTypeName(R.drawable.dog1)
                + '/' + getContext().getResources().getResourceEntryName(R.drawable.dog1));

        Uri uri_2 = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getContext().getResources().getResourcePackageName(R.drawable.dog2)
                + '/' + getContext().getResources().getResourceTypeName(R.drawable.dog2)
                + '/' + getContext().getResources().getResourceEntryName(R.drawable.dog2));


        Uri uri_3 = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getContext().getResources().getResourcePackageName(R.drawable.dog3)
                + '/' + getContext().getResources().getResourceTypeName(R.drawable.dog3)
                + '/' + getContext().getResources().getResourceEntryName(R.drawable.dog3));

        Uri uri_4 = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getContext().getResources().getResourcePackageName(R.drawable.dog4)
                + '/' + getContext().getResources().getResourceTypeName(R.drawable.dog4)
                + '/' + getContext().getResources().getResourceEntryName(R.drawable.dog4));

        Uri uri_profile = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getContext().getResources().getResourcePackageName(R.drawable.profile)
                + '/' + getContext().getResources().getResourceTypeName(R.drawable.profile)
                + '/' + getContext().getResources().getResourceEntryName(R.drawable.profile));

        Post temp_1 = new Post("Test_1", "test_1", uri_1.toString(), "1", uri_profile.toString(), 1);
        Post temp_2 = new Post("Test_2", "test_2", uri_2.toString(), "1", uri_profile.toString(), 1);
        Post temp_3 = new Post("Test_3", "test_3", uri_4.toString(), "1", uri_profile.toString(), 1);
        Post temp_4 = new Post("Test_4", "test_4", uri_3.toString(), "1", uri_profile.toString(), 1);

        PostList.add(temp_1);
        PostList.add(temp_2);
        PostList.add(temp_3);
        PostList.add(temp_4);

        postAdapter = new PostAdapter(getActivity(), PostList);
        PostRecyclerView.setAdapter(postAdapter);
    }

    private void setupPopupImageClick() {
        popupAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAndRequestForPermission();
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
            openGallery();
        }
    }

    private void openGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "select image"), REQUESCODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == REQUESCODE){
            if(data.getClipData() != null){
                int count = data.getClipData().getItemCount();
                for(int i = 0; i < count; i++) {
                    pickedImgUri = data.getClipData().getItemAt(i).getUri();
                    listslides.add(new Slide(pickedImgUri.toString()));
                }
            }
            else{
                pickedImgUri = data.getData();
                listslides.add(new Slide(pickedImgUri.toString()));
            }

            // showMessage("list slides update done");

            popup_add_post.setContentView(R.layout.popup_add_post);
            popup_add_post.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popup_add_post.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
            popup_add_post.getWindow().getAttributes().gravity = Gravity.TOP;

            popupPostImage = popup_add_post.findViewById(R.id.popup_post_image);
            popupPostImage.setAdapter(new SliderAdapter(listslides, popupPostImage));

            popupPostImage.setClipToPadding(false);
            popupPostImage.setClipChildren(false);
            popupPostImage.setOffscreenPageLimit(3);
            popupPostImage.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

            CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
            compositePageTransformer.addTransformer(new MarginPageTransformer(40));
            compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
                @Override
                public void transformPage(@NonNull View page, float position) {
                    float r = 1 - Math.abs(position);
                    page.setScaleY(0.85f + r * 0.15f);
                }
            });

            popupPostImage.setPageTransformer(compositePageTransformer);
        }
    }


    private void inipopup() {
        popup_add_post.setContentView(R.layout.popup_add_post);
        popup_add_post.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup_add_post.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        popup_add_post.getWindow().getAttributes().gravity = Gravity.TOP;

        popupUserImage = popup_add_post.findViewById(R.id.popup_user_image);

        popupPostImage = popup_add_post.findViewById(R.id.popup_post_image);
        popupPostImage.setAdapter(new SliderAdapter(listslides, popupPostImage));

        popupAlbumButton = popup_add_post.findViewById(R.id.upload_Album_Image);
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
                    //Post post = new Post
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