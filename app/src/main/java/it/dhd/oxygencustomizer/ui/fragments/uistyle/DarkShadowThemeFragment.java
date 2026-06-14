package it.dhd.oxygencustomizer.ui.fragments.uistyle;

// fix
import static it.dhd.oxygencustomizer.utils.DarkShadowUtils.ACCENT1;
import static it.dhd.oxygencustomizer.utils.DarkShadowUtils.ACCENT2;
import static it.dhd.oxygencustomizer.utils.DarkShadowUtils.ACCENT3;
import static it.dhd.oxygencustomizer.utils.DarkShadowUtils.BACKGROUND;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OplusRecyclerView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentAppListBinding;
import it.dhd.oxygencustomizer.ui.activity.MainActivity;
import it.dhd.oxygencustomizer.ui.adapters.DarkShadowColorsAdapter;
import it.dhd.oxygencustomizer.ui.adapters.FooterWidgetAdapter;
import it.dhd.oxygencustomizer.ui.adapters.SectionTitleAdapter;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.ui.dialogs.LoadingDialog;
import it.dhd.oxygencustomizer.ui.events.ColorSelectedEvent;
import it.dhd.oxygencustomizer.ui.models.DarkShadowItem;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.ColorUtils;
import it.dhd.oxygencustomizer.utils.DarkShadowUtils;
import it.dhd.oxygencustomizer.utils.OCPreferences;
import it.dhd.oxygencustomizer.utils.overlay.FabricatedUtil;
import it.dhd.oxygencustomizer.utils.overlay.OverlayUtil;

public class DarkShadowThemeFragment extends BaseFragment {

    // ── Base theme overlays ────────────────────────────────────────────────────
    // DSTSUI escluso: causa bootloop al boot su OOS16.0.7 (overlay service lo applica prima dell'app)
    String[] overlays = new String[]{"DST", "DSTSTG"};

    // ── Colors adapter (Phase 2) ───────────────────────────────────────────────
    private DstColorsAdapter mColorsAdapter;

    // ── Preference keys ────────────────────────────────────────────────────────
    private static final String PREF_BG  = "DST_PRESET_BG";
    private static final String PREF_AC  = "DST_PRESET_AC";
    private static final String PREF_PIN = "DST_PRESET_PIN";
    private static final String PREF_VAL_CUSTOM = "Custom";

    // ── Background presets: background_dark from type1b_*.xml ─────────────────
    private static final String[] BG_NAMES = {
        "Black Amoled", "Blue Gray", "Blue Gray Dark", "Dark Blue", "Dark Gray",
        "Dark Green", "Dark Pink", "Dark Purple", "Dark Steel", "Deep Purple",
        "Eerie", "Green", "Grey", "Grey Night", "Light Grey", "Night", "Onyx",
        "Taupe", "Transparency Crazy Full", "Transparency Higher",
        "Transparency Lower", "Transparency Medium",
    };
    private static final int[] BG_COLORS = {
        0xFF000000, 0xFF212232, 0xFF1b2029, 0xFF0a1236, 0xFF202026,
        0xFF001413, 0xFF270520, 0xFF1D0021, 0xFF3b4250, 0xFF1C0839,
        0xFF161117, 0xFF192f2d, 0xFF2B2E37, 0xFF353535, 0xFF2f333b,
        0xFF363844, 0xFF2D232C, 0xFF0e0e0e, 0x00000000, 0x40000000,
        0x80000000, 0x61000000,
    };

