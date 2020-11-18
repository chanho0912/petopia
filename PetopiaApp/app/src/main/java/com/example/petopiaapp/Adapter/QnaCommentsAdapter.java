package com.example.petopiaapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petopiaapp.Activities.HomeActivity;
import com.example.petopiaapp.R;
import com.example.petopiaapp.models.Comment;
import com.example.petopiaapp.models.QnaComments;
import com.example.petopiaapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class QnaCommentsAdapter extends RecyclerView.Adapter<QnaCommentsAdapter.ViewHolder>{

    private Context mContext;
    private List<QnaComments> mComment;

    private FirebaseUser firebaseUser;

    public QnaCommentsAdapter(Context mContext, List<QnaComments> mComment) {
        this.mContext = mContext;
        this.mComment = mComment;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.qna_comments_item,parent,false);
        return new QnaCommentsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QnaCommentsAdapter.ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final QnaComments comment = mComment.get(position);

        holder.comment.setText(comment.getComment());
        getUserInfo(holder.image_profile, holder.username, comment.getPublisher());

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, HomeActivity.class);
                intent.putExtra("publisherid",comment.getPublisher());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mComment.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView image_profile;
        public TextView username, comment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.qna_comments_image_profile);
            username = itemView.findViewById(R.id.qna_comments_username);
            comment = itemView.findViewById(R.id.qna_comments_comment);
        }
    }

    private void getUserInfo(final ImageView imageView, final TextView username, String puslisherid){
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("Users").child(puslisherid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user =snapshot.getValue(User.class);
                Glide.with(mContext).load(user.getImageurl()).into(imageView);
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
