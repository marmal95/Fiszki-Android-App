package fiszki.xyz.fiszkiapp.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fiszki.xyz.fiszkiapp.source.Flashcard;
import fiszki.xyz.fiszkiapp.R;
import fiszki.xyz.fiszkiapp.activities.FavouriteFlashcardsActivity;
import fiszki.xyz.fiszkiapp.activities.MyFlashcardsActivity;
import fiszki.xyz.fiszkiapp.activities.RecommendedFlashcardsActivity;
import fiszki.xyz.fiszkiapp.activities.SearchActivity;

public class FlashcardsAdapter extends ArrayAdapter<Flashcard> {

    public FlashcardsAdapter(Context context, int resource, List<Flashcard> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if(getContext() instanceof MyFlashcardsActivity)
            return getMyFlashcardsView(position, convertView, parent);
        else if(getContext() instanceof FavouriteFlashcardsActivity)
            return getFavouriteFlashcardsView(position, convertView, parent);
        else if(getContext() instanceof RecommendedFlashcardsActivity)
            return getFavouriteFlashcardsView(position, convertView, parent);
        else if(getContext() instanceof SearchActivity)
            return getFavouriteFlashcardsView(position, convertView, parent);

        return new View(getContext());
    }

    private View getMyFlashcardsView(int position, View convertView, ViewGroup parent){
        // Get the data item for this position
        Flashcard flashcard = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_item_my_flashcard, parent, false);

        // Update view
        ImageView flag1 = (ImageView)convertView.findViewById(R.id.flag_lang1);
        ImageView flag2 = (ImageView)convertView.findViewById(R.id.flag_lang2);
        TextView flashcardName = (TextView)convertView.findViewById(R.id.flashcardLabel);
        TextView timeCreated = (TextView)convertView.findViewById(R.id.timeCreated);

        flashcardName.setText(flashcard.getName());

        Date date = new Date(Long.valueOf(flashcard.getTimeCreated()) * 1000);
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);
        timeCreated.setText(getContext().getResources().getString(R.string.timeCreated, formatter.format((date))));

        // TODO: Extract to function
        switch(flashcard.getLangFrom()){
            case "pl":
                flag1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.poland_icon));
                break;
            case "en":
                flag1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.united_kingdom_icon));
                break;
            case "es":
                flag1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.spain_icon));
                break;
            case "fr":
                flag1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.france_icon));
                break;
            case "de":
                flag1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.germany_icon));
                break;
            case "it":
                flag1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.italy_icon));
                break;
        }

        switch(flashcard.getLangTo()){
            case "pl":
                flag2.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.poland_icon));
                break;
            case "en":
                flag2.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.united_kingdom_icon));
                break;
            case "es":
                flag2.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.spain_icon));
                break;
            case "fr":
                flag2.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.france_icon));
                break;
            case "de":
                flag2.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.germany_icon));
                break;
            case "it":
                flag1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.italy_icon));
                break;
        }

        return convertView;
    }

    private View getFavouriteFlashcardsView(int position, View convertView, ViewGroup parent){
        Flashcard flashcard = getItem(position);

        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_item_favourite_flashcard, parent, false);

        ImageView flag1 = (ImageView)convertView.findViewById(R.id.flag_lang1);
        ImageView flag2 = (ImageView)convertView.findViewById(R.id.flag_lang2);
        TextView flashcardName = (TextView)convertView.findViewById(R.id.flashcardLabel);

        flashcardName.setText(flashcard.getName());

        // TODO: Extract to function
        switch(flashcard.getLangFrom()){
            case "pl":
                flag1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.poland_icon));
                break;
            case "en":
                flag1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.united_kingdom_icon));
                break;
            case "es":
                flag1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.spain_icon));
                break;
            case "fr":
                flag1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.france_icon));
                break;
            case "de":
                flag1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.germany_icon));
                break;
            case "it":
                flag1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.italy_icon));
                break;
        }

        switch(flashcard.getLangTo()){
            case "pl":
                flag2.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.poland_icon));
                break;
            case "en":
                flag2.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.united_kingdom_icon));
                break;
            case "es":
                flag2.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.spain_icon));
                break;
            case "fr":
                flag2.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.france_icon));
                break;
            case "de":
                flag2.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.germany_icon));
                break;
            case "it":
                flag1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.italy_icon));
                break;
        }

        return convertView;
    }
}
