/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.*;

import com.aliucord.*;
import com.aliucord.entities.Plugin;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.views.TextInput;
import com.aliucord.views.ToolbarButton;
import com.aliucord.widgets.PluginCard;
import com.lytefast.flexinput.R$d;
import com.lytefast.flexinput.R$g;

import java.io.File;
import java.util.*;

public class Plugins extends SettingsPage {
    private static final int uniqueId = View.generateViewId();

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

        if (getHeaderBar().findViewById(uniqueId) == null) {
            ToolbarButton pluginFolderBtn = new ToolbarButton(context);
            pluginFolderBtn.setId(uniqueId);

            Toolbar.LayoutParams params = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.END;
            params.setMarginEnd(p);
            pluginFolderBtn.setLayoutParams(params);
            pluginFolderBtn.setPadding(p, p, p, p);

            //noinspection ConstantConditions
            Drawable pluginFolder = ContextCompat.getDrawable(context, R$d.ic_open_in_new_white_24dp).mutate();
            pluginFolder.setAlpha(185);
            pluginFolderBtn.setImageDrawable(pluginFolder);

            pluginFolderBtn.setOnClickListener(e -> {
                File dir = new File(Constants.PLUGINS_PATH);
                if (!dir.exists() && !dir.mkdir()) {
                    Utils.showToast(context, "Failed to create plugins directory!", true);
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(Constants.PLUGINS_PATH), "resource/folder");
                startActivity(Intent.createChooser(intent, "Open folder"));
            });

            addHeaderButton(pluginFolderBtn);
        }

        TextInput input = new TextInput(context);
        input.setHint(context.getString(R$g.search));
        EditText editText = input.getEditText();
        if (editText != null) editText.setMaxLines(1);
        addView(input);

        Utils.threadPool.execute(() -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            List<PluginCard> list = new ArrayList<>();
            for (Map.Entry<String, Plugin> entry : PluginManager.plugins.entrySet()) try {
                list.add(new PluginCard(context, entry.getKey(), entry.getValue(), fragmentManager, this));
            } catch (Throwable e) { PluginManager.logger.error("Exception while rendering plugin settings", e); }
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

                addView(recyclerView);

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
