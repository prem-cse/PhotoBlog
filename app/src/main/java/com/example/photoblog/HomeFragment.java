package com.example.photoblog;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<BlogPost> list;
    private FirebaseFirestore firestore;
    private Adapter adapter;
    private FirebaseAuth homeAuth;
    private DocumentSnapshot lastVisible;
    private boolean isfirstLoaded = true;
    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        list = new ArrayList<>();
        adapter = new Adapter(list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        firestore = FirebaseFirestore.getInstance();
        homeAuth = FirebaseAuth.getInstance();

        if(homeAuth.getCurrentUser() != null) {
          /*
          IMP: PAGINATION -> ONCE WE REACH END POST THEN LOAD MORE WHICH START FROM LAST
          POST . IF THE FIRST PAGE IS LOADED THEN IF OTHER USER POST SOMETHING THEN IT
          SHOULD BE ON THE TOP OF POST AND PAGINATION NOW WORK ACCORDINGLY
           */
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    boolean reached = recyclerView.canScrollVertically(1);
                    if(reached){
                        String desc = lastVisible.getString("Desc");
                      //  Toast.makeText(container.getContext(),"reach "+desc,Toast.LENGTH_SHORT).show();

                        loadMore();
                    }
                }
            });
            // SORT THE POST DATA IN DESCENDING ORDER
            Query q1 = firestore.collection("Post")
                    .orderBy("time",Query.Direction.DESCENDING).limit(3);

            q1.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                     if(isfirstLoaded) {
                         // IF IT IS FIRST TIME LODING THEN SET LASTVISIBLE TO END OF 3 POST
                         lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                     }
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String id = doc.getDocument().getId();
                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(id);
                            if(isfirstLoaded)
                            list.add(blogPost);
                            else list.set(0,blogPost);
                            adapter.notifyDataSetChanged();
                        }
                    }
                    isfirstLoaded = false;
                }
            });
        }

        // Inflate the layout for this fragment
        return view;


    }
    // ON RESUME WE WANT OUR VARIABLES TO BE REINITIALIZED
    @Override
    public void onResume() {
        super.onResume();
        lastVisible = null;
        isfirstLoaded = true;
    }
    public void loadMore(){
        Query q1 = firestore.collection("Post")
                .orderBy("time",Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);

        q1.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (!documentSnapshots.isEmpty()) {
                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String id = doc.getDocument().getId();
                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(id);
                            list.add(blogPost);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });

    }


}
