/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.settings;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.text.*;
import android.text.style.ClickableSpan;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.aliucord.*;
import com.aliucord.entities.CorePlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.fragments.ConfirmDialog;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.utils.*;
import com.aliucord.views.Button;
import com.aliucord.views.*;
import com.aliucord.widgets.PluginCard;
import com.discord.app.AppBottomSheet;
import com.discord.app.AppFragment;
import com.discord.widgets.user.usersheet.WidgetUserSheet;
import com.lytefast.flexinput.R;

import java.io.File;
import java.util.*;

import kotlin.comparisons.ComparisonsKt;

public class Plugins extends SettingsPage {
    public static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> implements Filterable {
        public static final class ViewHolder extends RecyclerView.ViewHolder {
            private final Adapter adapter;
            public final PluginCard card;

            public ViewHolder(Adapter adapter, PluginCard card) {
                super(card);
                this.adapter = adapter;
                this.card = card;

                card.repoButton.setOnClickListener(this::onGithubClick);
                card.changeLogButton.setOnClickListener(this::onChangeLogClick);
                card.uninstallButton.setOnClickListener(this::onUninstallClick);
                card.switchHeader.setOnCheckedListener(this::onToggleClick);
                card.settingsButton.setOnClickListener(this::onSettingsClick);
            }

            public void onGithubClick(View view) {
                adapter.onGithubClick(getAdapterPosition());
            }

            public void onChangeLogClick(View view) {
                adapter.onChangeLogClick(getAdapterPosition());
            }

            public void onSettingsClick(View view) {
                try {
                    adapter.onSettingsClick(getAdapterPosition());
                } catch (Throwable th) {
                    PluginManager.logger.errorToast("Failed to launch plugin settings", th);
                }
            }

            public void onToggleClick(boolean checked) {
                adapter.onToggleClick(this, checked, getAdapterPosition());
            }

            public void onUninstallClick(View view) {
                adapter.onUninstallClick(getAdapterPosition());
            }
        }

        private final AppFragment fragment;
        private final Context ctx;
        private final List<Plugin> originalData;
        private List<Plugin> data;
        public boolean showBuiltIn = false;

        @SuppressWarnings("unchecked")
        public Adapter(AppFragment fragment, Collection<Plugin> plugins) {
            super();

            this.fragment = fragment;
            ctx = fragment.requireContext();

            this.originalData = new ArrayList<>(plugins);
            originalData.removeIf(p -> p instanceof CorePlugin && ((CorePlugin)p).isHidden());
            originalData.sort(ComparisonsKt.compareBy(
                p -> p instanceof CorePlugin, // coreplugins last
                Plugin::getName // Natural order by title
            ));

            data = CollectionUtils.filter(originalData, Adapter::filterCorePlugins);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(this, new PluginCard(ctx));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Plugin p = data.get(position);
            Plugin.Manifest manifest = p.getManifest();

            boolean enabled = PluginManager.isPluginEnabled(p.getName());
            holder.card.switchHeader.setChecked(enabled);
            holder.card.switchHeader.setButtonVisibility(!(p instanceof CorePlugin) || !((CorePlugin) p).isRequired());
            holder.card.descriptionView.setText(MDUtils.render(p.getManifest().description));
            holder.card.settingsButton.setVisibility(p.settingsTab != null ? View.VISIBLE : View.GONE);
            holder.card.settingsButton.setEnabled(enabled);
            holder.card.uninstallButton.setVisibility(p.__filename != null ? View.VISIBLE : View.GONE);
            holder.card.repoButton.setVisibility(p.getManifest().updateUrl != null ? View.VISIBLE : View.GONE);
            holder.card.changeLogButton.setVisibility(p.getManifest().changelog != null ? View.VISIBLE : View.GONE);

            String title = p instanceof CorePlugin
                ? String.format("%s [BUILT-IN]", p.getName())
                : String.format("%s v%s by %s", p.getName(), manifest.version, TextUtils.join(", ", manifest.authors));
            SpannableString spannableTitle = new SpannableString(title);
            for (Plugin.Manifest.Author author : manifest.authors) {
                if (author.id < 1 || !author.hyperlink) continue;
                int i = title.indexOf(author.name, p.getName().length() + 2 + manifest.version.length() + 3);
                spannableTitle.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        WidgetUserSheet.Companion.show(author.id, fragment.getParentFragmentManager());
                    }
                }, i, i + author.name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            holder.card.titleView.setText(spannableTitle);
        }

