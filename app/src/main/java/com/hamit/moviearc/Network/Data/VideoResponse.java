package com.hamit.moviearc.Network.Data;


import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class VideoResponse {

    @SerializedName("id")
    private int id;

    @SerializedName("results")
    private List<Video> results;

    public int getId() {
        return id;
    }

    public List<Video> getResults() {
        return results;
    }

    // ✅ Helper to return only trailers
    public List<Video> getTrailers() {
        List<Video> trailers = new ArrayList<>();
        if (results != null) {
            for (Video v : results) {
                if ("Trailer".equalsIgnoreCase(v.getType())) {
                    trailers.add(v);
                }
            }
        }
        return trailers;
    }

    // ✅ Or just return the *first* trailer if that’s all you need
    public Video getFirstTrailer() {
        if (results != null) {
            for (Video v : results) {
                if ("Trailer".equalsIgnoreCase(v.getType())) {
                    return v;
                }
            }
        }
        return null;
    }
}

