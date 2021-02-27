package com.aliucord.installer;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomArrayAdapter extends ArrayAdapter<SpannableString> {
    private final List<Item> items;
    public CustomArrayAdapter(Context context, List<Item> items) {
        super(context, android.R.layout.select_dialog_item);

        ForegroundColorSpan redSpan = new ForegroundColorSpan(0xfff44336);
        ForegroundColorSpan greenSpan = new ForegroundColorSpan(0xff4caf50);
        for (Item item : items) {
            final String finalString = item.label + "\n(ver. " + item.version + ")";
            SpannableString spannable = new SpannableString(finalString);
            if (item.markVersionAsIncompatible) spannable.setSpan(redSpan, item.label.length(), finalString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            else spannable.setSpan(greenSpan, item.label.length(), finalString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            add(spannable);
        }

        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textView = view.findViewById(android.R.id.text1);
        Context context = getContext();

        textView.setTextSize(17);
        textView.setCompoundDrawablesWithIntrinsicBounds(items.get(position).icon, null, null, null);
        textView.setCompoundDrawablePadding(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics()));
        textView.setPadding(15, 7, 15, 0);
        return view;
    }
}
