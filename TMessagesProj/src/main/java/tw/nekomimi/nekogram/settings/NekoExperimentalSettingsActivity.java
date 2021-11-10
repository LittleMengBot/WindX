package tw.nekomimi.nekogram.settings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UndoView;

import java.util.ArrayList;
import kotlin.Unit;

import tw.nekomimi.nekogram.PopupBuilder;
import tw.nekomimi.nkmr.NekomuraConfig;
import tw.nekomimi.nkmr.Cells;
import tw.nekomimi.nkmr.Cells.*;

@SuppressLint("RtlHardcoded")
public class NekoExperimentalSettingsActivity extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter listAdapter;
    private AnimatorSet animatorSet;

    private boolean sensitiveCanChange = false;
    private boolean sensitiveEnabled = false;

    private ArrayList<NekomuraTGCell> rows = new ArrayList<>();
    private Cells nkmrCells = new Cells(this, rows);

    private final NekomuraTGCell header1 = addNekomuraTGCell(nkmrCells.new NekomuraTGHeader(LocaleController.getString("Experiment")));
    private final NekomuraTGCell smoothKeyboardRow = addNekomuraTGCell(nkmrCells.new NekomuraTGTextCheck(NekomuraConfig.smoothKeyboard));
    private final NekomuraTGCell increaseVoiceMessageQualityRow = addNekomuraTGCell(nkmrCells.new NekomuraTGTextCheck(NekomuraConfig.increaseVoiceMessageQuality));
    private final NekomuraTGCell mediaPreviewRow = addNekomuraTGCell(nkmrCells.new NekomuraTGTextCheck(NekomuraConfig.mediaPreview));
    private final NekomuraTGCell proxyAutoSwitchRow = addNekomuraTGCell(nkmrCells.new NekomuraTGTextCheck(NekomuraConfig.proxyAutoSwitch));
    private final NekomuraTGCell disableFilteringRow = addNekomuraTGCell(nkmrCells.new NekomuraTGCustom(Cells.ITEM_TYPE_TEXT_CHECK, true));
    //    private final NekomuraTGCell ignoreContentRestrictionsRow = addNekomuraTGCell(nkmrCells.new NekomuraTGTextCheck(NekomuraConfig.ignoreContentRestrictions, LocaleController.getString("IgnoreContentRestrictionsNotice")));
    private final NekomuraTGCell unlimitedFavedStickersRow = addNekomuraTGCell(nkmrCells.new NekomuraTGTextCheck(NekomuraConfig.unlimitedFavedStickers, LocaleController.getString("UnlimitedFavoredStickersAbout")));
    private final NekomuraTGCell unlimitedPinnedDialogsRow = addNekomuraTGCell(nkmrCells.new NekomuraTGTextCheck(NekomuraConfig.unlimitedPinnedDialogs, LocaleController.getString("UnlimitedPinnedDialogsAbout")));
    private final NekomuraTGCell enableStickerPinRow = addNekomuraTGCell(nkmrCells.new NekomuraTGTextCheck(NekomuraConfig.enableStickerPin, LocaleController.getString("EnableStickerPinAbout")));
    private final NekomuraTGCell useMediaStreamInVoipRow = addNekomuraTGCell(nkmrCells.new NekomuraTGTextCheck(NekomuraConfig.useMediaStreamInVoip));
    private final NekomuraTGCell customAudioBitrateRow = addNekomuraTGCell(nkmrCells.new NekomuraTGCustom(Cells.ITEM_TYPE_TEXT_SETTINGS_CELL, true));
    private final NekomuraTGCell divider0 = addNekomuraTGCell(nkmrCells.new NekomuraTGDivider());

    private UndoView tooltip;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        updateRows();

        return true;
    }

    @SuppressLint("NewApi")
    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("Experiment", R.string.Experiment));

        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);

        // Fragment: Set OnClick Callbacks
        listView.setOnItemClickListener((view, position, x, y) -> {
            NekomuraTGCell a = rows.get(position);
            if (a instanceof NekomuraTGTextCheck) {
                ((NekomuraTGTextCheck) a).onClick((TextCheckCell) view);
            } else if (a instanceof NekomuraTGSelectBox) {
                ((NekomuraTGSelectBox) a).onClick();
            } else if (a instanceof NekomuraTGTextInput) {
                ((NekomuraTGTextInput) a).onClick();
            } else if (a instanceof NekomuraTGTextDetail) {
                RecyclerListView.OnItemClickListener o = ((NekomuraTGTextDetail) a).onItemClickListener;
                if (o != null) {
                    try {
                        o.onItemClick(view, position);
                    } catch (Exception e) {
                    }
                }
            } else if (a instanceof NekomuraTGCustom) { // Custom onclick
                if (position == rows.indexOf(disableFilteringRow)) {
                    sensitiveEnabled = !sensitiveEnabled;
                    TLRPC.TL_account_setContentSettings req = new TLRPC.TL_account_setContentSettings();
                    req.sensitive_enabled = sensitiveEnabled;
                    AlertDialog progressDialog = new AlertDialog(getParentActivity(), 3);
                    progressDialog.show();
                    getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                        progressDialog.dismiss();
                        if (error == null) {
                            if (response instanceof TLRPC.TL_boolTrue && view instanceof TextCheckCell) {
                                ((TextCheckCell) view).setChecked(sensitiveEnabled);
                            }
                        } else {
                            AndroidUtilities.runOnUIThread(() -> AlertsCreator.processError(currentAccount, error, this, req));
                        }
                    }));
                } else if (position == rows.indexOf(customAudioBitrateRow)) {
                    PopupBuilder builder = new PopupBuilder(view);
                    builder.setItems(new String[]{
                            "32 (" + LocaleController.getString("Default", R.string.Default) + ")",
                            "64",
                            "128",
                            "192",
                            "256",
                            "320"
                    }, (i, __) -> {
                        switch (i) {
                            case 0:
                                NekomuraConfig.customAudioBitrate.setConfigInt(32);
                                break;
                            case 1:
                                NekomuraConfig.customAudioBitrate.setConfigInt(64);
                                break;
                            case 2:
                                NekomuraConfig.customAudioBitrate.setConfigInt(128);
                                break;
                            case 3:
                                NekomuraConfig.customAudioBitrate.setConfigInt(192);
                                break;
                            case 4:
                                NekomuraConfig.customAudioBitrate.setConfigInt(256);
                                break;
                            case 5:
                                NekomuraConfig.customAudioBitrate.setConfigInt(320);
                                break;
                        }
                        listAdapter.notifyItemChanged(position);
                        return Unit.INSTANCE;
                    });
                    builder.show();
                }
            }
        });

        // Cells: Set OnSettingChanged Callbacks
        nkmrCells.callBackSettingsChanged = (key, newValue) -> {
            if (key.equals(NekomuraConfig.smoothKeyboard.getKey())) {
                if ((boolean) newValue != NekomuraConfig.smoothKeyboard.Bool()) {
                    SharedConfig.toggleSmoothKeyboard();
                }
                if (SharedConfig.smoothKeyboard && getParentActivity() != null) {
                    getParentActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                }
                if (SharedConfig.smoothKeyboard) {
                    tooltip.setInfoText(AndroidUtilities.replaceTags(LocaleController.formatString("BetaWarning", R.string.BetaWarning)));
                    tooltip.showWithAction(0, UndoView.ACTION_CACHE_WAS_CLEARED, null, null);
                }
            } else if (key.equals(NekomuraConfig.mediaPreview.getKey())) {
                if (NekomuraConfig.mediaPreview.Bool()) {
                    tooltip.setInfoText(AndroidUtilities.replaceTags(LocaleController.formatString("BetaWarning", R.string.BetaWarning)));
                    tooltip.showWithAction(0, UndoView.ACTION_CACHE_WAS_CLEARED, null, null);
                }
            } else if (key.equals(NekomuraConfig.enableStickerPin.getKey())) {
                if (NekomuraConfig.mediaPreview.Bool()) {
                    tooltip.setInfoText(AndroidUtilities.replaceTags(LocaleController.formatString("EnableStickerPinTip", R.string.EnableStickerPinTip)));
                    tooltip.showWithAction(0, UndoView.ACTION_CACHE_WAS_CLEARED, null, null);
                }
            }

        };

        //Cells: Set ListAdapter
        nkmrCells.setListAdapter(listView, listAdapter);

        tooltip = new UndoView(context);
        frameLayout.addView(tooltip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.LEFT, 8, 0, 8, 8));

        return fragmentView;
    }

    public NekomuraTGCell addNekomuraTGCell(NekomuraTGCell a) {
        rows.add(a);
        return a;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            checkSensitive();
            listAdapter.notifyDataSetChanged();
        }
    }

    private void updateRows() {
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{EmptyCell.class, TextSettingsCell.class, TextCheckCell.class, HeaderCell.class, TextDetailSettingsCell.class, NotificationsCheckCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray));

        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_avatar_actionBarIconBlue));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_avatar_actionBarSelectorBlue));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, null, null, null, null, Theme.key_actionBarDefaultSubmenuBackground));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, null, null, null, null, Theme.key_actionBarDefaultSubmenuItem));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));

        return themeDescriptions;
    }

    private void checkSensitive() {
        TLRPC.TL_account_getContentSettings req = new TLRPC.TL_account_getContentSettings();
        getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null) {
                TLRPC.TL_account_contentSettings settings = (TLRPC.TL_account_contentSettings) response;
                sensitiveEnabled = settings.sensitive_enabled;
                sensitiveCanChange = settings.sensitive_can_change;
                int count = listView.getChildCount();
                ArrayList<Animator> animators = new ArrayList<>();
                for (int a = 0; a < count; a++) {
                    View child = listView.getChildAt(a);
                    RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.getChildViewHolder(child);
                    int position = holder.getAdapterPosition();
                    if (position == rows.indexOf(disableFilteringRow)) {
                        TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                        checkCell.setChecked(sensitiveEnabled);
                        checkCell.setEnabled(sensitiveCanChange, animators);
                        if (sensitiveCanChange) {
                            if (!animators.isEmpty()) {
                                if (animatorSet != null) {
                                    animatorSet.cancel();
                                }
                                animatorSet = new AnimatorSet();
                                animatorSet.playTogether(animators);
                                animatorSet.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animator) {
                                        if (animator.equals(animatorSet)) {
                                            animatorSet = null;
                                        }
                                    }
                                });
                                animatorSet.setDuration(150);
                                animatorSet.start();
                            }
                        }
                    }
                }
            } else {
                AndroidUtilities.runOnUIThread(() -> AlertsCreator.processError(currentAccount, error, this, req));
            }
        }));
    }

    //impl ListAdapter
    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();
            NekomuraTGCell a = rows.get(position);
            if (a != null) {
                return a.isEnabled();
            }
            return true;
        }

        @Override
        public int getItemViewType(int position) {
            NekomuraTGCell a = rows.get(position);
            if (a != null) {
                return a.getType();
            }
            return Cells.ITEM_TYPE_TEXT_DETAIL;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            NekomuraTGCell a = rows.get(position);
            if (a != null) {
                if (a instanceof NekomuraTGCustom) {
                    // Custom binds
                    if (holder.itemView instanceof TextCheckCell) {
                        TextCheckCell textCell = (TextCheckCell) holder.itemView;
                        textCell.setEnabled(true, null);
                        if (position == rows.indexOf(disableFilteringRow)) {
                            textCell.setTextAndValueAndCheck(LocaleController.getString("SensitiveDisableFiltering", R.string.SensitiveDisableFiltering), LocaleController.getString("SensitiveAbout", R.string.SensitiveAbout), sensitiveEnabled, true, true);
                            textCell.setEnabled(sensitiveCanChange, null);
                        }
                    } else if (holder.itemView instanceof TextSettingsCell) {
                        TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                        textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                        if (position == rows.indexOf(customAudioBitrateRow)) {
                            String value = String.valueOf(NekomuraConfig.customAudioBitrate.Int()) + "kbps";
                            if (NekomuraConfig.customAudioBitrate.Int() == 32)
                                value += " (" + LocaleController.getString("Default", R.string.Default) + ")";
                            textCell.setTextAndValue(LocaleController.getString("customGroupVoipAudioBitrate", R.string.customGroupVoipAudioBitrate), value, false);
                        }
                    }
                } else {
                    // Default binds
                    a.onBindViewHolder(holder);
                    if (position == rows.indexOf(smoothKeyboardRow) && AndroidUtilities.isTablet()) {
                        holder.itemView.setVisibility(View.GONE);
                    }
                }
            }
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case Cells.ITEM_TYPE_DIVIDER:
                    view = new ShadowSectionCell(mContext);
                    break;
                case Cells.ITEM_TYPE_TEXT_SETTINGS_CELL:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case Cells.ITEM_TYPE_TEXT_CHECK:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case Cells.ITEM_TYPE_HEADER:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case Cells.ITEM_TYPE_TEXT_DETAIL:
                    view = new TextDetailSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case Cells.ITEM_TYPE_TEXT:
                    view = new TextInfoPrivacyCell(mContext);
                    // view.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
            }
            //noinspection ConstantConditions
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }
    }
}