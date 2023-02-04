package com.example.calendrierserveur;

/*
    PROGRAMME pour le PROJET d'ASI
    DUCHANOIS Benjamin
    JORGE William
    Master 1 Informatique

    Pour accèder au serveur, penser à changer l'URL
    res/values/strings.xml
    Changer l'IP, le Port et le Path selon le serveur si besoin
 */

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.owlike.genson.Genson;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private Activity activity;
    private final RendezVous[] listeRdv = new RendezVous[1000];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Permet de retrouver l'activité facilement depuis les Thread
        this.activity = MainActivity.this;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();

        //---- CONNEXION AU SERVEUR ----
        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            try {
                //On récupère le nombre de rdv stockés
                URL url = new URL(getString(R.string.IP)+getString(R.string.Port)+getString(R.string.Path)+ getString(R.string.serverGetNombreDeRendezVous));
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                InputStream in = new BufferedInputStream( urlConnection.getInputStream());
                Scanner scanner = new Scanner(in);
                final int nbRdv = new Genson().deserialize( scanner.nextLine(), int.class);
                Log.i("Exchange-JSON", "Result == " + nbRdv);

                //On charge tous les rdv dans un tableau
                for (int i = 0; i<nbRdv; i++) {
                    url = new URL(getString(R.string.IP)+getString(R.string.Port)+getString(R.string.Path)+ getString(R.string.serverGet) + i);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    in = new BufferedInputStream(urlConnection.getInputStream());
                    scanner = new Scanner(in);
                    final RendezVous rendezVous = new Genson().deserialize(scanner.nextLine(), RendezVous.class);
                    Log.i("Exchange-JSON", "Result == " + url);
                    listeRdv[i] = rendezVous;
                }

                //---- CHARGEMENT DE LA VUE ----
                runOnUiThread(() -> {
                    //On vide entièrement la vue avec le layout principal
                    LinearLayout principal = (LinearLayout) findViewById(R.id.layoutPrincipal);
                    principal.removeAllViews();

                    //On ajoute le bouton d'ajout de Rdv
                    Button boutonAjouter = creationBouton();
                    boutonAjouter.setText(R.string.AddRdvButton);
                    boutonAjouter.setOnClickListener(view -> add());

                    //On définit les différents jours en leur créant un layout chacun
                    LinearLayout lundi = createLayout("Lundi");
                    LinearLayout mardi = createLayout("Mardi");
                    LinearLayout mercredi = createLayout("Mercredi");
                    LinearLayout jeudi = createLayout("Jeudi");
                    LinearLayout vendredi = createLayout("Vendredi");
                    LinearLayout samedi = createLayout("Samedi");
                    LinearLayout dimanche = createLayout("Dimanche");

                    //On ajoute le bouton et les jours au layout principal
                    principal.addView(boutonAjouter);
                    principal.addView(lundi);principal.addView(mardi);principal.addView(mercredi);principal.addView(jeudi);principal.addView(vendredi);principal.addView(samedi);principal.addView(dimanche);

                    //On créer ensuite les layout de chaque rdv
                    for (int i = 0; i<nbRdv; i++) {
                        LinearLayout linearLayout = creationListeRdv(i);

                        //On ajoute le rdv au jour qui lui correspond
                        switch (listeRdv[i].getJour()){
                            case 0: lundi.addView(linearLayout);
                                    break;
                            case 1: mardi.addView(linearLayout);
                                    break;
                            case 2: mercredi.addView(linearLayout);
                                    break;
                            case 3: jeudi.addView(linearLayout);
                                    break;
                            case 4: vendredi.addView(linearLayout);
                                    break;
                            case 5: samedi.addView(linearLayout);
                                    break;
                            case 6: dimanche.addView(linearLayout);
                                    break;
                            default:
                                    break;
                        }
                    }
                });


                in.close();

            } catch (Exception e) {
                Log.e( "Exchange-JSON", "Cannot found http server", e);
            } finally {
                if ( urlConnection != null) urlConnection.disconnect();
            }
        }).start();
    }

    //Crée les layout pour les différents jours
    private LinearLayout createLayout(String nom){
        //Layout vertical où on ajoutera les rdv
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        //Layout horizontal pour le nom et de possibles ajouts
        LinearLayout layoutHorizontal = new LinearLayout(this);
        layoutHorizontal.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        layoutHorizontal.setOrientation(LinearLayout.HORIZONTAL);

        //Nom du jour
        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        Typeface typeface = Typeface.create("Roboto", Typeface.BOLD);
        textView.setTypeface(typeface);
        textView.setTextColor(getColor(R.color.black));
        textView.setText(nom);
        textView.setTextSize(30);

        layoutHorizontal.addView(textView);
        linearLayout.addView(layoutHorizontal);
        return linearLayout;
    }

    //Crée les rendez-vous
    @SuppressLint("SetTextI18n")
    private LinearLayout creationListeRdv(int currentId){
        //On crée pour chaque rdv un Layout
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        //On crée un TextView pour son titre
        TextView textView = new TextView(activity);
        textView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        Typeface typeface = Typeface.create("Montserrat", Typeface.NORMAL);
        textView.setTypeface(typeface);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        textView.setTextColor(getColor(R.color.light_black));
        //Créer un String min, pour qu'il y est tjrs 2 digits aux minutes
        String min = String.valueOf(listeRdv[currentId].getMinute());
        if (listeRdv[currentId].getMinute() < 10)
            min = "0" + min;
        textView.setText(listeRdv[currentId].getHeure() + "h" + min + " : " + listeRdv[currentId].getTitre());
        //Et un bouton pour la modification
        Button button = creationBouton();
        button.setText(R.string.ModifButton);
        //On assigne l'update au bouton concerné
        button.setOnClickListener(view -> update(currentId));
        //On ajoute le tout au layout
        linearLayout.addView(textView);
        linearLayout.addView(button);

        return linearLayout;
    }


    //Permet de créer des bouton suivant notre style plus facilement
    private Button creationBouton(){
        //On crée le bouton et ses paramètres de base
        Button button = new Button(this);
        button.setLayoutParams(new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        //On lui ajoute nos couleurs persos
        button.setBackgroundTintList(getColorStateList(R.color.blue));
        button.setTextColor(getColor(R.color.white));
        Typeface typeface = Typeface.create("Roboto", Typeface.BOLD);
        button.setTypeface(typeface);
        return button;
    }

    //Fonction pour les boutons "Modifier"
    private void update(int id) {
        //Lance l'application UpdateActivity en lui précisant l'id du rdv à modifier
        Intent intent = new Intent(this, UpdateActivity.class);
        intent.putExtra("id",id + "");
        this.startActivity(intent);
    }

    //Fonction pour le bouton "Ajouter un rdv"
    private void add() {
        //Lance l'activité AddActivity
        Intent intent = new Intent(this, AddActivity.class);
        this.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}