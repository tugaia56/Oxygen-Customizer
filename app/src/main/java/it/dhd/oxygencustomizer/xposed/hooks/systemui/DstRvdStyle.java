package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.content.res.XResources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.ResourceManager;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class DstRvdStyle extends XposedMods {

    private static final String TILES_PKG = "com.android.systemui.tiles";
    private static final String PREF_RVD = "DST_PRESET_RVD";

    private String mPreset = null;

    public DstRvdStyle(Context context) { super(context); }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;
        mPreset = Xprefs.getString(PREF_RVD, null);
        if (Key.length > 0 && PREF_RVD.equals(Key[0])) {
            initResources();
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // No method hooks needed
    }

    @Override
    public boolean listensTo(String packageName) {
        return SYSTEM_UI.equals(packageName);
    }

    @Override
    public void initResources() {
        if (mPreset == null) return;
        XC_InitPackageResources.InitPackageResourcesParam resparam =
                ResourceManager.resparams.get(TILES_PKG);
        if (resparam == null) return;

        // Shared icon drawables (identical for all presets)
        try {
            resparam.res.setReplacement(TILES_PKG, "drawable", "more_row_stream_system",
                ResourceManager.modRes.getDrawable(R.drawable.dst_rvd_more_row_stream_system, null));
            resparam.res.setReplacement(TILES_PKG, "drawable", "more_row_stream_app",
                ResourceManager.modRes.getDrawable(R.drawable.dst_rvd_more_row_stream_app, null));
            resparam.res.setReplacement(TILES_PKG, "drawable", "systemui_icon_volume_double_ear_light",
                ResourceManager.modRes.getDrawable(R.drawable.dst_rvd_icon_double_ear_light, null));
            resparam.res.setReplacement(TILES_PKG, "drawable", "systemui_icon_volume_odi_captions",
                ResourceManager.modRes.getDrawable(R.drawable.dst_rvd_icon_odi_captions, null));
            resparam.res.setReplacement(TILES_PKG, "drawable", "systemui_icon_volume_odi_captions_disabled",
                ResourceManager.modRes.getDrawable(R.drawable.dst_rvd_icon_odi_captions_disabled, null));
        } catch (Throwable t) { log(t); }

        // Background drawables vary per preset — use DrawableLoader for dynamic android colors
        boolean isSquare = mPreset.startsWith("DSTSVD");
        String style = mPreset.replace("DSTRVD", "").replace("DSTSVD", "");

        XResources.DrawableLoader bgLoader = new XResources.DrawableLoader() {
            @Override
            public Drawable newDrawable(XResources xresources, int id) throws Throwable {
                GradientDrawable d = new GradientDrawable();
                float dp = mContext.getResources().getDisplayMetrics().density;
                if (isSquare) {
                    d.setShape(GradientDrawable.RECTANGLE);
                    d.setCornerRadius(8f * dp);
                } else {
                    d.setShape(GradientDrawable.OVAL);
                }
                switch (style) {
                    case "Accent":
                        d.setColor(getAndroidColor(xresources, "accent_material_dark", 0xFF0097FF));
                        break;
                    case "AccentShade":
                        d.setColor(getAndroidColor(xresources, "ripple_material_dark", 0x800097FF));
                        break;
                    case "DarkGray":
                        d.setColor(0xFF303030);
                        break;
                    case "LightGray":
                        d.setColor(0xFFBDBDBD);
                        break;
                    case "White":
                        d.setColor(0xFFFFFFFF);
                        break;
                    case "Outlined": {
                        int bg = getAndroidColor(xresources, "background_dark", 0xFF000000);
                        int accent = getAndroidColor(xresources, "accent_material_dark", 0xFF0097FF);
                        d.setColor(bg);
                        d.setStroke((int)(1f * dp + 0.5f), accent);
                        break;
                    }
                    case "SemiTrasp":
                        d.setColor(0x80000000);
                        break;
                    case "OutlinedTrasp": {
                        int accent2 = getAndroidColor(xresources, "accent_material_dark", 0xFF0097FF);
                        d.setColor(0x80000000);
                        d.setStroke((int)(1f * dp + 0.5f), accent2);
                        break;
                    }
                    default:
                        d.setColor(0xFFFFFFFF);
                }
                return d;
            }
        };

        try {
            resparam.res.setReplacement(TILES_PKG, "drawable", "systemui_icon_volume_app_adjust_bg", bgLoader);
            resparam.res.setReplacement(TILES_PKG, "drawable", "systemui_icon_volume_single_app_adjust_bg", bgLoader);
        } catch (Throwable t) { log(t); }
    }

    private int getAndroidColor(XResources xres, String name, int fallback) {
        try {
            int id = xres.getIdentifier(name, "color", "android");
            if (id != 0) return xres.getColor(id, null);
        } catch (Throwable ignored) {}
        return fallback;
    }
}
