package mekanism.additions.common.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.additions.common.entity.AdditionsEntityType;
import mekanism.common.registration.impl.ItemDeferredRegister;
import net.minecraft.entity.EntityType;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;

public class BabySkeletonSpawnEggItem extends SpawnEggItem {

    public BabySkeletonSpawnEggItem() {
        super(EntityType.SKELETON, 0xFFFFFF, 0x800080, ItemDeferredRegister.getMekBaseProperties());
        //TODO: Should we pass it "null" given I think this might be overriding the normal skeleton spawn egg at least pick block on skeleton returned spawn egg
    }

    @Nonnull
    @Override
    public EntityType<?> getType(@Nullable CompoundNBT nbt) {
        if (nbt != null && nbt.contains("EntityTag", NBT.TAG_COMPOUND)) {
            CompoundNBT entityTag = nbt.getCompound("EntityTag");
            if (entityTag.contains("id", 8)) {
                return EntityType.byKey(entityTag.getString("id")).orElse(AdditionsEntityType.BABY_SKELETON.getEntityType());
            }
        }
        return AdditionsEntityType.BABY_SKELETON.getEntityType();
    }
}