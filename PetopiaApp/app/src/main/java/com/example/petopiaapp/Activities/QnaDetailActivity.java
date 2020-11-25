package com.example.petopiaapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.petopiaapp.Adapter.CommentAdapter;
import com.example.petopiaapp.Adapter.QnaCommentsAdapter;
import com.example.petopiaapp.R;
import com.example.petopiaapp.models.Comment;
import com.example.petopiaapp.models.QnaComments;
import com.example.petopiaapp.models.QnaPost;
import com.example.petopiaapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QnaDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private QnaCommentsAdapter commentAdapter;
    private List<QnaComments> commentList;

    EditText addcomment;
    ImageView image_profile;
    TextView post;
    TextView description;

    String postid;
    String publisherid;

    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qna_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.qna_detail_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        commentList = new ArrayList<>();
        commentAdapter = new QnaCommentsAdapter(this,commentList);
        recyclerView.setAdapter(commentAdapter);

        addcomment = findViewById(R.id.add_comment);
        image_profile = findViewById(R.id.qna_detail_image_profile);
        post = findViewById(R.id.post);
        description = findViewById(R.id.qna_detail_desc);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent= getIntent();
        postid = intent.getStringExtra("qna_post_id");
        publisherid = intent.getStringExtra("publisherid");

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("QnaPosts").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                QnaPost post = snapshot.getValue(QnaPost.class);
                description.setText(post.getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(addcomment.getText().toString().equals("")){
                    Toast.makeText(QnaDetailActivity.this, "It's an empty comment!", Toast.LENGTH_SHORT).show();
                }
                else{
                    addcomment();
                }
            }
        });

        getImage();
        readComments();


    }
    private void addcomment(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("QnaComments").child(postid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment", addcomment.getText().toString());
        hashMap.put("publisher", firebaseUser.getUid());

        reference.push().setValue(hashMap);
        // addNotifications();
        addcomment.setText("");
    }

    private void addNotifications() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("QnaNotifications").child(publisherid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "commented: " +addcomment.getText().toString());
        hashMap.put("postid", postid);
        hashMap.put("ispost", true);

        reference.push().setValue(hashMap);
    }

    private void getImage(){
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Glide.with(getApplicationContext()).load(user.getImageurl()).into(image_profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readComments(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("QnaComments").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    QnaComments comment = snapshot1.getValue(QnaComments.class);
                    commentList.add(comment);
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}