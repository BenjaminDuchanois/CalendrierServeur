package com.example.calendrierserveur;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
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

public class UpdateActivity extends AppCompatActivity {

    Activity activity;
    EditText titre;
    EditText description;
    Spinner jour;
    TimePicker heure;
    Button boutonValider;
    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        activity = UpdateActivity.this;
        titre = (EditText) this.findViewById(R.id.title_text);
        description = (EditText) this.findViewById(R.id.description_text);
        jour = (Spinner) this.findViewById(R.id.day_spinner);
        heure = (TimePicker) this.findViewById(R.id.time_picker);
        boutonValider = (Button) this.findViewById(R.id.confirm_button);
        boutonValider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update(view);
            }
        });
        heure.setIs24HourView(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent = activity.getIntent();
                id = Integer.parseInt(intent.getStringExtra("id"));
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL("http://192.168.56.1:8081/CalendrierServeur/rest/rdv/get/" + id);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    InputStream in = new BufferedInputStream( urlConnection.getInputStream());
                    Scanner scanner = new Scanner(in);
                    final RendezVous rendezVous = new Genson().deserialize( scanner.nextLine(), RendezVous.class);
                    Log.i("Exchange-JSON", "Result == " + rendezVous);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            titre.setHint(rendezVous.getTitre());
                            if(!description.toString().matches(""))
                                description.setHint(rendezVous.getDescription());
                            jour.setSelection(rendezVous.getJour());
                            heure.setMinute(rendezVous.getMinute());
                            heure.setHour(rendezVous.getHeure());
                        }
                    });

                    in.close();
                } catch (Exception e) {
                    Log.e( "Exchange-JSON", "Cannot found http server", e);
                } finally {
                    if ( urlConnection != null) urlConnection.disconnect();
                }
            }
        }).start();
    }

    private void update(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String nvTitre, nvDesc;

                if(titre.getText().toString().equals(""))
                    nvTitre = titre.getHint().toString();
                else
                    nvTitre = titre.getText().toString();
                if(description.getText().toString().equals(""))
                    nvDesc = description.getHint().toString();
                else
                    nvDesc = description.getText().toString();

                RendezVous rdv = new RendezVous(
                        id,
                        nvTitre,
                        nvDesc,
                        (int) jour.getSelectedItemId(),
                        heure.getHour(),
                        heure.getMinute()
                );
                String message = new Genson().serialize( rdv );
                Log.i("Exchange-JSON", "Message == " + message);

                HttpURLConnection urlConnection = null;
                try{
                    URL url = new URL("http://192.168.56.1:8081/CalendrierServeur/rest/rdv/update");
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
            }
        }).start();

        this.finish();
    };
}