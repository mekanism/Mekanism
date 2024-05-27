package mekanism.common.attachments.containers.chemical.slurry;

import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.chemical.ComponentBackedChemicalTank;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ComponentBackedSlurryTank extends ComponentBackedChemicalTank<Slurry, SlurryStack, AttachedSlurries> implements ISlurryTank {

    public ComponentBackedSlurryTank(ItemStack attachedTo, int tankIndex, BiPredicate<@NotNull Slurry, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull Slurry, @NotNull AutomationType> canInsert, Predicate<@NotNull Slurry> validator, LongSupplier rate, LongSupplier capacity,
          @Nullable ChemicalAttributeValidator attributeValidator) {
        super(attachedTo, tankIndex, canExtract, canInsert, validator, rate, capacity, attributeValidator);
    }

    @Override
    protected ContainerType<?, AttachedSlurries, ?> containerType() {
        return ContainerType.SLURRY;
    }
}