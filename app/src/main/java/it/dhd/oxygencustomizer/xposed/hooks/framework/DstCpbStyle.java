package it.dhd.oxygencustomizer.xposed.hooks.framework;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.content.res.XResources;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.ResourceManager;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class DstCpbStyle extends XposedMods {

    private static final String PREF_CPB = "DST_PRESET_CPB";

    private String mPreset = null;

    public DstCpbStyle(Context context) { super(context); }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;
        mPreset = Xprefs.getString(PREF_CPB, null);
        if (Key.length > 0 && PREF_CPB.equals(Key[0])) {
            initResources();
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // No method hooks needed — only XResources replacements
    }

    @Override
    public boolean listensTo(String packageName) {
        return SYSTEM_UI.equals(packageName);
    }

    @Override
    public void initResources() {
        if (mPreset == null) return;

        final int outerRes;
        final int innerRes;
        final boolean tintOuter;
        final boolean tintInner;

        switch (mPreset) {
            case "DSTCPB1": // Aurora Theme — white stars outer (no tint), accent crescent inner
                outerRes  = R.drawable.dst_cpb_aurora_outer;
                innerRes  = R.drawable.dst_cpb_aurora_inner;
                tintOuter = false;
                tintInner = true;
                break;
            case "DSTCPB2": // Arrow — accent arrow outer, transparent arrow inner
                outerRes  = R.drawable.dst_cpb_arrow_outer;
                innerRes  = R.drawable.dst_cpb_arrow_inner;
                tintOuter = true;
                tintInner = false;
                break;
            case "DSTCPB3": // Radioactive — accent symbol outer, transparent inner
                outerRes  = R.drawable.dst_cpb_radioactive_outer;
                innerRes  = R.drawable.dst_cpb_radioactive_inner;
                tintOuter = true;
                tintInner = false;
                break;
            case "DSTCPB4": // Sprite Theme — accent quarters outer, semi-white tinted inner
                outerRes  = R.drawable.dst_cpb_sprite_outer;
                innerRes  = R.drawable.dst_cpb_sprite_inner;
                tintOuter = true;
                tintInner = true;
                break;
            case "DSTCPB5": // Stars — accent star-in-circle outer, transparent inner
                outerRes  = R.drawable.dst_cpb_stars_outer;
                innerRes  = R.drawable.dst_cpb_stars_inner;
                tintOuter = true;
                tintInner = false;
                break;
            default:
                return;
        }

        XResources.DrawableLoader outerLoader = new XResources.DrawableLoader() {
            @Override
            public Drawable newDrawable(XResources xresources, int id) throws Throwable {
                if (ResourceManager.modRes == null) throw new RuntimeException("modRes null");
                Drawable d = ResourceManager.modRes.getDrawable(outerRes, null).mutate();
                if (tintOuter) {
                    d.setTint(getAndroidColor(xresources, "accent_material_dark", 0xFF0097FF));
                }
                return d;
            }
        };

        XResources.DrawableLoader innerLoader = new XResources.DrawableLoader() {
            @Override
            public Drawable newDrawable(XResources xresources, int id) throws Throwable {
                if (ResourceManager.modRes == null) throw new RuntimeException("modRes null");
                Drawable d = ResourceManager.modRes.getDrawable(innerRes, null).mutate();
                if (tintInner) {
                    d.setTint(getAndroidColor(xresources, "accent_material_dark", 0xFF0097FF));
                }
                return d;
            }
        };

        // Apply to ALL packages in resparams — the CPB may be rendered in a process
        // other than SYSTEM_UI (android framework, OPlus launcher, etc.).
        for (XC_InitPackageResources.InitPackageResourcesParam rp :
                new ArrayList<>(ResourceManager.resparams.values())) {
            try {
                for (String size : new String[]{"76", "48", "16"}) {
                    rp.res.setReplacement("android", "drawable", "spinner_" + size + "_outer_holo", outerLoader);
                    rp.res.setReplacement("android", "drawable", "spinner_" + size + "_inner_holo", innerLoader);
                }
            } catch (Throwable t) { /* package may not have these drawables */ }
        }
    }

    private int getAndroidColor(XResources xres, String name, int fallback) {
        try {
            int id = xres.getIdentifier(name, "color", "android");
            if (id != 0) return xres.getColor(id, null);
        } catch (Throwable ignored) {}
        return fallback;
    }
}
