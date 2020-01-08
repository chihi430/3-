package com.study.android.pchsns.models;



import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

// [START post_class]
@IgnoreExtraProperties
    public class Post {

        public String uid;
        public String author;
        public String title;
        public String body;
        public String userphoto;
        public String postday;
        public String postphoto;
        public int starCount = 0;
        public List<String> stars = new ArrayList<String>();

        public Post() {
            // Default constructor required for calls to DataSnapshot.getValue(Post.class)
        }

    public String getPostphoto() {
        return postphoto;
    }

    public void setPostphoto(String postphoto) {
        this.postphoto = postphoto;
    }

    //안드로이드에서 사용하는 값
    public Post(String uid, String author, String title, String body, String userphoto, String postday,String postphoto) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.body = body;
        this.userphoto = userphoto;
        this.postday = postday;
        this.postphoto = postphoto;
    }

    // [START post_to_map] 클라우드에 들어가는 값
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("title", title);
        result.put("body", body);
        result.put("userphoto", userphoto);
        result.put("starCount", starCount);
        result.put("postday", postday);
        result.put("postphoto", postphoto);
        //result.put("stars", stars);

        return result;
    }
    // [END post_to_map]

}
// [END post_class]
