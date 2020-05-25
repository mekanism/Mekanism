package mekanism.common.inventory.slot.chemical;

import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.slot.ChemicalInventorySlot;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PigmentInventorySlot extends ChemicalInventorySlot<Pigment, PigmentStack> {

    @Nullable
    public static IPigmentHandler getCapability(ItemStack stack) {
        if (!stack.isEmpty()) {
            Optional<IPigmentHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.PIGMENT_HANDLER_CAPABILITY));
            if (capability.isPresent()) {
                return capability.get();
            }
        }
        return null;
    }

    //TODO: Implement creators as needed
    private PigmentInventorySlot(IPigmentTank pigmentTank, Supplier<World> worldSupplier, Predicate<@NonNull ItemStack> canExtract,
          Predicate<@NonNull ItemStack> canInsert, Predicate<@NonNull ItemStack> validator, @Nullable IMekanismInventory inventory, int x, int y) {
        super(pigmentTank, worldSupplier, canExtract, canInsert, validator, inventory, x, y);
    }

    @Nullable
    @Override
    protected IChemicalHandler<Pigment, PigmentStack> getCapability() {
        return getCapability(current);
    }

    @Nullable
    @Override
    protected Pair<ItemStack, PigmentStack> getConversion() {
        return null;
    }
}