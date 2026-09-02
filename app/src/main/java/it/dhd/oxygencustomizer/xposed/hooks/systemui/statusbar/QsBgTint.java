package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsBgTint.QS_BG_TINT_ALPHA;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsBgTint.QS_BG_TINT_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsBgTint.QS_BG_TINT_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsBgTint.QS_BG_TINT_PREFS;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class QsBgTint extends XposedMods {

    private static final String listenPackage = Constants.Packages.SYSTEM_UI;

    private final List<FrameLayout> mTintViews = new ArrayList<>();

    private boolean qsBgTintEnabled = false;
    private int qsBgTintColor = 0xFF000000;
    private int qsBgTintAlpha = 128;

    private final ControllersProvider.ExpandedQsFractionChangeListener mExpandedListener = fraction -> {
        float alpha = Math.min(fraction / 0.86f, 1.0f);
        for (FrameLayout v : mTintViews) v.setAlpha(alpha);
    };

    public QsBgTint(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;
        qsBgTintEnabled = Xprefs.getBoolean(QS_BG_TINT_ENABLED, false);
        qsBgTintColor   = Xprefs.getInt(QS_BG_TINT_COLOR, 0xFF000000);
        qsBgTintAlpha   = Xprefs.getSliderInt(QS_BG_TINT_ALPHA, 128);

        if (Key.length > 0) {
            for (String pref : QS_BG_TINT_PREFS) {
                if (Key[0].equals(pref)) {
                    updateTint();
                    break;
                }
            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            ReflectedClass OplusQSContainerImpl = ReflectedClass.of(
                    "com.oplus.systemui.qs.OplusQSContainerImpl",
                    "com.oplusos.systemui.qs.OplusQSContainerImpl"
            );

            try {
                OplusQSContainerImpl
                        .after("onFinishInflate")
                        .run(param -> addTintView((FrameLayout) param.thisObject));

                OplusQSContainerImpl
                        .after("updateResources")
                        .run(param -> updateTint());
            } catch (Throwable ignored) {}

            ReflectedClass OplusQSRootView = ReflectedClass.ofIfPossible(
                    "com.oplus.systemui.plugins.qs.OplusQSRootView");
            if (OplusQSRootView.getClazz() != null) {
                OplusQSRootView
                        .after("onFinishInflate")
                        .run(param -> addTintView((FrameLayout) param.thisObject));
            }

            ControllersProvider.registerExpandedQsFractionChangeCallback(mExpandedListener);
        } catch (Throwable t) {
            log(t);
        }
    }

    private void addTintView(FrameLayout root) {
        FrameLayout tintView = new FrameLayout(mContext);
        tintView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        tintView.setVisibility(View.GONE);
        root.addView(tintView, 0);
        mTintViews.add(tintView);
        updateTint();
    }

    private void updateTint() {
        if (mTintViews.isEmpty()) return;
        if (!qsBgTintEnabled) {
            for (FrameLayout v : mTintViews) v.post(() -> v.setVisibility(View.GONE));
            return;
        }
        int color = Color.argb(
                qsBgTintAlpha,
                Color.red(qsBgTintColor),
                Color.green(qsBgTintColor),
                Color.blue(qsBgTintColor)
        );
        for (FrameLayout v : mTintViews) {
            v.post(() -> {
                v.setBackgroundColor(color);
                v.setAlpha(1f);
                v.setVisibility(View.VISIBLE);
            });
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
