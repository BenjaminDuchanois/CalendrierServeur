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
    Button boutonRetour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        //Initialisation des composants graphiques
        activity = AddActivity.this;
        titre = (EditText) this.findViewById(R.id.title_text);
        description = (EditText) this.findViewById(R.id.description_text);
        jour = (Spinner) this.findViewById(R.id.day_spinner);
        heure = (TimePicker) this.findViewById(R.id.time_picker);
        boutonRetour = (Button) this.findViewById(R.id.cancel_button);
        boutonValider = (Button) this.findViewById(R.id.confirm_button);
        //Lance la fonction d'ajout quand le bouton confirmer est cliqué
        boutonValider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add(view);
            }
        });
        //Lance la fonction de retour quand le bouton annuler est cliqué (Revient à faire Retour)
        boutonRetour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { cancel(); }
        });
        //Permet de mettre le timePicker en format 24h
        heure.setIs24HourView(true);
    }

    //Fonction d'ajout
    private void add(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                //Si tous les champs ont bien été précisés
                if((!titre.getText().toString().equals("")) && (!description.getText().toString().equals(""))) {
                    //Crée un nouveau RendezVous suivant les données entrées
                    RendezVous rdv = new RendezVous(
                            //L'id est fixé à 0 mais sera modifié par le serveur selon le nombre de rdv enregistrés
                            0,
                            titre.getText().toString(),
                            description.getText().toString(),
                            (int) jour.getSelectedItemId(),
                            heure.getHour(),
                            heure.getMinute()
                    );
                    //Sérialise le rendez-vous avec Genson
                    String message = new Genson().serialize( rdv );
                    Log.i("Exchange-JSON", "Message == " + message);

                    HttpURLConnection urlConnection = null;
                    //Envoie le rdv sérialisé sur l'url /add qui sera ensuite géré par le serveur
                    try{
                        URL url = new URL(getString(R.string.IP)+":8081/CalendrierServeur/rest/rdv/add");
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
                    //Termine l'activité
                    activity.finish();
                }
                else {

                }
            }
        }).start();
    };

    //Termine l'activité
    private void cancel(){
        activity.finish();
    }
}