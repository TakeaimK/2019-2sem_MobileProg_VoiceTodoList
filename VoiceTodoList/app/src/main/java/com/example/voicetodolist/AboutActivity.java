package com.example.voicetodolist;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.vansuita.materialabout.builder.AboutBuilder;
import com.vansuita.materialabout.views.AboutView;


public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setTitle("About this App");

        AboutView view = AboutBuilder.with(this)
                .setPhoto(R.drawable.choi_sejin_profile)
                .setCover(R.drawable.kitty)
                .setName("Choi Sejin")
                .setSubTitle("Developing Anything")
                .setBrief("Just try, Be sincere, Do my best")
                .setAppIcon(R.mipmap.ic_launcher)
                .setAppName(R.string.app_name)
                .addGooglePlayStoreLink("8002078663318221363")
                .addGitHubLink("TakeaimK/2019-2sem_MobileProg_VoiceTodoList")
                .addFacebookLink("CSjinKor")
                .addFiveStarsAction()
                .setVersionNameAsAppSubTitle()
                .addShareAction(R.string.app_name)
                .setWrapScrollView(true)
                .setLinksAnimated(true)
                .setShowAsCard(true)
                .build();


        setContentView(view);
        ab.setBackgroundDrawable(new ColorDrawable(0xFF82B1FF));



    }
}
