package mekanism.common.integration.computer.opencomputers2;

import java.util.function.BooleanSupplier;
import mekanism.common.integration.computer.IComputerTile;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister.BlockEntityTypeBuilder;
import mekanism.common.tile.base.CapabilityTileEntity;

public class OC2CapabilityHelper {

    //TODO: Note this may need to be moved into another class after updating to avoid class loading issues without OC2 present
    /*private static final BlockCapability<Device, @Nullable Direction> CAPABILITY = BlockCapability.create(new ResourceLocation(MekanismHooks.OC2_MOD_ID, name), Device.class, Direction.class);
    private static final ICapabilityProvider<?, @Nullable Direction, Device> PROVIDER = getProvider();

    private static <TILE extends CapabilityTileEntity & IComputerTile> ICapabilityProvider<? super TILE, @Nullable Direction, Device> getProvider() {
        return (tile, context) -> tile.getCapability(CAPABILITY, () -> {
            if (tile.isComputerCapabilityPersistent()) {
                return BasicCapabilityResolver.persistent(CAPABILITY, () -> MekanismDevice.create(tile));
            }
            return BasicCapabilityResolver.create(CAPABILITY, () -> MekanismDevice.create(tile));
        }, context);
    }*/

    @SuppressWarnings("unchecked")
    public static <TILE extends CapabilityTileEntity & IComputerTile> void addCapability(BlockEntityTypeBuilder<TILE, ?> builder, BooleanSupplier supportsComputer) {
        //TODO - 1.20.2: Reimplement when OC2 updates
        //builder.with(CAPABILITY, (ICapabilityProvider<? super TILE, @Nullable Direction, Device>) PROVIDER, supportsComputer);
    }
}