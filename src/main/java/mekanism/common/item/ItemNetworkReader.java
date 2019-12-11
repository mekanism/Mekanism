package mekanism.common.item;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.world.World;

public class ItemNetworkReader extends ItemEnergized {

    public static double ENERGY_PER_USE = 400;

    public ItemNetworkReader() {
        super(60_000);
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        if (!world.isRemote && player != null) {
            ItemStack stack = player.getHeldItem(context.getHand());
            TileEntity tileEntity = MekanismUtils.getTileEntity(world, context.getPos());
            boolean drain = !player.isCreative();
            //TODO: lang keys for the different information
            if (getEnergy(stack) >= ENERGY_PER_USE && tileEntity != null) {
                if (drain) {
                    setEnergy(stack, getEnergy(stack) - ENERGY_PER_USE);
                }
                Coord4D tileCoord = Coord4D.get(tileEntity);
                Direction opposite = context.getFace().getOpposite();
                CapabilityUtils.getCapabilityHelper(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, opposite).ifPresentElse(transmitter -> {
                          player.sendMessage(TextComponentUtil.build(EnumColor.GRAY, "------------- ", EnumColor.DARK_BLUE, Mekanism.LOG_TAG, EnumColor.GRAY, " -------------"));
                          player.sendMessage(TextComponentUtil.build(EnumColor.GRAY, " *Transmitters: ", EnumColor.DARK_GRAY, transmitter.getTransmitterNetworkSize()));
                          player.sendMessage(TextComponentUtil.build(EnumColor.GRAY, " *Acceptors: ", EnumColor.DARK_GRAY, transmitter.getTransmitterNetworkAcceptorSize()));
                          player.sendMessage(TextComponentUtil.build(EnumColor.GRAY, " *Needed: ", EnumColor.DARK_GRAY, transmitter.getTransmitterNetworkNeeded()));
                          player.sendMessage(TextComponentUtil.build(EnumColor.GRAY, " *Buffer: ", EnumColor.DARK_GRAY, transmitter.getTransmitterNetworkBuffer()));
                          player.sendMessage(TextComponentUtil.build(EnumColor.GRAY, " *Throughput: ", EnumColor.DARK_GRAY, transmitter.getTransmitterNetworkFlow()));
                          player.sendMessage(TextComponentUtil.build(EnumColor.GRAY, " *Capacity: ", EnumColor.DARK_GRAY, transmitter.getTransmitterNetworkCapacity()));

                          CapabilityUtils.getCapabilityHelper(tileEntity, Capabilities.HEAT_TRANSFER_CAPABILITY, opposite).ifPresent(
                                transfer -> player.sendMessage(TextComponentUtil.build(EnumColor.GRAY, " *Temperature: ", EnumColor.DARK_GRAY,
                                      transfer.getTemp() + "K above ambient"))
                          );
                          player.sendMessage(TextComponentUtil.build(EnumColor.GRAY, "------------- ", EnumColor.DARK_BLUE, "[=======]", EnumColor.GRAY, " -------------"));
                      },
                      () -> CapabilityUtils.getCapabilityHelper(tileEntity, Capabilities.HEAT_TRANSFER_CAPABILITY, opposite).ifPresentElse(transfer -> {
                                player.sendMessage(TextComponentUtil.build(EnumColor.GRAY, "------------- ", EnumColor.DARK_BLUE, Mekanism.LOG_TAG,
                                      EnumColor.GRAY, " -------------"));
                                player.sendMessage(TextComponentUtil.build(EnumColor.GRAY, " *Temperature: ", EnumColor.DARK_GRAY,
                                      transfer.getTemp() + "K above ambient"));
                                player.sendMessage(TextComponentUtil.build(EnumColor.GRAY, "------------- ", EnumColor.DARK_BLUE, "[=======]",
                                      EnumColor.GRAY, " -------------"));
                            },
                            () -> {
                                Set<DynamicNetwork<?, ?, ?>> iteratedNetworks = new HashSet<>();
                                for (Direction iterSide : EnumUtils.DIRECTIONS) {
                                    Coord4D coord = tileCoord.offset(iterSide);
                                    TileEntity tile = MekanismUtils.getTileEntity(world, coord.getPos());
                                    Direction iterSideOpposite = iterSide.getOpposite();
                                    CapabilityUtils.getCapabilityHelper(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, iterSideOpposite).ifPresent(transmitter -> {
                                        if (transmitter.getTransmitterNetwork().getPossibleAcceptors().contains(coord.offset(iterSideOpposite)) &&
                                            !iteratedNetworks.contains(transmitter.getTransmitterNetwork())) {
                                            player.sendMessage(TextComponentUtil.build(EnumColor.GRAY, "------------- ", EnumColor.DARK_BLUE, "[",
                                                  transmitter.getTransmissionType(), "]", EnumColor.GRAY, " -------------"));
                                            //TODO: Better way of handling the connected sides
                                            player.sendMessage(TextComponentUtil.build(EnumColor.GRAY, " *Connected sides: " + EnumColor.DARK_GRAY,
                                                  transmitter.getTransmitterNetwork().getAcceptorDirections().get(coord.offset(iterSideOpposite)).toString()));
                                            player.sendMessage(TextComponentUtil.build(EnumColor.GRAY, "------------- ", EnumColor.DARK_BLUE, "[=======]",
                                                  EnumColor.GRAY, " -------------"));
                                            iteratedNetworks.add(transmitter.getTransmitterNetwork());
                                        }
                                    });
                                }
                            }
                      )
                );
                return ActionResultType.SUCCESS;
            }

            if (player.isSneaking() && MekanismAPI.debug) {
                String[] strings = TransmitterNetworkRegistry.getInstance().toStrings();
                //TODO: Lang string for Mekanism Debug?
                player.sendMessage(TextComponentUtil.build(EnumColor.GRAY, "---------- ", EnumColor.DARK_BLUE, "[Mekanism Debug]", EnumColor.GRAY, " ----------"));
                for (String s : strings) {
                    player.sendMessage(TextComponentUtil.build(EnumColor.DARK_GRAY, s));
                }
                player.sendMessage(TextComponentUtil.build(EnumColor.GRAY, "------------- ", EnumColor.DARK_BLUE, "[=======]", EnumColor.GRAY, " -------------"));
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean canSend(ItemStack itemstack) {
        return false;
    }
}