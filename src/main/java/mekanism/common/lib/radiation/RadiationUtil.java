package mekanism.common.lib.radiation;

import mekanism.api.radiation.IRadiationSource;
import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.api.radiation.capability.IRadiationShielding;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.util.EnumUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class RadiationUtil {

    /**
     * Calculates approximately how long in ticks radiation will take to decay
     *
     * @param magnitude Magnitude
     * @param source    {@code true} for if it is a {@link IRadiationSource} or an {@link IRadiationEntity} decaying
     */
    public static long getDecayTime(double magnitude, boolean source) {
        double decayRate = source ? MekanismConfig.general.radiationSourceDecayRate.get() : MekanismConfig.general.radiationTargetDecayRate.get();
        long ticks = 0;
        double localMagnitude = magnitude;
        while (localMagnitude > RadiationManager.get().minRadiationMagnitude()) {
            localMagnitude *= decayRate;
            ticks += SharedConstants.TICKS_PER_SECOND;
        }
        return ticks;
    }

    public static double getRadiationResistance(LivingEntity entity) {
        double resistance = 0;
        for (EquipmentSlot type : EnumUtils.ARMOR_SLOTS) {
            ItemStack stack = entity.getItemBySlot(type);
            if (!stack.isEmpty()) {
                IRadiationShielding shielding = stack.getCapability(Capabilities.RADIATION_SHIELDING);
                if (shielding != null) {
                    resistance += shielding.getRadiationShielding();
                }
            }
        }
        if (resistance < 1 && Mekanism.hooks.curios.isLoaded()) {
            IItemHandler handler = CuriosIntegration.getCuriosInventory(entity);
            if (handler != null) {
                for (int i = 0, slots = handler.getSlots(); i < slots; i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    IRadiationShielding shielding = stack.getCapability(Capabilities.RADIATION_SHIELDING);
                    if (shielding != null) {
                        resistance += shielding.getRadiationShielding();
                        if (resistance >= 1) {
                            return 1;
                        }
                    }
                }
            }
        }
        return resistance;
    }

    public static double computeExposure(RadiationSource source, BlockPos blockPos) {
        return source.getMagnitude() / Math.max(1, blockPos.distSqr(source.getPosition()));
    }
}
