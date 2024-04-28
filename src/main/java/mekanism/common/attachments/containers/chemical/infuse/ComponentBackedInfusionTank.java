package mekanism.common.attachments.containers.chemical.infuse;

import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.AutomationType;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.attachments.containers.chemical.ComponentBackedChemicalTank;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComponentBackedInfusionTank extends ComponentBackedChemicalTank<InfuseType, InfusionStack, AttachedInfuseTypes> implements IInfusionTank {

    public ComponentBackedInfusionTank(ItemStack attachedTo, int tankIndex, BiPredicate<@NotNull InfuseType, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull InfuseType, @NotNull AutomationType> canInsert, Predicate<@NotNull InfuseType> validator, LongSupplier rate, LongSupplier capacity,
          @Nullable ChemicalAttributeValidator attributeValidator) {
        super(attachedTo, tankIndex, canExtract, canInsert, validator, rate, capacity, attributeValidator);
    }

    @Override
    protected Supplier<? extends DataComponentType<AttachedInfuseTypes>> dataComponentType() {
        return MekanismDataComponents.ATTACHED_INFUSE_TYPES;
    }
}