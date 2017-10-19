package fiszki.xyz.fiszkiapp.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

import fiszki.xyz.fiszkiapp.utils.Pair;
import fiszki.xyz.fiszkiapp.R;

public class WordsAdapter extends BaseAdapter implements Filterable {

    private Context mContext;
    private WordsFilter wordsFilter;

    private ArrayList<Pair> words;
    private ArrayList<Pair> filteredWords;

    public WordsAdapter(Context context, ArrayList<Pair> words) {
        this.mContext = context;
        this.words = words;
        this.filteredWords = words;

        getFilter();
    }

    @Override
    public int getCount() {
        return this.filteredWords.size();
    }

    @Override
    public Object getItem(int position) {
        return this.filteredWords.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        ViewHolder viewHolder;

        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.flashcard_preview_row, parent, false);
            viewHolder.word1 = (TextView)convertView.findViewById(R.id.wordToTranslate);
            viewHolder.word2 = (TextView)convertView.findViewById(R.id.wordTranslation);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder)convertView.getTag();

        Pair p = (Pair)getItem(position);
        viewHolder.word1.setText(p.getLeftValue());
        viewHolder.word2.setText(p.getRightValue());

        return convertView;
    }

    @Override
    public Filter getFilter() {
        if(wordsFilter == null)
            wordsFilter = new WordsFilter();
        return wordsFilter;
    }

    private class ViewHolder{
        TextView word1;
        TextView word2;
    }

    private class WordsFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if(constraint != null && constraint.length() > 0){
                ArrayList<Pair> temp = new ArrayList<>();

                for(Pair hm : words)
                    if((hm.getLeftValue()).contains(constraint.toString())
                            || (hm.getRightValue()).contains(constraint.toString()))
                        temp.add(hm);

                filterResults.count = temp.size();
                filterResults.values = temp;
            } else{
                filterResults.count = words.size();
                filterResults.values = words;
            }

            return filterResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredWords = (ArrayList<Pair>)results.values;
            notifyDataSetChanged();
        }
    }
}
