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

import fiszki.xyz.fiszkiapp.R;
import fiszki.xyz.fiszkiapp.activities.MyFlashcardsActivity;
import fiszki.xyz.fiszkiapp.source.Flashcard;

public class FlashcardsAdapter extends ArrayAdapter<Flashcard> {

    public FlashcardsAdapter(Context context, int resource, List<Flashcard> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if(getContext() instanceof MyFlashcardsActivity)
            return getMyFlashcardsView(position, convertView, parent);

        return getDefaultsFlashcardsView(position, convertView, parent);
    }

    private View getMyFlashcardsView(int position, View convertView, ViewGroup parent){
        Flashcard flashcard = getItem(position);

        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_item_my_flashcard, parent, false);

        initDefaultView(convertView, flashcard);
        appendTimeCreated(convertView, flashcard);

        return convertView;
    }

    private View getDefaultsFlashcardsView(int position, View convertView, ViewGroup parent){
        Flashcard flashcard = getItem(position);

        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_item_favourite_flashcard, parent, false);

        initDefaultView(convertView, flashcard);

        return convertView;
    }

    private void initDefaultView(View convertView, Flashcard flashcard) {
        ImageView flag1 = (ImageView)convertView.findViewById(R.id.flag_lang1);
        ImageView flag2 = (ImageView)convertView.findViewById(R.id.flag_lang2);
        TextView flashcardName = (TextView)convertView.findViewById(R.id.flashcardLabel);

        flashcardName.setText(flashcard.getName());
        setFlagByLanguage(flashcard.getLangFrom(), flag1);
        setFlagByLanguage(flashcard.getLangTo(), flag2);
    }

    private void appendTimeCreated(View convertView, Flashcard flashcard) {
        TextView timeCreated = (TextView)convertView.findViewById(R.id.timeCreated);
        Date date = new Date(Long.valueOf(flashcard.getTimeCreated()) * 1000);
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);
        timeCreated.setText(getContext().getResources().getString(R.string.timeCreated, formatter.format((date))));
    }

    private void setFlagByLanguage(String language, ImageView flagImageView) {
        switch(language){
            case "pl":
                flagImageView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.poland_icon));
                break;
            case "en":
                flagImageView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.united_kingdom_icon));
                break;
            case "es":
                flagImageView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.spain_icon));
                break;
            case "fr":
                flagImageView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.france_icon));
                break;
            case "de":
                flagImageView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.germany_icon));
                break;
            case "it":
                flagImageView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.italy_icon));
                break;
            default:
                flagImageView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.unknown_flag));
                break;
        }
    }
}
