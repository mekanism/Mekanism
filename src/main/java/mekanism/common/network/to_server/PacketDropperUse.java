package mekanism.common.network.to_server;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.IntFunction;
import mekanism.api.AutomationType;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.container.IResourceContainer;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.tier.BaseTier;
import mekanism.common.Mekanism;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.advancements.triggers.UseGaugeDropperTrigger.UseDropperAction;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.MultiTypeCapability;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.ResourceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public record PacketDropperUse(DropperAction action, TankType tankType, int tankId) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketDropperUse> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("use_dropper"));
    public static final StreamCodec<ByteBuf, PacketDropperUse> STREAM_CODEC = StreamCodec.composite(
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
        //todo - 26.1: validate that this successfully gets the tile
        if (tankId >= 0 && context.player() instanceof ServerPlayer player && player.containerMenu instanceof MekanismTileContainer<?> mekTileContainer) {
            //TODO - 26.1: Validate if this automatically performs player.containerMenu.synchronizeCarriedToRemote();
            // Either way we might want to remove our manual calls, and then PR it doing so on root commit for the player cursor access
            ItemAccess itemAccess = ItemAccess.forPlayerCursor(player, mekTileContainer);
            ItemResource itemResource = itemAccess.getResource();
            if (!itemResource.isEmpty() && itemResource.getItem() instanceof ItemGaugeDropper) {
                TileEntityMekanism tile = mekTileContainer.getTileEntity();
                if (tile != null) {
                    if (tile instanceof TileEntityMultiblock<?> multiblock) {
                        MultiblockData structure = multiblock.getMultiblock();
                        if (structure.isFormed()) {
                            if (tankType == TankType.FLUID_TANK) {
                                handleResourceTank(player, itemAccess, Capabilities.FLUID, structure.getFluidTanks(), tile.getLevel(), structure.getBounds().getCenter());
                            } else if (tankType == TankType.CHEMICAL_TANK) {
                                handleResourceTank(player, itemAccess, Capabilities.CHEMICAL, structure.getChemicalTanks(), tile.getLevel(), structure.getBounds().getCenter());
                            }
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
                        if (tankType == TankType.FLUID_TANK) {
                            handleResourceTank(player, itemAccess, Capabilities.FLUID, tile.getFluidTanks(), tile.getLevel(), tile.getBlockPos());
                        } else if (tankType == TankType.CHEMICAL_TANK) {
                            handleResourceTank(player, itemAccess, Capabilities.CHEMICAL, tile.getChemicalTanks(), tile.getLevel(), tile.getBlockPos());
                        }
                    }
                }
            }
        }
    }

    @Nullable
    private <TANK> TANK getTank(List<TANK> tanks) {
        return tankId >= 0 && tankId < tanks.size() ? tanks.get(tankId) : null;
    }

    private <RESOURCE extends Resource, TANK extends IResourceContainer<RESOURCE>> void handleResourceTank(ServerPlayer player, ItemAccess itemAccess,
          MultiTypeCapability<ResourceHandler<RESOURCE>> capability, List<TANK> tanks, Level level, BlockPos pos) {
        TANK tank = getTank(tanks);
        if (tank == null) {
            return;
        } else if (action == DropperAction.DUMP_TANK) {
            //Dump the tank
            tank.setEmpty();
            if (tank instanceof IChemicalTank chemicalTank) {
                //If the tank has radioactive substances in it make sure we properly emit the radiation to the environment
                IRadiationManager.INSTANCE.dumpRadiation(level, pos, chemicalTank.getStack());
            }
            MekanismCriteriaTriggers.USE_GAUGE_DROPPER.value().trigger(player, UseDropperAction.DUMP);
            return;
        }
        ResourceHandler<RESOURCE> dropperHandler = capability.getCapability(itemAccess);
        if (dropperHandler != null) {
            if (action == DropperAction.FILL_DROPPER) {
                //Insert fluid into dropper
                transferBetween(tank.getResource(), tank.amount(), player, UseDropperAction.FILL,
                      tank, (target, type, amount, transaction) -> target.extract(type, amount, transaction, AutomationType.MANUAL),
                      dropperHandler, ResourceUtils::insertManual
                );
            } else if (action == DropperAction.DRAIN_DROPPER) {
                //Extract fluid from dropper
                int tankNeeded = tank.getNeeded();
                if (tankNeeded > 0) {
                    RESOURCE currentType = tank.getResource();
                    if (currentType.isEmpty()) {
                        //The tank is empty, try to figure out what is in the dropper that is able to be inserted into the tank
                        currentType = ResourceHandlerUtil.findExtractableResource(dropperHandler, resource -> tank.isValidForInsertion(resource, AutomationType.MANUAL), null);
                        if (currentType == null) {
                            //Failed to find a resource that could be extracted that is valid for the fluid tank, exit
                            return;
                        }
                        //Update how much the tank needs based on the type we are going to try to insert in case it has a lower limit than its maximum capacity
                        tankNeeded = tank.getLimit(currentType);
                        if (tankNeeded == 0) {
                            return;
                        }
                    }
                    transferBetween(currentType, tankNeeded, player, UseDropperAction.DRAIN,
                          dropperHandler, ResourceUtils::extractManual,
                          tank, (target, type, amount, transaction) -> target.insert(type, amount, transaction, AutomationType.MANUAL)
                    );
                }
            }
        }
    }

    private <RESOURCE extends Resource, EXTRACT_FROM, INSERT_INTO> void transferBetween(RESOURCE type, int needed, ServerPlayer player, UseDropperAction action,
          EXTRACT_FROM extractFrom, ContainerInteractor<RESOURCE, EXTRACT_FROM> extractor, INSERT_INTO insertInto, ContainerInteractor<RESOURCE, INSERT_INTO> insertor) {
        if (type.isEmpty() || needed <= 0) {
            return;
        }
        //TODO - 26.1: Evaluate if we want to be using ResourceHandlerUtil#move, I suspect it doesn't quite fit our uses, but we might want to evaluate it
        int drainAmount;
        try (Transaction simulation = Transaction.openRoot()) {
            drainAmount = extractor.process(extractFrom, type, needed, simulation);
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = insertor.process(insertInto, type, drainAmount, transaction);
            if (inserted > 0) {
                //There is room for at least some of the fluid, extract what we can
                int extracted = extractor.process(extractFrom, type, inserted, transaction);
                if (extracted == inserted) {
                    //We were able to extract the same amount as we inserted, commit it, sync and trigger advancements
                    //Note: This should always be true given we simulated how much could be extracted at once, but we validate it just in case
                    transaction.commit();
                    player.containerMenu.synchronizeCarriedToRemote();
                    MekanismCriteriaTriggers.USE_GAUGE_DROPPER.value().trigger(player, action);
                }
            }
        }
    }

    @FunctionalInterface
    private interface ContainerInteractor<RESOURCE extends Resource, TARGET> {

        int process(TARGET target, RESOURCE resource, int amount, TransactionContext transaction);
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