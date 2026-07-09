package it.dhd.oxygencustomizer.xposed.hooks.framework;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.resparams;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.content.res.XResources;
import android.graphics.Color;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class MonetFreeze extends XposedMods {

    private static final String PREF_MONET_FREEZE          = "DST_MONET_FREEZE";
    private static final String PREF_ACCENT1_COLOR         = "DSTACCENT1";
    private static final String PREF_PIN                   = "DST_PRESET_PIN";
    private static final String PREF_PIN_NUM               = "DST_PRESET_PIN_NUM";
    private static final String PREF_PIN_RAINBOW_COLOR     = "DST_PIN_RAINBOW_COLOR";
    private static final String PREF_PIN_NUM_RAINBOW_COLOR = "DST_PIN_NUM_RAINBOW_COLOR";

    public MonetFreeze(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {}

    @Override
    public void initResources() {
        if (Xprefs == null) return;

        var resParam = resparams.get(SYSTEM_UI);
        if (resParam == null) return;

        XResources xRes = resParam.res;

        // Freeze accent_material_dark to the user's chosen colour
        if (Xprefs.getBoolean(PREF_MONET_FREEZE, false)) {
            int accent = Xprefs.getInt(PREF_ACCENT1_COLOR, Color.RED);
            xRes.setReplacement("android", "color", "accent_material_dark", accent);
        }

        // PIN keyboard background colours
        String pinPref = Xprefs.getString(PREF_PIN, null);
        if (pinPref != null) {
            switch (pinPref) {
                case "DSTPINAccent":
                    applyPinBgColors(xRes, Xprefs.getInt(PREF_ACCENT1_COLOR, Color.RED), false);
                    break;
                case "DSTPINAccentShade":
                    applyPinBgColors(xRes, Xprefs.getInt(PREF_ACCENT1_COLOR, Color.RED), true);
                    break;
                case "DSTPINRainbow":
                    applyPinBgColors(xRes, Xprefs.getInt(PREF_PIN_RAINBOW_COLOR, 0xFFFFFFFF), false);
                    break;
            }
        }

        // PIN number text colour
        String pinNumPref = Xprefs.getString(PREF_PIN_NUM, null);
        if (pinNumPref != null) {
            switch (pinNumPref) {
                case "DSTNUMPINAccent":
                    applyPinNumColor(xRes, Xprefs.getInt(PREF_ACCENT1_COLOR, Color.RED));
                    break;
                case "DSTNUMPINAccentShade": {
                    int a = Xprefs.getInt(PREF_ACCENT1_COLOR, Color.RED);
                    applyPinNumColor(xRes, 0x80000000 | (a & 0x00FFFFFF));
                    break;
                }
                case "DSTNUMPINRainbow":
                    applyPinNumColor(xRes, Xprefs.getInt(PREF_PIN_NUM_RAINBOW_COLOR, 0xFFFF0000));
                    break;
            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {}

    @Override
    public boolean listensTo(String packageName) {
        return SYSTEM_UI.equals(packageName);
    }

    private void applyPinBgColors(XResources xRes, int accent, boolean isShade) {
        int rgb    = accent & 0x00FFFFFF;
        int full   = 0xFF000000 | rgb;
        int ripple = 0x80000000 | rgb;
        int outer1 = 0xCC000000 | rgb;
        int outer2 = 0x40000000 | rgb;
        int outer3 = 0x21000000 | rgb;
        int shadow = isShade ? ripple : full;

        xRes.setReplacement(SYSTEM_UI, "color", "coui_numeric_keyboard_border_color",             full);
        xRes.setReplacement(SYSTEM_UI, "color", "coui_numeric_keyboard_inner_gradient_color_1",   ripple);
        xRes.setReplacement(SYSTEM_UI, "color", "coui_numeric_keyboard_inner_gradient_color_2",   ripple);
        xRes.setReplacement(SYSTEM_UI, "color", "coui_numeric_keyboard_upper_inner_shadow_color",  shadow);
        xRes.setReplacement(SYSTEM_UI, "color", "coui_numeric_keyboard_outer_gradient_color_1",   outer1);
        xRes.setReplacement(SYSTEM_UI, "color", "coui_numeric_keyboard_outer_gradient_color_2",   outer2);
        xRes.setReplacement(SYSTEM_UI, "color", "coui_numeric_keyboard_outer_gradient_color_3",   outer3);
        xRes.setReplacement(SYSTEM_UI, "color", "coui_simple_lock_transparent_filled_rectangle_icon_color",   full);
        xRes.setReplacement(SYSTEM_UI, "color", "coui_simple_lock_transparent_outlined_rectangle_icon_color", 0x33FFFFFF);
        xRes.setReplacement(SYSTEM_UI, "color", "coui_numeric_keyboard_dark_word_text_normal_color",       full);
        xRes.setReplacement(SYSTEM_UI, "color", "coui_numeric_keyboard_dark_word_text_normal_light_color", full);
    }

    private void applyPinNumColor(XResources xRes, int color) {
        xRes.setReplacement(SYSTEM_UI, "color", "coui_numeric_keyboard_number_color",
                0xFF000000 | (color & 0x00FFFFFF));
    }
}
