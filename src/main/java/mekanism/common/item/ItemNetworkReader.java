package mekanism.common.item;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class ItemNetworkReader extends ItemEnergized {

    public ItemNetworkReader(Properties properties) {
        super(MekanismConfig.gear.networkReaderChargeRate, MekanismConfig.gear.networkReaderMaxEnergy, properties.rarity(Rarity.UNCOMMON));
    }

    private void displayBorder(PlayerEntity player, Object toDisplay, boolean brackets) {
        player.sendMessage(MekanismLang.NETWORK_READER_BORDER.translateColored(EnumColor.GRAY, "-------------", EnumColor.DARK_BLUE,
              brackets ? MekanismLang.GENERIC_SQUARE_BRACKET.translate(toDisplay) : toDisplay), Util.DUMMY_UUID);
    }

    private void displayEndBorder(PlayerEntity player) {
        displayBorder(player, "[=======]", false);
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        if (!world.isRemote && player != null) {
            BlockPos pos = context.getPos();
            TileEntity tile = MekanismUtils.getTileEntity(world, pos);
            if (tile != null) {
                if (!player.isCreative()) {
                    FloatingLong energyPerUse = MekanismConfig.gear.networkReaderEnergyUsage.get();
                    IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(player.getHeldItem(context.getHand()), 0);
                    if (energyContainer == null || energyContainer.extract(energyPerUse, Action.SIMULATE, AutomationType.MANUAL).smallerThan(energyPerUse)) {
                        return ActionResultType.FAIL;
                    }
                    energyContainer.extract(energyPerUse, Action.EXECUTE, AutomationType.MANUAL);
                }
                Direction opposite = context.getFace().getOpposite();
                if (tile instanceof TileEntityTransmitter) {
                    displayTransmitterInfo(player, ((TileEntityTransmitter) tile).getTransmitter(), tile, opposite);
                } else {
                    Optional<IHeatHandler> heatHandler = CapabilityUtils.getCapability(tile, Capabilities.HEAT_HANDLER_CAPABILITY, opposite).resolve();
                    if (heatHandler.isPresent()) {
                        IHeatHandler transfer = heatHandler.get();
                        displayBorder(player, MekanismLang.MEKANISM, true);
                        sendTemperature(player, transfer);
                        displayEndBorder(player);
                    } else {
                        displayConnectedNetworks(player, world, pos);
                    }
                }
                return ActionResultType.SUCCESS;
            } else if (player.isSneaking() && MekanismAPI.debug) {
                displayBorder(player, MekanismLang.DEBUG_TITLE, true);
                for (ITextComponent component : TransmitterNetworkRegistry.getInstance().toComponents()) {
                    player.sendMessage(TextComponentUtil.build(EnumColor.DARK_GRAY, component), Util.DUMMY_UUID);
                }
                displayEndBorder(player);
            }
        }
        return ActionResultType.PASS;
    }

    private void displayTransmitterInfo(PlayerEntity player, Transmitter<?, ?, ?> transmitter, TileEntity tile, Direction opposite) {
        displayBorder(player, MekanismLang.MEKANISM, true);
        if (transmitter.hasTransmitterNetwork()) {
            DynamicNetwork<?, ?, ?> transmitterNetwork = transmitter.getTransmitterNetwork();
            player.sendMessage(MekanismLang.NETWORK_READER_TRANSMITTERS.translateColored(EnumColor.GRAY, EnumColor.DARK_GRAY, transmitterNetwork.transmittersSize()), Util.DUMMY_UUID);
            player.sendMessage(MekanismLang.NETWORK_READER_ACCEPTORS.translateColored(EnumColor.GRAY, EnumColor.DARK_GRAY, transmitterNetwork.getAcceptorCount()), Util.DUMMY_UUID);
            sendMessageIfNonNull(player, MekanismLang.NETWORK_READER_NEEDED, transmitterNetwork.getNeededInfo());
            sendMessageIfNonNull(player, MekanismLang.NETWORK_READER_BUFFER, transmitterNetwork.getStoredInfo());
            sendMessageIfNonNull(player, MekanismLang.NETWORK_READER_THROUGHPUT, transmitterNetwork.getFlowInfo());
            sendMessageIfNonNull(player, MekanismLang.NETWORK_READER_CAPACITY, transmitterNetwork.getNetworkReaderCapacity());
            CapabilityUtils.getCapability(tile, Capabilities.HEAT_HANDLER_CAPABILITY, opposite).ifPresent(heatHandler -> sendTemperature(player, heatHandler));
        } else {
            player.sendMessage(MekanismLang.NO_NETWORK.translate(), Util.DUMMY_UUID);
        }
        displayEndBorder(player);
    }

    private void displayConnectedNetworks(PlayerEntity player, World world, BlockPos pos) {
        Set<DynamicNetwork<?, ?, ?>> iteratedNetworks = new ObjectOpenHashSet<>();
        for (Direction side : EnumUtils.DIRECTIONS) {
            TileEntity tile = MekanismUtils.getTileEntity(world, pos.offset(side));
            if (tile instanceof TileEntityTransmitter) {
                Transmitter<?, ?, ?> transmitter = ((TileEntityTransmitter) tile).getTransmitter();
                DynamicNetwork<?, ?, ?> transmitterNetwork = transmitter.getTransmitterNetwork();
                if (transmitterNetwork.hasAcceptor(pos) && !iteratedNetworks.contains(transmitterNetwork)) {
                    displayBorder(player, compileList(transmitter.getSupportedTransmissionTypes()), false);
                    player.sendMessage(MekanismLang.NETWORK_READER_CONNECTED_SIDES.translateColored(EnumColor.GRAY, EnumColor.DARK_GRAY,
                          compileList(transmitterNetwork.getAcceptorDirections(pos))), Util.DUMMY_UUID);
                    displayEndBorder(player);
                    iteratedNetworks.add(transmitterNetwork);
                }
            }
        }
    }

    private void sendTemperature(PlayerEntity player, IHeatHandler handler) {
        ITextComponent temp = MekanismUtils.getTemperatureDisplay(handler.getTotalTemperature(), TemperatureUnit.KELVIN, true);
        player.sendMessage(MekanismLang.NETWORK_READER_TEMPERATURE.translateColored(EnumColor.GRAY, EnumColor.DARK_GRAY, temp), Util.DUMMY_UUID);
    }

    private void sendMessageIfNonNull(PlayerEntity player, ILangEntry langEntry, Object toSend) {
        if (toSend != null) {
            player.sendMessage(langEntry.translateColored(EnumColor.GRAY, EnumColor.DARK_GRAY, toSend), Util.DUMMY_UUID);
        }
    }

    private <ENUM extends Enum<ENUM>> ITextComponent compileList(Set<ENUM> elements) {
        if (elements.isEmpty()) {
            return MekanismLang.GENERIC_SQUARE_BRACKET.translate("");
        }
        ITextComponent component = null;
        for (ENUM element : elements) {
            if (component == null) {
                component = TextComponentUtil.build(element);
            } else {
                component = MekanismLang.GENERIC_WITH_COMMA.translate(component, element);
            }
        }
        return MekanismLang.GENERIC_SQUARE_BRACKET.translate(component);
    }
}