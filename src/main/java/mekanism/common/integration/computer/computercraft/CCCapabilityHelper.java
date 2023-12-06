package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import java.util.function.BooleanSupplier;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.computer.ComputerEnergyHelper;
import mekanism.common.integration.computer.ComputerFilterHelper;
import mekanism.common.integration.computer.IComputerTile;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister.BlockEntityTypeBuilder;
import mekanism.common.tile.base.CapabilityTileEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

public class CCCapabilityHelper {

    //TODO: Note this may need to be moved into another class after updating to avoid class loading issues without CC present
    // We also will likely need to update the capabilities name as odds are I did not guess what they will choose correctly
    private static final BlockCapability<IPeripheral, @Nullable Direction> CAPABILITY = BlockCapability.createSided(new ResourceLocation(MekanismHooks.CC_MOD_ID, "peripheral"), IPeripheral.class);
    private static final ICapabilityProvider<?, @Nullable Direction, IPeripheral> PROVIDER = getProvider();

    private static <TILE extends CapabilityTileEntity & IComputerTile> ICapabilityProvider<TILE, @Nullable Direction, IPeripheral> getProvider() {
        return CapabilityTileEntity.capabilityProvider(CAPABILITY, (tile, cap) -> {
            if (tile.isComputerCapabilityPersistent()) {
                return BasicCapabilityResolver.persistent(cap, () -> MekanismPeripheral.create(tile));
            }
            return BasicCapabilityResolver.create(cap, () -> MekanismPeripheral.create(tile));
        });
    }

    @SuppressWarnings("unchecked")
    public static <TILE extends CapabilityTileEntity & IComputerTile> void addCapability(BlockEntityTypeBuilder<TILE> builder, BooleanSupplier supportsComputer) {
        builder.with(CAPABILITY, (ICapabilityProvider<? super TILE, @Nullable Direction, IPeripheral>) PROVIDER, supportsComputer);
    }

    public static void registerApis() {
        ComputerCraftAPI.registerAPIFactory(CCApiObject.create(ComputerEnergyHelper.class, "mekanismEnergyHelper"));
        ComputerCraftAPI.registerAPIFactory(CCApiObject.create(ComputerFilterHelper.class, "mekanismFilterHelper"));
    }
}
