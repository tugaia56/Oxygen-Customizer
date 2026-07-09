package it.dhd.oxygencustomizer.xposed;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.HashMap;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;

public class ResourceManager implements IXposedHookInitPackageResources, IXposedHookZygoteInit {

    private static String MODULE_PATH = null;
    public final static HashMap<String, XC_InitPackageResources.InitPackageResourcesParam> resparams = new HashMap<>();
    public static Resources modRes;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        // Check if this is a re-initialization (e.g. Monet overlay update after boot).
        // LSPosed fires handleInitPackageResources every time a new ResourcesImpl is
        // applied via ResourcesManager.applyNewResourceImpl — which happens whenever the
        // Monet/ThemeOverlayController refreshes dynamic colors. Without re-applying our
        // setReplacement calls on the fresh ResourcesImpl, the accent color reverts to the
        // Monet blue a few seconds after SystemUI starts.
        boolean isReInit = resparams.containsKey(resparam.packageName);
        resparams.put(resparam.packageName, resparam);

        if (isReInit && !XPLauncher.runningMods.isEmpty()) {
            for (XposedMods mod : new ArrayList<>(XPLauncher.runningMods)) {
                try {
                    mod.initResources();
                } catch (Throwable t) {
                    XposedBridge.log("[ OC ResourceManager ] initResources error in "
                            + mod.getClass().getSimpleName() + ": " + t);
                }
            }
        }
    }

    public static String getModulePath() {
        return MODULE_PATH;
    }

}
