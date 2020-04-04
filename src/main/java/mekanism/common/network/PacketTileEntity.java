package mekanism.common.network;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import mekanism.api.TileNetworkList;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.base.ITileNetwork;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkEvent.Context;

@Deprecated//TODO: Remove
public class PacketTileEntity {

    private TileNetworkList parameters;
    private PacketBuffer storedBuffer;
    private BlockPos pos;

    public PacketTileEntity(TileEntity tile, TileNetworkList params) {
        this(tile.getPos());
        parameters = params;
    }

    private PacketTileEntity(BlockPos pos) {
        this.pos = pos;
    }

    public static void handle(PacketTileEntity message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntity tile = MekanismUtils.getTileEntity(player.world, message.pos);
            if (tile instanceof ITileNetwork) {
                try {
                    ((ITileNetwork) tile).handlePacketData(message.storedBuffer);
                } catch (Exception e) {
                    Mekanism.logger.error("FIXME: Packet handling error", e);
                }
            }
            message.storedBuffer.release();
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketTileEntity pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.pos);
        encodeTileNetworkList(pkt.parameters.toArray(), buf);
    }

    public static PacketTileEntity decode(PacketBuffer buf) {
        PacketTileEntity packet = new PacketTileEntity(buf.readBlockPos());
        packet.storedBuffer = new PacketBuffer(buf.copy());
        return packet;
    }

    @Deprecated
    public static void encodeTileNetworkList(Object[] dataValues, PacketBuffer output) {
        for (Object data : dataValues) {
            if (data instanceof Byte) {
                output.writeByte((Byte) data);
            } else if (data instanceof Integer) {
                output.writeInt((Integer) data);
            } else if (data instanceof Short) {
                output.writeShort((Short) data);
            } else if (data instanceof Long) {
                output.writeLong((Long) data);
            } else if (data instanceof Boolean) {
                output.writeBoolean((Boolean) data);
            } else if (data instanceof Double) {
                output.writeDouble((Double) data);
            } else if (data instanceof Float) {
                output.writeFloat((Float) data);
            } else if (data instanceof FloatingLong) {
                ((FloatingLong) data).writeToBuffer(output);
            } else if (data instanceof String) {
                output.writeString((String) data);
            } else if (data instanceof UUID) {
                output.writeUniqueId((UUID) data);
            } else if (data instanceof Direction) {
                output.writeInt(((Direction) data).ordinal());
            } else if (data instanceof ItemStack) {
                output.writeItemStack((ItemStack) data);
            } else if (data instanceof FluidStack) {
                output.writeFluidStack((FluidStack) data);
            } else if (data instanceof ChemicalStack) {
                ChemicalUtils.writeChemicalStack(output, (ChemicalStack<?>) data);
            } else if (data instanceof CompoundNBT) {
                output.writeCompoundTag((CompoundNBT) data);
            } else if (data instanceof ResourceLocation) {
                output.writeResourceLocation((ResourceLocation) data);
            } else if (data instanceof Enum) {
                output.writeEnumValue((Enum<?>) data);
            } else if (data instanceof int[]) {
                for (int i : (int[]) data) {
                    output.writeInt(i);
                }
            } else if (data instanceof byte[]) {
                for (byte b : (byte[]) data) {
                    output.writeByte(b);
                }
            } else if (data instanceof List) {
                encodeTileNetworkList(((List<?>) data).toArray(), output);
            } else {
                throw new RuntimeException("Un-encodable data passed to encode(): " + data + ", full data: " + Arrays.toString(dataValues));
            }
        }
    }
}