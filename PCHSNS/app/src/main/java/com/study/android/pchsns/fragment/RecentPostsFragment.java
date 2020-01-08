package com.study.android.pchsns.fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class  RecentPostsFragment extends PostListFragment {

    public RecentPostsFragment() {}

    @Override
    public Query getQuery(FirebaseFirestore databaseReference) {
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys

        return databaseReference.collection("posts").orderBy("postday",Query.Direction.DESCENDING);
    }
}
