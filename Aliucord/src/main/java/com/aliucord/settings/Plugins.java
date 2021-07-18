/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.*;
import android.text.style.ClickableSpan;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.*;

import com.aliucord.*;
import com.aliucord.entities.Plugin;
import com.aliucord.fragments.ConfirmDialog;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.utils.ReflectUtils;
import com.aliucord.views.TextInput;
import com.aliucord.views.ToolbarButton;
import com.discord.app.AppBottomSheet;
import com.discord.app.AppFragment;
import com.discord.utilities.mg_recycler.*;
import com.discord.widgets.user.usersheet.WidgetUserSheet;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.lytefast.flexinput.R$d;
import com.lytefast.flexinput.R$g;

import java.io.File;
import java.util.*;

public class Plugins extends SettingsPage {
    private static final int uniqueId = View.generateViewId();

    private static final int resId = Utils.getResId("widget_settings_item_connected_account", "layout");
    private static final int disconnectBtnId = Utils.getResId("connected_account_disconnect", "id");
    private static final int accountImgId = Utils.getResId("connected_account_img", "id");
    private static final int accountNameId = Utils.getResId("connected_account_name", "id");
    private static final int displaySwitchId = Utils.getResId("display_switch", "id");
    private static final int displayAsStatusSwitchId = Utils.getResId("display_activity_switch", "id");
    private static final int syncFriendsSwitchId = Utils.getResId("sync_friends_switch", "id");
    private static final int extraInfoId = Utils.getResId("extra_info", "id");

    public static class Adapter extends MGRecyclerAdapterSimple<Adapter.PluginItem> implements Filterable {
        public static final class PluginItem implements MGRecyclerDataPayload {
            public final Plugin plugin;
            public PluginItem(Plugin plugin) {
                this.plugin = plugin;
            }

            @Override
            public String getKey() { return plugin.name; }

            @Override
            public int getType() { return 0; }
        }

        public static final class ViewHolder extends MGRecyclerViewHolder<Adapter, PluginItem> {
            private final ImageView disconnectBtn;
            private final ImageView accountImg;
            private final TextView accountName;
            private final SwitchMaterial displaySwitch;
            private final SwitchMaterial displayAsStatusSwitch;
            private final SwitchMaterial syncFriendsSwitch;
            private final TextView extraInfo;
            private final TextView label;

            public ViewHolder(Adapter adapter) {
                super(resId, adapter);
                View view = this.itemView;
                disconnectBtn = view.findViewById(disconnectBtnId);
                accountImg = view.findViewById(accountImgId);
                accountName = view.findViewById(accountNameId);
                displaySwitch = view.findViewById(displaySwitchId);
                displayAsStatusSwitch = view.findViewById(displayAsStatusSwitchId);
                syncFriendsSwitch = view.findViewById(syncFriendsSwitchId);
                extraInfo = view.findViewById(extraInfoId);
                // LinearLayout integrationsRoot = view.findViewById(Utils.getResId("integrations_root", "id"));
                label = view.findViewById(Utils.getResId("label", "id"));
            }

            @SuppressLint("SetTextI18n")
            public void onConfigure(int idx, PluginItem pluginItem) {
                super.onConfigure(idx, pluginItem);

                displayAsStatusSwitch.setVisibility(View.GONE);
                syncFriendsSwitch.setVisibility(View.GONE);

                Context ctx = disconnectBtn.getContext();
                Plugin p = pluginItem.plugin;
                Plugin.Manifest manifest = p.getManifest();
                String name = p.name;
                boolean isEnabled = PluginManager.isPluginEnabled(name);

                accountImg.setImageDrawable(ContextCompat.getDrawable(ctx, R$d.ic_github_white));
                accountImg.setOnClickListener(e -> {
                    String url = manifest.updateUrl.replace("raw.githubusercontent.com", "github.com").replaceFirst("/builds.*", "");
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    adapter.fragment.startActivity(intent);
                });

                String title = String.format("%s v%s by %s", name, manifest.version, TextUtils.join(", ", manifest.authors));
                SpannableString spannableTitle = new SpannableString(title);
                for (Plugin.Manifest.Author author : manifest.authors) {
                    if (author.id < 1) continue;
                    int i = title.indexOf(author.name, name.length() + 2 + manifest.version.length() + 3);
                    spannableTitle.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            WidgetUserSheet.Companion.show(author.id, adapter.fragment.getParentFragmentManager());
                        }
                    }, i, i + author.name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                accountName.setText(spannableTitle);

