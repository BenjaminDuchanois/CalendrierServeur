package com.example.calendrierserveur;

/*
    PROGRAMME pour le PROJET d'ASI
    COTE CLIENT
    DUCHANOIS Benjamin
    JORGE William
    Master 1 Informatique

    Pour accèder au serveur, penser à changer l'URL
    res/values/strings.xml
    Changer l'IP, le Port et le Path selon le serveur si besoin
 */

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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class UpdateActivity extends AppCompatActivity {

    Activity activity;
    EditText titre;
    EditText description;
    Spinner jour;
    TimePicker heure;
    Button boutonValider;
    Button boutonSuppr;
    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        //Initialisation des composants graphiques
        activity = UpdateActivity.this;
        titre = (EditText) this.findViewById(R.id.title_text);
        description = (EditText) this.findViewById(R.id.description_text);
        jour = (Spinner) this.findViewById(R.id.day_spinner);
        heure = (TimePicker) this.findViewById(R.id.time_picker);
        boutonValider = (Button) this.findViewById(R.id.confirm_button);
        boutonSuppr = (Button) this.findViewById(R.id.suppr_button);
        //Lance la fonction d'update quand le bouton confirmer est cliqué
        boutonValider.setOnClickListener(this::update);
        boutonSuppr.setOnClickListener(this::suppr);
        //Permet de mettre le timePicker au format 24h
        heure.setIs24HourView(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        new Thread(() -> {
            Intent intent = activity.getIntent();
            id = Integer.parseInt(intent.getStringExtra("id"));
            HttpURLConnection urlConnection = null;
            try {
                //Charge le rdv selctioné avec l'url /get/{id}
                URL url = new URL(getString(R.string.IP)+getString(R.string.Port)+getString(R.string.Path)+ getString(R.string.serverGet) + id);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                InputStream in = new BufferedInputStream( urlConnection.getInputStream());
                Scanner scanner = new Scanner(in);
                final RendezVous rendezVous = new Genson().deserialize( scanner.nextLine(), RendezVous.class);
                Log.i("Exchange-JSON", "Result == " + rendezVous);

                //Entre les données du rdv dans les champs correspondant
                runOnUiThread(() -> {
                    titre.setHint(rendezVous.getTitre());
                    if(!description.toString().matches(""))
                        description.setHint(rendezVous.getDescription());
                    jour.setSelection(rendezVous.getJour());
                    heure.setMinute(rendezVous.getMinute());
                    heure.setHour(rendezVous.getHeure());
                });

                in.close();
            } catch (Exception e) {
                Log.e( "Exchange-JSON", "Cannot found http server", e);
            } finally {
                if ( urlConnection != null) urlConnection.disconnect();
            }
        }).start();
    }

    //Fonction de modification
    private void update(View view) {
        new Thread(() -> {
            String nvTitre, nvDesc;

            //Si les données ne sont pas entrées, reprend celles de base dans le hint
            //Autrement prend en compte les nouvelles données
            if(titre.getText().toString().equals(""))
                nvTitre = titre.getHint().toString();
            else
                nvTitre = titre.getText().toString();
            if(description.getText().toString().equals(""))
                nvDesc = description.getHint().toString();
            else
                nvDesc = description.getText().toString();

            //Crée un nouveau rdv avec les nouvelles données
            RendezVous rdv = new RendezVous(
                    id,
                    nvTitre,
                    nvDesc,
                    (int) jour.getSelectedItemId(),
                    heure.getHour(),
                    heure.getMinute()
            );

            URL url = null;
            try {
                url = new URL(getString(R.string.IP)+getString(R.string.Port)+getString(R.string.Path)+ getString(R.string.serverModification));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            appelURL(rdv, url);

        }).start();

        this.finish();
    }

    private void suppr(View view){
        new Thread(() -> {
            RendezVous rdv = new RendezVous();
            rdv.setIdRdv(id);

            URL url = null;
            try {
                url = new URL(getString(R.string.IP)+getString(R.string.Port)+getString(R.string.Path)+ getString(R.string.serverSuppression));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            appelURL(rdv, url);
        }).start();

        this.finish();
    }

    private void appelURL(RendezVous rdv, URL url){
        //Sérialise le rdv
        String message = new Genson().serialize( rdv );
        Log.i("Exchange-JSON", "Message == " + message);

        HttpURLConnection urlConnection = null;
        try{
            //Envoie le rdv sur l'url /update où le serveur se chargera de remplacer l'ancien par le nouveau
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
}