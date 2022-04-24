package mekanism.client.render.armor;

import javax.annotation.Nonnull;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.client.IItemRenderProperties;

public interface ISpecialGear extends IItemRenderProperties {

    @Nonnull
    ICustomArmor getGearModel(EquipmentSlot slot);
}