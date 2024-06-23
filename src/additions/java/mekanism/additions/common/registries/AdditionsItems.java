package mekanism.additions.common.registries;

import java.util.EnumMap;
import java.util.Map;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.item.ItemBalloon;
import mekanism.additions.common.item.ItemWalkieTalkie;
import mekanism.api.text.EnumColor;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.util.EnumUtils;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

public class AdditionsItems {

    private AdditionsItems() {
    }

    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(MekanismAdditions.MODID);

    public static final ItemRegistryObject<DeferredSpawnEggItem> BABY_BOGGED_SPAWN_EGG = ITEMS.registerSpawnEgg(AdditionsEntityTypes.BABY_BOGGED, 0x8FB85A, 0x1D3B06);
    public static final ItemRegistryObject<DeferredSpawnEggItem> BABY_CREEPER_SPAWN_EGG = ITEMS.registerSpawnEgg(AdditionsEntityTypes.BABY_CREEPER, 0x31E02F, 0x1E1E1E);
    public static final ItemRegistryObject<DeferredSpawnEggItem> BABY_ENDERMAN_SPAWN_EGG = ITEMS.registerSpawnEgg(AdditionsEntityTypes.BABY_ENDERMAN, 0x242424, 0x1E1E1E);
    public static final ItemRegistryObject<DeferredSpawnEggItem> BABY_SKELETON_SPAWN_EGG = ITEMS.registerSpawnEgg(AdditionsEntityTypes.BABY_SKELETON, 0xFFFFFF, 0x800080);
    public static final ItemRegistryObject<DeferredSpawnEggItem> BABY_STRAY_SPAWN_EGG = ITEMS.registerSpawnEgg(AdditionsEntityTypes.BABY_STRAY, 0x7B9394, 0xF2FAFA);
    public static final ItemRegistryObject<DeferredSpawnEggItem> BABY_WITHER_SKELETON_SPAWN_EGG = ITEMS.registerSpawnEgg(AdditionsEntityTypes.BABY_WITHER_SKELETON, 0x303030, 0x525454);
    public static final ItemRegistryObject<ItemWalkieTalkie> WALKIE_TALKIE = ITEMS.registerItem("walkie_talkie", ItemWalkieTalkie::new);

    public static final Map<EnumColor, ItemRegistryObject<ItemBalloon>> BALLOONS = new EnumMap<>(EnumColor.class);

    static {
        for (EnumColor color : EnumUtils.COLORS) {
            BALLOONS.put(color, ITEMS.register(color.getRegistryPrefix() + "_balloon", () -> new ItemBalloon(color)));
        }
    }
}