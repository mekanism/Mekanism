package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class EnergyRecipeData implements RecipeUpgradeData<EnergyRecipeData> {

    private final List<IEnergyContainer> energyContainers;

    EnergyRecipeData(ListTag containers) {
        int count = DataHandlerUtils.getMaxId(containers, NBTConstants.CONTAINER);
        energyContainers = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            energyContainers.add(BasicEnergyContainer.create(FloatingLong.MAX_VALUE, null));
        }
        DataHandlerUtils.readContainers(energyContainers, containers);
    }

    private EnergyRecipeData(List<IEnergyContainer> energyContainers) {
        this.energyContainers = energyContainers;
    }

    @Nullable
    @Override
    public EnergyRecipeData merge(EnergyRecipeData other) {
        List<IEnergyContainer> allContainers = new ArrayList<>(energyContainers.size() + other.energyContainers.size());
        allContainers.addAll(energyContainers);
        allContainers.addAll(other.energyContainers);
        return new EnergyRecipeData(allContainers);
    }

    @Override
    public boolean applyToStack(ItemStack stack) {
        if (energyContainers.isEmpty()) {
            return true;
        }
        Item item = stack.getItem();
        Optional<IStrictEnergyHandler> capability = stack.getCapability(Capabilities.STRICT_ENERGY_CAPABILITY).resolve();
        List<IEnergyContainer> energyContainers = new ArrayList<>();
        if (capability.isPresent()) {
            IStrictEnergyHandler energyHandler = capability.get();
            for (int container = 0; container < energyHandler.getEnergyContainerCount(); container++) {
                energyContainers.add(BasicEnergyContainer.create(energyHandler.getMaxEnergy(container), null));
            }
        } else if (item instanceof BlockItem blockItem) {
            TileEntityMekanism tile = getTileFromBlock(blockItem.getBlock());
            if (tile == null || !tile.handles(SubstanceType.ENERGY)) {
                //Something went wrong
                return false;
            }
            for (int container = 0; container < tile.getEnergyContainerCount(); container++) {
                energyContainers.add(BasicEnergyContainer.create(tile.getMaxEnergy(container), null));
            }
        } else {
            return false;
        }
        if (energyContainers.isEmpty()) {
            //We don't actually have any tanks in the output
            return true;
        }
        IMekanismStrictEnergyHandler outputHandler = new IMekanismStrictEnergyHandler() {
            @Nonnull
            @Override
            public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
                return energyContainers;
            }

            @Override
            public void onContentsChanged() {
            }
        };
        boolean hasData = false;
        for (IEnergyContainer energyContainer : this.energyContainers) {
            if (!energyContainer.isEmpty()) {
                hasData = true;
                if (!outputHandler.insertEnergy(energyContainer.getEnergy(), Action.EXECUTE).isZero()) {
                    //If we have a remainder, stop trying to insert as our upgraded item's buffer is just full
                    break;
                }
            }
        }
        if (hasData) {
            //We managed to transfer it all into valid slots, so save it to the stack
            ItemDataUtils.writeContainers(stack, NBTConstants.ENERGY_CONTAINERS, energyContainers);
        }
        return true;
    }
}