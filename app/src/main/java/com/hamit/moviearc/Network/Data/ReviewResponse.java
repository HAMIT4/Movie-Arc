package com.hamit.moviearc.Network.Data;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ReviewResponse {

    @SerializedName("id")
    private int id;

    @SerializedName("page")
    private int page;

    @SerializedName("results")
    private List<Review> results;

    @SerializedName("total_pages")
    private int totalPages;

    @SerializedName("total_results")
    private int totalResults;

    // Getters
    public int getId() {
        return id;
    }

    public int getPage() {
        return page;
    }

    public List<Review> getResults() {
        return results;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getTotalResults() {
        return totalResults;
    }

    // Inner class for Review
    public static class Review {
        @SerializedName("author")
        private String author;

        @SerializedName("author_details")
        private AuthorDetails authorDetails;

        @SerializedName("content")
        private String content;

        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("id")
        private String id;

        @SerializedName("updated_at")
        private String updatedAt;

        @SerializedName("url")
        private String url;

        // Getters
        public String getAuthor() {
            return author;
        }

        public AuthorDetails getAuthorDetails() {
            return authorDetails;
        }

        public String getContent() {
            return content;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getId() {
            return id;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public String getUrl() {
            return url;
        }
    }

    // Inner class for AuthorDetails
    public static class AuthorDetails {
        @SerializedName("name")
        private String name;

        @SerializedName("username")
        private String username;

        @SerializedName("avatar_path")
        private String avatarPath;

        @SerializedName("rating")
        private Integer rating;

        // Getters
        public String getName() {
            return name;
        }

        public String getUsername() {
            return username;
        }

        public String getAvatarPath() {
            return avatarPath;
        }

        public Integer getRating() {
            return rating;
        }
    }
}

