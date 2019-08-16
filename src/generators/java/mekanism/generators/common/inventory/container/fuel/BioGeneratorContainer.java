package mekanism.generators.common.inventory.container.fuel;

import javax.annotation.Nonnull;
import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.inventory.slot.SlotEnergy.SlotCharge;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.generators.common.inventory.container.GeneratorsContainerTypes;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidUtil;

public class BioGeneratorContainer extends FuelGeneratorContainer<TileEntityBioGenerator> {

    public BioGeneratorContainer(int id, PlayerInventory inv, TileEntityBioGenerator tile) {
        super(GeneratorsContainerTypes.BIO_GENERATOR, id, inv, tile);
    }

    public BioGeneratorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityBioGenerator.class));
    }

    @Override
    protected void addSlots() {
        addSlot(new Slot(tile, 0, 17, 35));
        addSlot(new SlotCharge(tile, 1, 143, 35));
    }

    @Override
    protected boolean tryFuel(ItemStack slotStack) {
        if (tile.getFuel(slotStack) > 0) {
            return true;
        }
        if (FluidRegistry.isFluidRegistered("bioethanol")) {
            return new LazyOptionalHelper<>(FluidUtil.getFluidContained(slotStack)).matches(fluidStack -> fluidStack.getFluid() == FluidRegistry.getFluid("bioethanol"));
        }
        return false;
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanismgenerators.container.bio_generator");
    }
}