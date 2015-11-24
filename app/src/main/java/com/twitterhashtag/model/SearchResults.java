package com.twitterhashtag.model;

import com.google.gson.annotations.SerializedName;
import com.twitterhashtag.list.Searches;


/**
 * Created by Hardik9850 on 21-Nov-15.
 */
public class SearchResults {

    @SerializedName("statuses")
    private Searches statuses;

    @SerializedName("search_metadata")
    private SearchMetadata metadata;


    public Searches getStatuses() {
        return statuses;
    }

    public void setStatuses(Searches statuses) {
        this.statuses = statuses;
    }

    public SearchMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(SearchMetadata metadata) {
        this.metadata = metadata;
    }
}
