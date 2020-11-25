package com.example.petopiaapp.ui.home;
import com.example.petopiaapp.Activities.PostActivity;
import com.example.petopiaapp.Adapter.FinalPostAdapter;
import com.example.petopiaapp.Adapter.PostAdapter_temp;
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
import android.util.Log;
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

import com.example.petopiaapp.models.FinalPost;
import com.example.petopiaapp.models.Post_temp;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    RecyclerView recyclerView ;
    RecyclerView.LayoutManager layoutManager;
    FinalPostAdapter postAdapter ;
    List<FinalPost> postLists ;
    private List<String> followingList;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.postRV);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);

        // Log.d("aaaaaaaaaa", "lalalalalala");
        postLists = new ArrayList<>();
        postAdapter = new FinalPostAdapter(getContext(), postLists);
        recyclerView.setAdapter(postAdapter);


        checkFollowing();

        fab_open = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fab_close);

        fab = (FloatingActionButton) view.findViewById(R.id.fab_main);
        fab_camera = (FloatingActionButton) view.findViewById(R.id.fab_camera);
        fab_album = (FloatingActionButton) view.findViewById(R.id.fab_album);

        fab.setOnClickListener(this);
        fab_camera.setOnClickListener(this);
        fab_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), PostActivity.class));
            }
        });


        return view;
    }

    private void checkFollowing(){
        followingList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear();
                for(DataSnapshot snapshot1: snapshot.getChildren()){
                    followingList.add(snapshot1.getKey());
                }

                readPosts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readPosts(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postLists.clear();
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    FinalPost post = snapshot1.getValue(FinalPost.class);
                    postLists.add(post);

                    /*
                    for (String id : followingList){
                        Log.d("aaaaaaaaaa", "lalalalalala" + id);
                        Log.d("aaaaaaaaaa", "publisher" + post.getPublisher());
                        if(post.getPublisher().equals(id)){
                            postLists.add(post);
                        }
                    }
                     */

                }

                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

            // showMessage("list slides update done");

            popup_add_post.setContentView(R.layout.activity_post);
            popup_add_post.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popup_add_post.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
            popup_add_post.getWindow().getAttributes().gravity = Gravity.TOP;


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
            case R.id.fab_camera:
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