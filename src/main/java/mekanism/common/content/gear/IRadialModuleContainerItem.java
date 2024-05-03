package mekanism.common.content.gear;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
import mekanism.common.lib.radial.IGenericRadialModeItem;
import mekanism.common.lib.radial.data.NestingRadialData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IRadialModuleContainerItem extends IModuleContainerItem, IGenericRadialModeItem {

    ResourceLocation getRadialIdentifier();

    @Nullable
    @Override
    default RadialData<?> getRadialData(ItemStack stack) {
        List<NestedRadialMode> nestedModes = new ArrayList<>();
        Consumer<NestedRadialMode> adder = nestedModes::add;
        for (IModule<?> module : getModules(stack)) {
            if (module.handlesRadialModeChange()) {
                addRadialModes(module, stack, adder);
            }
        }
        if (nestedModes.isEmpty()) {
            //No modes available, return that we don't actually currently support radials
            return null;
        } else if (nestedModes.size() == 1) {
            //If we only have one mode available, just return it rather than having to select the singular mode
            return nestedModes.getFirst().nestedData();
        }
        return new NestingRadialData(getRadialIdentifier(), nestedModes);
    }

    @Nullable
    @Override
    default <M extends IRadialMode> M getMode(ItemStack stack, RadialData<M> radialData) {
        for (IModule<?> module : getModules(stack)) {
            if (module.handlesRadialModeChange()) {
                M mode = getMode(module, stack, radialData);
                if (mode != null) {
                    return mode;
                }
            }
        }
        return null;
    }

    @Override
    default <M extends IRadialMode> void setMode(ItemStack stack, Player player, RadialData<M> radialData, M mode) {
        IModuleContainer moduleContainer = moduleContainer(stack);
        if (moduleContainer != null) {
            for (IModule<?> module : moduleContainer.modules()) {
                if (module.handlesRadialModeChange() && setMode(module, player, moduleContainer, stack, radialData, mode)) {
                    return;
                }
            }
        }
    }

    private static <MODULE extends ICustomModule<MODULE>> void addRadialModes(IModule<MODULE> module, ItemStack stack, Consumer<NestedRadialMode> adder) {
        module.getCustomInstance().addRadialModes(module, stack, adder);
    }

    @Nullable
    private static <M extends IRadialMode, MODULE extends ICustomModule<MODULE>> M getMode(IModule<MODULE> module, ItemStack stack, RadialData<M> radialData) {
        return module.getCustomInstance().getMode(module, stack, radialData);
    }

    private static <M extends IRadialMode, MODULE extends ICustomModule<MODULE>> boolean setMode(IModule<MODULE> module, Player player, IModuleContainer moduleContainer,
          ItemStack stack, RadialData<M> radialData, M mode) {
        return module.getCustomInstance().setMode(module, player, moduleContainer, stack, radialData, mode);
    }
}