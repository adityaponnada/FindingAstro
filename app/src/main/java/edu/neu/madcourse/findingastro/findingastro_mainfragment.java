package edu.neu.madcourse.findingastro;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class findingastro_mainfragment extends Fragment {


    public findingastro_mainfragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView =
                inflater.inflate(R.layout.fragment_findingastro_mainfragment, container, false);

        View new_button = (Button)rootView.findViewById(R.id.new_game_button);
        View rules_button = (Button)rootView.findViewById(R.id.rules_button);
        View challengers = (Button)rootView.findViewById(R.id.challenge_button);

        new_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), skyview_activity.class);
                startActivity(intent);
            }
        });

        rules_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), findingastro_rules.class);
                startActivity(intent);
            }
        });

        challengers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), findingastro_challenge.class);
                startActivity(intent);
            }
        });


        // Inflate the layout for this fragment
        return rootView;
    }

}
