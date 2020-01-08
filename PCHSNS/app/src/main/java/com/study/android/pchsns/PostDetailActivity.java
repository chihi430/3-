package com.study.android.pchsns;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ViewTarget;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.study.android.pchsns.models.Comment;
import com.study.android.pchsns.models.Post;
import com.study.android.pchsns.models.UserModel;
import com.study.android.pchsns.viewholder.PostViewHolder;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "PostDetailActivity";

    public static final String EXTRA_POST_KEY = "post_key";

    private DocumentReference mPostReference;
    private String mPostKey;
    private CommentAdapter mAdapter;

    private TextView mAuthorView;
    private TextView mTitleView;
    private TextView mBodyView;
    private EditText mCommentField;
    private Button mCommentButton;
    private TextView mPostday;
    private RecyclerView mCommentsRecycler;
    private ImageView mUserPhoto;
    private ImageView mPostPhoto;

    long now = System.currentTimeMillis();
    private Date data = new Date(now);
    SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd (a hh:mm)");
    String getTime = sdf.format(data);
    final private RequestOptions requestOptions = new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(90));



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);


        // Get post key from intent
        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }

        // Initialize Database
        mPostReference = FirebaseFirestore.getInstance().collection("posts").document(mPostKey);

        // Initialize Views
        mAuthorView = findViewById(R.id.post_author);
        mTitleView = findViewById(R.id.post_title);
        mBodyView = findViewById(R.id.post_body);
        mCommentField = findViewById(R.id.field_comment_text);
        mCommentButton = findViewById(R.id.button_post_comment);
        mCommentsRecycler = findViewById(R.id.recycler_comments);
        mPostday = findViewById(R.id.post_date);
        mUserPhoto = findViewById(R.id.post_author_photo);
        mPostPhoto = findViewById(R.id.post_image);


        mCommentButton.setOnClickListener(this);
        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));


    }

    @Override
    public void onStart() {
        super.onStart();
        final StorageReference storageReference;
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageRef;
        // Add value event listener to the post
        // [START post_value_event_listener]
        mPostReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                Post post = snapshot.toObject(Post.class);
                // [START_EXCLUDE]
                mAuthorView.setText(post.author);
                mTitleView.setText(post.title);
                mBodyView.setText(post.body);
                mPostday.setText(post.postday);
                Log.d("tag", post.userphoto);

                Glide.with(PostDetailActivity.this)
                     .load(FirebaseStorage.getInstance().getReference("userPhoto/"+post.userphoto))
                     .error(R.drawable.default_user_image)
                     .apply(requestOptions)
                     .into(mUserPhoto);

                Glide.with(PostDetailActivity.this)
                        .load(FirebaseStorage.getInstance().getReference("postPhoto/"+post.postphoto))
                        .error(R.drawable.border)
                        .apply(requestOptions)
                        .override(500,500)
                        .into(mPostPhoto);


//                requestManager
//                        .load(storageReference.child("userPhoto/"+comment.comment_userphoto))
//                        .error(R.drawable.user)
//                        .apply(requestOptions)
//                        .into(holder.commentImageView);

            }
        });
        mAdapter = new CommentAdapter(mPostReference.collection("post-comments"));
        mCommentsRecycler.setAdapter(mAdapter);
        // Listen for comments

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.cleanupListener();
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_post_comment) {
            postComment();
        }
    }

    private void postComment() {
        final String uid = getUid();
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(uid);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                //User user = documentSnapshot.toObject(User.class);
                UserModel user = documentSnapshot.toObject(UserModel.class);
                if (user == null) {
                    Log.e(TAG, "User " + uid + " is unexpectedly null");
                    Toast.makeText(PostDetailActivity.this,
                            "Error: could not fetch user.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Comment comment = new Comment(uid, user.getUsernm(), mCommentField.getText().toString(), getTime, uid);
                    mPostReference.collection("post-comments").document().set(comment);

                    mCommentField.setText(null);
                }
            }
        });
    }

    private static class CommentViewHolder extends RecyclerView.ViewHolder {

        public TextView authorView;
        public TextView bodyView;
        public TextView lookView;
        public ImageView commentImageView;


        CommentViewHolder(View itemView) {
            super(itemView);

            authorView = itemView.findViewById(R.id.comment_author);
            bodyView = itemView.findViewById(R.id.comment_body);
            lookView = itemView.findViewById(R.id.comment_day);
            commentImageView = itemView.findViewById(R.id.comment_photo);

        }
    }

    //private static class FirestoreAdapter extends RecyclerView.Adapter<CommentViewHolder> {
    private class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {

        final private RequestOptions requestOptions = new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(90));
        public RequestManager requestManager;
        private StorageReference storageReference;
        private FirebaseStorage storage = FirebaseStorage.getInstance();
        private StorageReference storageRef;
        private List<String> mCommentIds = new ArrayList<>();
        private List<Comment> mComments = new ArrayList<>();

        private ListenerRegistration listenerRegistration;




        public CommentAdapter(Query query) {
            // Create child event listener
            // [START child_event_listener_recycler]
            this.requestManager = requestManager;

            EventListener childEventListener = new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshots,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {return;}
                            String commentKey;
                            int commentIndex;
                            Comment comment;

                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                switch (dc.getType()) {
                                    case ADDED:
                                        // A new comment has been added, add it to the displayed list
                                        comment = dc.getDocument().toObject(Comment.class);
                                        // [START_EXCLUDE]
                                        // Update RecyclerView
                                        mCommentIds.add(dc.getDocument().getId());
                                        mComments.add(comment);
                                        notifyItemInserted(mComments.size() - 1);
                                        break;
                                    case MODIFIED:
                                        // A comment has changed, use the key to determine if we are displaying this
                                        // comment and if so displayed the changed comment.
                                        comment = dc.getDocument().toObject(Comment.class);
                                        commentKey = dc.getDocument().getId();
                                        // [START_EXCLUDE]
                                        commentIndex = mCommentIds.indexOf(commentKey);
                                        if (commentIndex > -1) {
                                            // Replace with the new data
                                            mComments.set(commentIndex, comment);

                                            // Update the RecyclerView
                                            notifyItemChanged(commentIndex);
                                        } else {
                                            Log.w(TAG, "onChildChanged:unknown_child:" + commentKey);
                                        }
                                        // [END_EXCLUDE]
                                        break;
                                    case REMOVED:
                                        // A comment has changed, use the key to determine if we are displaying this
                                        // comment and if so remove it.
                                        commentKey = dc.getDocument().getId();
                                        // [START_EXCLUDE]
                                        commentIndex = mCommentIds.indexOf(commentKey);
                                        if (commentIndex > -1) {
                                            // Remove data from the list
                                            mCommentIds.remove(commentIndex);
                                            mComments.remove(commentIndex);

                                            // Update the RecyclerView
                                            notifyItemRemoved(commentIndex);
                                        } else {
                                            Log.w(TAG, "onChildRemoved:unknown_child:" + commentKey);
                                        }
                                        // [END_EXCLUDE]
                                        break;
                                }
                            }

                        }
            };
            // [END child_event_listener_recycler]
            listenerRegistration = query.addSnapshotListener(childEventListener);
        }

        @Override
        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_comment, parent, false);


            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CommentViewHolder holder, int position) {

            Comment comment = mComments.get(position);


            holder.authorView.setText(comment.author);
            holder.bodyView.setText(comment.text);
            holder.lookView.setText(comment.comment_day);

            //추가한부분
            if (comment.comment_userphoto ==null)
            {

                Glide.with(PostDetailActivity.this)
                        .load(R.drawable.ic_action_account_circle_40)
                        .apply(requestOptions)
                        .into(holder.commentImageView);
            }
            else
            {
                Glide.with(PostDetailActivity.this)
                        .load(FirebaseStorage.getInstance().getReference("userPhoto/"+comment.comment_userphoto))
                        .error(R.drawable.user)
                        .apply(requestOptions)
                        .into(holder.commentImageView);
            }


        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }

        public void cleanupListener() {
            listenerRegistration.remove();
        }
    }
}
