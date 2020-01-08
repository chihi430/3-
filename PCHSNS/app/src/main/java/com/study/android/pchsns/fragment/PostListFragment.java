package com.study.android.pchsns.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.study.android.pchsns.PostDetailActivity;
import com.study.android.pchsns.R;
import com.study.android.pchsns.adapter.FirestoreAdapter;
import com.study.android.pchsns.models.Post;
import com.study.android.pchsns.models.UserModel;
import com.study.android.pchsns.viewholder.PostViewHolder;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class PostListFragment extends Fragment {

    private static final String TAG = "PostListFragment";

    // [START define_database_reference]
    private FirebaseFirestore db;
    private ImageView mDeleteimage;
    // [END define_database_reference]

    private PostAdapter mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;



    long now = System.currentTimeMillis();
    private Date data = new Date(now);
    SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd (a hh:mm)");
    String getTime = sdf.format(data);



    public PostListFragment() { }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);

        // [START create_database_reference]
        db = FirebaseFirestore.getInstance();
        // [END create_database_reference]




        mRecycler = rootView.findViewById(R.id.messages_list);
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery(db);

        mAdapter = new PostAdapter(postsQuery);
        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecycler.setAdapter(mAdapter);
    }



    class PostAdapter extends FirestoreAdapter<PostViewHolder> {


        //추가함
        final private RequestOptions requestOptions = new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(90));
        private StorageReference storageReference;
        private String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //

        PostAdapter(Query query) {
            super(query);
            //추가한부분
            storageReference  = FirebaseStorage.getInstance().getReference();
        }

        @Override
        public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_post, parent, false);

            return new PostViewHolder(view);
        }



        @Override
        public void onBindViewHolder(PostViewHolder viewHolder, int position) {

            DocumentSnapshot documentSnapshot = getSnapshot(position);
            final UserModel user = documentSnapshot.toObject(UserModel.class);
            Post post = documentSnapshot.toObject(Post.class);


            final String postKey = documentSnapshot.getId();
            Log.d(TAG,postKey);
            Log.d(TAG, "ㅎㅇㅎㅇ"+ user.getUserphoto());

            // 작성자 본인일경우 게시물을 삭제할수 있도록 하는 조건문
            if(!myUid.equals(post.uid)){
                Log.d("tag", "어떤내용이 나올까요"+post.uid);
                Log.d("tag", "어떤내용이 나옴?"+myUid);
                viewHolder.deleteView.setVisibility(View.INVISIBLE);

            }

            //게시물에 이미지 로드
            Glide.with(getActivity())
                    .load(FirebaseStorage.getInstance().getReference("postPhoto/"+post.postphoto))
                    .error(R.drawable.border)
                    .override(1000,1000)
                    .apply(requestOptions)
                    .into(viewHolder.postPhoto);

            //작성자에 대한 프로필  파이어베이스 스토리지 사진 로드
            if (user.getUserphoto() ==null)
            {
                Glide.with(getActivity()).load(R.drawable.ic_action_account_circle_40)
                        .apply(requestOptions)
                        .into(viewHolder.authorimageView);
            }
            else
            {
                Glide.with(getActivity())
                        .load(storageReference.child("userPhoto/"+user.getUserphoto()))
                        .error(R.drawable.user)
                        .apply(requestOptions)
                        .into(viewHolder.authorimageView);
            }


            // Set click listener for the whole post view
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Launch PostDetailActivity
                    Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                    intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
                    startActivity(intent);
                }
            });
            // Determine if the current user has liked this post and set UI accordingly
            if (post.stars.indexOf(getUid())>-1) {
            //if (post.stars.containsKey(getUid())) {
                viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_24);
            } else {
                viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
            }


            // Bind Post to ViewHolder, setting OnClickListener for the star button
            viewHolder.bindToPost(post, new View.OnClickListener() {
                @Override
                public void onClick(View starView) {
                    db.collection("posts").document(postKey).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Post post = documentSnapshot.toObject(Post.class);
                            if (post.stars.indexOf(getUid())==-1) {
                                post.stars.add(getUid());
                            } else {
                                post.stars.remove(getUid());
                            }
                            post.starCount = post.stars.size();
                                    documentSnapshot.getReference().set(post);
                        }
                    });
                }
            });
            //삭제 포스트
            viewHolder.deletePost(post, new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final Dialog deleteDialog = new Dialog(getActivity());
                    deleteDialog.setContentView(R.layout.cutom_dialog);
                    deleteDialog.setTitle("삭제");

                    Button delete = deleteDialog.findViewById(R.id.button3);
                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            db.collection("posts").document(postKey).delete()
                                 .addOnSuccessListener(new OnSuccessListener<Void>() {
                                     @Override
                                     public void onSuccess(Void aVoid) {
                                     Toast.makeText(getActivity(),"해당 게시물이 삭제되었습니다.",Toast.LENGTH_SHORT).show();
                                     deleteDialog.dismiss();
                                    }
                            });
                        }
                    });


                    Button cancel = deleteDialog.findViewById(R.id.cancel);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteDialog.dismiss();
                        }
                    });

                    deleteDialog.show();
                }
            });

        }

    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }


    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(FirebaseFirestore databaseReference);

}
