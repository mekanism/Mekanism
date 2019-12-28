package mekanism.additions.common.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.common.registration.impl.ItemDeferredRegister;
import net.minecraft.entity.EntityType;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;

public class BabySkeletonSpawnEggItem extends SpawnEggItem {

    public BabySkeletonSpawnEggItem() {
        //Note: We pass null for now so that it does not override "pick block" on skeletons or some other existing type
        super(null, 0xFFFFFF, 0x800080, ItemDeferredRegister.getMekBaseProperties());
    }

    @Nonnull
    @Override
    public EntityType<?> getType(@Nullable CompoundNBT nbt) {
        if (nbt != null && nbt.contains("EntityTag", NBT.TAG_COMPOUND)) {
            CompoundNBT entityTag = nbt.getCompound("EntityTag");
            if (entityTag.contains("id", 8)) {
                return EntityType.byKey(entityTag.getString("id")).orElse(AdditionsEntityTypes.BABY_SKELETON.getEntityType());
            }
        }
        return AdditionsEntityTypes.BABY_SKELETON.getEntityType();
    }
}