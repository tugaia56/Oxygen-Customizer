package it.dhd.oxygencustomizer.utils;

import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.dhd.oxygencustomizer.ui.models.DarkShadowItem;

public final class DarkShadowUtils {

    public static final DarkShadowItem ACCENT1 =
            new DarkShadowItem("ACCENT1", "Main Accent Dark", "xx",
                    new ArrayList<>() {{
                        add("accent_material_dark");
                        add("holo_blue_light");
                        // system_accent1 shades that reference accent_material_dark in OOS theme
                        add("system_accent1_100"); add("system_accent1_200"); add("system_accent1_300");
                        add("system_accent1_400"); add("system_accent1_500"); add("system_accent1_600");
                        add("system_accent1_700");
                        // system_accent2 shades (toggle track, etc.)
                        add("system_accent2_100"); add("system_accent2_200"); add("system_accent2_300");
                        add("system_accent2_400"); add("system_accent2_600"); add("system_accent2_700");
                        add("system_accent2_800");
                        // system_accent3 shades
                        add("system_accent3_100"); add("system_accent3_200"); add("system_accent3_300");
                        add("system_accent3_400"); add("system_accent3_500"); add("system_accent3_600");
                        add("system_accent3_700"); add("system_accent3_800");
                    }},
                    new ArrayList<>() {{
                        add("android");
                    }},
                    Color.RED
            );


    public static final DarkShadowItem ACCENT2 =
            new DarkShadowItem("ACCENT2", "Main Accent Light", "",
                    new ArrayList<>() {{
                        add("accent_material_light");
                    }},
                    new ArrayList<>() {{
                        add("android");
                    }},
                    Color.YELLOW
            );


    public static final DarkShadowItem ACCENT3 =
            new DarkShadowItem("ACCENT3", "Main Ripple Accent", "",
                    new ArrayList<>() {{
                        add("ripple_material_dark");
                    }},
                    new ArrayList<>() {{
                        add("android");
                    }},
                    Color.BLUE
            );


    public static final DarkShadowItem BACKGROUND =
            new DarkShadowItem("BACKGROUND", "Background", "",
                    new ArrayList<>() {{
                        addAll(List.of("background_dark",
                                "legacy_primary", "legacy_primary_dark", "black",
                                "primary_dark_material_dark", "primary_material_dark"));
                    }},
                    Map.of(
                            "background_floating_material_dark", 25,
                            "button_material_dark", 30,
                            "holo_primary", 35,
                            "holo_light_primary_dark", 40,
                            "button_material_light", 45,
                            "holo_primary_dark", 50,
                            "background_holo_dark", 55,
                            "background_leanback_dark", 60
                    ),
                    Map.of(
                            "group_button_dialog_focused_holo_dark",  0xe6,  // 10% trasparente
                            "group_button_dialog_focused_holo_light", 0xcc,  // 20% trasparente
                            "group_button_dialog_pressed_holo_dark",  0xa6,  // 40% trasparente
                            "group_button_dialog_pressed_holo_light", 0x80,  // 50% trasparente
                            "car_blue_grey_800",                      0x33,  // 80% trasparente
                            "car_blue_grey_900",                      0x1a   // 90% trasparente
                            // background_device_default_dark, primary_device_default_dark,
                            // surface_dark rimossi: sono colori di sistema critici, la loro
                            // modifica via FabricatedOverlay forza un resource reload su tutte le app
                    ),
                    new ArrayList<>() {{
                        add("android");
                    }},
                    Color.DKGRAY
            );


    public static int getColor(DarkShadowItem darkShadowItem) {
        return OCPreferences.getInt("DST" + darkShadowItem.getOverlayName(), darkShadowItem.getColor());
    }

    public static void saveColor(DarkShadowItem darkShadowItem) {
        OCPreferences.putInt("DST" + darkShadowItem.getOverlayName(), darkShadowItem.getColor());
    }

}
