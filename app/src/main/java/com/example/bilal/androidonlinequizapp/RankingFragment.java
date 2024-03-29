package com.example.bilal.androidonlinequizapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bilal.androidonlinequizapp.Common.Common;
import com.example.bilal.androidonlinequizapp.Interface.ItemClickListener;
import com.example.bilal.androidonlinequizapp.Interface.RankingCallBack;
import com.example.bilal.androidonlinequizapp.Model.Question;
import com.example.bilal.androidonlinequizapp.Model.QuestionScore;
import com.example.bilal.androidonlinequizapp.Model.Ranking;
import com.example.bilal.androidonlinequizapp.ViewHolder.RankingViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//T2-->Start//
public class RankingFragment extends Fragment {
    View myFregment;

    //T4-->Start//
    RecyclerView rankingList;
    LinearLayoutManager layoutManager;
    FirebaseRecyclerAdapter<Ranking,RankingViewHolder> adapter;

    FirebaseDatabase database;
    DatabaseReference questionScore,rankingTbl;

    int sum=0;
    //T4-->End//

    public static RankingFragment newInstance(){
        RankingFragment rankingFragment = new RankingFragment();
        return rankingFragment;
    }

    //Press Ctrl+O

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //T4-->Start//
        database = FirebaseDatabase.getInstance();
        questionScore = database.getReference("Question_Score");
        rankingTbl = database.getReference("Ranking");
        //T4-->End//
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myFregment = inflater.inflate(R.layout.fragment_ranking,container,false);

        //T4-->Start//
        //Init View
        rankingList = (RecyclerView)myFregment.findViewById(R.id.rankingList);
        layoutManager = new LinearLayoutManager(getActivity());
        rankingList.setHasFixedSize(true);
        //Because OrderByChild method of firebase will sort list will ascending
        //so we need reverse our Recycler data
        //By LayoutManager
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        rankingList.setLayoutManager(layoutManager);

        updateScore(Common.currentUser.getUsername(), new RankingCallBack<Ranking>() {
            @Override
            public void callBack(Ranking ranking) {
                //Update to Ranking Table
                rankingTbl.child(ranking.getUsername())
                        .setValue(ranking);
                //showRanking(); //After Upload , we will sort rank table and sort result
            }
        });

        //Set Adapter
        adapter = new FirebaseRecyclerAdapter<Ranking, RankingViewHolder>(
                Ranking.class,
                R.layout.layout_ranking,
                RankingViewHolder.class,
                rankingTbl.orderByChild("score")
        ) {
            @Override
            protected void populateViewHolder(RankingViewHolder viewHolder, final Ranking model, int position) {

                viewHolder.txt_name.setText(model.getUsername());
                viewHolder.txt_score.setText(String.valueOf(model.getScore()));

                //Fixed crash when click to item
                viewHolder.setItemClickListener(new ItemClickListener(){
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //T5-->Start//
                        Intent scoreDetail = new Intent(getActivity(),ScoreDetail.class);
                        scoreDetail.putExtra("viewUser",model.getUsername());
                        startActivity(scoreDetail);
                        //T5-->End//
                    }
                });
            }
        };

        adapter.notifyDataSetChanged();
        rankingList.setAdapter(adapter);
        //T4-->End//

        return myFregment;
    }



    //T4-->Start//
    private void updateScore(final String username, final RankingCallBack<Ranking> callback) {
        questionScore.orderByChild("user").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data:dataSnapshot.getChildren())
                        {
                            QuestionScore ques = data.getValue(QuestionScore.class);
                            sum+=Integer.parseInt(ques.getScore());
                        }
                        //After sumary all score, we need process sum variable here
                        //Because firebase is async db , so if process outside , our 'sum'
                        //value will be reset to 0
                        Ranking ranking = new Ranking(username,sum);
                        callback.callBack(ranking);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
    //T4-->End//
}
//T2-->End//