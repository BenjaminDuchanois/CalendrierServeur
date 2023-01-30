package com.example.calendrierserveur;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.owlike.genson.Genson;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class AddActivity extends AppCompatActivity {

    Activity activity;
    EditText titre;
    EditText description;
    Spinner jour;
    TimePicker heure;
    Button boutonValider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        activity = AddActivity.this;
        titre = (EditText) this.findViewById(R.id.title_text);
        description = (EditText) this.findViewById(R.id.description_text);
        jour = (Spinner) this.findViewById(R.id.day_spinner);
        heure = (TimePicker) this.findViewById(R.id.time_picker);
        boutonValider = (Button) this.findViewById(R.id.confirm_button);
        boutonValider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add(view);
            }
        });
        heure.setIs24HourView(true);
    }

    private void add(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String nvTitre, nvDesc;

                if((!titre.getText().toString().equals("")) && (!description.getText().toString().equals(""))) {
                    RendezVous rdv = new RendezVous(
                            0,
                            titre.getText().toString(),
                            description.getText().toString(),
                            (int) jour.getSelectedItemId(),
                            heure.getHour(),
                            heure.getMinute()
                    );
                    String message = new Genson().serialize( rdv );
                    Log.i("Exchange-JSON", "Message == " + message);

                    HttpURLConnection urlConnection = null;
                    try{
                        URL url = new URL("http://192.168.43.85:8081/CalendrierServeur/rest/rdv/add");
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("PUT");
                        urlConnection.setDoOutput(true);
                        urlConnection.setRequestProperty("Content-Type", "application/json");

                        OutputStream out = new BufferedOutputStream( urlConnection.getOutputStream() );
                        out.write( message.getBytes());
                        out.close();

                        InputStream in = new BufferedInputStream( urlConnection.getInputStream());
                        Scanner scanner = new Scanner(in);
                        Log.i("Exchange-JSON", "Result == " + scanner.nextLine());
                        in.close();
                    } catch ( Exception e ) {
                        Log.e("Exchange-JSON", "Cannot found http server", e);
                    } finally {
                        if ( urlConnection != null ) urlConnection.disconnect();
                    }
                    activity.finish();
                }
                else {

                }
            }
        }).start();
    };
}