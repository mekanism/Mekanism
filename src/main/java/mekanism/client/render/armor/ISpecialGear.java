package mekanism.client.render.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public interface ISpecialGear extends IClientItemExtensions {

    @NotNull
    ICustomArmor getGearModel(EquipmentSlot slot);
}