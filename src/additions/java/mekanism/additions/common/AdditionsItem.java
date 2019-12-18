package mekanism.additions.common;

import mekanism.additions.common.item.BabySkeletonSpawnEggItem;
import mekanism.additions.common.item.ItemBalloon;
import mekanism.additions.common.item.ItemWalkieTalkie;
import mekanism.api.text.EnumColor;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import net.minecraft.item.SpawnEggItem;

public class AdditionsItem {

    public static ItemDeferredRegister ITEMS = new ItemDeferredRegister(MekanismAdditions.MODID);

    //TODO: FIXME We are required to use a custom item as we cannot use the base SpawnEggItem due to needing something like
    // https://github.com/MinecraftForge/MinecraftForge/pull/6299 to allow for lazy initialization
    public static final ItemRegistryObject<SpawnEggItem> BABY_SKELETON_SPAWN_EGG = ITEMS.register("baby_skeleton_spawn_egg", BabySkeletonSpawnEggItem::new);
    public static final ItemRegistryObject<ItemWalkieTalkie> WALKIE_TALKIE = ITEMS.register("walkie_talkie", ItemWalkieTalkie::new);

    public static final ItemRegistryObject<ItemBalloon> BLACK_BALLOON = registerBalloon(EnumColor.BLACK);
    public static final ItemRegistryObject<ItemBalloon> RED_BALLOON = registerBalloon(EnumColor.RED);
    public static final ItemRegistryObject<ItemBalloon> GREEN_BALLOON = registerBalloon(EnumColor.DARK_GREEN);
    public static final ItemRegistryObject<ItemBalloon> BROWN_BALLOON = registerBalloon(EnumColor.BROWN);
    public static final ItemRegistryObject<ItemBalloon> BLUE_BALLOON = registerBalloon(EnumColor.DARK_BLUE);
    public static final ItemRegistryObject<ItemBalloon> PURPLE_BALLOON = registerBalloon(EnumColor.PURPLE);
    public static final ItemRegistryObject<ItemBalloon> CYAN_BALLOON = registerBalloon(EnumColor.DARK_AQUA);
    public static final ItemRegistryObject<ItemBalloon> LIGHT_GRAY_BALLOON = registerBalloon(EnumColor.GRAY);
    public static final ItemRegistryObject<ItemBalloon> GRAY_BALLOON = registerBalloon(EnumColor.DARK_GRAY);
    public static final ItemRegistryObject<ItemBalloon> PINK_BALLOON = registerBalloon(EnumColor.BRIGHT_PINK);
    public static final ItemRegistryObject<ItemBalloon> LIME_BALLOON = registerBalloon(EnumColor.BRIGHT_GREEN);
    public static final ItemRegistryObject<ItemBalloon> YELLOW_BALLOON = registerBalloon(EnumColor.YELLOW);
    public static final ItemRegistryObject<ItemBalloon> LIGHT_BLUE_BALLOON = registerBalloon(EnumColor.INDIGO);
    public static final ItemRegistryObject<ItemBalloon> MAGENTA_BALLOON = registerBalloon(EnumColor.PINK);
    public static final ItemRegistryObject<ItemBalloon> ORANGE_BALLOON = registerBalloon(EnumColor.ORANGE);
    public static final ItemRegistryObject<ItemBalloon> WHITE_BALLOON = registerBalloon(EnumColor.WHITE);

    private static ItemRegistryObject<ItemBalloon> registerBalloon(EnumColor color) {
        return ITEMS.register(color.registry_prefix + "_balloon", () -> new ItemBalloon(color));
    }
}