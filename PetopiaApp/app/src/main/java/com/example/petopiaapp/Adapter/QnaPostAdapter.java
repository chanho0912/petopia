package com.example.petopiaapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petopiaapp.Activities.CommentsActivity;
import com.example.petopiaapp.Activities.QnaDetailActivity;
import com.example.petopiaapp.Fragment.PostDetailFragment;
import com.example.petopiaapp.R;
import com.example.petopiaapp.models.Post_temp;
import com.example.petopiaapp.models.QnaPost;
import com.example.petopiaapp.models.User;
import com.example.petopiaapp.ui.home.Post;
import com.example.petopiaapp.ui.profile.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class QnaPostAdapter extends RecyclerView.Adapter<QnaPostAdapter.MyViewHolder>{
    Context mContext;
    List<QnaPost> mList;

    public QnaPostAdapter(Context mContext, List<QnaPost> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(mContext).inflate(R.layout.qna_item, parent, false);
        return new MyViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final QnaPost post = mList.get(position);
        holder.tvTitle.setText(mList.get(position).getTitle());
        //holder.tvTitle.setText("Hello");
        holder.tvDesc.setText(mList.get(position).getDescription());
        Glide.with(mContext).load(mList.get(position).getUserPhoto()).into(holder.UserImage);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, QnaDetailActivity.class);
                intent.putExtra("qna_post_id", post.getPostid());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView tvTitle;
        TextView tvDesc;
        ImageView UserImage;
        CardView cardView;

        public MyViewHolder(View itemView){
            super(itemView);

            tvTitle = itemView.findViewById(R.id.row_qna_title);
            tvDesc = itemView.findViewById(R.id.row_qna_desc);
            UserImage = itemView.findViewById(R.id.row_qna_profile_img);
            cardView = itemView.findViewById(R.id.QnaCardView);
        }

    }
}
