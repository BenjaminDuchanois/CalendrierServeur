package com.example.calendrierserveur;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.owlike.genson.Genson;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private Activity activity;
    private RendezVous[] listeRdv = new RendezVous[1000];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.activity = MainActivity.this;
    }

    @Override
    protected void onResume() {
        super.onResume();

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL(getString(R.string.IP)+":8081/CalendrierServeur/rest/rdv/getNbRdv");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    InputStream in = new BufferedInputStream( urlConnection.getInputStream());
                    Scanner scanner = new Scanner(in);
                    final int nbRdv = new Genson().deserialize( scanner.nextLine(), int.class);
                    Log.i("Exchange-JSON", "Result == " + nbRdv);

                    //On charge les données dans un tableau
                    for (int i = 0; i<nbRdv; i++) {
                        url = new URL(getString(R.string.IP)+":8081/CalendrierServeur/rest/rdv/get/" + i);
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        in = new BufferedInputStream(urlConnection.getInputStream());
                        scanner = new Scanner(in);
                        final RendezVous rendezVous = new Genson().deserialize(scanner.nextLine(), RendezVous.class);
                        Log.i("Exchange-JSON", "Result == " + url);
                        listeRdv[i] = rendezVous;
                    }

                    //On charge la vue
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LinearLayout principal = (LinearLayout) findViewById(R.id.layoutPrincipal);
                            principal.removeAllViews();

                            Button boutonAjouter = creationBouton();
                            boutonAjouter.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    add();
                                }
                            });

                            //On définit les différents jours
                            LinearLayout lundi = createLayout("Lundi");
                            LinearLayout mardi = createLayout("Mardi");
                            LinearLayout mercredi = createLayout("Mercredi");
                            LinearLayout jeudi = createLayout("Jeudi");
                            LinearLayout vendredi = createLayout("Vendredi");
                            LinearLayout samedi = createLayout("Samedi");
                            LinearLayout dimanche = createLayout("Dimanche");

                            //On ajoute les jours au layout principal
                            principal.addView(lundi);principal.addView(mardi);principal.addView(mercredi);principal.addView(jeudi);principal.addView(vendredi);principal.addView(samedi);principal.addView(dimanche);
                            principal.addView(boutonAjouter);

                            //On affiche les rendez vous
                            for (int i = 0; i<nbRdv; i++) {
                                final int currentId = i;
                                //On crée pour chaque rdv un Layout
                                LinearLayout linearLayout = new LinearLayout(activity);
                                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                //On crée un TextView
                                TextView textView = new TextView(activity);
                                textView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                                textView.setText(listeRdv[i].getHeure() + "h" + listeRdv[i].getMinute() + " : " + listeRdv[i].getTitre());
                                //Et un bouton pour la modification
                                Button button = new Button(activity);
                                button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        update(listeRdv[currentId].getIdRdv());
                                    }
                                });
                                //On ajoute le tout au layout
                                linearLayout.addView(textView);
                                linearLayout.addView(button);

                                //On ajoute le rdv au jour qui correspond
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

    //Crée les layout pour les différents jours
    private LinearLayout createLayout(String nom){
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout layoutHorizontal = new LinearLayout(this);
        layoutHorizontal.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        layoutHorizontal.setOrientation(LinearLayout.HORIZONTAL);

        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        textView.setText(nom);
        textView.setTextSize(30);

        layoutHorizontal.addView(textView);
        linearLayout.addView(layoutHorizontal);
        return linearLayout;
    }

    private Button creationBouton(){
        Button boutonRond = new Button(this);
        boutonRond.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        boutonRond.getLayoutParams().width = 50;
        boutonRond.getLayoutParams().height = 50;
        ((RelativeLayout.LayoutParams) boutonRond.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        ((RelativeLayout.LayoutParams) boutonRond.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
        boutonRond.setBackgroundResource(R.color.black);
        boutonRond.setText("+");
        boutonRond.setTextColor(getResources().getColor(R.color.white));
        boutonRond.setTextSize(20);
        boutonRond.setElevation(4);
        boutonRond.setBackgroundTintList(getResources().getColorStateList(R.color.black));
        boutonRond.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        boutonRond.setPadding(16,16,16,16);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(getResources().getColor(R.color.black));
        boutonRond.setBackground(drawable);

        return boutonRond;
    }

    private void update(int id) {
        Intent intent = new Intent(this, UpdateActivity.class);
        intent.putExtra("id",id + "");
        this.startActivity(intent);
    }

    private void add() {
        Intent intent = new Intent(this, AddActivity.class);
        this.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}