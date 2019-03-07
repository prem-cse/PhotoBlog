package com.example.photoblog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private List<BlogPost> list;
    public Context context;
    private FirebaseFirestore firestore;
    private FirebaseAuth adapterAuth;
    public Adapter(List<BlogPost> list){
       // this.context = context;
        this.list = list;
    }
    @NonNull
    @Override
    public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list,parent,false);
        context = parent.getContext();
        firestore = FirebaseFirestore.getInstance();
        adapterAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Adapter.ViewHolder holder, int position) {
       // holder.setIsRecyclable(false);

        String descText = list.get(position).getDesc();
        holder.setDesc(descText);
        String imageUri = list.get(position).getPostImage();
        String thumbUri = list.get(position).getThumb();
        holder.setImage(imageUri,thumbUri);
        String userId = list.get(position).getUser();
        final String postId = list.get(position).id;
        final String currUserId = adapterAuth.getCurrentUser().getUid();

        firestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()) {
                        String name = task.getResult().getString("name");
                        String camel = name.substring(0, 1).toUpperCase().concat(name.substring(1));
                        String UserImage = task.getResult().getString("userImage");
                        holder.setUsername(camel);
                        holder.setCircleImageView(UserImage);
                    }
                }else{
                    holder.setUsername("User");
                }
            }
        });

/*
       long sec = list.get(position).getTimestamp().getTime();
       String s = android.text.format.DateFormat.format("MM/dd/yyyy",new Date(sec)).toString();
       holder.setTime(s);
*/

        // LIKES



        firestore.collection("Post").document(postId).collection("Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if(documentSnapshots.isEmpty()||documentSnapshots == null){

                      holder.countLikes(String.valueOf(0));
                }else{

                      holder.countLikes(String.valueOf(documentSnapshots.size()));
                }

            }
        });

        firestore.collection("Post").document(postId).collection("Likes").document(currUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                   if(documentSnapshot.exists()&&documentSnapshot!=null){
                       holder.favourite.setImageDrawable(context.getDrawable(R.mipmap.favourite));
                   }else{
                       holder.favourite.setImageDrawable(context.getDrawable(R.mipmap.favourite_grey));
                   }
            }
        });
        holder.favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firestore.collection("Post").document(postId).collection("Likes").document(currUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.getResult().exists()){
                            firestore.collection("Post").document(postId).collection("Likes").document(currUserId).delete();
                        }else{
                            Map<String,Object> map = new HashMap<>();
                            map.put("timestamp", FieldValue.serverTimestamp());
                            firestore.collection("Post").document(postId).collection("Likes").document(currUserId).set(map);
                        }

                    }
                });

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View view;
        private TextView cardDesc;
        private TextView cardName;
        private TextView cardTime;
        private ImageView cardImage;
        private CircleImageView circleImageView;
        private ImageView favourite;
        private TextView likes;
        public ViewHolder(View itemView) {
            super(itemView);

            view = itemView;

            favourite = view.findViewById(R.id.favourite);

        }

        public void setDesc(String text){
            cardDesc = view.findViewById(R.id.cardDesc);
            cardDesc.setText(text);
        }
        public void setImage(String downloadUri,String thumbUri){
            cardImage = view.findViewById(R.id.cardImage);
            RequestOptions placeholders = new RequestOptions();
            placeholders.placeholder(R.mipmap.blankrec);

            //  IF THUMBNAIL IS LOADED FIRST THEN ONLY THUMBNAIL WILL BE USED
            Glide.with(context).applyDefaultRequestOptions(placeholders).load(downloadUri).thumbnail(Glide.with(context).load(thumbUri))
                    .into(cardImage);
        }

        public void setUsername(String name){
            cardName = view.findViewById(R.id.username);
            cardName.setText(name);
        }

        public void setTime(String date){
            cardTime = view.findViewById(R.id.cardTime);
            cardTime.setText(date);
        }

        public void setCircleImageView(String circle) {
            circleImageView = view.findViewById(R.id.circleImageView2);

            RequestOptions placeholders = new RequestOptions();
            placeholders.placeholder(R.mipmap.blankcircle);
            Glide.with(context).applyDefaultRequestOptions(placeholders).load(circle).into(circleImageView);
        }
        public void countLikes(String ctr){
            likes = view.findViewById(R.id.likes);
            likes.setText(ctr);
        }
    }
}
