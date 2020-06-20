package mekanism.additions.common.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.common.registration.impl.EntityTypeRegistryObject;
import mekanism.common.registration.impl.ItemDeferredRegister;
import net.minecraft.entity.EntityType;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;

public class AdditionsSpawnEggItem extends SpawnEggItem {

    private final EntityTypeRegistryObject<?> entityTypeRO;

    @SuppressWarnings("ConstantConditions")
    public AdditionsSpawnEggItem(EntityTypeRegistryObject<?> entityTypeRO, int primaryColor, int secondaryColor) {
        //Note: We pass null for now so that it does not override "pick block" on skeletons or some other existing type
        super(null, primaryColor, secondaryColor, ItemDeferredRegister.getMekBaseProperties());
        this.entityTypeRO = entityTypeRO;
    }

    @Nonnull
    @Override
    public EntityType<?> getType(@Nullable CompoundNBT nbt) {
        if (nbt != null && nbt.contains(NBTConstants.ENTITY_TAG, NBT.TAG_COMPOUND)) {
            CompoundNBT entityTag = nbt.getCompound(NBTConstants.ENTITY_TAG);
            if (entityTag.contains(NBTConstants.ID, NBT.TAG_STRING)) {
                return EntityType.byKey(entityTag.getString(NBTConstants.ID)).orElse(entityTypeRO.getEntityType());
            }
        }
        return entityTypeRO.getEntityType();
    }
}