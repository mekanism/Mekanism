package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.Range4D;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.OreDictCache;
import mekanism.common.PacketHandler;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISustainedData;
import mekanism.api.TileNetworkList;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityOredictionificator extends TileEntityContainerBlock implements IRedstoneControl,
      ISpecialConfigData, ISustainedData, ISecurityTile {

    public static final int MAX_LENGTH = 24;
    public static List<String> possibleFilters = Arrays.asList("ingot", "ore", "dust", "nugget");
    public HashList<OredictionificatorFilter> filters = new HashList<>();
    public RedstoneControl controlType = RedstoneControl.DISABLED;

    public boolean didProcess;

    public TileComponentSecurity securityComponent = new TileComponentSecurity(this);

    public TileEntityOredictionificator() {
        super(BlockStateMachine.MachineType.OREDICTIONIFICATOR.blockName);

        inventory = NonNullList.withSize(2, ItemStack.EMPTY);
        doAutoSync = false;
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            if (playersUsing.size() > 0) {
                for (EntityPlayer player : playersUsing) {
                    Mekanism.packetHandler
                          .sendTo(new TileEntityMessage(Coord4D.get(this), getGenericPacket(new TileNetworkList())),
                                (EntityPlayerMP) player);
                }
            }

            didProcess = false;

            if (MekanismUtils.canFunction(this) && !inventory.get(0).isEmpty()
                  && getValidName(inventory.get(0)) != null) {
                ItemStack result = getResult(inventory.get(0));

                if (!result.isEmpty()) {
                    if (inventory.get(1).isEmpty()) {
                        inventory.get(0).shrink(1);

                        if (inventory.get(0).getCount() <= 0) {
                            inventory.set(0, ItemStack.EMPTY);
                        }

                        inventory.set(1, result);
                        didProcess = true;
                    } else if (inventory.get(1).isItemEqual(result) && inventory.get(1).getCount() < inventory.get(1)
                          .getMaxStackSize()) {
                        inventory.get(0).shrink(1);

                        if (inventory.get(0).getCount() <= 0) {
                            inventory.set(0, ItemStack.EMPTY);
                        }

                        inventory.get(1).grow(1);
                        didProcess = true;
                    }

                    markDirty();
                }
            }
        }
    }

    public String getValidName(ItemStack stack) {
        List<String> def = OreDictCache.getOreDictName(stack);

        for (String s : def) {
            for (String pre : possibleFilters) {
                if (s.startsWith(pre)) {
                    return s;
                }
            }
        }

        return null;
    }

    public ItemStack getResult(ItemStack stack) {
        String s = getValidName(stack);

        if (s == null) {
            return ItemStack.EMPTY;
        }

        List<ItemStack> ores = OreDictionary.getOres(s);

        for (OredictionificatorFilter filter : filters) {
            if (filter.filter.equals(s)) {
                if (ores.size() - 1 >= filter.index) {
                    return StackUtils.size(ores.get(filter.index), 1);
                } else {
                    return ItemStack.EMPTY;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        if (side == MekanismUtils.getLeft(facing)) {
            return new int[]{0};
        } else if (side == MekanismUtils.getRight(facing)) {
            return new int[]{1};
        } else {
            return InventoryUtils.EMPTY;
        }
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
        return slotID == 1;
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        return slotID == 0 && !getResult(itemstack).isEmpty();

    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        nbtTags.setInteger("controlType", controlType.ordinal());

        NBTTagList filterTags = new NBTTagList();

        for (OredictionificatorFilter filter : filters) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            filter.write(tagCompound);
            filterTags.appendTag(tagCompound);
        }

        if (filterTags.tagCount() != 0) {
            nbtTags.setTag("filters", filterTags);
        }

        return nbtTags;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];

        if (nbtTags.hasKey("filters")) {
            NBTTagList tagList = nbtTags.getTagList("filters", NBT.TAG_COMPOUND);

            for (int i = 0; i < tagList.tagCount(); i++) {
                filters.add(OredictionificatorFilter.readFromNBT(tagList.getCompoundTagAt(i)));
            }
        }

        //to fix any badly placed blocks in the world
        if (facing.getAxis() == EnumFacing.Axis.Y) {
            facing = EnumFacing.NORTH;
        }
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            int type = dataStream.readInt();

            if (type == 0) {
                controlType = RedstoneControl.values()[dataStream.readInt()];
                didProcess = dataStream.readBoolean();

                filters.clear();

                int amount = dataStream.readInt();

                for (int i = 0; i < amount; i++) {
                    filters.add(OredictionificatorFilter.readFromPacket(dataStream));
                }
            } else if (type == 1) {
                controlType = RedstoneControl.values()[dataStream.readInt()];
                didProcess = dataStream.readBoolean();
            } else if (type == 2) {
                filters.clear();

                int amount = dataStream.readInt();

                for (int i = 0; i < amount; i++) {
                    filters.add(OredictionificatorFilter.readFromPacket(dataStream));
                }
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);

        data.add(0);

        data.add(controlType.ordinal());
        data.add(didProcess);

        data.add(filters.size());

        for (OredictionificatorFilter filter : filters) {
            filter.write(data);
        }

        return data;
    }

    public TileNetworkList getGenericPacket(TileNetworkList data) {
        super.getNetworkedData(data);

        data.add(1);

        data.add(controlType.ordinal());
        data.add(didProcess);

        return data;

    }

    public TileNetworkList getFilterPacket(TileNetworkList data) {
        super.getNetworkedData(data);

        data.add(2);

        data.add(filters.size());

        for (OredictionificatorFilter filter : filters) {
            filter.write(data);
        }

        return data;
    }

    @Override
    public void openInventory(@Nonnull EntityPlayer player) {
        if (!world.isRemote) {
            Mekanism.packetHandler
                  .sendToReceivers(new TileEntityMessage(Coord4D.get(this), getFilterPacket(new TileNetworkList())),
                        new Range4D(Coord4D.get(this)));
        }
    }

    @Override
    public NBTTagCompound getConfigurationData(NBTTagCompound nbtTags) {
        NBTTagList filterTags = new NBTTagList();

        for (OredictionificatorFilter filter : filters) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            filter.write(tagCompound);
            filterTags.appendTag(tagCompound);
        }

        if (filterTags.tagCount() != 0) {
            nbtTags.setTag("filters", filterTags);
        }

        return nbtTags;
    }

    @Override
    public void setConfigurationData(NBTTagCompound nbtTags) {
        if (nbtTags.hasKey("filters")) {
            NBTTagList tagList = nbtTags.getTagList("filters", NBT.TAG_COMPOUND);

            for (int i = 0; i < tagList.tagCount(); i++) {
                filters.add(OredictionificatorFilter.readFromNBT(tagList.getCompoundTagAt(i)));
            }
        }
    }

    @Override
    public String getDataType() {
        return getBlockType().getTranslationKey() + "." + fullName + ".name";
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        ItemDataUtils.setBoolean(itemStack, "hasOredictionificatorConfig", true);

        NBTTagList filterTags = new NBTTagList();

        for (OredictionificatorFilter filter : filters) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            filter.write(tagCompound);
            filterTags.appendTag(tagCompound);
        }

        if (filterTags.tagCount() != 0) {
            ItemDataUtils.setList(itemStack, "filters", filterTags);
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        if (ItemDataUtils.hasData(itemStack, "hasOredictionificatorConfig")) {
            if (ItemDataUtils.hasData(itemStack, "filters")) {
                NBTTagList tagList = ItemDataUtils.getList(itemStack, "filters");

                for (int i = 0; i < tagList.tagCount(); i++) {
                    filters.add(OredictionificatorFilter.readFromNBT(tagList.getCompoundTagAt(i)));
                }
            }
        }
    }

    @Override
    public RedstoneControl getControlType() {
        return controlType;
    }

    @Override
    public void setControlType(RedstoneControl type) {
        controlType = type;
    }

    @Override
    public boolean canPulse() {
        return true;
    }

    @Override
    public TileComponentSecurity getSecurity() {
        return securityComponent;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        return capability == Capabilities.CONFIG_CARD_CAPABILITY
              || capability == Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY
              || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (capability == Capabilities.CONFIG_CARD_CAPABILITY
              || capability == Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY) {
            return (T) this;
        }

        return super.getCapability(capability, side);
    }

    @Override
    public boolean canSetFacing(int i) {
        return i != 0 && i != 1;
    }

    public static class OredictionificatorFilter {

        public String filter;
        public int index;

        public static OredictionificatorFilter readFromNBT(NBTTagCompound nbtTags) {
            OredictionificatorFilter filter = new OredictionificatorFilter();

            filter.read(nbtTags);

            return filter;
        }

        public static OredictionificatorFilter readFromPacket(ByteBuf dataStream) {
            OredictionificatorFilter filter = new OredictionificatorFilter();

            filter.read(dataStream);

            return filter;
        }

        public void write(NBTTagCompound nbtTags) {
            nbtTags.setString("filter", filter);
            nbtTags.setInteger("index", index);
        }

        protected void read(NBTTagCompound nbtTags) {
            filter = nbtTags.getString("filter");
            index = nbtTags.getInteger("index");
        }

        public void write(TileNetworkList data) {
            data.add(filter);
            data.add(index);
        }

        protected void read(ByteBuf dataStream) {
            filter = PacketHandler.readString(dataStream);
            index = dataStream.readInt();
        }

        @Override
        public OredictionificatorFilter clone() {
            OredictionificatorFilter newFilter = new OredictionificatorFilter();
            newFilter.filter = filter;
            newFilter.index = index;

            return newFilter;
        }

        @Override
        public int hashCode() {
            int code = 1;
            code = 31 * code + filter.hashCode();
            return code;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof OredictionificatorFilter && ((OredictionificatorFilter) obj).filter.equals(filter);
        }
    }
}
