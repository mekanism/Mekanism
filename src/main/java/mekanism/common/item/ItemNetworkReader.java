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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class ItemNetworkReader extends ItemEnergized {

    public static double ENERGY_PER_USE = 400;

    public ItemNetworkReader() {
        super("network_reader", 60000);
    }

    @Nonnull
    @Override
    public ActionResultType onItemUseFirst(PlayerEntity player, World world, BlockPos pos, Direction side, float hitX, float hitY, float hitZ, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);
            boolean drain = !player.isCreative();
            if (getEnergy(stack) >= ENERGY_PER_USE && tileEntity != null) {
                //TODO: Some of this stuff can maybe be extracted
                CapabilityUtils.getCapabilityHelper(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite()).getIfPresentElseDo(transmitter -> {
                          if (drain) {
                              setEnergy(stack, getEnergy(stack) - ENERGY_PER_USE);
                          }
                          player.sendMessage(new StringTextComponent(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + Mekanism.LOG_TAG + EnumColor.GREY + " -------------"));
                          player.sendMessage(new StringTextComponent(EnumColor.GREY + " *Transmitters: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetworkSize()));
                          player.sendMessage(new StringTextComponent(EnumColor.GREY + " *Acceptors: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetworkAcceptorSize()));
                          player.sendMessage(new StringTextComponent(EnumColor.GREY + " *Needed: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetworkNeeded()));
                          player.sendMessage(new StringTextComponent(EnumColor.GREY + " *Buffer: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetworkBuffer()));
                          player.sendMessage(new StringTextComponent(EnumColor.GREY + " *Throughput: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetworkFlow()));
                          player.sendMessage(new StringTextComponent(EnumColor.GREY + " *Capacity: " + EnumColor.DARK_GREY + transmitter.getTransmitterNetworkCapacity()));

                          CapabilityUtils.getCapabilityHelper(tileEntity, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite()).ifPresent(
                                transfer -> player.sendMessage(new StringTextComponent(EnumColor.GREY + " *Temperature: " + EnumColor.DARK_GREY + transfer.getTemp() + "K above ambient"))
                          );
                          player.sendMessage(new StringTextComponent(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
                          return ActionResultType.SUCCESS;
                      },
                      () -> CapabilityUtils.getCapabilityHelper(tileEntity, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite()).getIfPresentElseDo(transfer -> {
                                if (drain) {
                                    setEnergy(stack, getEnergy(stack) - ENERGY_PER_USE);
                                }
                                player.sendMessage(new StringTextComponent(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + Mekanism.LOG_TAG + EnumColor.GREY + " -------------"));
                                player.sendMessage(new StringTextComponent(EnumColor.GREY + " *Temperature: " + EnumColor.DARK_GREY + transfer.getTemp() + "K above ambient"));
                                player.sendMessage(new StringTextComponent(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
                                return ActionResultType.SUCCESS;
                            },
                            () -> {
                                if (drain) {
                                    setEnergy(stack, getEnergy(stack) - ENERGY_PER_USE);
                                }
                                Set<DynamicNetwork> iteratedNetworks = new HashSet<>();

                                for (Direction iterSide : Direction.values()) {
                                    Coord4D coord = Coord4D.get(tileEntity).offset(iterSide);
                                    TileEntity tile = coord.getTileEntity(world);
                                    CapabilityUtils.getCapabilityHelper(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, iterSide.getOpposite()).ifPresent(transmitter -> {
                                        if (transmitter.getTransmitterNetwork().getPossibleAcceptors().contains(coord.offset(iterSide.getOpposite())) &&
                                            !iteratedNetworks.contains(transmitter.getTransmitterNetwork())) {
                                            player.sendMessage(new StringTextComponent(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[" +
                                                                                       transmitter.getTransmissionType().getName() + "]" + EnumColor.GREY + " -------------"));
                                            player.sendMessage(new StringTextComponent(EnumColor.GREY + " *Connected sides: " + EnumColor.DARK_GREY +
                                                                                       transmitter.getTransmitterNetwork().getAcceptorDirections().get(coord.offset(iterSide.getOpposite()))));
                                            player.sendMessage(new StringTextComponent(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
                                            iteratedNetworks.add(transmitter.getTransmitterNetwork());
                                        }
                                    });
                                }
                                return ActionResultType.SUCCESS;
                            }
                      )
                );
            }

            if (player.isSneaking() && MekanismAPI.debug) {
                String[] strings = TransmitterNetworkRegistry.getInstance().toStrings();
                player.sendMessage(new StringTextComponent(EnumColor.GREY + "---------- " + EnumColor.DARK_BLUE + "[Mekanism Debug]" + EnumColor.GREY + " ----------"));
                for (String s : strings) {
                    player.sendMessage(new StringTextComponent(EnumColor.DARK_GREY + s));
                }
                player.sendMessage(new StringTextComponent(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean canSend(ItemStack itemstack) {
        return false;
    }
}