    // ── Accent presets: accent_material_dark from type1a_*.xml ────────────────
    private static final String[] AC_NAMES = {
        "Blue", "Blue Android", "Blue Gray 300", "Blue Gray 400", "Blue Gray 500",
        "Blue Gray 600", "Blue Gray 700", "Blue Green", "Coral", "Coral Vivid",
        "Crayola", "Cyano", "Deep Purple", "Fuchsia", "Fuchsia Light",
        "Gray 400", "Gray 500", "Green Android", "Green Dark", "Green Fluorescent",
        "Green Fluorish", "Green New", "Indigo", "Lime", "McLaren",
        "Orange", "Orange Android", "Pale Blue", "Pink", "Pixel Blue",
        "Purple", "Purple Dark", "Purple Fluorescent", "Red", "Red Dark",
        "Red Deep", "Red Deep Dark", "Red Umbrella", "Sky Blue", "Star Wars",
        "Taupe", "Teal", "Teal Dark", "Turquoise", "Violet",
        "Violet Dark", "Yellow", "Yellow Dark",
    };
    private static final int[] AC_COLORS = {
        0xFF0097ff, 0xFF4285f4, 0xFF90A4AE, 0xFF78909C, 0xFF607d8b,
        0xFF546e7a, 0xFF455a64, 0xFF2E61F5, 0xFFEF5350, 0xFFff404c,
        0xFFFBA723, 0xFF0097A7, 0xFF78038C, 0xFFC51162, 0xFFe70ca5,
        0xFFbdbdbd, 0xFF9E9E9E, 0xFF3ddc84, 0xFF557a52, 0xFF80ff00,
        0xFF1cff12, 0xFF7DB695, 0xFF304FFE, 0xFF9dd200, 0xFFff7514,
        0xFFE0610E, 0xFFf86734, 0xFFA1B6ED, 0xFFF39DCC, 0xFF5e97f6,
        0xFF880e4f, 0xFF6200EA, 0xFFd401e9, 0xFFff0000, 0xFFd50000,
        0xFFcc0000, 0xFFbb0000, 0xFFa92c2c, 0xFF2962FF, 0xFFff2837,
        0xFFB37AA3, 0xFF00897b, 0xFF00695c, 0xFF26a69a, 0xFF908dff,
        0xFF7268fc, 0xFFffd600, 0xFFffc107,
    };

    // ── Keypad PIN button presets (com.android.systemui) ──────────────────────
    private static final String[] PIN_NAMES    = {"Accent", "Accent Shade", "Rainbow"};
    private static final String[] PIN_OVERLAYS = {"DSTPINAccent", "DSTPINAccentShade", "DSTPINRainbow"};


    // ── Round Volume Button 3 Dots presets (com.android.systemui) ─────────────
    private static final String PREF_RVD = "DST_PRESET_RVD";
    private static final String[] RVD_NAMES    = {"Accent", "Accent Shade", "Dark Gray", "Light Gray", "Outlined", "Semi Transparent", "Outlined Transparent"};
    private static final String[] RVD_OVERLAYS = {"DSTRVDAccent", "DSTRVDAccentShade", "DSTRVDDarkGray", "DSTRVDLightGray", "DSTRVDOutlined", "DSTRVDSemiTrasp", "DSTRVDOutlinedTrasp"};

    // ── UI ─────────────────────────────────────────────────────────────────────
    private FragmentAppListBinding binding;
    private LoadingDialog loadingDialog;
    private DstColorPresetsAdapter mColorPresetsAdapter;
    private DstUtilityAdapter    mUtilityAdapter;
    private DstApplyThemeAdapter mApplyThemeAdapter;

    // ── Custom color picker dialog IDs (for EventBus matching) ────────────────
    private int mBgPickerDialogId;
    private int mAcPickerDialogId;
    private int mAc2PickerDialogId;
    private int mAc3PickerDialogId;

    @Override
    public String getTitle() {
        return getString(R.string.dark_shadow_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onColorSelected(ColorSelectedEvent event) {
        int id    = event.dialogId();
        int color = event.selectedColor();
        if (id == mBgPickerDialogId) {
            BACKGROUND.setColor(color);
            mListener.onEnabledClicked(BACKGROUND);
            OCPreferences.putString(PREF_BG, PREF_VAL_CUSTOM);
            if (mColorPresetsAdapter != null) mColorPresetsAdapter.notifyItemChanged(0);
            if (mColorsAdapter       != null) mColorsAdapter.notifyItemChanged(0);
        } else if (id == mAcPickerDialogId) {
            ACCENT1.setColor(color);
            mListener.onEnabledClicked(ACCENT1);
            OCPreferences.putString(PREF_AC, PREF_VAL_CUSTOM);
            if (mColorPresetsAdapter != null) mColorPresetsAdapter.notifyItemChanged(1);
            if (mColorsAdapter       != null) mColorsAdapter.notifyItemChanged(1);
        } else if (id == mAc2PickerDialogId) {
            ACCENT2.setColor(color);
            mListener.onEnabledClicked(ACCENT2);
            if (mColorsAdapter   != null) mColorsAdapter.notifyItemChanged(2);
        } else if (id == mAc3PickerDialogId) {
            ACCENT3.setColor(color);
            mListener.onEnabledClicked(ACCENT3);
            if (mColorsAdapter   != null) mColorsAdapter.notifyItemChanged(3);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAppListBinding.inflate(inflater, container, false);
        binding.recyclerView.addItemDecoration(new OplusRecyclerView.OplusRecyclerViewItemDecoration(requireContext()));
        mBgPickerDialogId  = View.generateViewId();
        mAcPickerDialogId  = View.generateViewId();
        mAc2PickerDialogId = View.generateViewId();
        mAc3PickerDialogId = View.generateViewId();

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireContext());

        binding.progress.setVisibility(View.GONE);
        binding.searchView.setVisibility(View.GONE);

        binding.appFunctionSwitch.setTitle(getString(R.string.dark_shadow_enable_theme));
        binding.appFunctionSwitch.setSwitchChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                enableShadowTheme();
            } else {
                disableShadowTheme();
            }
        });
        binding.appFunctionSwitch.setSwitchChecked(
                OverlayUtil.isOverlayEnabled("OxygenCustomizerComponent" + overlays[0] + Build.VERSION.SDK_INT + ".overlay")
        );

        // RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(initDarkShadowColors());
        binding.recyclerView.setHasFixedSize(true);

        return binding.getRoot();
    }

    private final DarkShadowColorsAdapter.OnUserAction mListener = new DarkShadowColorsAdapter.OnUserAction() {
        @Override
        public void onColorChanged(DarkShadowItem darkShadowItem) {

        }

        @Override
        public void onEnabledClicked(DarkShadowItem darkShadowItem) {
            loadingDialog.show(getString(R.string.loading_dialog_wait));
            Log.w("DarkShadowThemeFragment", "onEnabledClicked: " + darkShadowItem.toString());
            DarkShadowUtils.saveColor(darkShadowItem);
//            enableShadowTheme();
            int i = 0;
            for (String resName : darkShadowItem.getResourceNames()) {
                FabricatedUtil
                        .buildAndEnableOverlay(
                                darkShadowItem.getPackages().get(0),
                                darkShadowItem.getOverlayName() + "_" + i,
                                "color",
                                resName,
                                String.format("0x%08X", (0xFFFFFFFF & darkShadowItem.getColor()))
                        );
                i++;
            }
            int j = i;
            if (!darkShadowItem.getAdjustColors().isEmpty()) {
                for (String resName : darkShadowItem.getAdjustColors().keySet()) {
                    FabricatedUtil
                            .buildAndEnableOverlay(
                                    darkShadowItem.getPackages().get(0),
                                    darkShadowItem.getOverlayName() + "_" + j,
                                    "color",
                                    resName,
                                    String.format("0x%08X", (0xFFFFFFFF & ColorUtils.adjustColor(darkShadowItem.getColor(), darkShadowItem.getAdjustColors().get(resName))))
                            );
                    j++;
                }
            }
            // Update PIN accent overlay if Accent/AccentShade preset is active
            if ("ACCENT1".equals(darkShadowItem.getOverlayName())) {
                String pinPref = OCPreferences.getString(PREF_PIN, null);
                if ("DSTPINAccent".equals(pinPref)) {
                    applyPinAccentFabricated(darkShadowItem.getColor(), false);
                } else if ("DSTPINAccentShade".equals(pinPref)) {
                    applyPinAccentFabricated(darkShadowItem.getColor(), true);
                }
            }
            loadingDialog.dismiss();
        }

        @Override
        public void onDisabledClicked(DarkShadowItem darkShadowItem) {
            loadingDialog.show(getString(R.string.loading_dialog_wait));
            Log.w("DarkShadowThemeFragment", "onDisabledClicked: " + darkShadowItem.toString());
            int i = 0;
            for (String resName : darkShadowItem.getResourceNames()) {
                FabricatedUtil
                        .disableOverlay(
                                darkShadowItem.getOverlayName() + "_" + i
                        );
                i++;
            }
            int j = i;
            if (!darkShadowItem.getAdjustColors().isEmpty()) {
                for (String resName : darkShadowItem.getAdjustColors().keySet()) {
                    FabricatedUtil
                            .disableOverlay(
                                    darkShadowItem.getOverlayName() + "_" + j
                            );
                    j++;
                }
            }
            loadingDialog.dismiss();
        }
    };

    private void enableShadowTheme() {
        loadingDialog.show(getString(R.string.loading_dialog_wait));
        for (String overlay : overlays) {
            OverlayUtil.enableOverlay("OxygenCustomizerComponent" + overlay + Build.VERSION.SDK_INT + ".overlay");
        }
        loadingDialog.dismiss();
    }

    private void disableShadowTheme() {
        loadingDialog.show(getString(R.string.loading_dialog_wait));
        for (String overlay : overlays) {
            OverlayUtil.disableOverlay("OxygenCustomizerComponent" + overlay + Build.VERSION.SDK_INT + ".overlay");
        }
        loadingDialog.dismiss();
    }

    private void restartSystemUI() {
        loadingDialog.show(getString(R.string.loading_dialog_wait));
        new Thread(() -> {
            AppUtils.restartScope("systemui");
            if (isAdded()) requireActivity().runOnUiThread(() -> loadingDialog.dismiss());
        }).start();
    }

    private RecyclerView.Adapter<RecyclerView.ViewHolder> initDarkShadowColors() {
        SectionTitleAdapter titleColorPresets = new SectionTitleAdapter(getString(R.string.dark_shadow_section_color_presets));
        mColorPresetsAdapter                  = new DstColorPresetsAdapter();
        SectionTitleAdapter titleUtility      = new SectionTitleAdapter(getString(R.string.dark_shadow_section_utility));
        mUtilityAdapter                       = new DstUtilityAdapter();
        SectionTitleAdapter titleApplyTheme   = new SectionTitleAdapter(getString(R.string.dark_shadow_section_apply_theme));
        mApplyThemeAdapter                    = new DstApplyThemeAdapter();
        SectionTitleAdapter titleColors       = new SectionTitleAdapter(getString(R.string.dark_shadow_section_colors));
        mColorsAdapter                        = new DstColorsAdapter();
        FooterWidgetAdapter footer            = new FooterWidgetAdapter(getString(R.string.dark_shadow_footer), v -> openDarkShadow());

        return new ConcatAdapter(
                titleColorPresets, mColorPresetsAdapter,
                titleUtility, mUtilityAdapter,
                titleApplyTheme, mApplyThemeAdapter,
                titleColors, mColorsAdapter,
                footer);
    }

    private void openDarkShadow() {
        requireActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://mythemedarkandmore.altervista.org/")));
    }

    // ── Preset dialogs ─────────────────────────────────────────────────────────

    /**
     * BG and AC presets: applied via FabricatedUtil, no overlay APK needed.
     * First entry is always "Custom" which opens the colour picker.
     */
    private void showFabricatedPresetDialog(String title, String[] names, int[] colors,
                                            DarkShadowItem item, String prefKey,
                                            int pickerDialogId, Runnable onChanged) {
        // Build list with "Custom" prepended
        String[] dialogNames = new String[names.length + 1];
        dialogNames[0] = getString(R.string.dark_shadow_custom);
        System.arraycopy(names, 0, dialogNames, 1, names.length);

        String current = OCPreferences.getString(prefKey, null);
        int currentIdx = -1;
        if (PREF_VAL_CUSTOM.equals(current)) {
            currentIdx = 0;
        } else if (current != null) {
            for (int i = 0; i < names.length; i++) {
                if (names[i].equals(current)) { currentIdx = i + 1; break; }
            }
        }

        final int[] selected = {currentIdx};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setSingleChoiceItems(dialogNames, currentIdx, (d, which) -> selected[0] = which)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    if (selected[0] < 0) return;
                    if (selected[0] == 0) {
                        // Open colour picker — result arrives in onColorSelected()
                        ((MainActivity) requireActivity()).showColorPickerDialog(
                                pickerDialogId,
                                DarkShadowUtils.getColor(item),
                                true, true, true);
                    } else {
                        item.setColor(colors[selected[0] - 1]);
                        mListener.onEnabledClicked(item);
                        OCPreferences.putString(prefKey, names[selected[0] - 1]);
                        if (onChanged != null) onChanged.run();
                    }
                })
                .setNeutralButton(R.string.dark_shadow_disable, (d, w) -> {
                    mListener.onDisabledClicked(item);
                    OCPreferences.putString(prefKey, null);
                    if (onChanged != null) onChanged.run();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }


    /** Set specific alpha on a color, replacing its alpha channel. */
    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    /** Apply coui_numeric_keyboard colors via FabricatedUtil, matching current DST accent. */
    private void applyPinAccentFabricated(int accent, boolean isShade) {
        int rgb    = accent & 0x00FFFFFF;
        String hex    = String.format("0x%08X", 0xFF000000 | rgb);
        String ripple = String.format("0x%08X", 0x80000000 | rgb);
        String outer1 = String.format("0x%08X", 0xCC000000 | rgb);
        String outer2 = String.format("0x%08X", 0x40000000 | rgb);
        String outer3 = String.format("0x%08X", 0x21000000 | rgb);
        String shadow  = isShade ? ripple : hex;
        FabricatedUtil.buildAndEnableOverlays(
            new Object[]{"com.android.systemui", "DSTNKBborder",  "color", "coui_numeric_keyboard_border_color",              hex},
            new Object[]{"com.android.systemui", "DSTNKBinner1",  "color", "coui_numeric_keyboard_inner_gradient_color_1",    ripple},
            new Object[]{"com.android.systemui", "DSTNKBinner2",  "color", "coui_numeric_keyboard_inner_gradient_color_2",    ripple},
            new Object[]{"com.android.systemui", "DSTNKBshadow",  "color", "coui_numeric_keyboard_upper_inner_shadow_color",  shadow},
            new Object[]{"com.android.systemui", "DSTNKBouter1",  "color", "coui_numeric_keyboard_outer_gradient_color_1",   outer1},
            new Object[]{"com.android.systemui", "DSTNKBouter2",  "color", "coui_numeric_keyboard_outer_gradient_color_2",   outer2},
            new Object[]{"com.android.systemui", "DSTNKBouter3",  "color", "coui_numeric_keyboard_outer_gradient_color_3",   outer3}
        );
    }

    private void disablePinAccentFabricated() {
        for (String name : new String[]{"DSTNKBborder","DSTNKBinner1","DSTNKBinner2",
                                        "DSTNKBshadow","DSTNKBouter1","DSTNKBouter2","DSTNKBouter3"}) {
            FabricatedUtil.disableOverlay(name);
        }
    }

    /**
     * PIN button preset: overlay APK based (has drawables, needs OverlayUtil).
     */
    private void showOverlayPresetDialog(String title, String[] names, String[] overlayNames,
                                         String prefKey, Runnable onChanged) {
        String current = OCPreferences.getString(prefKey, null);
        int currentIdx = -1;
        for (int i = 0; i < overlayNames.length; i++) {
            if (overlayNames[i].equals(current)) { currentIdx = i; break; }
        }
        final int[] selected = {currentIdx};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setSingleChoiceItems(names, currentIdx, (d, which) -> selected[0] = which)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    if (selected[0] < 0) return;
                    loadingDialog.show(getString(R.string.loading_dialog_wait));
                    if (current != null) {
                        OverlayUtil.disableOverlay("OxygenCustomizerComponent" + current + ".overlay");
                    }
                    String newOverlay = overlayNames[selected[0]];
                    // Disable old fabricated PIN colors if switching away from an accent preset
                    if ("DSTPINAccent".equals(current) || "DSTPINAccentShade".equals(current)) {
                        disablePinAccentFabricated();
                    }
                    OverlayUtil.enableOverlay("OxygenCustomizerComponent" + newOverlay + ".overlay");
                    if ("DSTPINAccent".equals(newOverlay)) {
                        applyPinAccentFabricated(DarkShadowUtils.getColor(ACCENT1), false);
                    } else if ("DSTPINAccentShade".equals(newOverlay)) {
                        applyPinAccentFabricated(DarkShadowUtils.getColor(ACCENT1), true);
                    }
                    OCPreferences.putString(prefKey, newOverlay);
                    loadingDialog.dismiss();
                    if (onChanged != null) onChanged.run();
                })
                .setNeutralButton(R.string.dark_shadow_disable, (d, w) -> {
                    if (current == null) return;
                    loadingDialog.show(getString(R.string.loading_dialog_wait));
                    OverlayUtil.disableOverlay("OxygenCustomizerComponent" + current + ".overlay");
                    if ("DSTPINAccent".equals(current) || "DSTPINAccentShade".equals(current)) {
                        disablePinAccentFabricated();
                    }
                    OCPreferences.putString(prefKey, null);
                    loadingDialog.dismiss();
                    if (onChanged != null) onChanged.run();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    // ── Colors rows adapter (Phase 2) ──────────────────────────────────────────

    private class DstColorsAdapter extends RecyclerView.Adapter<DstColorsAdapter.ViewHolder> {

        private static final int COUNT = 4;

        private final DarkShadowItem[] ITEMS     = {BACKGROUND, ACCENT1, ACCENT2, ACCENT3};
        private final int[]            TITLE_RES = {
            R.string.dark_shadow_color_background,
            R.string.dark_shadow_color_accent,
            R.string.dark_shadow_color_accent_light,
            R.string.dark_shadow_color_ripple_accent,
        };
        private final int[]    DIALOG_IDS    = {
            mBgPickerDialogId, mAcPickerDialogId,
            mAc2PickerDialogId, mAc3PickerDialogId
        };
        // Pref keys (null = no preset pref to track)
        private final String[] PREFS = {PREF_BG, PREF_AC, null, null};

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_widget_list, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DarkShadowItem item = ITEMS[position];
            holder.title.setText(TITLE_RES[position]);

            // Colored circle
            int savedColor = DarkShadowUtils.getColor(item);
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(savedColor);
            holder.icon.setImageDrawable(circle);
            holder.icon.setVisibility(View.VISIBLE);

            holder.summary.setText(String.format("#%08X", 0xFFFFFFFFL & savedColor));
            holder.summary.setVisibility(View.VISIBLE);

            if (position == 0) {
                holder.container.setBackgroundResource(R.drawable.preference_background_top);
            } else if (position == COUNT - 1) {
                holder.container.setBackgroundResource(R.drawable.preference_background_bottom);
            } else {
                holder.container.setBackgroundResource(R.drawable.preference_background_middle);
            }

            int pos = position;
            holder.container.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(TITLE_RES[pos])
                        .setPositiveButton(R.string.dark_shadow_pick_color, (d, w) ->
                                ((MainActivity) requireActivity()).showColorPickerDialog(
                                        DIALOG_IDS[pos],
                                        DarkShadowUtils.getColor(item),
                                        true, true, true))
                        .setNeutralButton(R.string.dark_shadow_disable, (d, w) -> {
                            mListener.onDisabledClicked(item);
                            if (PREFS[pos] != null) OCPreferences.putString(PREFS[pos], null);
                            if (mColorPresetsAdapter != null && pos < 2) mColorPresetsAdapter.notifyItemChanged(pos);
                            notifyItemChanged(pos);
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            });
        }

        @Override
        public int getItemCount() { return COUNT; }

        class ViewHolder extends RecyclerView.ViewHolder {
            final RelativeLayout container;
            final ImageView       icon;
            final TextView        title, summary;

            ViewHolder(@NonNull View v) {
                super(v);
                container = v.findViewById(R.id.container);
                icon      = v.findViewById(R.id.icon);
                title     = v.findViewById(R.id.title);
                summary   = v.findViewById(R.id.summary);
            }
        }
    }

    // ── Color presets adapter (Sfondo + Accent) ────────────────────────────────

    private class DstColorPresetsAdapter extends RecyclerView.Adapter<DstColorPresetsAdapter.ViewHolder> {

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_widget_list, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.summary.setVisibility(View.VISIBLE);
            holder.container.setBackgroundResource(position == 0
                    ? R.drawable.preference_background_top
                    : R.drawable.preference_background_bottom);

            if (position == 0) {
                holder.title.setText(R.string.dark_shadow_preset_background);
                String saved = OCPreferences.getString(PREF_BG, null);
                holder.summary.setText(saved != null ? saved : getString(R.string.dark_shadow_none));
                holder.container.setOnClickListener(v ->
                        showFabricatedPresetDialog(
                                getString(R.string.dark_shadow_preset_background),
                                BG_NAMES, BG_COLORS, BACKGROUND, PREF_BG, mBgPickerDialogId,
                                () -> { if (mColorPresetsAdapter != null) mColorPresetsAdapter.notifyItemChanged(0); }));
            } else {
                holder.title.setText(R.string.dark_shadow_preset_accent);
                String saved = OCPreferences.getString(PREF_AC, null);
                holder.summary.setText(saved != null ? saved : getString(R.string.dark_shadow_none));
                holder.container.setOnClickListener(v ->
                        showFabricatedPresetDialog(
                                getString(R.string.dark_shadow_preset_accent),
                                AC_NAMES, AC_COLORS, ACCENT1, PREF_AC, mAcPickerDialogId,
                                () -> { if (mColorPresetsAdapter != null) mColorPresetsAdapter.notifyItemChanged(1); }));
            }
        }

        @Override public int getItemCount() { return 2; }

        class ViewHolder extends RecyclerView.ViewHolder {
            final RelativeLayout container;
            final TextView title, summary;
            ViewHolder(@NonNull View v) {
                super(v);
                container = v.findViewById(R.id.container);
                title     = v.findViewById(R.id.title);
                summary   = v.findViewById(R.id.summary);
                v.findViewById(R.id.icon).setVisibility(View.GONE);
            }
        }
    }


    // ── Utility: PIN preset + QS Edit Button + Round Volume Button ────────────

    private class DstUtilityAdapter extends RecyclerView.Adapter<DstUtilityAdapter.ViewHolder> {

        private static final int COUNT = 2;

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_widget_list, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.summary.setVisibility(View.VISIBLE);
            if (position == 0) {
                holder.container.setBackgroundResource(R.drawable.preference_background_top);
            } else if (position == COUNT - 1) {
                holder.container.setBackgroundResource(R.drawable.preference_background_bottom);
            } else {
                holder.container.setBackgroundResource(R.drawable.preference_background_middle);
            }

            switch (position) {
                case 0: {
                    holder.title.setText(R.string.dark_shadow_preset_pin);
                    String savedOverlay = OCPreferences.getString(PREF_PIN, null);
                    String displayName  = getString(R.string.dark_shadow_none);
                    if (savedOverlay != null) {
                        for (int i = 0; i < PIN_OVERLAYS.length; i++) {
                            if (PIN_OVERLAYS[i].equals(savedOverlay)) { displayName = PIN_NAMES[i]; break; }
                        }
                    }
                    holder.summary.setText(displayName);
                    holder.container.setOnClickListener(v ->
                            showOverlayPresetDialog(
                                    getString(R.string.dark_shadow_preset_pin),
                                    PIN_NAMES, PIN_OVERLAYS, PREF_PIN,
                                    () -> { if (mUtilityAdapter != null) mUtilityAdapter.notifyItemChanged(0); }));
                    break;
                }
                default: {
                    holder.title.setText(R.string.dark_shadow_preset_rvd);
                    String savedOverlay = OCPreferences.getString(PREF_RVD, null);
                    String displayName  = getString(R.string.dark_shadow_none);
                    if (savedOverlay != null) {
                        for (int i = 0; i < RVD_OVERLAYS.length; i++) {
                            if (RVD_OVERLAYS[i].equals(savedOverlay)) { displayName = RVD_NAMES[i]; break; }
                        }
                    }
                    holder.summary.setText(displayName);
                    holder.container.setOnClickListener(v ->
                            showOverlayPresetDialog(
                                    getString(R.string.dark_shadow_preset_rvd),
                                    RVD_NAMES, RVD_OVERLAYS, PREF_RVD,
                                    () -> { if (mUtilityAdapter != null) mUtilityAdapter.notifyItemChanged(1); }));
                    break;
                }
            }
        }

        @Override public int getItemCount() { return COUNT; }

        class ViewHolder extends RecyclerView.ViewHolder {
            final RelativeLayout container;
            final TextView title, summary;
            ViewHolder(@NonNull View v) {
                super(v);
                container = v.findViewById(R.id.container);
                title     = v.findViewById(R.id.title);
                summary   = v.findViewById(R.id.summary);
                v.findViewById(R.id.icon).setVisibility(View.GONE);
            }
        }
    }

    // ── Apply Theme ───────────────────────────────────────────────────────────

    private class DstApplyThemeAdapter extends RecyclerView.Adapter<DstApplyThemeAdapter.ViewHolder> {

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_widget_list, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.summary.setVisibility(View.VISIBLE);
            holder.container.setBackgroundResource(R.drawable.preference_background_center);
            holder.title.setText(R.string.dark_shadow_apply_theme);
            holder.summary.setText(R.string.dark_shadow_apply_theme_summary);
            holder.container.setOnClickListener(v -> restartSystemUI());
        }

        @Override public int getItemCount() { return 1; }

        class ViewHolder extends RecyclerView.ViewHolder {
            final RelativeLayout container;
            final TextView title, summary;
            ViewHolder(@NonNull View v) {
                super(v);
                container = v.findViewById(R.id.container);
                title     = v.findViewById(R.id.title);
                summary   = v.findViewById(R.id.summary);
                v.findViewById(R.id.icon).setVisibility(View.GONE);
            }
        }
    }

}
