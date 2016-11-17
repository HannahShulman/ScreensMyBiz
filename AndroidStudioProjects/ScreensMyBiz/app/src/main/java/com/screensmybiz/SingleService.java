package com.screensmybiz;

/**
 * Created by hannashulmah on 14/11/2016.
 */
public class SingleService {
    String title;
    String address;
    String town;
    String description;
    String uid;
    int Image;
    int num_reviews;
    int num_stars;
    boolean like;


    public SingleService( String title, String address,String town, String description, int image, boolean like,  int num_reviews, int num_stars) {
        this.address = address;
        this.town = town;
        this.description = description;
        Image = image;
        this.like = like;
        this.num_reviews = num_reviews;
        this.num_stars = num_stars;
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImage() {
        return Image;
    }

    public void setImage(int image) {
        Image = image;
    }

    public boolean isLike() {
        return like;
    }

    public void setLike(boolean like) {
        this.like = like;
    }

    public int getNum_reviews() {
        return num_reviews;
    }

    public void setNum_reviews(int num_reviews) {
        this.num_reviews = num_reviews;
    }

    public int getNum_stars() {
        return num_stars;
    }

    public void setNum_stars(int star_review) {
        this.num_stars = star_review;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }
}
