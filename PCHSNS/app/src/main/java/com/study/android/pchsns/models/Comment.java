package com.study.android.pchsns.models;


import com.google.firebase.firestore.IgnoreExtraProperties;

// [START comment_class]
@IgnoreExtraProperties
public class Comment {

    public String uid;
    public String author;
    public String text;
    public String comment_day;
    public String comment_userphoto;

    public Comment() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    public Comment(String uid, String author, String text, String comment_day,String comment_userphoto) {
        this.uid = uid;
        this.author = author;
        this.text = text;
        this.comment_day = comment_day;
        this.comment_userphoto = comment_userphoto;
    }

}
// [END comment_class]
