package mekanism.common.item;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.MekanismAPI;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.CapabilityUtils;
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
        super("network_reader", 60000);
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        if (!world.isRemote && player != null) {
            ItemStack stack = player.getHeldItem(context.getHand());
            TileEntity tileEntity = world.getTileEntity(context.getPos());
            boolean drain = !player.isCreative();
            //TODO: lang keys for the different information
            if (getEnergy(stack) >= ENERGY_PER_USE && tileEntity != null) {
                if (drain) {
                    setEnergy(stack, getEnergy(stack) - ENERGY_PER_USE);
                }
                Direction opposite = context.getFace().getOpposite();
                CapabilityUtils.getCapabilityHelper(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, opposite).ifPresentElse(transmitter -> {
                          player.sendMessage(TextComponentUtil.build(EnumColor.GREY, "------------- ", EnumColor.DARK_BLUE, Mekanism.LOG_TAG, EnumColor.GREY, " -------------"));
                          player.sendMessage(TextComponentUtil.build(EnumColor.GREY, " *Transmitters: ", EnumColor.DARK_GREY, transmitter.getTransmitterNetworkSize()));
                          player.sendMessage(TextComponentUtil.build(EnumColor.GREY, " *Acceptors: ", EnumColor.DARK_GREY, transmitter.getTransmitterNetworkAcceptorSize()));
                          player.sendMessage(TextComponentUtil.build(EnumColor.GREY, " *Needed: ", EnumColor.DARK_GREY, transmitter.getTransmitterNetworkNeeded()));
                          player.sendMessage(TextComponentUtil.build(EnumColor.GREY, " *Buffer: ", EnumColor.DARK_GREY, transmitter.getTransmitterNetworkBuffer()));
                          player.sendMessage(TextComponentUtil.build(EnumColor.GREY, " *Throughput: ", EnumColor.DARK_GREY, transmitter.getTransmitterNetworkFlow()));
                          player.sendMessage(TextComponentUtil.build(EnumColor.GREY, " *Capacity: ", EnumColor.DARK_GREY, transmitter.getTransmitterNetworkCapacity()));

                          CapabilityUtils.getCapabilityHelper(tileEntity, Capabilities.HEAT_TRANSFER_CAPABILITY, opposite).ifPresent(
                                transfer -> player.sendMessage(TextComponentUtil.build(EnumColor.GREY, " *Temperature: ", EnumColor.DARK_GREY,
                                      transfer.getTemp() + "K above ambient"))
                          );
                          player.sendMessage(TextComponentUtil.build(EnumColor.GREY, "------------- ", EnumColor.DARK_BLUE, "[=======]", EnumColor.GREY, " -------------"));
                      },
                      () -> CapabilityUtils.getCapabilityHelper(tileEntity, Capabilities.HEAT_TRANSFER_CAPABILITY, opposite).ifPresentElse(transfer -> {
                                player.sendMessage(TextComponentUtil.build(EnumColor.GREY, "------------- ", EnumColor.DARK_BLUE, Mekanism.LOG_TAG,
                                      EnumColor.GREY, " -------------"));
                                player.sendMessage(TextComponentUtil.build(EnumColor.GREY, " *Temperature: ", EnumColor.DARK_GREY,
                                      transfer.getTemp() + "K above ambient"));
                                player.sendMessage(TextComponentUtil.build(EnumColor.GREY, "------------- ", EnumColor.DARK_BLUE, "[=======]",
                                      EnumColor.GREY, " -------------"));
                            },
                            () -> {
                                Set<DynamicNetwork> iteratedNetworks = new HashSet<>();

                                for (Direction iterSide : Direction.values()) {
                                    Coord4D coord = Coord4D.get(tileEntity).offset(iterSide);
                                    TileEntity tile = coord.getTileEntity(world);
                                    Direction iterSideOpposite = iterSide.getOpposite();
                                    CapabilityUtils.getCapabilityHelper(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, iterSideOpposite).ifPresent(transmitter -> {
                                        if (transmitter.getTransmitterNetwork().getPossibleAcceptors().contains(coord.offset(iterSideOpposite)) &&
                                            !iteratedNetworks.contains(transmitter.getTransmitterNetwork())) {
                                            player.sendMessage(TextComponentUtil.build(EnumColor.GREY, "------------- ", EnumColor.DARK_BLUE, "[",
                                                  transmitter.getTransmissionType(), "]", EnumColor.GREY, " -------------"));
                                            //TODO: Better way of handling the connected sides
                                            player.sendMessage(TextComponentUtil.build(EnumColor.GREY, " *Connected sides: " + EnumColor.DARK_GREY,
                                                  transmitter.getTransmitterNetwork().getAcceptorDirections().get(coord.offset(iterSideOpposite)).toString()));
                                            player.sendMessage(TextComponentUtil.build(EnumColor.GREY, "------------- ", EnumColor.DARK_BLUE, "[=======]",
                                                  EnumColor.GREY, " -------------"));
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
                player.sendMessage(TextComponentUtil.build(EnumColor.GREY, "---------- ", EnumColor.DARK_BLUE, "[Mekanism Debug]", EnumColor.GREY, " ----------"));
                for (String s : strings) {
                    player.sendMessage(TextComponentUtil.build(EnumColor.DARK_GREY, s));
                }
                player.sendMessage(TextComponentUtil.build(EnumColor.GREY, "------------- ", EnumColor.DARK_BLUE, "[=======]", EnumColor.GREY, " -------------"));
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean canSend(ItemStack itemstack) {
        return false;
    }
}