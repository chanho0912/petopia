package com.example.petopiaapp.ui.qna;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petopiaapp.Activities.HomeActivity;
import com.example.petopiaapp.Activities.PostActivity;
import com.example.petopiaapp.Adapter.PostAdapter_temp;
import com.example.petopiaapp.Adapter.QnaPostAdapter;
import com.example.petopiaapp.R;
import com.example.petopiaapp.models.Post_temp;
import com.example.petopiaapp.models.QnaPost;
import com.example.petopiaapp.models.User;
import com.example.petopiaapp.ui.home.SliderAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class qnaFragment extends Fragment implements View.OnClickListener {

    private qnaViewModel qnaViewModel;

    private Animation fab_open, fab_close;
    private FloatingActionButton fab, fab_camera, fab_album;
    private Boolean isFabOpen = false;

    private Activity frag_qna_activity = null;

    Dialog  qna_post;
    ImageView popupUserImage, popupPostButton;
    TextView popupTitle, popupDescription;
    ProgressBar popupClickProgress;

    RecyclerView recyclerView ;
    QnaPostAdapter qnaPostAdapter ;
    List<QnaPost> postLists ;

    FirebaseUser firebaseUser;
    StorageReference storageRef;

    FirebaseDatabase database;
    DatabaseReference reference;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qna, container, false);

        inipopup();

        recyclerView = view.findViewById(R.id.postRV_qna);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //postLists = new ArrayList<>();
        //qnaPostAdapter = new QnaPostAdapter(getContext(), postLists);
        //recyclerView.setAdapter(qnaPostAdapter);

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
                qna_post.show();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.frag_qna_activity = activity;
        qna_post = new Dialog(activity);
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

    private void inipopup() {
        qna_post.setContentView(R.layout.popup_add_post);
        qna_post.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        qna_post.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        qna_post.getWindow().getAttributes().gravity = Gravity.TOP;

        popupUserImage = qna_post.findViewById(R.id.popup_user_image_qna);
        popupPostButton = qna_post.findViewById(R.id.add_post_button_qna);
        popupTitle = qna_post.findViewById(R.id.popup_title_qna);
        popupDescription = qna_post.findViewById(R.id.popup_description_qna);
        popupClickProgress = qna_post.findViewById(R.id.popup_progressBar_qna);

        // here glide user photo in popupUserImage
        final String[] UserImageUrl = new String[1];

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference("uploads");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                UserImageUrl[0] = user.getImageurl();
                Glide.with(getActivity()).load(user.getImageurl()).into(popupUserImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        popupPostButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                popupPostButton.setVisibility(View.INVISIBLE);
                popupClickProgress.setVisibility(View.VISIBLE);

                if(!popupTitle.getText().toString().isEmpty() && !popupDescription.getText().toString().isEmpty()){
                    showMessage("posting...");

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("QnaPosts");

                    storageRef = FirebaseStorage.getInstance().getReference("uploads");
                    DatabaseReference _reference = FirebaseDatabase.getInstance().
                            getReference("Users").child(firebaseUser.getUid());

                    QnaPost qnaPost = new QnaPost( popupDescription.toString(),
                            popupTitle.toString(),
                            firebaseUser.getUid(),
                            UserImageUrl[0]);
                    add_post(qnaPost);
                }
                else{
                    showMessage("please verify all input fields and choose Post Image");
                    popupPostButton.setVisibility(View.VISIBLE);
                    popupClickProgress.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("QnaPosts");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postLists = new ArrayList<>();
                for(DataSnapshot postsnap: snapshot.getChildren()){
                    QnaPost qnaPost = postsnap.getValue(QnaPost.class);
                    postLists.add(qnaPost);
                }
                qnaPostAdapter = new QnaPostAdapter(getActivity(), postLists);
                recyclerView.setAdapter(qnaPostAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void add_post(final QnaPost qnaPost) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("QnaPosts").push();

        String key = myRef.getKey();
        qnaPost.setPostid(key);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("description", popupDescription.getText().toString());
        hashMap.put("postid", key);
        hashMap.put("title", popupTitle.getText().toString());
        hashMap.put("userId", qnaPost.getUserId());
        hashMap.put("userPhoto", qnaPost.getUserPhoto());

        myRef.setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showMessage("upload success!");
                popupClickProgress.setVisibility(View.INVISIBLE);
                popupPostButton.setVisibility(View.VISIBLE);
                qna_post.dismiss();
            }
        }) ;
   }

    private void showMessage(String message) {
        Toast.makeText(frag_qna_activity, message, Toast.LENGTH_SHORT).show();
    }
}