package mekanism.client.render.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.client.IItemRenderProperties;
import org.jetbrains.annotations.NotNull;

public interface ISpecialGear extends IItemRenderProperties {

    @NotNull
    ICustomArmor getGearModel(EquipmentSlot slot);
}