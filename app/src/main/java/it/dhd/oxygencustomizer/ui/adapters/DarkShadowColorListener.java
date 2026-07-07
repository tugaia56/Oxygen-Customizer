package it.dhd.oxygencustomizer.ui.adapters;

import it.dhd.oxygencustomizer.ui.models.DarkShadowItem;

public interface DarkShadowColorListener {
    void onEnabledClicked(DarkShadowItem darkShadowItem);
    void onDisabledClicked(DarkShadowItem darkShadowItem);
}