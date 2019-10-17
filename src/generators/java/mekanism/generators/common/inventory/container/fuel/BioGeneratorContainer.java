package mekanism.generators.common.inventory.container.fuel;

import mekanism.common.base.LazyOptionalHelper;
import mekanism.generators.common.GeneratorTags;
import mekanism.generators.common.inventory.container.GeneratorsContainerTypes;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidUtil;

public class BioGeneratorContainer extends FuelGeneratorContainer<TileEntityBioGenerator> {

    public BioGeneratorContainer(int id, PlayerInventory inv, TileEntityBioGenerator tile) {
        super(GeneratorsContainerTypes.BIO_GENERATOR, id, inv, tile);
    }

    public BioGeneratorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityBioGenerator.class));
    }

    @Override
    protected boolean tryFuel(ItemStack slotStack) {
        if (tile.getFuel(slotStack) > 0) {
            return true;
        }
        return new LazyOptionalHelper<>(FluidUtil.getFluidContained(slotStack)).matches(fluidStack -> fluidStack.getFluid().isIn(GeneratorTags.BIO_ETHANOL));
    }
}