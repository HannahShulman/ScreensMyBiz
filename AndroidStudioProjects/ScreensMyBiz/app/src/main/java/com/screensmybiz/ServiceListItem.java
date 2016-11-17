package com.screensmybiz;

/**
 * Created by hannashulmah on 10/11/2016.
 */
public class ServiceListItem extends Service {

    String address;
    String description;
    int vote;
    int feedback;

    public ServiceListItem(int image, String title, String address, String description, int feedback, int vote) {
        super(image, title);
        this.address = address;
        this.description = description;
        this.feedback = feedback;
        this.vote = vote;
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

    public int getVote() {
        return vote;
    }

    public void setVote(int vote) {
        this.vote = vote;
    }

    public int getFeedback() {
        return feedback;
    }

    public void setFeedback(int feedback) {
        this.feedback = feedback;
    }
}
