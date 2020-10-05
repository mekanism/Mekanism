package mekanism.additions.common.registries;

import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.item.AdditionsSpawnEggItem;
import mekanism.additions.common.item.ItemBalloon;
import mekanism.additions.common.item.ItemWalkieTalkie;
import mekanism.api.text.EnumColor;
import mekanism.common.registration.impl.EntityTypeRegistryObject;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import net.minecraft.entity.Entity;

public class AdditionsItems {

    private AdditionsItems() {
    }

    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(MekanismAdditions.MODID);

    public static final ItemRegistryObject<AdditionsSpawnEggItem> BABY_CREEPER_SPAWN_EGG = registerSpawnEgg(AdditionsEntityTypes.BABY_CREEPER, 0x31E02F, 0x1E1E1E);
    public static final ItemRegistryObject<AdditionsSpawnEggItem> BABY_ENDERMAN_SPAWN_EGG = registerSpawnEgg(AdditionsEntityTypes.BABY_ENDERMAN, 0x242424, 0x1E1E1E);
    public static final ItemRegistryObject<AdditionsSpawnEggItem> BABY_SKELETON_SPAWN_EGG = registerSpawnEgg(AdditionsEntityTypes.BABY_SKELETON, 0xFFFFFF, 0x800080);
    public static final ItemRegistryObject<AdditionsSpawnEggItem> BABY_STRAY_SPAWN_EGG = registerSpawnEgg(AdditionsEntityTypes.BABY_STRAY, 0x7B9394, 0xF2FAFA);
    public static final ItemRegistryObject<AdditionsSpawnEggItem> BABY_WITHER_SKELETON_SPAWN_EGG = registerSpawnEgg(AdditionsEntityTypes.BABY_WITHER_SKELETON, 0x303030, 0x525454);
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
        return ITEMS.register(color.getRegistryPrefix() + "_balloon", () -> new ItemBalloon(color));
    }

    private static <ENTITY extends Entity> ItemRegistryObject<AdditionsSpawnEggItem> registerSpawnEgg(EntityTypeRegistryObject<ENTITY> entityTypeProvider,
          int primaryColor, int secondaryColor) {
        //Note: We are required to use a custom item as we cannot use the base SpawnEggItem due to the entity type not being initialized yet
        return ITEMS.register(entityTypeProvider.getInternalRegistryName() + "_spawn_egg", () -> new AdditionsSpawnEggItem(entityTypeProvider, primaryColor, secondaryColor));
    }
}