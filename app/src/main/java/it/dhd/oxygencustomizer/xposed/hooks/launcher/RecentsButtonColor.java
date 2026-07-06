package it.dhd.oxygencustomizer.xposed.hooks.launcher;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.LAUNCHER;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.getModulePath;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.resparams;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.XposedMods;

/**
 * Clear All button color for Recents.
 *
 * OOS 16+: color is applied via FabricatedUtil from Launcher.java (app side) —
 *          targets toggle_bar_apply_btn_enabled_color in com.android.launcher.
 * OOS 15:  XResources DrawableLoader intercepts recent_clear_circle and
 *          oplus_recent_clear_all at drawable-load time.
 */
public class RecentsButtonColor extends XposedMods {

    private static final String listenPackage = LAUNCHER;

    private boolean mEnabled = false;
    private int mColor = Color.WHITE;

    private XModuleResources moddedRes;

    public RecentsButtonColor(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;
        mEnabled = Xprefs.getBoolean("launcher_recent_btn_enabled", false);
        // Resolved color is written by Launcher.java (app side) via OCPreferences
        mColor = Xprefs.getInt("launcher_clear_all_resolved_color", Color.WHITE);
    }

    @Override
    public void initResources() {
        // OOS 15: intercept drawable loading for the Clear All circle button
        if (resparams == null || resparams.get(LAUNCHER) == null) return;
        XResources xRes = resparams.get(LAUNCHER).res;
        moddedRes = XModuleResources.createInstance(getModulePath(), xRes);

        xRes.setReplacement(LAUNCHER, "drawable", "recent_clear_circle",
                new XResources.DrawableLoader() {
                    @Override
                    public Drawable newDrawable(XResources res, int id) throws Throwable {
                        if (!mEnabled) return null;
                        GradientDrawable d = new GradientDrawable();
                        d.setColor(mColor);
                        int size = Math.round(48 * res.getDisplayMetrics().density);
                        d.setSize(size, size);
                        d.setCornerRadius(size / 2f);
                        return d;
                    }
                });

        xRes.setReplacement(LAUNCHER, "drawable", "oplus_recent_clear_all",
                new XResources.DrawableLoader() {
                    @Override
                    public Drawable newDrawable(XResources res, int id) throws Throwable {
                        if (!mEnabled || moddedRes == null) return null;
                        try {
                            return moddedRes.getDrawable(R.drawable.ic_recent_clear_all_white, null);
                        } catch (Throwable t) {
                            return null;
                        }
                    }
                });
    }

    @Override
    @SuppressLint("DiscouragedApi")
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // Color override for OOS 16+ is handled via FabricatedUtil from Launcher.java (app side).
        // No Xposed view hook needed here.
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
