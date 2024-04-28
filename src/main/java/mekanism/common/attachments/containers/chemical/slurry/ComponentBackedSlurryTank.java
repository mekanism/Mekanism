package mekanism.common.attachments.containers.chemical.slurry;

import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.AutomationType;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.attachments.containers.chemical.ComponentBackedChemicalTank;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComponentBackedSlurryTank extends ComponentBackedChemicalTank<Slurry, SlurryStack, AttachedSlurries> implements ISlurryTank {

    public ComponentBackedSlurryTank(ItemStack attachedTo, int tankIndex, BiPredicate<@NotNull Slurry, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull Slurry, @NotNull AutomationType> canInsert, Predicate<@NotNull Slurry> validator, LongSupplier rate, LongSupplier capacity,
          @Nullable ChemicalAttributeValidator attributeValidator) {
        super(attachedTo, tankIndex, canExtract, canInsert, validator, rate, capacity, attributeValidator);
    }

    @Override
    protected Supplier<? extends DataComponentType<AttachedSlurries>> dataComponentType() {
        return MekanismDataComponents.ATTACHED_SLURRIES;
    }
}