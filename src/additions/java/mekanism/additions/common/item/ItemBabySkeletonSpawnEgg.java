package mekanism.additions.common.item;

import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.entity.AdditionsEntityType;
import mekanism.common.Mekanism;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.ResourceLocation;

public class ItemBabySkeletonSpawnEgg extends SpawnEggItem {

    public ItemBabySkeletonSpawnEgg() {
        super(AdditionsEntityType.BABY_SKELETON.getEntityType(), 0xFFFFFF, 0x800080, new Item.Properties().group(Mekanism.tabMekanism));
        setRegistryName(new ResourceLocation(MekanismAdditions.MODID, "baby_skeleton_spawn_egg"));
    }
}