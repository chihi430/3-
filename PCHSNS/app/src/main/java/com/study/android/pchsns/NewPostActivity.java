package com.study.android.pchsns;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.study.android.pchsns.common.Util9;
import com.study.android.pchsns.models.Post;
import com.study.android.pchsns.models.UserModel;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NewPostActivity extends BaseActivity {
    private static final int PICK_FROM_ALBUM = 1;
    private static final String TAG = "NewPostActivity";
    private static final String REQUIRED = "Required";

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    private FirebaseFirestore db;
    // [END declare_database_ref]
    long now = System.currentTimeMillis();
    private Date data = new Date(now);
    SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd (a hh:mm)");
    String getTime = sdf.format(data);

    private EditText mTitleField;
    private EditText mBodyField;

    private ImageView mUserImage;
    private FloatingActionButton mSubmitButton;
    private FloatingActionButton mImagePostButton;

    //게시하는 사용자 정보
    private TextView mAuthorView;
    private ImageView mpostUserPhoto;
    private TextView mPostday;


    private ImageView mPostImage;
    private Uri mPostImageUri;
    private Post post;

    final private RequestOptions requestOptions = new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(90));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        // [END initialize_database_ref]

        mTitleField = findViewById(R.id.field_title); // 게시물 제목
        mBodyField = findViewById(R.id.field_body); // 게시물 내용
        mSubmitButton = findViewById(R.id.fab_submit_post); // 게시물 게재
        mUserImage = findViewById(R.id.user_photo); // 올리는 사용자 사진
        mPostImage = findViewById(R.id.field_image_post); // 이미지 올리는 경로
        mImagePostButton = findViewById(R.id.fab_gallery); // 갤러리이동 버튼

        //게시자 정보
        mpostUserPhoto = findViewById(R.id.post_author_photo);
        mAuthorView = findViewById(R.id.post_author);
        mPostday = findViewById(R.id.post_date);




        final String userId = getUid();

        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserModel userModel = documentSnapshot.toObject(UserModel.class);
                mAuthorView.setText(userModel.getUsernm());
                mPostday.setText(getTime);

                Glide.with(NewPostActivity.this)
                        .load(FirebaseStorage.getInstance().getReference("userPhoto/"+getUid()))
                        .apply(requestOptions)
                        .error(R.drawable.user)
                        .into(mpostUserPhoto);

            }
        });



        // final String timesta = dateFormatHour.format(date);
        // Log.d("tag" , "시간나옴? "+timesta);
        // mPostView.setText(time);
        // Intent intent = getIntent();
        // String username = intent.getExtras().getString("username");

        Toast.makeText(this, "뭐가나옴 : "+getUid(), Toast.LENGTH_SHORT).show();



        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });

        mImagePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);
            }
        });

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==PICK_FROM_ALBUM && resultCode== NewPostActivity.RESULT_OK) {
            mPostImage.setImageURI(data.getData());
            mPostImageUri = data.getData();
        }
    }

    private void submitPost() {
        final String title = mTitleField.getText().toString();
        final String body = mBodyField.getText().toString();

        // Title is required
        if (TextUtils.isEmpty(title)) {
            mTitleField.setError(REQUIRED);
            return;
        }

        // Body is required
        if (TextUtils.isEmpty(body)) {
            mBodyField.setError(REQUIRED);
            return;
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        // [START single_value_read]
        final String userId = getUid();


        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserModel userModel = documentSnapshot.toObject(UserModel.class);
                post = documentSnapshot.toObject(Post.class);

                if (mPostImageUri != null) {
                    String token = FirebaseInstanceId.getInstance().getToken();
                    post.setPostphoto(mPostImageUri.toString());
                    //Toast.makeText(getApplicationContext(), "포토 경로"+mPostImageUri.toString(),Toast.LENGTH_SHORT).show();
                }

                if (userModel == null) {
                    Log.e(TAG, "User " + userId + " is unexpectedly null");
                    Toast.makeText(NewPostActivity.this,
                            "Error: could not fetch user.",
                            Toast.LENGTH_SHORT).show();
                }
                else {

                    if (mPostImageUri == null) {
                        writeNewPost(userId, userModel.getUsernm(), title, body, userId, getTime, post.getPostphoto());
                        Util9.showMessage(NewPostActivity.this, "Success to Post.");
                    }
                    else{
                        //게시물에 사진올리기
                        Glide.with(NewPostActivity.this)
                                .asBitmap()
                                .load(mPostImageUri)
                                .apply(new RequestOptions().override(500, 500))
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                        byte[] data = baos.toByteArray();


                                        FirebaseStorage.getInstance().getReference().child("postPhoto/" + post.getPostphoto()).putBytes(data);
                                        //Log.d("tag", Mountainref.putBytes(data).toString());
                                        Toast.makeText(getApplicationContext(), "이미지 올리기 성공!" + post.getPostphoto(), Toast.LENGTH_SHORT).show();
                                        Log.d("tag", post.getPostphoto());
                                    }
                                });

                        writeNewPost(userId, userModel.getUsernm(), title, body, userId, getTime, post.getPostphoto());

                    }

                }

                // Finish this Activity, back to the stream
                setEditingEnabled(true);
                finish();
            }
        });
    }

    private void setEditingEnabled(boolean enabled) {
        mTitleField.setEnabled(enabled);
        mBodyField.setEnabled(enabled);
        if (enabled) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
    }

    // [START write_fan_out]
    private void writeNewPost(String userId, String username, String title, String body, String userphoto, String day, String postphoto) {


        Post post = new Post(userId, username, title, body, userphoto, day, postphoto);

        db.collection("posts").add(post);
    }
    // [END write_fan_out]
}
