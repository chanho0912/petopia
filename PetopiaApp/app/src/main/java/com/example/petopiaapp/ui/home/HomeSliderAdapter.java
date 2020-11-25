package com.example.petopiaapp.ui.home;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.petopiaapp.R;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class HomeSliderAdapter extends RecyclerView.Adapter<HomeSliderAdapter.HomeSliderViewHolder>{
    private List<Slide> slideList;
    private ViewPager2 viewPager2;
    private Context mContext;

    public HomeSliderAdapter(Context mcontext, List<Slide> slideList, ViewPager2 viewPager2) {
        this.mContext = mcontext;
        this.slideList = slideList;
        this.viewPager2 = viewPager2;
    }

    @NonNull
    @Override
    public HomeSliderAdapter.HomeSliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeSliderAdapter.HomeSliderViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.slide_item_container,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull HomeSliderAdapter.HomeSliderViewHolder holder, int position) {
        holder.setImage(slideList.get(position));
    }

    @Override
    public int getItemCount() {
        return slideList.size();
    }

    class HomeSliderViewHolder extends RecyclerView.ViewHolder{

        private RoundedImageView imageView;

        HomeSliderViewHolder(@NonNull View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.imageSlide);
        }

        void setImage(Slide slide){
            Glide.with(mContext).load(slide.getImage()).into(imageView);
        }
    }
}
