/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.settings;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliucord.Constants;
import com.aliucord.Logger;
import com.aliucord.Main;
import com.aliucord.PluginManager;
import com.aliucord.Utils;
import com.aliucord.entities.Plugin;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.views.TextInput;
import com.aliucord.widgets.PluginCard;
import com.discord.utilities.color.ColorCompat;
import com.google.android.material.appbar.AppBarLayout;
import com.lytefast.flexinput.R$b;
import com.lytefast.flexinput.R$d;
import com.lytefast.flexinput.R$g;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Plugins extends SettingsPage {
    public static class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
        public static class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(@NonNull View itemView) { super(itemView); }
        }

        private final List<PluginCard> items;
        private List<PluginCard> itemsFiltered;
        private final RecyclerView recyclerView;
        public Adapter(RecyclerView rv, List<PluginCard> items) {
            this(rv, items, new ArrayList<>(items));
        }
        public Adapter(RecyclerView rv, List<PluginCard> items, List<PluginCard> filtered) {
            this.items = items;
            itemsFiltered = filtered;
            recyclerView = rv;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(new LinearLayout(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            PluginCard card = itemsFiltered.get(position);
            if (card.getParent() != null) ((ViewGroup) card.getParent()).removeView(card);
            ((LinearLayout) holder.itemView).addView(card);
        }

        @Override
        public int getItemCount() {
            return itemsFiltered.size();
        }

        private final Adapter _this = this;
        private final Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<PluginCard> resultsList;
                if (constraint == null || constraint.equals("")) resultsList = items;
                else {
                    resultsList = new ArrayList<>();
                    for (PluginCard card : items) {
                        if (
                            card.titleView != null &&
                                card.titleView.getText().toString().toLowerCase().contains(constraint.toString().toLowerCase().trim())
                        ) resultsList.add(card);
                    }
                }
                FilterResults results = new FilterResults();
                results.values = resultsList;
                return results;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                itemsFiltered = (List<PluginCard>) results.values;
                recyclerView.setAdapter(_this);
            }
        };
        @Override
        public Filter getFilter() {
            return filter;
        }
    }

    @Override
    @SuppressLint("SetTextI18n")
    public void onViewBound(View view) {
        super.onViewBound(view);
        //noinspection ResultOfMethodCallIgnored
        setActionBarTitle("Plugins");

        Context context = requireContext();
        int padding = Utils.getDefaultPadding();
        int p = padding / 2;

        File folder = new File(Constants.BASE_PATH, "plugins");

        LinearLayout v = (LinearLayout) ((NestedScrollView) ((CoordinatorLayout)
            view).getChildAt(1)).getChildAt(0);
        v.setPadding(padding, padding, padding, padding);

        Toolbar toolbar = (Toolbar) ((AppBarLayout) ((CoordinatorLayout) view).getChildAt(0)).getChildAt(0);

        AppCompatImageButton pluginFolderBtn = new AppCompatImageButton(context);
        int ic1 = R$d.ic_open_in_new_white_24dp;

        Toolbar.LayoutParams pluginFolderParams = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
        pluginFolderParams.gravity = Gravity.END;
        pluginFolderParams.setMarginEnd(p);
        pluginFolderBtn.setLayoutParams(pluginFolderParams);
        pluginFolderBtn.setPadding(p, p, p, p);

        pluginFolderBtn.setBackgroundColor(Color.TRANSPARENT);

        Drawable pluginFolder = ContextCompat.getDrawable(context, ic1).mutate();
        pluginFolder.setAlpha(185);
        pluginFolderBtn.setImageDrawable(pluginFolder);

        pluginFolderBtn.setOnClickListener(e -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(String.valueOf(folder)), "resource/folder");
            startActivity(Intent.createChooser(intent, "Open folder"));
        });

        toolbar.addView(pluginFolderBtn);

        TextInput input = new TextInput(context);
        input.setHint(context.getString(R$g.search));
        EditText editText = input.getEditText();
        if (editText != null) editText.setMaxLines(1);
        v.addView(input);

        Utils.threadPool.execute(() -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            List<PluginCard> list = new ArrayList<>();
            for (Map.Entry<String, Plugin> entry : PluginManager.plugins.entrySet()) try {
                list.add(new PluginCard(context, entry.getKey(), entry.getValue(), fragmentManager));
            } catch (Throwable e) { Main.logger.error("Exception while rendering plugin settings", e); }
            list.sort(Comparator.comparing(a -> a.pluginName));

            Utils.mainThread.post(() -> {
                RecyclerView recyclerView = new RecyclerView(context);
                recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
                Adapter adapter = new Adapter(recyclerView, list);
                recyclerView.setAdapter(adapter);
                ShapeDrawable shape = new ShapeDrawable(new RectShape());
                shape.setTint(Color.TRANSPARENT);
                shape.setIntrinsicHeight(padding);
                DividerItemDecoration decoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
                decoration.setDrawable(shape);
                recyclerView.addItemDecoration(decoration);
                recyclerView.setPadding(0, padding, 0, 0);

                v.addView(recyclerView);

                if (editText != null) editText.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        adapter.getFilter().filter(s);
                    }

                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}
                });
            });
        });
    }
}
