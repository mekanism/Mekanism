package mekanism.common.attachments.containers.energy;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.tier.EnergyCubeTier;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class ComponentBackedEnergyCubeContainer extends ComponentBackedEnergyContainer {

    public static ComponentBackedEnergyCubeContainer create(ContainerType<?, ?, ?> ignored, ItemStack attachedTo, int containerIndex) {
        if (!(attachedTo.getItem() instanceof ItemBlockEnergyCube item)) {
            throw new IllegalStateException("Attached to should always be an energy cube item");
        }
        return new ComponentBackedEnergyCubeContainer(attachedTo, containerIndex, item.getTier());
    }

    private final boolean isCreative;

    private ComponentBackedEnergyCubeContainer(ItemStack attachedTo, int containerIndex, EnergyCubeTier tier) {
        super(attachedTo, containerIndex, BasicEnergyContainer.alwaysTrue, BasicEnergyContainer.alwaysTrue, tier::getOutput, tier::getMaxEnergy);
        isCreative = tier == EnergyCubeTier.CREATIVE;
    }

    @Override
    public FloatingLong insert(FloatingLong amount, Action action, AutomationType automationType) {
        return super.insert(amount, action.combine(!isCreative), automationType);
    }

    @Override
    public FloatingLong extract(FloatingLong amount, Action action, AutomationType automationType) {
        return super.extract(amount, action.combine(!isCreative), automationType);
    }
}