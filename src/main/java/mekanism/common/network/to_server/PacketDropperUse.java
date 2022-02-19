package mekanism.common.network.to_server;

import java.util.List;
import java.util.Optional;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.capabilities.chemical.dynamic.IGasTracker;
import mekanism.common.capabilities.chemical.dynamic.IInfusionTracker;
import mekanism.common.capabilities.chemical.dynamic.IPigmentTracker;
import mekanism.common.capabilities.chemical.dynamic.ISlurryTracker;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketDropperUse implements IMekanismPacket {

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

    @Override
    public void handle(NetworkEvent.Context context) {
        PlayerEntity player = context.getSender();
        if (player == null || tankId < 0) {
            return;
        }
        ItemStack stack = player.inventory.getCarried();
        if (!stack.isEmpty() && stack.getItem() instanceof ItemGaugeDropper) {
            TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.level, pos);
            if (tile != null) {
                if (tile instanceof TileEntityMultiblock) {
                    MultiblockData structure = ((TileEntityMultiblock<?>) tile).getMultiblock();
                    if (structure.isFormed()) {
                        handleTankType(structure, player, stack, new Coord4D(structure.getBounds().getCenter(), player.level));
                    }
                } else {
                    if (action == DropperAction.DUMP_TANK && !player.isCreative()) {
                        //If the dropper is being used to dump the tank and the player is not in creative
                        // check if the block the tank is in is a tiered block and if it is, and it is creative
                        // don't allow clearing the tank
                        Block block = tile.getBlockType();
                        if (Attribute.has(block, AttributeTier.class) && Attribute.get(block, AttributeTier.class).getTier().getBaseTier() == BaseTier.CREATIVE) {
                            return;
                        }
                    }
                    handleTankType(tile, player, stack, tile.getTileCoord());
                }
            }
        }
    }

    private <HANDLER extends IMekanismFluidHandler & IGasTracker & IInfusionTracker & IPigmentTracker & ISlurryTracker> void handleTankType(HANDLER handler,
          PlayerEntity player, ItemStack stack, Coord4D coord) {
        if (tankType == TankType.FLUID_TANK) {
            IExtendedFluidTank fluidTank = handler.getFluidTank(tankId, null);
            if (fluidTank != null) {
                handleFluidTank(player, stack, fluidTank);
            }
        } else if (tankType == TankType.GAS_TANK) {
            handleChemicalTanks(player, stack, handler.getGasTanks(null), coord);
        } else if (tankType == TankType.INFUSION_TANK) {
            handleChemicalTanks(player, stack, handler.getInfusionTanks(null), coord);
        } else if (tankType == TankType.PIGMENT_TANK) {
            handleChemicalTanks(player, stack, handler.getPigmentTanks(null), coord);
        } else if (tankType == TankType.SLURRY_TANK) {
            handleChemicalTanks(player, stack, handler.getSlurryTanks(null), coord);
        }
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> void handleChemicalTanks(
          PlayerEntity player, ItemStack stack, List<TANK> tanks, Coord4D coord) {
        //This method is a workaround for Eclipse's compiler showing an error/warning if we try to just assign the tanks
        // to a variable in handleTankType and then have the size check and call to handleChemicalTank happen there
        if (tankId < tanks.size()) {
            handleChemicalTank(player, stack, tanks.get(tankId), coord);
        }
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void handleChemicalTank(PlayerEntity player, ItemStack stack,
          IChemicalTank<CHEMICAL, STACK> tank, Coord4D coord) {
        if (action == DropperAction.DUMP_TANK) {
            //Dump the tank
            if (!tank.isEmpty()) {
                if (tank instanceof IGasTank) {
                    //If the tank is a gas tank and has radioactive substances in it make sure we properly emit the radiation to the environment
                    MekanismAPI.getRadiationManager().dumpRadiation(coord, ((IGasTank) tank).getStack());
                }
                tank.setEmpty();
            }
        } else {
            Optional<IChemicalHandler<CHEMICAL, STACK>> cap = stack.getCapability(ChemicalUtil.getCapabilityForChemical(tank)).resolve();
            if (cap.isPresent()) {
                IChemicalHandler<CHEMICAL, STACK> handler = cap.get();
                if (handler instanceof IMekanismChemicalHandler) {
                    IChemicalTank<CHEMICAL, STACK> itemTank = ((IMekanismChemicalHandler<CHEMICAL, STACK, ?>) handler).getChemicalTank(0, null);
                    //It is a chemical tank
                    if (itemTank != null) {
                        //Validate something didn't go terribly wrong, and we actually do have the tank we expect to have
                        if (action == DropperAction.FILL_DROPPER) {
                            //Insert chemical into dropper
                            transferBetweenTanks(tank, itemTank, player);
                        } else if (action == DropperAction.DRAIN_DROPPER) {
                            //Extract chemical from dropper
                            transferBetweenTanks(itemTank, tank, player);
                        }
                    }
                }
            }
        }
    }

    private void handleFluidTank(PlayerEntity player, ItemStack stack, IExtendedFluidTank fluidTank) {
        if (action == DropperAction.DUMP_TANK) {
            //Dump the tank
            fluidTank.setEmpty();
            return;
        }
        Optional<IFluidHandlerItem> capability = FluidUtil.getFluidHandler(stack).resolve();
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
                    ((ServerPlayerEntity) player).refreshContainer(player.containerMenu);
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
                    ((ServerPlayerEntity) player).refreshContainer(player.containerMenu);
                }
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeEnum(action);
        buffer.writeEnum(tankType);
        buffer.writeVarInt(tankId);
    }

    public static PacketDropperUse decode(PacketBuffer buffer) {
        return new PacketDropperUse(buffer.readBlockPos(), buffer.readEnum(DropperAction.class), buffer.readEnum(TankType.class), buffer.readVarInt());
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