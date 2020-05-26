package mekanism.common.network;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import mekanism.api.Action;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.chemical.dynamic.IGasTracker;
import mekanism.common.capabilities.chemical.dynamic.IInfusionTracker;
import mekanism.common.capabilities.chemical.dynamic.IPigmentTracker;
import mekanism.common.capabilities.chemical.dynamic.ISlurryTracker;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketDropperUse {

    private final BlockPos pos;
    private final DropperAction action;
    private final TankType tankType;
    private final int tankId;

    public PacketDropperUse(BlockPos pos, DropperAction action, TankType tankType, int tankId) {
        this.pos = pos;
        this.action = action;
        this.tankType = tankType;
        this.tankId = tankId;
    }

    public static void handle(PacketDropperUse message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            ItemStack stack = player.inventory.getItemStack();
            if (!stack.isEmpty() && stack.getItem() instanceof ItemGaugeDropper) {
                TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, player.world, message.pos);
                if (tile != null) {
                    if (tile instanceof TileEntityMultiblock<?>) {
                        MultiblockData structure = ((TileEntityMultiblock<?>) tile).getMultiblock();
                        if (structure.isFormed()) {
                            handleTankType(structure, message, player, stack);
                        }
                    } else {
                        handleTankType(tile, message, player, stack);
                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    private static <HANDLER extends IMekanismFluidHandler & IGasTracker & IInfusionTracker & IPigmentTracker & ISlurryTracker> void handleTankType(HANDLER handler,
          PacketDropperUse message, PlayerEntity player, ItemStack stack) {
        if (message.tankType == TankType.FLUID_TANK) {
            IExtendedFluidTank fluidTank = handler.getFluidTank(message.tankId, null);
            if (fluidTank != null) {
                handleFluidTank(player, stack, fluidTank, message.action);
            }
        } else {
            List<? extends IChemicalTank<?, ?>> tanks;
            switch (message.tankType) {
                case GAS_TANK:
                    tanks = handler.getGasTanks(null);
                    break;
                case INFUSION_TANK:
                    tanks = handler.getInfusionTanks(null);
                    break;
                case PIGMENT_TANK:
                    tanks = handler.getPigmentTanks(null);
                    break;
                case SLURRY_TANK:
                    tanks = handler.getSlurryTanks(null);
                    break;
                default:
                    tanks = Collections.emptyList();
            }
            if (message.tankId >= 0 && message.tankId < tanks.size()) {
                handleChemicalTank(player, stack, tanks.get(message.tankId), message.action);
            }
        }
    }

    public static void encode(PacketDropperUse pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeEnumValue(pkt.action);
        buf.writeEnumValue(pkt.tankType);
        buf.writeVarInt(pkt.tankId);
    }

    public static PacketDropperUse decode(PacketBuffer buf) {
        return new PacketDropperUse(buf.readBlockPos(), buf.readEnumValue(DropperAction.class), buf.readEnumValue(TankType.class), buf.readVarInt());
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void handleChemicalTank(PlayerEntity player, ItemStack stack,
          IChemicalTank<CHEMICAL, STACK> chemicalTank, DropperAction action) {
        if (action == DropperAction.DUMP_TANK) {
            //Dump the tank
            chemicalTank.setEmpty();
            return;
        }
        IChemicalTank<CHEMICAL, STACK> itemChemicalTank = null;
        //TODO: Eventually try to clean this up further to reduce duplicate code
        STACK emptyStack = chemicalTank.getEmptyStack();
        if (emptyStack == GasStack.EMPTY) {
            Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
            if (capability.isPresent()) {
                IGasHandler handler = capability.get();
                if (handler instanceof IMekanismChemicalHandler) {
                    itemChemicalTank = ((IMekanismChemicalHandler<CHEMICAL, STACK, ?>) handler).getChemicalTank(0, null);
                }
            }
        } else if (emptyStack == InfusionStack.EMPTY) {
            Optional<IInfusionHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.INFUSION_HANDLER_CAPABILITY));
            if (capability.isPresent()) {
                IInfusionHandler handler = capability.get();
                if (handler instanceof IMekanismChemicalHandler) {
                    itemChemicalTank = ((IMekanismChemicalHandler<CHEMICAL, STACK, ?>) handler).getChemicalTank(0, null);
                }
            }
        } else if (emptyStack == PigmentStack.EMPTY) {
            Optional<IPigmentHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.PIGMENT_HANDLER_CAPABILITY));
            if (capability.isPresent()) {
                IPigmentHandler handler = capability.get();
                if (handler instanceof IMekanismChemicalHandler) {
                    itemChemicalTank = ((IMekanismChemicalHandler<CHEMICAL, STACK, ?>) handler).getChemicalTank(0, null);
                }
            }
        } else if (emptyStack == SlurryStack.EMPTY) {
            Optional<ISlurryHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.SLURRY_HANDLER_CAPABILITY));
            if (capability.isPresent()) {
                ISlurryHandler handler = capability.get();
                if (handler instanceof IMekanismChemicalHandler) {
                    itemChemicalTank = ((IMekanismChemicalHandler<CHEMICAL, STACK, ?>) handler).getChemicalTank(0, null);
                }
            }
        }
        //It is a chemical tank
        if (itemChemicalTank != null) {
            //Validate something didn't go terribly wrong and we actually do have the tank we expect to have
            if (action == DropperAction.FILL_DROPPER) {
                //Insert chemical into dropper
                transferBetweenTanks(chemicalTank, itemChemicalTank, player);
            } else if (action == DropperAction.DRAIN_DROPPER) {
                //Extract chemical from dropper
                transferBetweenTanks(itemChemicalTank, chemicalTank, player);
            }
        }
    }

    private static void handleFluidTank(PlayerEntity player, ItemStack stack, IExtendedFluidTank fluidTank, DropperAction action) {
        if (action == DropperAction.DUMP_TANK) {
            //Dump the tank
            fluidTank.setEmpty();
            return;
        }
        Optional<IFluidHandlerItem> capability = MekanismUtils.toOptional(FluidUtil.getFluidHandler(stack));
        if (capability.isPresent()) {
            IFluidHandlerItem fluidHandlerItem = capability.get();
            if (fluidHandlerItem instanceof IMekanismFluidHandler) {
                IExtendedFluidTank itemFluidTank = ((IMekanismFluidHandler) fluidHandlerItem).getFluidTank(0, null);
                if (itemFluidTank != null) {
                    if (action == DropperAction.FILL_DROPPER) {
                        //Insert fluid into dropper
                        transferBetweenTanks(fluidTank, itemFluidTank, player);
                    } else if (action == DropperAction.DRAIN_DROPPER) {
                        //Extract fluid from dropper
                        transferBetweenTanks(itemFluidTank, fluidTank, player);
                    }
                }
            }
        }
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void transferBetweenTanks(IChemicalTank<CHEMICAL, STACK> drainTank,
          IChemicalTank<CHEMICAL, STACK> fillTank, PlayerEntity player) {
        if (!drainTank.isEmpty() && fillTank.getNeeded() > 0) {
            STACK chemicalInDrainTank = drainTank.getStack();
            STACK simulatedRemainder = fillTank.insert(chemicalInDrainTank, Action.SIMULATE, AutomationType.MANUAL);
            long remainder = simulatedRemainder.getAmount();
            long amount = chemicalInDrainTank.getAmount();
            if (remainder < amount) {
                //We are able to fit at least some of the chemical from our drain tank into the fill tank
                STACK extractedChemical = drainTank.extract(amount - remainder, Action.EXECUTE, AutomationType.MANUAL);
                if (!extractedChemical.isEmpty()) {
                    //If we were able to actually extract it from our tank, then insert it into the tank
                    MekanismUtils.logMismatchedStackSize(fillTank.insert(extractedChemical, Action.EXECUTE, AutomationType.MANUAL).getAmount(), 0);
                    ((ServerPlayerEntity) player).sendContainerToPlayer(player.openContainer);
                }
            }
        }
    }

    private static void transferBetweenTanks(IExtendedFluidTank drainTank, IExtendedFluidTank fillTank, PlayerEntity player) {
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
                    ((ServerPlayerEntity) player).sendContainerToPlayer(player.openContainer);
                }
            }
        }
    }

    public enum DropperAction {
        FILL_DROPPER,
        DRAIN_DROPPER,
        DUMP_TANK
    }

    public enum TankType {
        GAS_TANK,
        FLUID_TANK,
        INFUSION_TANK,
        PIGMENT_TANK,
        SLURRY_TANK
    }
}