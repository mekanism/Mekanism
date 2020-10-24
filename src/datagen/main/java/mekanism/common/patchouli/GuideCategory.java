package mekanism.common.patchouli;

import java.util.Locale;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.Mekanism;
import net.minecraft.util.Util;

/**
 * Created by Thiakil on 19/05/2020.
 */
public enum GuideCategory {
    ITEMS,
    ITEMS_METAL_AND_ORE,
    ITEMS_GEAR,
    ITEMS_UPGRADES,
    BLOCKS,
    MULTIBLOCKS,
    ORE_PROCESSING,
    CHEMICALS
    ;

    public final String id;
    private final String translationKey;

    GuideCategory() {
        this.id = name().toLowerCase(Locale.ROOT);
        this.translationKey = Util.makeTranslationKey("guidebook", Mekanism.rl("category."+id));
    }

    public String getTranslationKeyName() {
        return translationKey+".name";
    }

    public String getTranslationKeyDescription() {
        return translationKey+".description";
    }
}
