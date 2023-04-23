package com.app.SSR.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;


import com.app.SSR.R;

public class AboutUs extends AppCompatActivity {

    ImageButton f,w,l,g,i,t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aboiust);

        f = findViewById(R.id.facebook);
        w = findViewById(R.id.whatsapp);
        l = findViewById(R.id.linkdin);
        i = findViewById(R.id.instagram);
        g = findViewById(R.id.gituhb);
        t = findViewById(R.id.twitter);

        f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sharerUrl = "https://www.facebook.com/sarthi.raval.50/" ;
              Intent  intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
                startActivity(intent);            }
        });
        g.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sharerUrl = "https://github.com/sarthiraval" ;
                Intent  intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
                startActivity(intent);            }
        });

        i.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sharerUrl = "https://www.instagram.com/r.sarthi/" ;
                Intent  intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
                startActivity(intent);            }
        });
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sharerUrl = "https://twitter.com/RavalSarthi" ;
                Intent  intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
                startActivity(intent);            }
        });

        w.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent sendIntent = new Intent();
//                sendIntent.setAction(Intent.ACTION_SEND);
//                sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
//                sendIntent.setType("text/plain");
//                sendIntent.setPackage("com.whatsapp");
//                startActivity(Intent.createChooser(sendIntent, ""));
//                startActivity(sendIntent);

//                String number="9726760701";
//
//                String url = "https://api.whatsapp.com/send?phone="+number;
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(url));
//                startActivity(i);

//                String contact = "+00 9726760701";
//                String url = "https://api.whatsapp.com/send?phone=" + contact;
//                try {
//                    PackageManager pm = context.getPackageManager();
//                    pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
//                    Intent i = new Intent(Intent.ACTION_VIEW);
//                    i.setData(Uri.parse(url));
//                    startActivity(i);
//                } catch (PackageManager.NameNotFoundException e) {
//                    Toast.makeText(About.this, "Whatsapp app not installed in your phone", Toast.LENGTH_SHORT).show();
//                    e.printStackTrace();
//                }
                try {
                    String headerReceiver = "";// Replace with your message.
                    String bodyMessageFormal = "";// Replace with your message.
                    String whatsappContain = headerReceiver + bodyMessageFormal;
                    String trimToNumner = "+919726760701"; //10 digit number
                    Intent intent = new Intent ( Intent.ACTION_VIEW );
                    intent.setData ( Uri.parse ( "https://wa.me/" + trimToNumner + "/?text=" + "" ) );
                    startActivity ( intent );
                } catch (Exception e) {
                    e.printStackTrace ();
                }
           }
        });

        l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String sharerUrl = "https://www.linkedin.com/in/sarthi-raval-791705184/" ;
                Intent  intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
                startActivity(intent);
            }

        });
    }

    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(AboutUs.this, MainActivity.class));
    }
}