        private final Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Plugin> resultsList;
                if (constraint == null || constraint.equals("")) {
                    if (showBuiltIn) resultsList = originalData;
                    else resultsList = CollectionUtils.filter(originalData, Adapter::filterCorePlugins);
                } else {
                    String search = constraint.toString().toLowerCase().trim();
                    resultsList = CollectionUtils.filter(originalData, p -> {
                        if (!showBuiltIn && p instanceof CorePlugin) return false;
                        if (p.getName().toLowerCase().contains(search)) return true;
                        Plugin.Manifest manifest = p.getManifest();
                        if (manifest.description.toLowerCase().contains(search)) return true;
                        for (Plugin.Manifest.Author author : manifest.authors)
                            if (author.name.toLowerCase().contains(search)) return true;
                        return false;
                    });
                }
                FilterResults results = new FilterResults();
                results.values = resultsList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                @SuppressWarnings("unchecked")
                List<Plugin> res = (List<Plugin>) results.values;
                var diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return getItemCount();
                    }
                    @Override
                    public int getNewListSize() {
                        return res.size();
                    }
                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        return data.get(oldItemPosition).getName().equals(res.get(newItemPosition).getName());
                    }
                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        return true;
                    }
                }, false);
                data = res;
                diff.dispatchUpdatesTo(Adapter.this);
            }
        };

        @Override
        public Filter getFilter() {
            return filter;
        }

        private String getGithubUrl(Plugin plugin) {
            return plugin
                .getManifest().updateUrl
                .replace("raw.githubusercontent.com", "github.com")
                .replaceFirst("/builds.*", "");
        }

        public void onGithubClick(int position) {
            Utils.launchUrl(getGithubUrl(data.get(position)));
        }

        public void onChangeLogClick(int position) {
            Plugin p = data.get(position);
            Plugin.Manifest manifest = p.getManifest();
            if (manifest.changelog != null) {
                String url = getGithubUrl(p);
                ChangelogUtils.show(ctx, p.getName() + " v" + manifest.version, manifest.changelogMedia, manifest.changelog, new ChangelogUtils.FooterAction(R.e.ic_account_github_white_24dp, url));
            }
        }

        public void onSettingsClick(int position) throws Throwable {
            Plugin p = data.get(position);
            if (p.settingsTab.type == Plugin.SettingsTab.Type.PAGE && p.settingsTab.page != null) {
                Fragment page = p.settingsTab.args != null
                    ? ReflectUtils.invokeConstructorWithArgs(p.settingsTab.page, p.settingsTab.args)
                    : p.settingsTab.page.newInstance();
                Utils.openPageWithProxy(ctx, page);
            } else if (p.settingsTab.type == Plugin.SettingsTab.Type.BOTTOM_SHEET && p.settingsTab.bottomSheet != null) {
                AppBottomSheet sheet = p.settingsTab.args != null
                    ? ReflectUtils.invokeConstructorWithArgs(p.settingsTab.bottomSheet, p.settingsTab.args)
                    : p.settingsTab.bottomSheet.newInstance();

                sheet.show(fragment.getParentFragmentManager(), p.getName() + "Settings");
            }
        }

        public void onToggleClick(ViewHolder holder, boolean state, int position) {
            Plugin p = data.get(position);
            PluginManager.togglePlugin(p.getName());
            holder.card.settingsButton.setEnabled(state);
            if (p.requiresRestart()) Utils.promptRestart();
        }

        public void onUninstallClick(int position) {
            Plugin p = data.get(position);
            ConfirmDialog dialog = new ConfirmDialog()
                .setIsDangerous(true)
                .setTitle("Delete " + p.getName())
                .setDescription("Are you sure you want to delete this plugin? This action cannot be undone.");
            dialog.setOnOkListener(e -> {
                File pluginFile = new File(Constants.BASE_PATH + "/plugins/" + p.__filename + ".zip");
                if (pluginFile.exists() && !pluginFile.delete()) {
                    PluginManager.logger.errorToast("Failed to delete plugin " + p.getName(), null);
                    return;
                }

                PluginManager.stopPlugin(p.getName());
                PluginManager.plugins.remove(p.getName());
                PluginManager.logger.infoToast("Successfully deleted " + p.getName());

                dialog.dismiss();
                data.remove(position);
                if (originalData != data) originalData.remove(p);
                notifyItemRemoved(position);

                if (p.requiresRestart()) Utils.promptRestart();
            });

            dialog.show(fragment.getParentFragmentManager(), "Confirm Plugin Uninstall");
        }

        public static boolean filterCorePlugins(Plugin p) {
            return !(p instanceof CorePlugin);
        }
    }

    @Override
    public void onViewBound(View view) {
        super.onViewBound(view);
        setActionBarTitle("Plugins");
        setActionBarSubtitle(PluginManager.getPluginsInfo());
        removeScrollView();

        var context = view.getContext();
        int padding = DimenUtils.getDefaultPadding();

        addHeaderButton("Open Plugins Folder", R.e.ic_open_in_new_white_24dp, item -> {
            File dir = new File(Constants.PLUGINS_PATH);
            if (!dir.exists() && !dir.mkdir()) {
                Utils.showToast("Failed to create plugins directory!", true);
                return true;
            }
            Utils.launchFileExplorer(dir);
            return true;
        });

        if (!PluginManager.failedToLoad.isEmpty()) {
            var failedPluginsView = new Button(context);
            failedPluginsView.setText("Plugin Errors");
            failedPluginsView.setOnClickListener(v -> Utils.openPage(context, FailedPluginsPage.class));
            addView(failedPluginsView);
            addView(new Divider(context));
        }

        TextInput input = new TextInput(context);
        input.setHint(context.getString(R.h.search));

        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        Adapter adapter = new Adapter(this, PluginManager.plugins.values());
        recyclerView.setAdapter(adapter);
        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.setTint(Color.TRANSPARENT);
        shape.setIntrinsicHeight(padding);
        DividerItemDecoration decoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        decoration.setDrawable(shape);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setPadding(0, padding, 0, 0);

        addView(input);
        addView(recyclerView);

        EditText editText = input.getEditText();
        editText.setMaxLines(1);
        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                adapter.getFilter().filter(s);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });

        getHeaderBar().getMenu()
            .add("Show built-in")
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
            .setCheckable(true)
            .setOnMenuItemClickListener(item -> {
                var show = !adapter.showBuiltIn;
                adapter.showBuiltIn = show;
                adapter.getFilter().filter(editText.getText());
                item.setChecked(show);
                return true;
            });
    }
}
