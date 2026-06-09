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
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public interface IRadialModuleContainerItem extends IModuleContainerItem, IGenericRadialModeItem {

    Identifier getRadialIdentifier();

    @Nullable
    @Override
    default <ITEM extends TypedInstance<Item> & DataComponentGetter> RadialData<?> getRadialData(ITEM instance) {
        List<NestedRadialMode> nestedModes = new ArrayList<>();
        Consumer<NestedRadialMode> adder = nestedModes::add;
        for (IModule<?> module : getModules(instance)) {
            if (module.handlesRadialModeChange()) {
                addRadialModes(module, instance, adder);
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
    default <ITEM extends TypedInstance<Item> & DataComponentGetter, M extends IRadialMode> M getMode(ITEM instance, RadialData<M> radialData) {
        for (IModule<?> module : getModules(instance)) {
            if (module.handlesRadialModeChange()) {
                M mode = getMode(module, instance, radialData);
                if (mode != null) {
                    return mode;
                }
            }
        }
        return null;
    }

    @Override
    default <M extends IRadialMode> void setMode(ItemAccess itemAccess, Player player, RadialData<M> radialData, M mode, @Nullable TransactionContext transaction) {
        IModuleContainer moduleContainer = moduleContainer(itemAccess);
        if (moduleContainer != null) {
            for (IModule<?> module : moduleContainer.modules()) {
                if (module.handlesRadialModeChange()) {
                    try (Transaction subTransaction = Transaction.open(transaction)) {
                        if (setMode(module, player, itemAccess, radialData, mode, subTransaction)) {
                            subTransaction.commit();
                            return;
                        }
                    }
                }
            }
        }
    }

    private static <ITEM extends TypedInstance<Item> & DataComponentGetter, MODULE extends ICustomModule<MODULE>> void addRadialModes(IModule<MODULE> module,
          ITEM instance, Consumer<NestedRadialMode> adder) {
        module.getCustomInstance().addRadialModes(module, instance, adder);
    }

    @Nullable
    private static <ITEM extends TypedInstance<Item> & DataComponentGetter, M extends IRadialMode, MODULE extends ICustomModule<MODULE>> M getMode(IModule<MODULE> module,
          ITEM instance, RadialData<M> radialData) {
        return module.getCustomInstance().getMode(module, instance, radialData);
    }

    private static <M extends IRadialMode, MODULE extends ICustomModule<MODULE>> boolean setMode(IModule<MODULE> module, Player player, ItemAccess itemAccess,
          RadialData<M> radialData, M mode, @Nullable TransactionContext transaction) {
        return module.getCustomInstance().setMode(module, player, itemAccess, radialData, mode, transaction);
    }
}