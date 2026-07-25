package mekanism.common.integration.computercraft;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import mekanism.common.Mekanism;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.computer.ComputerFilterHelper;
import mekanism.common.tile.TileEntityBoundingBlock;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(value = Mekanism.MODID, depends = MekanismHooks.CC_MOD_ID)
public class MekanismComputerCraft {

    public MekanismComputerCraft(IEventBus modEventBus) {
        modEventBus.addListener(EventPriority.LOW, FMLCommonSetupEvent.class, _ -> ComputerCraftAPI.registerAPIFactory(CCApiObject.create(ComputerFilterHelper.class, "mekanismFilterHelper")));
        //If ComputerCraft is loaded add the capability for it
        modEventBus.addListener(RegisterCapabilitiesEvent.class, event -> TileEntityBoundingBlock.proxyCapability(event, PeripheralCapability.get()));
    }
}