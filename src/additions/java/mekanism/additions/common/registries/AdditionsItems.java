package mekanism.additions.common.registries;

import java.util.EnumMap;
import java.util.Map;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.item.ItemBalloon;
import mekanism.additions.common.item.ItemWalkieTalkie;
import mekanism.api.text.EnumColor;
import mekanism.common.registration.impl.EntityTypeRegistryObject;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.util.EnumUtils;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.ForgeSpawnEggItem;

public class AdditionsItems {

    private AdditionsItems() {
    }

    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(MekanismAdditions.MODID);

    public static final ItemRegistryObject<ForgeSpawnEggItem> BABY_CREEPER_SPAWN_EGG = registerSpawnEgg(AdditionsEntityTypes.BABY_CREEPER, 0x31E02F, 0x1E1E1E);
    public static final ItemRegistryObject<ForgeSpawnEggItem> BABY_ENDERMAN_SPAWN_EGG = registerSpawnEgg(AdditionsEntityTypes.BABY_ENDERMAN, 0x242424, 0x1E1E1E);
    public static final ItemRegistryObject<ForgeSpawnEggItem> BABY_SKELETON_SPAWN_EGG = registerSpawnEgg(AdditionsEntityTypes.BABY_SKELETON, 0xFFFFFF, 0x800080);
    public static final ItemRegistryObject<ForgeSpawnEggItem> BABY_STRAY_SPAWN_EGG = registerSpawnEgg(AdditionsEntityTypes.BABY_STRAY, 0x7B9394, 0xF2FAFA);
    public static final ItemRegistryObject<ForgeSpawnEggItem> BABY_WITHER_SKELETON_SPAWN_EGG = registerSpawnEgg(AdditionsEntityTypes.BABY_WITHER_SKELETON, 0x303030, 0x525454);
    public static final ItemRegistryObject<ItemWalkieTalkie> WALKIE_TALKIE = ITEMS.register("walkie_talkie", ItemWalkieTalkie::new);

    public static final Map<EnumColor, ItemRegistryObject<ItemBalloon>> BALLOONS = new EnumMap<>(EnumColor.class);

    static {
        for (EnumColor color : EnumUtils.COLORS) {
            BALLOONS.put(color, ITEMS.register(color.getRegistryPrefix() + "_balloon", () -> new ItemBalloon(color)));
        }
    }

    private static <ENTITY extends Entity> ItemRegistryObject<ForgeSpawnEggItem> registerSpawnEgg(EntityTypeRegistryObject<ENTITY> entityTypeProvider,
          int primaryColor, int secondaryColor) {
        //Note: We are required to use a custom item as we cannot use the base SpawnEggItem due to the entity type not being initialized yet
        return ITEMS.register(entityTypeProvider.getInternalRegistryName() + "_spawn_egg", props -> new ForgeSpawnEggItem(entityTypeProvider, primaryColor, secondaryColor, props));
    }
}