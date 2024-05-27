package mekanism.common.attachments.containers.chemical.pigment;

import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.chemical.ComponentBackedChemicalTank;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ComponentBackedPigmentTank extends ComponentBackedChemicalTank<Pigment, PigmentStack, AttachedPigments> implements IPigmentTank {

    public ComponentBackedPigmentTank(ItemStack attachedTo, int tankIndex, BiPredicate<@NotNull Pigment, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull Pigment, @NotNull AutomationType> canInsert, Predicate<@NotNull Pigment> validator, LongSupplier rate, LongSupplier capacity,
          @Nullable ChemicalAttributeValidator attributeValidator) {
        super(attachedTo, tankIndex, canExtract, canInsert, validator, rate, capacity, attributeValidator);
    }

    @Override
    protected ContainerType<?, AttachedPigments, ?> containerType() {
        return ContainerType.PIGMENT;
    }
}