                disconnectBtn.setOnClickListener(e -> {
                    ConfirmDialog confirm = new ConfirmDialog()
                            .setTitle("Delete " + name)
                            .setDescription("Are you sure you want to delete this plugin?")
                            .setIsDangerous(true);
                    confirm.setOnOkListener(_e -> {
                                File pluginFile = new File(Constants.BASE_PATH + "/plugins/" + p.__filename + ".zip");
                                if (pluginFile.exists() && !pluginFile.delete()) {
                                    PluginManager.logger.error(ctx, "Failed to delete plugin " + p.name, null);
                                    return;
                                }

                                PluginManager.stopPlugin(name);
                                PluginManager.plugins.remove(name);
                                PluginManager.logger.info(ctx, "Successfully deleted " + p.name);

                                this.itemView.setVisibility(View.GONE);

                                int position = getAdapterPosition();
                                if (position != RecyclerView.NO_POSITION) {
                                    adapter.getInternalData().remove(position);
                                    access$dispatchUpdates(adapter, null, adapter.items, adapter.getInternalData());
                                }

                                adapter.items.remove(pluginItem);

                                confirm.dismiss();
                            });
                    confirm.show(adapter.fragment.getParentFragmentManager(), "Confirm Delete");
                });

                displaySwitch.setChecked(isEnabled);
                displaySwitch.setText("Enabled");
                displaySwitch.setOnClickListener(e -> {
                    PluginManager.togglePlugin(name);
                    if (p.settingsTab != null) extraInfo.setEnabled(extraInfo.isEnabled());
                });

                label.setAllCaps(false);
                label.setText(manifest.description);

                if (p.settingsTab != null) {
                    extraInfo.setText("Launch Settings");
                    extraInfo.setVisibility(View.VISIBLE);
                    extraInfo.setEnabled(isEnabled);
                    if (p.settingsTab.type == Plugin.SettingsTab.Type.PAGE && p.settingsTab.page != null)
                        extraInfo.setOnClickListener(v -> {
                            try {
                                Utils.openPageWithProxy(
                                        v.getContext(),
                                        p.settingsTab.args != null
                                                ? ReflectUtils.invokeConstructorWithArgs(p.settingsTab.page, p.settingsTab.args)
                                                : p.settingsTab.page.newInstance());
                            } catch (Throwable e) { PluginManager.logger.error(ctx, "Failed to open settings page for " + p.name, e); }
                        });
                    else if (p.settingsTab.type == Plugin.SettingsTab.Type.BOTTOM_SHEET && p.settingsTab.bottomSheet != null)
                        extraInfo.setOnClickListener(v -> {
                            try {
                                AppBottomSheet sheet = p.settingsTab.args != null
                                        ? ReflectUtils.invokeConstructorWithArgs(p.settingsTab.bottomSheet, p.settingsTab.args)
                                        : p.settingsTab.bottomSheet.newInstance();

                                sheet.show(adapter.fragment.getParentFragmentManager(), name + "Settings");
                            } catch (Throwable e) { PluginManager.logger.error(ctx, "Failed to open settings page for " + p.name, e); }
                        });
                }
            }
        }

        public final AppFragment fragment;
        public final List<PluginItem> items;

        private final Adapter _this = this;

        public Adapter(AppFragment fragment, RecyclerView rv, Collection<Plugin> plugins) {
            super(rv, false);

            this.fragment = fragment;
            this.items = CollectionUtils.map(plugins, PluginItem::new);
            items.sort(Comparator.comparing(p -> p.plugin.name));

            setData(new ArrayList<>(items));
        }

        @NonNull
        @Override
        public MGRecyclerViewHolder<Adapter, PluginItem> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(this);
        }

        private final Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<PluginItem> resultsList;
                if (constraint == null || constraint.equals("")) resultsList = items;
                else {
                    String search = constraint.toString().toLowerCase().trim();
                    resultsList = CollectionUtils.filter(items, p -> p.plugin.name.toLowerCase().contains(search));
                }
                FilterResults results = new FilterResults();
                results.values = resultsList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                access$dispatchUpdates(_this, null, _this.getInternalData(), (List<PluginItem>) results.values);
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

        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        Adapter adapter = new Adapter(this, recyclerView, PluginManager.plugins.values());
        recyclerView.setAdapter(adapter);
        addView(recyclerView);

        if (editText != null) editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                adapter.getFilter().filter(s);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }
}
