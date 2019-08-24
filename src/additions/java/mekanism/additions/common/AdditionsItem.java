package mekanism.additions.common;

import javax.annotation.Nonnull;
import mekanism.additions.common.item.ItemBabySkeletonSpawnEgg;
import mekanism.additions.common.item.ItemBalloon;
import mekanism.api.IItemProvider;
import mekanism.api.text.EnumColor;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

public enum AdditionsItem implements IItemProvider {
    BABY_SKELETON_SPAWN_EGG(new ItemBabySkeletonSpawnEgg()),

    BLACK_BALLOON(new ItemBalloon(EnumColor.BLACK)),
    RED_BALLOON(new ItemBalloon(EnumColor.RED)),
    GREEN_BALLOON(new ItemBalloon(EnumColor.DARK_GREEN)),
    BROWN_BALLOON(new ItemBalloon(EnumColor.BROWN)),
    BLUE_BALLOON(new ItemBalloon(EnumColor.DARK_BLUE)),
    PURPLE_BALLOON(new ItemBalloon(EnumColor.PURPLE)),
    CYAN_BALLOON(new ItemBalloon(EnumColor.DARK_AQUA)),
    LIGHT_GRAY_BALLOON(new ItemBalloon(EnumColor.GRAY)),
    GRAY_BALLOON(new ItemBalloon(EnumColor.DARK_GRAY)),
    PINK_BALLOON(new ItemBalloon(EnumColor.BRIGHT_PINK)),
    LIME_BALLOON(new ItemBalloon(EnumColor.BRIGHT_GREEN)),
    YELLOW_BALLOON(new ItemBalloon(EnumColor.YELLOW)),
    LIGHT_BLUE_BALLOON(new ItemBalloon(EnumColor.INDIGO)),
    MAGENTA_BALLOON(new ItemBalloon(EnumColor.PINK)),
    ORANGE_BALLOON(new ItemBalloon(EnumColor.ORANGE)),
    WHITE_BALLOON(new ItemBalloon(EnumColor.WHITE));

    private final Item item;

    AdditionsItem(Item item) {
        this.item = item;
    }

    @Override
    @Nonnull
    public Item getItem() {
        return item;
    }

    public static void registerItems(IForgeRegistry<Item> registry) {
        for (AdditionsItem additionsItem : values()) {
            registry.register(additionsItem.getItem());
        }
    }
}