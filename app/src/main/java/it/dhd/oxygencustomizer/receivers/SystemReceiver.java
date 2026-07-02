package it.dhd.oxygencustomizer.receivers;

/*
 *  Copyright (C) 2015 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.utils.OCPreferences;
import it.dhd.oxygencustomizer.utils.UpdateScheduler;
import it.dhd.oxygencustomizer.utils.WeatherScheduler;
import it.dhd.oxygencustomizer.utils.overlay.FabricatedUtil;

public class SystemReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            WeatherScheduler.scheduleUpdates(context);
            WeatherScheduler.scheduleUpdateNow(context);
            UpdateScheduler.scheduleUpdates(context);

            // Update QS Clock on BOOT_COMPLETED
            Intent broadcast = new Intent(Constants.ACTIONS_BOOT_COMPLETED);
            broadcast.putExtra("packageName", SYSTEM_UI);
            context.sendBroadcast(broadcast);

            // Re-apply DST PIN fabricated overlays so SystemUI picks them up
            // (Magisk post-exec.sh may run before SystemUI has fully loaded its resources;
            //  re-enabling here guarantees the resource-reload notification fires while SystemUI is running)
            reapplyPinFabricatedOnBoot();
        }
    }

    private static void reapplyPinFabricatedOnBoot() {
        String pinPref = OCPreferences.getString("DST_PRESET_PIN", null);
        if (!"DSTPINAccent".equals(pinPref) && !"DSTPINAccentShade".equals(pinPref)) return;

        new Thread(() -> {
            // Small delay to let OOS theme service finish its own overlay setup
            try { Thread.sleep(5000); } catch (InterruptedException ignored) {}

            boolean isShade = "DSTPINAccentShade".equals(pinPref);
            int accent = OCPreferences.getInt("DSTACCENT1", Color.RED);
            int rgb    = accent & 0x00FFFFFF;
            String hex    = String.format("0x%08X", 0xFF000000 | rgb);
            String ripple = String.format("0x%08X", 0x80000000 | rgb);
            String outer1 = String.format("0x%08X", 0xCC000000 | rgb);
            String outer2 = String.format("0x%08X", 0x40000000 | rgb);
            String outer3 = String.format("0x%08X", 0x21000000 | rgb);
            String shadow  = isShade ? ripple : hex;

            FabricatedUtil.buildAndEnableOverlays(
                new Object[]{"com.android.systemui", "DSTNKBborder",     "color", "coui_numeric_keyboard_border_color",                        hex},
                new Object[]{"com.android.systemui", "DSTNKBinner1",     "color", "coui_numeric_keyboard_inner_gradient_color_1",               ripple},
                new Object[]{"com.android.systemui", "DSTNKBinner2",     "color", "coui_numeric_keyboard_inner_gradient_color_2",               ripple},
                new Object[]{"com.android.systemui", "DSTNKBshadow",     "color", "coui_numeric_keyboard_upper_inner_shadow_color",             shadow},
                new Object[]{"com.android.systemui", "DSTNKBouter1",     "color", "coui_numeric_keyboard_outer_gradient_color_1",               outer1},
                new Object[]{"com.android.systemui", "DSTNKBouter2",     "color", "coui_numeric_keyboard_outer_gradient_color_2",               outer2},
                new Object[]{"com.android.systemui", "DSTNKBouter3",     "color", "coui_numeric_keyboard_outer_gradient_color_3",               outer3},
                new Object[]{"com.android.systemui", "DSTNKBdotfillT",    "color", "coui_simple_lock_transparent_filled_rectangle_icon_color",   hex},
                new Object[]{"com.android.systemui", "DSTNKBdotoutlineT", "color", "coui_simple_lock_transparent_outlined_rectangle_icon_color", "0x33FFFFFF"},
                new Object[]{"com.android.systemui", "DSTNKBwordtxt",    "color", "coui_numeric_keyboard_dark_word_text_normal_color",          hex},
                new Object[]{"com.android.systemui", "DSTNKBwordtxtL",   "color", "coui_numeric_keyboard_dark_word_text_normal_light_color",    hex}
            );
        }).start();
    }
}