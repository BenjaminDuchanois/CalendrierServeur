package com.example.calendrierserveur;

import org.json.JSONException;
import org.json.JSONObject;

public class RendezVous {
    private int idRdv;
    private String titre;
    private String description;
    private int jour;
    private int heure;
    private int minute;

    public RendezVous() {
        this.setIdRdv(0);
        this.setTitre("Null");
        this.setDescription("Null");
        this.setJour(0);
        this.setHeure(0);
        this.setMinute(0);
    }

    public RendezVous(int idRdv, String titre, String description, int jour, int heure, int minute) {
        this.setIdRdv(idRdv);
        this.setTitre(titre);
        this.setDescription(description);
        this.setJour(jour);
        this.setHeure(heure);
        this.setMinute(minute);
    }

    public int getIdRdv() {
        return idRdv;
    }

    public void setIdRdv(int idRdv) {
        this.idRdv = idRdv;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getJour() {
        return jour;
    }

    public void setJour(int jour) {
        this.jour = jour;
    }

    public int getHeure() {
        return heure;
    }

    public void setHeure(int heure) {
        this.heure = heure;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("idRDV", idRdv);
            jsonObject.put("titre", titre);
            jsonObject.put("description", description);
            jsonObject.put("jour", jour);
            jsonObject.put("heure", heure);
            jsonObject.put("minute", minute);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

}
