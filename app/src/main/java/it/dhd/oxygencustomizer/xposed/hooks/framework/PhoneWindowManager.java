package it.dhd.oxygencustomizer.xposed.hooks.framework;

import static android.content.Context.RECEIVER_EXPORTED;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.ACTION_SCREENSHOT;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.graphics.Insets;
import android.graphics.ParcelableColorSpace;
import android.graphics.Rect;
import android.hardware.HardwareBuffer;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Constructor;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;

@SuppressWarnings("RedundantThrows")
public class PhoneWindowManager extends XposedMods {
	public static final String listenPackage = FRAMEWORK;

	private static final int TAKE_SCREENSHOT_PROVIDED_IMAGE = 3;

	private static final String KEY_BUFFER = "bitmap_util_buffer";
	private static final String KEY_COLOR_SPACE = "bitmap_util_color_space";

	private Object windowMan = null;
	private static boolean broadcastRegistered = false;
	private Object mDisplayManagerInternal;
	private Display mDefaultDisplay;
	private Object mDefaultDisplayPolicy;
	private Object mHandler;
//	private boolean ScreenshotChordInsecure;
//	private final ArrayList<Object> screenshotChords = new ArrayList<>();
	private Class<?> ScreenshotRequestClass;
	private List<UserHandle> userHandleList;
	private String currentPackage = "";
	private int currentUser = -1;

	public PhoneWindowManager(Context context) {
		super(context);
	}

	@Override
	public void updatePrefs(String... Key) {
//		ScreenshotChordInsecure = Xprefs.getBoolean("ScreenshotChordInsecure", false);
	}

	final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				String action = intent.getAction();
                if (action.equals(ACTION_SCREENSHOT)) {
                    try {
                        callMethod(windowMan, "handleScreenShot", 1); //13 QPR3
                    } catch (Throwable ignored) {
                        callMethod(windowMan, "handleScreenShot", 1, 1); //pre 13 QPR3
                    }
                }
			} catch (Throwable ignored) {}
		}
	};


	private boolean isPackageAvailableForUser(String packageName, UserHandle userHandle) {
		//noinspection unchecked
		return ((List<PackageInfo>)callMethod(mContext.getPackageManager(), "getInstalledPackagesAsUser", PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA), getObjectField(userHandle, "mHandle"))).stream().anyMatch(packageInfo -> packageInfo.packageName.equals(packageName) && packageInfo.applicationInfo.enabled);
	}

	@SuppressLint("WrongConstant")
	@Override
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpParam) throws Throwable {
		//noinspection unchecked

//		Collections.addAll(screenshotChords, KEYCODE_POWER, KEYCODE_VOLUME_DOWN);

		if (!broadcastRegistered) {
			broadcastRegistered = true;

			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(ACTION_SCREENSHOT);
			mContext.registerReceiver(broadcastReceiver, intentFilter, RECEIVER_EXPORTED); //for Android 14, receiver flag is mandatory
		}

		try {
			Class<?> PhoneWindowManagerClass = findClass("com.android.server.policy.PhoneWindowManager", lpParam.classLoader);


			ScreenshotRequestClass = findClassIfExists("com.android.internal.util.ScreenshotRequest", lpParam.classLoader); //13 QPR3
			hookAllMethods(PhoneWindowManagerClass, "enableScreen", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					windowMan = param.thisObject;
				}
			});

		} catch (Throwable ignored) {}
	}

	@SuppressLint("MissingPermission")
	private void sendAppProfileSwitchAvailable(boolean isAvailable) {
		new Thread(() -> {
			Intent broadcast = new Intent();
			broadcast.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
			broadcast.putExtra("available", isAvailable);
			mContext.sendBroadcast(broadcast);
		}).start();
	}

	/** @noinspection unused*/
	@Deprecated
	private void takeInsecureScreenshot() {
		if (mDisplayManagerInternal == null) {
			initVars();
		}

		Object SCBuffer = callMethod(mDisplayManagerInternal, "systemScreenshot", mDefaultDisplay.getDisplayId());

		if(ScreenshotRequestClass != null)
		{
			Bitmap screenshotBitmap = (Bitmap) callMethod(SCBuffer, "asBitmap");

			Object screenshotRequest = null;
			try {
				for(Constructor<?> constructor: ScreenshotRequestClass.getDeclaredConstructors())
				{
					if(constructor.getParameterCount() == 8)
					{
						screenshotRequest = constructor.newInstance(
								TAKE_SCREENSHOT_PROVIDED_IMAGE /* type */,
								0 /* source. 0 is global actions */,
								new ComponentName("", ""), 1 /* task id*/ ,
								0 /* user id */,
								screenshotBitmap,
								new Rect(0, 0, screenshotBitmap.getWidth(), screenshotBitmap.getHeight()),
								Insets.of(0, 0, 0, 0));
					}
				}
			}
			catch (Throwable ignored){}

			if(screenshotRequest != null)
			{
				callMethod(getObjectField(mDefaultDisplayPolicy, "mScreenshotHelper"),
						"takeScreenshot",
						screenshotRequest,
						mHandler,
						null);
			}
		}
		else
		{
			HardwareBuffer mHardwareBuffer = (HardwareBuffer) getObjectField(SCBuffer, "mHardwareBuffer");

			ParcelableColorSpace colorSpace = new ParcelableColorSpace((ColorSpace) getObjectField(SCBuffer, "mColorSpace"));

			Bundle bundle = new Bundle();
			bundle.putParcelable(KEY_BUFFER, mHardwareBuffer);
			bundle.putParcelable(KEY_COLOR_SPACE, colorSpace);

			callMethod(getObjectField(mDefaultDisplayPolicy, "mScreenshotHelper"),
					"provideScreenshot",
					bundle,
					new Rect(0, 0, mHardwareBuffer.getWidth(), mHardwareBuffer.getHeight()),
					Insets.of(0, 0, 0, 0),
					1,
					1,
					new ComponentName("", ""),
					0,
					mHandler,
					null);
		}
	}

	private void initVars() {
		mDisplayManagerInternal = getObjectField(windowMan, "mDisplayManagerInternal");
		mDefaultDisplay = (Display) getObjectField(windowMan, "mDefaultDisplay");
		mDefaultDisplayPolicy = getObjectField(windowMan, "mDefaultDisplayPolicy");
		mHandler = getObjectField(windowMan, "mHandler");
	}

	@Override
	public boolean listensTo(String packageName) {
		return listenPackage.equals(packageName);
	}
}