package mekanism.common.item.interfaces;

import java.util.Optional;
import java.util.function.Consumer;
import mekanism.api.chemical.Chemical;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.util.ChemicalUtils;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;

@FunctionalInterface
public interface IChemicalItem extends ICustomCreativeTabContents {

    ResourceKey<Chemical> getChemicalType();

    default boolean hasChemical(ItemAccess itemAccess) {
        return ChemicalUtils.hasChemicalOfType(itemAccess, getChemicalType());
    }

    default <ITEM extends TypedInstance<Item> & DataComponentGetter> boolean hasChemical(ITEM instance) {
        return hasChemical(ItemAccessUtils.sideEffectFreeAccess(instance));
    }

    @Override
    default void addItems(ItemDisplayParameters displayParameters, Holder<Item> item, Consumer<ItemStack> tabOutput) {
        Optional<Reference<Chemical>> chemical = displayParameters.holders().get(getChemicalType());
        //noinspection OptionalIsPresent - Capturing lambda
        if (chemical.isPresent()) {
            tabOutput.accept(ContainerType.CHEMICAL.getFilledVariant(item, chemical.get(), null));
        }
    }
}