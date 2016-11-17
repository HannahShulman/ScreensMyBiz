package com.screensmybiz;

/**
 * Created by hannashulmah on 10/11/2016.
 */
public class Service {
    String title;
    int image;
    int tag;

    public Service(int image, String title) {
        this.image = image;
        this.title = title;
    }



    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }
}
