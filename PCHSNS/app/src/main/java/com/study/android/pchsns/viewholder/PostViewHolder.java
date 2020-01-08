package com.study.android.pchsns.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.study.android.pchsns.R;
import com.study.android.pchsns.models.Post;

public class PostViewHolder extends RecyclerView.ViewHolder {

    public TextView titleView;
    public TextView authorView;
    public ImageView starView;
    public ImageView authorimageView;
    public ImageView deleteView;
    public TextView numStarsView;
    public TextView bodyView;
    public TextView postday;
    public ImageView postPhoto;

    public PostViewHolder(View itemView) {
        super(itemView);

        titleView = itemView.findViewById(R.id.post_title);
        authorView = itemView.findViewById(R.id.post_author);
        starView = itemView.findViewById(R.id.star);
        authorimageView = itemView.findViewById(R.id.post_author_photo); // 작성자의 이미지
        numStarsView = itemView.findViewById(R.id.post_num_stars);
        bodyView = itemView.findViewById(R.id.post_body);
        postday = itemView.findViewById(R.id.post_date);
        postPhoto = itemView.findViewById(R.id.post_image);
        deleteView = itemView.findViewById(R.id.deleteimage);
    }

    public void bindToPost(Post post, View.OnClickListener starClickListener) {
        titleView.setText(post.title);
        authorView.setText(post.author);
        //authorimageView = itemView.findViewById(R.id.post_author_photo); // 작성자의 이미지
        numStarsView.setText(String.valueOf(post.starCount));
        bodyView.setText(post.body);
        postday.setText(post.postday);
        starView.setOnClickListener(starClickListener);
    }
    public void deletePost(Post post, View.OnClickListener deleteViewClickListener)
    {
        deleteView.setOnClickListener(deleteViewClickListener);
    }
}
