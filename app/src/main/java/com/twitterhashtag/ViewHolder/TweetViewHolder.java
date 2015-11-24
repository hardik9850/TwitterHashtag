package com.twitterhashtag.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.squareup.picasso.Picasso;
import com.twitterhashtag.R;
import com.twitterhashtag.model.Search;

/**
 * Created by Neel Raj on 23-08-2015.
 */
public class TweetViewHolder extends RecyclerView.ViewHolder {

    private View itemView;

    private TextView username,tweet,name;
    private ImageView userpic;
    private Search reference;


    public TweetViewHolder(View itemView) {
        super(itemView);

        this.itemView=itemView;

        username = (TextView) itemView.findViewById(R.id.userName);
        tweet = (TextView) itemView.findViewById(R.id.tweetStatus);
        userpic = (ImageView) itemView.findViewById(R.id.userimage);
        name = (TextView) itemView.findViewById(R.id.Name);

    }

    public void update (Search search){
        reference = search;
        username.setText("@"+search.getUser().getScreenName());
        tweet.setText(search.getText());
        name.setText(search.getUser().getName());

        Picasso.with(itemView.getContext()).load(search.getUser().getProfileImageUrl())
                .into(userpic);
    }
}
