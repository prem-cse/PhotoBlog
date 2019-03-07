package com.example.photoblog;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;

public class PostId {

    @Exclude
    public String id;
    public <T extends PostId> T withId(@NonNull final String id){
        this.id = id;
        return (T)this;
    }
}
