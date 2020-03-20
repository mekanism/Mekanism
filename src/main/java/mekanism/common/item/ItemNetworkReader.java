package mekanism.common.item;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.MekanismAPI;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.AutomationType;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class ItemNetworkReader extends ItemEnergized {

    public static double ENERGY_PER_USE = 400;

    public ItemNetworkReader(Properties properties) {
        super(60_000, properties);
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        if (!world.isRemote && player != null) {
            ItemStack stack = player.getHeldItem(context.getHand());
            TileEntity tileEntity = MekanismUtils.getTileEntity(world, context.getPos());
            if (tileEntity != null) {
                if (!player.isCreative()) {
                    IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                    if (energyContainer == null) {
                        return ActionResultType.FAIL;
                    }
                    energyContainer.extract(ENERGY_PER_USE, Action.EXECUTE, AutomationType.MANUAL);
                }
                Direction opposite = context.getFace().getOpposite();
                Optional<IGridTransmitter<?, ?, ?>> gridTransmitter = MekanismUtils.toOptional(CapabilityUtils.getCapability(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, opposite));
                if (gridTransmitter.isPresent()) {
                    IGridTransmitter<?, ?, ?> transmitter = gridTransmitter.get();
                    player.sendMessage(MekanismLang.NETWORK_READER_BORDER.translateColored(EnumColor.GRAY, "-------------",
                          MekanismLang.GENERIC_SQUARE_BRACKET.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM)));
                    player.sendMessage(MekanismLang.NETWORK_READER_TRANSMITTERS.translateColored(EnumColor.GRAY, EnumColor.DARK_GRAY, transmitter.getTransmitterNetworkSize()));
                    player.sendMessage(MekanismLang.NETWORK_READER_ACCEPTORS.translateColored(EnumColor.GRAY, EnumColor.DARK_GRAY, transmitter.getTransmitterNetworkAcceptorSize()));
                    player.sendMessage(MekanismLang.NETWORK_READER_NEEDED.translateColored(EnumColor.GRAY, EnumColor.DARK_GRAY, transmitter.getTransmitterNetworkNeeded()));
                    player.sendMessage(MekanismLang.NETWORK_READER_BUFFER.translateColored(EnumColor.GRAY, EnumColor.DARK_GRAY, transmitter.getTransmitterNetworkBuffer()));
                    player.sendMessage(MekanismLang.NETWORK_READER_THROUGHPUT.translateColored(EnumColor.GRAY, EnumColor.DARK_GRAY, transmitter.getTransmitterNetworkFlow()));
                    player.sendMessage(MekanismLang.NETWORK_READER_CAPACITY.translateColored(EnumColor.GRAY, EnumColor.DARK_GRAY, transmitter.getTransmitterNetworkCapacity()));
                    CapabilityUtils.getCapability(tileEntity, Capabilities.HEAT_TRANSFER_CAPABILITY, opposite).ifPresent(transfer ->
                          player.sendMessage(MekanismLang.NETWORK_READER_ABOVE_AMBIENT.translateColored(EnumColor.GRAY, EnumColor.DARK_GRAY, transfer.getTemp())));
                    player.sendMessage(MekanismLang.NETWORK_READER_BORDER.translateColored(EnumColor.GRAY, "-------------", EnumColor.DARK_BLUE, "[=======]"));
                } else {
                    Optional<IHeatTransfer> heatTransfer = MekanismUtils.toOptional(CapabilityUtils.getCapability(tileEntity, Capabilities.HEAT_TRANSFER_CAPABILITY, opposite));
                    if (heatTransfer.isPresent()) {
                        IHeatTransfer transfer = heatTransfer.get();
                        MekanismLang.NETWORK_READER_BORDER.translateColored(EnumColor.GRAY, "-------------",
                              MekanismLang.GENERIC_SQUARE_BRACKET.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM));
                        player.sendMessage(MekanismLang.NETWORK_READER_ABOVE_AMBIENT.translateColored(EnumColor.GRAY, EnumColor.DARK_GRAY, transfer.getTemp()));
                        player.sendMessage(MekanismLang.NETWORK_READER_BORDER.translateColored(EnumColor.GRAY, "-------------", EnumColor.DARK_BLUE, "[=======]"));
                    } else {
                        Coord4D tileCoord = Coord4D.get(tileEntity);
                        Set<DynamicNetwork<?, ?, ?>> iteratedNetworks = new ObjectOpenHashSet<>();
                        for (Direction iterSide : EnumUtils.DIRECTIONS) {
                            Coord4D coord = tileCoord.offset(iterSide);
                            TileEntity tile = MekanismUtils.getTileEntity(world, coord.getPos());
                            Direction iterSideOpposite = iterSide.getOpposite();
                            CapabilityUtils.getCapability(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, iterSideOpposite).ifPresent(transmitter -> {
                                if (transmitter.getTransmitterNetwork().getPossibleAcceptors().contains(coord.offset(iterSideOpposite)) &&
                                    !iteratedNetworks.contains(transmitter.getTransmitterNetwork())) {
                                    player.sendMessage(MekanismLang.NETWORK_READER_BORDER.translateColored(EnumColor.GRAY, "-------------",
                                          MekanismLang.GENERIC_SQUARE_BRACKET.translateColored(EnumColor.DARK_BLUE, transmitter.getTransmissionType())));
                                    player.sendMessage(MekanismLang.NETWORK_READER_CONNECTED_SIDES.translateColored(EnumColor.GRAY, EnumColor.DARK_GRAY,
                                          transmitter.getTransmitterNetwork().getAcceptorDirections().get(coord.offset(iterSideOpposite)).toString()));
                                    player.sendMessage(MekanismLang.NETWORK_READER_BORDER.translateColored(EnumColor.GRAY, "-------------", EnumColor.DARK_BLUE, "[=======]"));
                                    iteratedNetworks.add(transmitter.getTransmitterNetwork());
                                }
                            });
                        }
                    }
                }
                return ActionResultType.SUCCESS;
            }
            if (player.isShiftKeyDown() && MekanismAPI.debug) {
                MekanismLang.NETWORK_READER_BORDER.translateColored(EnumColor.GRAY, "----------",
                      MekanismLang.GENERIC_SQUARE_BRACKET.translateColored(EnumColor.DARK_BLUE, MekanismLang.DEBUG_TITLE));
                for (ITextComponent component : TransmitterNetworkRegistry.getInstance().toComponents()) {
                    player.sendMessage(TextComponentUtil.build(EnumColor.DARK_GRAY, component));
                }
                player.sendMessage(MekanismLang.NETWORK_READER_BORDER.translateColored(EnumColor.GRAY, "-------------", EnumColor.DARK_BLUE, "[=======]"));
            }
        }
        return ActionResultType.PASS;
    }
}