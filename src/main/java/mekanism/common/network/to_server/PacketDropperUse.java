package mekanism.common.network.to_server;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.tier.BaseTier;
import mekanism.common.Mekanism;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.advancements.triggers.UseGaugeDropperTrigger.UseDropperAction;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketDropperUse(BlockPos pos, DropperAction action, TankType tankType, int tankId) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketDropperUse> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("use_dropper"));
    public static final StreamCodec<ByteBuf, PacketDropperUse> STREAM_CODEC = StreamCodec.composite(
          BlockPos.STREAM_CODEC, PacketDropperUse::pos,
          DropperAction.STREAM_CODEC, PacketDropperUse::action,
          TankType.STREAM_CODEC, PacketDropperUse::tankType,
          ByteBufCodecs.VAR_INT, PacketDropperUse::tankId,
          PacketDropperUse::new
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketDropperUse> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        if (tankId >= 0 && context.player() instanceof ServerPlayer player) {
            ItemStack stack = player.containerMenu.getCarried();
            if (!stack.isEmpty() && stack.getItem() instanceof ItemGaugeDropper) {
                TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.level(), pos);
                if (tile != null) {
                    if (tile instanceof TileEntityMultiblock<?> multiblock) {
                        MultiblockData structure = multiblock.getMultiblock();
                        if (structure.isFormed()) {
                            handleTankType(structure, player, stack, player.level(), structure.getBounds().getCenter());
                        }
                    } else {
                        if (action == DropperAction.DUMP_TANK && !player.isCreative()) {
                            //If the dropper is being used to dump the tank and the player is not in creative
                            // check if the block the tank is in is a tiered block and if it is, and it is creative
                            // don't allow clearing the tank
                            if (Attribute.getBaseTier(tile.getBlockHolder()) == BaseTier.CREATIVE) {
                                return;
                            }
                        }
                        handleTankType(tile, player, stack, tile.getLevel(), tile.getBlockPos());
                    }
                }
            }
        }
    }

    private <HANDLER extends IMekanismFluidHandler & IMekanismChemicalHandler> void handleTankType(HANDLER handler, ServerPlayer player, ItemStack stack, Level level, BlockPos pos) {
        if (tankType == TankType.FLUID_TANK) {
            IExtendedFluidTank fluidTank = handler.getFluidTank(tankId, null);
            if (fluidTank != null) {
                handleFluidTank(player, stack, fluidTank);
            }
        } else if (tankType == TankType.CHEMICAL_TANK) {
            IChemicalTank chemicalTank = handler.getChemicalTank(tankId, null);
            if (chemicalTank != null) {
                handleChemicalTank(player, stack, chemicalTank, level, pos);
            }
        }
    }

    private void handleChemicalTank(ServerPlayer player, ItemStack stack, IChemicalTank tank, Level level, BlockPos pos) {
        if (action == DropperAction.DUMP_TANK) {
            //Dump the tank
            if (!tank.isEmpty()) {
                //If the tank has radioactive substances in it make sure we properly emit the radiation to the environment
                IRadiationManager.INSTANCE.dumpRadiation(level, pos, tank.getStack());
                tank.setEmpty();
                MekanismCriteriaTriggers.USE_GAUGE_DROPPER.value().trigger(player, UseDropperAction.DUMP);
            }
        } else {
            IChemicalHandler handler = Capabilities.CHEMICAL.getCapability(stack);
            if (handler instanceof IMekanismChemicalHandler chemicalHandler) {
                IChemicalTank itemTank = chemicalHandler.getChemicalTank(0, null);
                //It is a chemical tank
                if (itemTank != null) {
                    //Validate something didn't go terribly wrong, and we actually do have the tank we expect to have
                    if (action == DropperAction.FILL_DROPPER) {
                        //Insert chemical into dropper
                        transferBetweenTanks(tank, itemTank, player);
                        MekanismCriteriaTriggers.USE_GAUGE_DROPPER.value().trigger(player, UseDropperAction.FILL);
                    } else if (action == DropperAction.DRAIN_DROPPER) {
                        //Extract chemical from dropper
                        transferBetweenTanks(itemTank, tank, player);
                        MekanismCriteriaTriggers.USE_GAUGE_DROPPER.value().trigger(player, UseDropperAction.DRAIN);
                    }
                }
            }
        }
    }

    private void handleFluidTank(ServerPlayer player, ItemStack stack, IExtendedFluidTank fluidTank) {
        if (action == DropperAction.DUMP_TANK) {
            //Dump the tank
            fluidTank.setEmpty();
            MekanismCriteriaTriggers.USE_GAUGE_DROPPER.value().trigger(player, UseDropperAction.DUMP);
            return;
        }
        IFluidHandlerItem fluidHandlerItem = Capabilities.FLUID.getCapability(stack);
        if (fluidHandlerItem instanceof IMekanismFluidHandler fluidHandler) {
            IExtendedFluidTank itemFluidTank = fluidHandler.getFluidTank(0, null);
            if (itemFluidTank != null) {
                if (action == DropperAction.FILL_DROPPER) {
                    //Insert fluid into dropper
                    transferBetweenTanks(fluidTank, itemFluidTank, player);
                    MekanismCriteriaTriggers.USE_GAUGE_DROPPER.value().trigger(player, UseDropperAction.FILL);
                } else if (action == DropperAction.DRAIN_DROPPER) {
                    //Extract fluid from dropper
                    transferBetweenTanks(itemFluidTank, fluidTank, player);
                    MekanismCriteriaTriggers.USE_GAUGE_DROPPER.value().trigger(player, UseDropperAction.DRAIN);
                }
            }
        }
    }

    private static void transferBetweenTanks(IChemicalTank drainTank, IChemicalTank fillTank, Player player) {
        if (!drainTank.isEmpty() && fillTank.getNeeded() > 0) {
            ChemicalStack chemicalInDrainTank = drainTank.getStack();
            ChemicalStack simulatedRemainder = fillTank.insert(chemicalInDrainTank, Action.SIMULATE, AutomationType.MANUAL);
            long remainder = simulatedRemainder.getAmount();
            long amount = chemicalInDrainTank.getAmount();
            if (remainder < amount) {
                //We are able to fit at least some of the chemical from our drain tank into the fill tank
                ChemicalStack extractedChemical = drainTank.extract(amount - remainder, Action.EXECUTE, AutomationType.MANUAL);
                if (!extractedChemical.isEmpty()) {
                    //If we were able to actually extract it from our tank, then insert it into the tank
                    MekanismUtils.logMismatchedStackSize(fillTank.insert(extractedChemical, Action.EXECUTE, AutomationType.MANUAL).getAmount(), 0);
                    player.containerMenu.synchronizeCarriedToRemote();
                }
            }
        }
    }

    private static void transferBetweenTanks(IExtendedFluidTank drainTank, IExtendedFluidTank fillTank, Player player) {
        if (!drainTank.isEmpty() && fillTank.getNeeded() > 0) {
            FluidStack fluidInDrainTank = drainTank.getFluid();
            FluidStack simulatedRemainder = fillTank.insert(fluidInDrainTank, Action.SIMULATE, AutomationType.MANUAL);
            int remainder = simulatedRemainder.getAmount();
            int amount = fluidInDrainTank.getAmount();
            if (remainder < amount) {
                //We are able to fit at least some of the fluid from our drain tank into the fill tank
                FluidStack extractedFluid = drainTank.extract(amount - remainder, Action.EXECUTE, AutomationType.MANUAL);
                if (!extractedFluid.isEmpty()) {
                    //If we were able to actually extract it from our tank, then insert it into the tank
                    MekanismUtils.logMismatchedStackSize(fillTank.insert(extractedFluid, Action.EXECUTE, AutomationType.MANUAL).getAmount(), 0);
                    player.containerMenu.synchronizeCarriedToRemote();
                }
            }
        }
    }

    public enum DropperAction {
        FILL_DROPPER,
        DRAIN_DROPPER,
        DUMP_TANK;

        public static final IntFunction<DropperAction> BY_ID = ByIdMap.continuous(DropperAction::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, DropperAction> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, DropperAction::ordinal);
    }

    public enum TankType {
        CHEMICAL_TANK,
        FLUID_TANK;

        public static final IntFunction<TankType> BY_ID = ByIdMap.continuous(TankType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, TankType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, TankType::ordinal);
    }
}