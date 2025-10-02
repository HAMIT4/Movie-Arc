package com.hamit.moviearc.Network.Data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MultiSearchResponse implements Parcelable {

    @SerializedName("page")
    private int page;

    @SerializedName("results")
    private List<ResultItem> results;

    @SerializedName("total_pages")
    private int totalPages;

    @SerializedName("total_results")
    private int totalResults;

    // Default constructor
    public MultiSearchResponse() {
    }

    // Parcelable constructor
    protected MultiSearchResponse(Parcel in) {
        page = in.readInt();
        results = in.createTypedArrayList(ResultItem.CREATOR);
        totalPages = in.readInt();
        totalResults = in.readInt();
    }

    public static final Creator<MultiSearchResponse> CREATOR = new Creator<MultiSearchResponse>() {
        @Override
        public MultiSearchResponse createFromParcel(Parcel in) {
            return new MultiSearchResponse(in);
        }

        @Override
        public MultiSearchResponse[] newArray(int size) {
            return new MultiSearchResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(page);
        dest.writeTypedList(results);
        dest.writeInt(totalPages);
        dest.writeInt(totalResults);
    }

    // Getters and setters
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public List<ResultItem> getResults() { return results; }
    public void setResults(List<ResultItem> results) { this.results = results; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public int getTotalResults() { return totalResults; }
    public void setTotalResults(int totalResults) { this.totalResults = totalResults; }

    // Inner class for Results - also made Parcelable
    public static class ResultItem implements Parcelable {
        @SerializedName("adult")
        private boolean adult;

        @SerializedName("backdrop_path")
        private String backdropPath;

        @SerializedName("id")
        private int id;

        @SerializedName("title")
        private String title;

        @SerializedName("name")
        private String name;  // For TV shows or people

        @SerializedName("original_title")
        private String originalTitle;

        @SerializedName("original_name")
        private String originalName; // For TV

        @SerializedName("overview")
        private String overview;

        @SerializedName("poster_path")
        private String posterPath;

        @SerializedName("media_type")
        private String mediaType;

        @SerializedName("original_language")
        private String originalLanguage;

        @SerializedName("genre_ids")
        private List<Integer> genreIds;

        @SerializedName("popularity")
        private double popularity;

        @SerializedName("release_date")
        private String releaseDate;

        @SerializedName("first_air_date")
        private String firstAirDate; // For TV shows

        @SerializedName("video")
        private Boolean video;

        @SerializedName("vote_average")
        private double voteAverage;

        @SerializedName("vote_count")
        private int voteCount;

        @SerializedName("origin_country")
        private List<String> originCountry; // For TV

        // Default constructor
        public ResultItem() {
        }

        // Parcelable constructor
        protected ResultItem(Parcel in) {
            adult = in.readByte() != 0;
            backdropPath = in.readString();
            id = in.readInt();
            title = in.readString();
            name = in.readString();
            originalTitle = in.readString();
            originalName = in.readString();
            overview = in.readString();
            posterPath = in.readString();
            mediaType = in.readString();
            originalLanguage = in.readString();
            genreIds = new ArrayList<>();
            in.readList(genreIds, Integer.class.getClassLoader());
            popularity = in.readDouble();
            releaseDate = in.readString();
            firstAirDate = in.readString();
            byte tmpVideo = in.readByte();
            video = tmpVideo == 0 ? null : tmpVideo == 1;
            voteAverage = in.readDouble();
            voteCount = in.readInt();
            originCountry = in.createStringArrayList();
        }

        public static final Creator<ResultItem> CREATOR = new Creator<ResultItem>() {
            @Override
            public ResultItem createFromParcel(Parcel in) {
                return new ResultItem(in);
            }

            @Override
            public ResultItem[] newArray(int size) {
                return new ResultItem[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte((byte) (adult ? 1 : 0));
            dest.writeString(backdropPath);
            dest.writeInt(id);
            dest.writeString(title);
            dest.writeString(name);
            dest.writeString(originalTitle);
            dest.writeString(originalName);
            dest.writeString(overview);
            dest.writeString(posterPath);
            dest.writeString(mediaType);
            dest.writeString(originalLanguage);
            dest.writeList(genreIds);
            dest.writeDouble(popularity);
            dest.writeString(releaseDate);
            dest.writeString(firstAirDate);
            dest.writeByte((byte) (video == null ? 0 : video ? 1 : 2));
            dest.writeDouble(voteAverage);
            dest.writeInt(voteCount);
            dest.writeStringList(originCountry);
        }

        // Getters and setters
        public boolean isAdult() { return adult; }
        public void setAdult(boolean adult) { this.adult = adult; }

        public String getBackdropPath() { return backdropPath; }
        public void setBackdropPath(String backdropPath) { this.backdropPath = backdropPath; }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getOriginalTitle() { return originalTitle; }
        public void setOriginalTitle(String originalTitle) { this.originalTitle = originalTitle; }

        public String getOriginalName() { return originalName; }
        public void setOriginalName(String originalName) { this.originalName = originalName; }

        public String getOverview() { return overview; }
        public void setOverview(String overview) { this.overview = overview; }

        public String getPosterPath() { return posterPath; }
        public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

        public String getMediaType() { return mediaType; }
        public void setMediaType(String mediaType) { this.mediaType = mediaType; }

        public String getOriginalLanguage() { return originalLanguage; }
        public void setOriginalLanguage(String originalLanguage) { this.originalLanguage = originalLanguage; }

        public List<Integer> getGenreIds() { return genreIds; }
        public void setGenreIds(List<Integer> genreIds) { this.genreIds = genreIds; }

        public double getPopularity() { return popularity; }
        public void setPopularity(double popularity) { this.popularity = popularity; }

        public String getReleaseDate() { return releaseDate; }
        public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

        public String getFirstAirDate() { return firstAirDate; }
        public void setFirstAirDate(String firstAirDate) { this.firstAirDate = firstAirDate; }

        public Boolean getVideo() { return video; }
        public void setVideo(Boolean video) { this.video = video; }

        public double getVoteAverage() { return voteAverage; }
        public void setVoteAverage(double voteAverage) { this.voteAverage = voteAverage; }

        public int getVoteCount() { return voteCount; }
        public void setVoteCount(int voteCount) { this.voteCount = voteCount; }

        public List<String> getOriginCountry() { return originCountry; }
        public void setOriginCountry(List<String> originCountry) { this.originCountry = originCountry; }

        // Helper method to get display title (either title or name)
        public String getDisplayTitle() {
            return title != null ? title : name;
        }

        // Helper method to get display original title (either originalTitle or originalName)
        public String getDisplayOriginalTitle() {
            return originalTitle != null ? originalTitle : originalName;
        }

        // Helper method to get display date (either releaseDate or firstAirDate)
        public String getDisplayDate() {
            return releaseDate != null ? releaseDate : firstAirDate;
        }
    }
}