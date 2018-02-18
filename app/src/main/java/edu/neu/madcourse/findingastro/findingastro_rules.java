package edu.neu.madcourse.findingastro;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class findingastro_rules extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findingastro_rules);

        ImageView tute_1 = (ImageView)findViewById(R.id.image_tut_one);
        ImageView tute_3 = (ImageView)findViewById(R.id.image_tute_three);
        ImageView tute_2 = (ImageView)findViewById(R.id.image_tute_two);
        Button startGame_button = (Button)findViewById(R.id.startgame);

        tute_1.setImageResource(R.drawable.tut_one);
        tute_3.setImageResource(R.drawable.tut_three);
        tute_2.setImageResource(R.drawable.tut_two);


        startGame_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), skyview_activity.class);
                startActivity(intent);
            }
        });


    }
}
