package it.dhd.oxygencustomizer.ui.fragments.uistyle;

// fix
import static it.dhd.oxygencustomizer.utils.DarkShadowUtils.ACCENT1;
import static it.dhd.oxygencustomizer.utils.DarkShadowUtils.ACCENT2;
import static it.dhd.oxygencustomizer.utils.DarkShadowUtils.ACCENT3;
import static it.dhd.oxygencustomizer.utils.DarkShadowUtils.BACKGROUND;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.widget.Toast;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.TypedValue;
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
import com.google.android.material.materialswitch.MaterialSwitch;

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
    // DSTSUI è mutable (non immutable), quindi abilitarlo a runtime via OC è sicuro (non causa bootloop al boot)
    String[] overlays = new String[]{"DST", "DSTSTG", "DSTSUI"};

    // ── Colors adapter (Phase 2) ───────────────────────────────────────────────
    private DstColorsAdapter mColorsAdapter;

    // ── Preference keys ────────────────────────────────────────────────────────
    private static final String PREF_BG  = "DST_PRESET_BG";
    private static final String PREF_AC  = "DST_PRESET_AC";
    private static final String PREF_PIN     = "DST_PRESET_PIN";
    private static final String PREF_PIN_NUM = "DST_PRESET_PIN_NUM";
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

    // ── Keypad PIN number color presets (com.android.systemui) ───────────────
    private static final String[] PIN_NUM_NAMES    = {"Accent", "Accent Shade", "Rainbow"};
    private static final String[] PIN_NUM_OVERLAYS = {"DSTNUMPINAccent", "DSTNUMPINAccentShade", "DSTNUMPINRainbow"};


    // ── Circular Progress Bar presets (android) ──────────────────────────────
    private static final String PREF_CPB = "DST_PRESET_CPB";
    private static final String[] CPB_NAMES    = {
        "Aurora Theme", "Arrow", "Radioactive", "Sprite Theme", "Stars"
    };
    private static final String[] CPB_OVERLAYS = {
        "DSTCPB1", "DSTCPB2", "DSTCPB3", "DSTCPB4", "DSTCPB5"
    };

    // ── Dialog style presets (android) ───────────────────────────────────────
    private static final String PREF_DLG = "DST_PRESET_DLG";
    private static final String[] DLG_NAMES    = {
        "Dialog Higher Transparent", "Dialog Higher Transparent Outlined",
        "Dialog Lower Transparent",  "Dialog Lower Transparent Outlined",
        "Dialog Medium Transparent", "Dialog Medium Transparent Outlined",
        "Dialog Solid",              "Dialog Solid Outlined"
    };
    private static final String[] DLG_OVERLAYS = {
        "DSTDHT", "DSTDHTO", "DSTDLT", "DSTDLYO", "DSTDMT", "DSTDMTO", "DSTDS", "DSTDSO"
    };

    // ── Volume Button 3 Dots presets (Round + Square, com.android.systemui) ───
    private static final String PREF_RVD = "DST_PRESET_RVD";
    private static final String[] RVD_NAMES    = {
        "Round Accent", "Round Accent Shade", "Round Dark Gray", "Round Light Gray", "Round White",
        "Round Outlined", "Round Semi Transparent", "Round Outlined Transparent",
        "Square Accent", "Square Accent Shade", "Square Dark Gray", "Square Light Gray", "Square White",
        "Square Outlined", "Square Semi Transparent", "Square Outlined Transparent"
    };
    private static final String[] RVD_OVERLAYS = {
        "DSTRVDAccent", "DSTRVDAccentShade", "DSTRVDDarkGray", "DSTRVDLightGray", "DSTRVDWhite",
        "DSTRVDOutlined", "DSTRVDSemiTrasp", "DSTRVDOutlinedTrasp",
        "DSTSVDAccent", "DSTSVDAccentShade", "DSTSVDDarkGray", "DSTSVDLightGray", "DSTSVDWhite",
        "DSTSVDOutlined", "DSTSVDSemiTrasp", "DSTSVDOutlinedTrasp"
    };

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
    private int mPinBgRainbowPickerDialogId;
    private int mPinNumRainbowPickerDialogId;
    private Runnable mPinRainbowOnChanged;
    private static final String PREF_PIN_RAINBOW_COLOR     = "DST_PIN_RAINBOW_COLOR";
    private static final String PREF_PIN_NUM_RAINBOW_COLOR = "DST_PIN_NUM_RAINBOW_COLOR";
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
        } else if (id == mPinBgRainbowPickerDialogId) {
            applyPinAccentFabricated(color, false);
            OCPreferences.putInt(PREF_PIN_RAINBOW_COLOR, color);
            if (mPinRainbowOnChanged != null) { mPinRainbowOnChanged.run(); mPinRainbowOnChanged = null; }
        } else if (id == mPinNumRainbowPickerDialogId) {
            applyPinNumFabricated(color);
            OCPreferences.putInt(PREF_PIN_NUM_RAINBOW_COLOR, color);
            if (mPinRainbowOnChanged != null) { mPinRainbowOnChanged.run(); mPinRainbowOnChanged = null; }
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
        mPinBgRainbowPickerDialogId  = View.generateViewId();
        mPinNumRainbowPickerDialogId = View.generateViewId();

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireContext());

        binding.progress.setVisibility(View.GONE);
        binding.searchView.setVisibility(View.GONE);

        binding.appFunctionSwitch.forcePosition("center");
        View switchContainer = binding.appFunctionSwitch.findViewById(R.id.container);
        if (switchContainer != null) {
            switchContainer.setBackgroundResource(R.drawable.dst_enable_switch_bg);
        }
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
            new Object[]{"com.android.systemui", "DSTNKBouter3",  "color", "coui_numeric_keyboard_outer_gradient_color_3",   outer3},
            // PIN dot indicators (pallini) — OOS16: coui_simple_lock_transparent_*
            new Object[]{"com.android.systemui", "DSTNKBdotfillT",    "color", "coui_simple_lock_transparent_filled_rectangle_icon_color",   hex},
            new Object[]{"com.android.systemui", "DSTNKBdotoutlineT", "color", "coui_simple_lock_transparent_outlined_rectangle_icon_color", "0x33FFFFFF"},
            // Word text under number keys (e.g. "+" under 1) — accent_material_dark reference in stock
            new Object[]{"com.android.systemui", "DSTNKBwordtxt",    "color", "coui_numeric_keyboard_dark_word_text_normal_color",       hex},
            new Object[]{"com.android.systemui", "DSTNKBwordtxtL",   "color", "coui_numeric_keyboard_dark_word_text_normal_light_color", hex}
        );
    }

    private void disablePinAccentFabricated() {
        for (String name : new String[]{"DSTNKBborder","DSTNKBinner1","DSTNKBinner2",
                                        "DSTNKBshadow","DSTNKBouter1","DSTNKBouter2","DSTNKBouter3",
                                        "DSTNKBdotfillT","DSTNKBdotoutlineT",
                                        "DSTNKBwordtxt","DSTNKBwordtxtL"}) {
            FabricatedUtil.disableOverlay(name);
        }
    }

    private void applyPinNumFabricated(int color) {
        String hex = String.format("0x%08X", 0xFF000000 | (color & 0x00FFFFFF));
        FabricatedUtil.buildAndEnableOverlays(
            new Object[]{"com.android.systemui", "DSTNKBnumcolor", "color", "coui_numeric_keyboard_number_color", hex}
        );
    }

    private void disablePinNumFabricated() {
        FabricatedUtil.disableOverlay("DSTNKBnumcolor");
    }

    /**
     * PIN/RVD/SVD button preset: overlay APK based (has drawables, needs OverlayUtil).
     * @param competingPrefKey se non null, l'overlay salvato in questa pref viene disabilitato
     *                         automaticamente quando si attiva un nuovo overlay (mutua esclusione).
     */
    private void showOverlayPresetDialog(String title, String[] names, String[] overlayNames,
                                         String prefKey, String competingPrefKey, Runnable onChanged) {
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
                    boolean isPinNum = PREF_PIN_NUM.equals(prefKey);
                    if (isPinNum) {
                        // Disabilita tutti i DSTNUMPIN RRO per evitare residui stale
                        for (String o : PIN_NUM_OVERLAYS) {
                            OverlayUtil.disableOverlay("OxygenCustomizerComponent" + o + ".overlay");
                        }
                        disablePinNumFabricated();
                    } else {
                        // Disabilita overlay precedente dello stesso gruppo
                        if (current != null) {
                            OverlayUtil.disableOverlay("OxygenCustomizerComponent" + current + ".overlay");
                        }
                    }
                    // Disabilita overlay del gruppo concorrente (mutua esclusione RVD ↔ SVD)
                    if (competingPrefKey != null) {
                        String competing = OCPreferences.getString(competingPrefKey, null);
                        if (competing != null) {
                            OverlayUtil.disableOverlay("OxygenCustomizerComponent" + competing + ".overlay");
                            OCPreferences.putString(competingPrefKey, null);
                        }
                    }
                    String newOverlay = overlayNames[selected[0]];
                    // Disable old fabricated PIN bg colors if switching away from an accent preset
                    if ("DSTPINAccent".equals(current) || "DSTPINAccentShade".equals(current)) {
                        disablePinAccentFabricated();
                    }
                    OverlayUtil.enableOverlay("OxygenCustomizerComponent" + newOverlay + ".overlay");
                    if ("DSTPINAccent".equals(newOverlay)) {
                        applyPinAccentFabricated(DarkShadowUtils.getColor(ACCENT1), false);
                        Toast.makeText(requireContext(),
                                "Colori PIN attivi dopo riavvio UISystem (non del device)",
                                Toast.LENGTH_LONG).show();
                    } else if ("DSTPINAccentShade".equals(newOverlay)) {
                        applyPinAccentFabricated(DarkShadowUtils.getColor(ACCENT1), true);
                        Toast.makeText(requireContext(),
                                "Colori PIN attivi dopo riavvio UISystem (non del device)",
                                Toast.LENGTH_LONG).show();
                    } else if ("DSTNUMPINAccent".equals(newOverlay)) {
                        applyPinNumFabricated(DarkShadowUtils.getColor(ACCENT1));
                    } else if ("DSTNUMPINAccentShade".equals(newOverlay)) {
                        int accent1 = DarkShadowUtils.getColor(ACCENT1);
                        int shade = 0x80000000 | (accent1 & 0x00FFFFFF);
                        applyPinNumFabricated(shade);
                    } else if ("DSTNUMPINRainbow".equals(newOverlay)) {
                        mPinRainbowOnChanged = onChanged;
                        int savedColor = OCPreferences.getInt(PREF_PIN_NUM_RAINBOW_COLOR, 0xFFFF0000);
                        ((MainActivity) requireActivity()).showColorPickerDialog(
                                mPinNumRainbowPickerDialogId, savedColor, true, false, true);
                    } else if ("DSTPINRainbow".equals(newOverlay)) {
                        mPinRainbowOnChanged = onChanged;
                        int savedColor = OCPreferences.getInt(PREF_PIN_RAINBOW_COLOR, 0xFFFFFFFF);
                        ((MainActivity) requireActivity()).showColorPickerDialog(
                                mPinBgRainbowPickerDialogId, savedColor, true, false, true);
                    }
                    OCPreferences.putString(prefKey, newOverlay);
                    loadingDialog.dismiss();
                    if (onChanged != null) onChanged.run();
                })
                .setNeutralButton(R.string.dark_shadow_disable, (d, w) -> {
                    if (current == null) return;
                    loadingDialog.show(getString(R.string.loading_dialog_wait));
                    boolean isPinNum = PREF_PIN_NUM.equals(prefKey);
                    if (isPinNum) {
                        // Disabilita tutti i DSTNUMPIN RRO per evitare residui stale
                        for (String o : PIN_NUM_OVERLAYS) {
                            OverlayUtil.disableOverlay("OxygenCustomizerComponent" + o + ".overlay");
                        }
                        disablePinNumFabricated();
                    } else {
                        OverlayUtil.disableOverlay("OxygenCustomizerComponent" + current + ".overlay");
                        if ("DSTPINAccent".equals(current) || "DSTPINAccentShade".equals(current)) {
                            disablePinAccentFabricated();
                        }
                    }
                    OCPreferences.putString(prefKey, null);
                    loadingDialog.dismiss();
                    if (onChanged != null) onChanged.run();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showPinCategoryDialog(Runnable onChanged) {
        android.content.Context ctx = requireContext();
        final String[] labels  = {
            getString(R.string.dark_shadow_preset_pin_bg),
            getString(R.string.dark_shadow_preset_pin_num)
        };
        final int[] iconRes = { R.drawable.ic_drawing, R.drawable.ic_mods_ui };
        final Runnable[] actions = {
            () -> showOverlayPresetDialog(getString(R.string.dark_shadow_preset_pin_bg),
                    PIN_NAMES, PIN_OVERLAYS, PREF_PIN, null, onChanged),
            () -> showOverlayPresetDialog(getString(R.string.dark_shadow_preset_pin_num),
                    PIN_NUM_NAMES, PIN_NUM_OVERLAYS, PREF_PIN_NUM, null, onChanged)
        };

        TypedValue accentTv = new TypedValue();
        ctx.getTheme().resolveAttribute(android.R.attr.colorAccent, accentTv, true);
        final int accent = accentTv.data;
        final float dp = ctx.getResources().getDisplayMetrics().density;

        android.widget.LinearLayout container = new android.widget.LinearLayout(ctx);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);

        final androidx.appcompat.app.AlertDialog[] dialogRef = new androidx.appcompat.app.AlertDialog[1];

        for (int i = 0; i < 2; i++) {
            final int idx = i;
            android.widget.LinearLayout row = new android.widget.LinearLayout(ctx);
            row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            int hPad = (int)(24 * dp), vPad = (int)(16 * dp);
            row.setPadding(hPad, vPad, hPad, vPad);
            android.util.TypedValue ripple = new android.util.TypedValue();
            ctx.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, ripple, true);
            row.setBackgroundResource(ripple.resourceId);
            row.setClickable(true);
            row.setFocusable(true);
            row.setOnClickListener(v -> {
                if (dialogRef[0] != null) dialogRef[0].dismiss();
                actions[idx].run();
            });

            android.widget.ImageView iv = new android.widget.ImageView(ctx);
            android.widget.LinearLayout.LayoutParams ivLp =
                new android.widget.LinearLayout.LayoutParams((int)(24*dp), (int)(24*dp));
            iv.setLayoutParams(ivLp);
            iv.setImageResource(iconRes[idx]);
            iv.setImageTintList(ColorStateList.valueOf(accent));
            row.addView(iv);

            android.widget.TextView tv = new android.widget.TextView(ctx);
            android.widget.LinearLayout.LayoutParams tvLp =
                new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
            tvLp.setMarginStart((int)(16 * dp));
            tv.setLayoutParams(tvLp);
            tv.setText(labels[idx]);
            tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16);
            row.addView(tv);

            container.addView(row);
        }

        androidx.appcompat.app.AlertDialog d = new MaterialAlertDialogBuilder(ctx)
                .setTitle(R.string.dark_shadow_preset_pin)
                .setView(container)
                .create();
        dialogRef[0] = d;
        d.show();
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
            holder.icon.setVisibility(View.VISIBLE);
            holder.container.setBackgroundResource(position == 0
                    ? R.drawable.preference_background_top
                    : R.drawable.preference_background_bottom);

            DarkShadowItem colorItem = (position == 0) ? BACKGROUND : ACCENT1;
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(DarkShadowUtils.getColor(colorItem));
            holder.icon.setImageDrawable(circle);
            holder.icon.setImageTintList(null);

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


    // ── Utility: PIN + Dialog + CPB + Volume Button ───────────────────────────

    private class DstUtilityAdapter extends RecyclerView.Adapter<DstUtilityAdapter.ViewHolder> {

        private static final int COUNT = 5;
        private static final int TYPE_NORMAL = 0;
        private static final int TYPE_SWITCH = 1;
        private static final String PREF_QS_BG = "DST_QS_BG_ENABLED";

        @Override
        public int getItemViewType(int position) {
            return position == 4 ? TYPE_SWITCH : TYPE_NORMAL;
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_SWITCH) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_widget_switch, parent, false);
                return new SwitchViewHolder(v);
            }
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_widget_list, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.summary.setVisibility(View.VISIBLE);
            holder.icon.setVisibility(View.VISIBLE);
            TypedValue tv = new TypedValue();
            requireContext().getTheme().resolveAttribute(android.R.attr.colorAccent, tv, true);
            holder.icon.setImageTintList(ColorStateList.valueOf(tv.data));
            if (position == 0) {
                holder.container.setBackgroundResource(R.drawable.preference_background_top);
            } else if (position == COUNT - 1) {
                holder.container.setBackgroundResource(R.drawable.preference_background_bottom);
            } else {
                holder.container.setBackgroundResource(R.drawable.preference_background_middle);
            }

            switch (position) {
                case 0: {
                    holder.icon.setImageResource(R.drawable.ic_lock);
                    holder.title.setText(R.string.dark_shadow_preset_pin);
                    String savedBg  = OCPreferences.getString(PREF_PIN, null);
                    String savedNum = OCPreferences.getString(PREF_PIN_NUM, null);
                    String bgName   = getString(R.string.dark_shadow_none);
                    String numName  = getString(R.string.dark_shadow_none);
                    for (int i = 0; i < PIN_OVERLAYS.length; i++)
                        if (PIN_OVERLAYS[i].equals(savedBg)) { bgName = PIN_NAMES[i]; break; }
                    for (int i = 0; i < PIN_NUM_OVERLAYS.length; i++)
                        if (PIN_NUM_OVERLAYS[i].equals(savedNum)) { numName = PIN_NUM_NAMES[i]; break; }
                    holder.summary.setMaxLines(Integer.MAX_VALUE);
                    holder.summary.setSingleLine(false);
                    holder.summary.setText(
                            getString(R.string.dark_shadow_preset_pin_bg) + ": " + bgName + "\n" +
                            getString(R.string.dark_shadow_preset_pin_num) + ": " + numName);
                    holder.container.setOnClickListener(v ->
                            showPinCategoryDialog(
                                    () -> { if (mUtilityAdapter != null) mUtilityAdapter.notifyItemChanged(0); }));
                    break;
                }
                case 1: {
                    holder.icon.setImageResource(R.drawable.ic_ui_styles);
                    holder.title.setText(R.string.dark_shadow_preset_dlg);
                    String savedOverlay = OCPreferences.getString(PREF_DLG, null);
                    String displayName  = getString(R.string.dark_shadow_none);
                    if (savedOverlay != null) {
                        for (int i = 0; i < DLG_OVERLAYS.length; i++) {
                            if (DLG_OVERLAYS[i].equals(savedOverlay)) { displayName = DLG_NAMES[i]; break; }
                        }
                    }
                    holder.summary.setText(displayName);
                    holder.container.setOnClickListener(v ->
                            showOverlayPresetDialog(
                                    getString(R.string.dark_shadow_preset_dlg),
                                    DLG_NAMES, DLG_OVERLAYS, PREF_DLG, null,
                                    () -> { if (mUtilityAdapter != null) mUtilityAdapter.notifyItemChanged(1); }));
                    break;
                }
                case 2: {
                    holder.icon.setImageResource(R.drawable.arc_progress);
                    holder.title.setText(R.string.dark_shadow_preset_cpb);
                    String savedOverlay = OCPreferences.getString(PREF_CPB, null);
                    String displayName  = getString(R.string.dark_shadow_none);
                    if (savedOverlay != null) {
                        for (int i = 0; i < CPB_OVERLAYS.length; i++) {
                            if (CPB_OVERLAYS[i].equals(savedOverlay)) { displayName = CPB_NAMES[i]; break; }
                        }
                    }
                    holder.summary.setText(displayName);
                    holder.container.setOnClickListener(v ->
                            showOverlayPresetDialog(
                                    getString(R.string.dark_shadow_preset_cpb),
                                    CPB_NAMES, CPB_OVERLAYS, PREF_CPB, null,
                                    () -> { if (mUtilityAdapter != null) mUtilityAdapter.notifyItemChanged(2); }));
                    break;
                }
                case 3: {
                    holder.icon.setImageResource(R.drawable.ic_sysui_volume);
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
                                    RVD_NAMES, RVD_OVERLAYS, PREF_RVD, null,
                                    () -> { if (mUtilityAdapter != null) mUtilityAdapter.notifyItemChanged(3); }));
                    break;
                }
                case 4: {
                    SwitchViewHolder svh = (SwitchViewHolder) holder;
                    svh.icon.setImageResource(R.drawable.ic_qs);
                    svh.title.setText(R.string.dst_qs_solid_bg);
                    svh.summary.setText(R.string.dst_qs_solid_bg_summary);
                    svh.switchWidget.setOnCheckedChangeListener(null);
                    svh.switchWidget.setChecked(OCPreferences.getBoolean(PREF_QS_BG, false));
                    svh.switchWidget.setOnCheckedChangeListener((btn, isChecked) -> {
                        OCPreferences.putBoolean(PREF_QS_BG, isChecked);
                        AppUtils.restartScope("systemui");
                    });
                    svh.container.setOnClickListener(v -> svh.switchWidget.toggle());
                    break;
                }
            }
        }

        @Override public int getItemCount() { return COUNT; }

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

        class SwitchViewHolder extends ViewHolder {
            final MaterialSwitch switchWidget;
            SwitchViewHolder(@NonNull View v) {
                super(v);
                switchWidget = v.findViewById(R.id.switch_widget);
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