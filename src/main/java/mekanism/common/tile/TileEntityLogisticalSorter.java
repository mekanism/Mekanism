package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.Range4D;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.MekanismSounds;
import mekanism.common.base.IActiveState;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISustainedData;
import mekanism.api.TileNetworkList;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig.client;
import mekanism.common.content.transporter.Finder;
import mekanism.common.content.transporter.InvStack;
import mekanism.common.content.transporter.StackSearcher;
import mekanism.common.content.transporter.TItemStackFilter;
import mekanism.common.content.transporter.TOreDictFilter;
import mekanism.common.content.transporter.TransitRequest;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.prefab.TileEntityElectricBlock;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityLogisticalSorter extends TileEntityElectricBlock implements IRedstoneControl, IActiveState,
      ISpecialConfigData, ISustainedData, ISecurityTile, IComputerIntegration {

    public final int MAX_DELAY = 10;
    public final double ENERGY_PER_ITEM = 5;
    public HashList<TransporterFilter> filters = new HashList<>();
    public RedstoneControl controlType = RedstoneControl.DISABLED;
    public EnumColor color;
    public boolean autoEject;
    public boolean roundRobin;
    public int rrIndex = 0;
    public int delayTicks;
    public boolean isActive;
    public boolean clientActive;
    public TileComponentSecurity securityComponent = new TileComponentSecurity(this);
    public String[] methods = {"setDefaultColor", "setRoundRobin", "setAutoEject", "addFilter", "removeFilter",
          "addOreFilter", "removeOreFilter"};

    public TileEntityLogisticalSorter() {
        super("LogisticalSorter", BlockStateMachine.MachineType.LOGISTICAL_SORTER.baseEnergy);
        inventory = NonNullList.withSize(1, ItemStack.EMPTY);
        doAutoSync = false;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!world.isRemote) {
            delayTicks = Math.max(0, delayTicks - 1);

            if (delayTicks == 6) {
                setActive(false);
            }

            if (MekanismUtils.canFunction(this) && delayTicks == 0) {
                TileEntity back = Coord4D.get(this).offset(facing.getOpposite()).getTileEntity(world);
                TileEntity front = Coord4D.get(this).offset(facing).getTileEntity(world);

                boolean sentItems = false;
                int min = 0;

                outer:
                for (TransporterFilter filter : filters) {
                    for (StackSearcher search = new StackSearcher(back, facing.getOpposite()); search.i >= 0; ) {
                        InvStack invStack = filter.getStackFromInventory(search);

                        if (invStack == null || invStack.getStack().isEmpty()) {
                            break;
                        }

                        if (filter.canFilter(invStack.getStack(), true)) {
                            if (filter instanceof TItemStackFilter) {
                                TItemStackFilter itemFilter = (TItemStackFilter) filter;

                                if (itemFilter.sizeMode) {
                                    min = itemFilter.min;
                                }
                            }

                            TransitRequest request = TransitRequest.getFromStack(invStack.getStack());
                            TransitResponse response = emitItemToTransporter(front, request, filter.color, min);

                            if (!response.isEmpty()) {
                                invStack.use(response.stack.getCount());
                                back.markDirty();
                                setActive(true);
                                sentItems = true;

                                break outer;
                            }
                        }
                    }
                }

                if (!sentItems && autoEject) {
                    TransitRequest request = TransitRequest
                          .getTopStacks(back, facing.getOpposite(), 64, new StrictFilterFinder());
                    TransitResponse response = emitItemToTransporter(front, request, color, 0);

                    if (!response.isEmpty()) {
                        response.getInvStack(back, facing).use(response.stack.getCount());
                        back.markDirty();
                        setActive(true);
                    }
                }

                delayTicks = 10;
            }

            if (playersUsing.size() > 0) {
                for (EntityPlayer player : playersUsing) {
                    Mekanism.packetHandler
                          .sendTo(new TileEntityMessage(Coord4D.get(this), getGenericPacket(new TileNetworkList())),
                                (EntityPlayerMP) player);
                }
            }
        }
    }

    public TransitResponse emitItemToTransporter(TileEntity front, TransitRequest request, EnumColor filterColor,
          int min) {
        if (CapabilityUtils
              .hasCapability(front, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, facing.getOpposite())) {
            ILogisticalTransporter transporter = CapabilityUtils
                  .getCapability(front, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, facing.getOpposite());

            if (!roundRobin) {
                return TransporterUtils.insert(this, transporter, request, filterColor, true, min);
            } else {
                return TransporterUtils.insertRR(this, transporter, request, filterColor, true, min);
            }
        } else {
            return InventoryUtils.putStackInInventory(front, request, facing, false);
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        nbtTags.setInteger("controlType", controlType.ordinal());

        if (color != null) {
            nbtTags.setInteger("color", TransporterUtils.colors.indexOf(color));
        }

        nbtTags.setBoolean("autoEject", autoEject);
        nbtTags.setBoolean("roundRobin", roundRobin);

        nbtTags.setInteger("rrIndex", rrIndex);

        NBTTagList filterTags = new NBTTagList();

        for (TransporterFilter filter : filters) {
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

        if (nbtTags.hasKey("color")) {
            color = TransporterUtils.colors.get(nbtTags.getInteger("color"));
        }

        autoEject = nbtTags.getBoolean("autoEject");
        roundRobin = nbtTags.getBoolean("roundRobin");

        rrIndex = nbtTags.getInteger("rrIndex");

        if (nbtTags.hasKey("filters")) {
            NBTTagList tagList = nbtTags.getTagList("filters", NBT.TAG_COMPOUND);

            for (int i = 0; i < tagList.tagCount(); i++) {
                filters.add(TransporterFilter.readFromNBT(tagList.getCompoundTagAt(i)));
            }
        }
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            int type = dataStream.readInt();

            if (type == 0) {
                int clickType = dataStream.readInt();

                if (clickType == 0) {
                    color = TransporterUtils.increment(color);
                } else if (clickType == 1) {
                    color = TransporterUtils.decrement(color);
                } else if (clickType == 2) {
                    color = null;
                }
            } else if (type == 1) {
                autoEject = !autoEject;
            } else if (type == 2) {
                roundRobin = !roundRobin;
                rrIndex = 0;
            } else if (type == 3) {
                // Move filter up
                int filterIndex = dataStream.readInt();
                filters.swap(filterIndex, filterIndex - 1);
                for (EntityPlayer player : playersUsing) {
                    openInventory(player);
                }
            } else if (type == 4) {
                // Move filter down
                int filterIndex = dataStream.readInt();
                filters.swap(filterIndex, filterIndex + 1);
                for (EntityPlayer player : playersUsing) {
                    openInventory(player);
                }
            }
            return;
        }

        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            int type = dataStream.readInt();

            if (type == 0) {
                clientActive = dataStream.readBoolean();
                controlType = RedstoneControl.values()[dataStream.readInt()];

                int c = dataStream.readInt();

                if (c != -1) {
                    color = TransporterUtils.colors.get(c);
                } else {
                    color = null;
                }

                autoEject = dataStream.readBoolean();
                roundRobin = dataStream.readBoolean();

                filters.clear();

                int amount = dataStream.readInt();

                for (int i = 0; i < amount; i++) {
                    filters.add(TransporterFilter.readFromPacket(dataStream));
                }
            } else if (type == 1) {
                clientActive = dataStream.readBoolean();
                controlType = RedstoneControl.values()[dataStream.readInt()];

                int c = dataStream.readInt();

                if (c != -1) {
                    color = TransporterUtils.colors.get(c);
                } else {
                    color = null;
                }

                autoEject = dataStream.readBoolean();
                roundRobin = dataStream.readBoolean();
            } else if (type == 2) {
                filters.clear();

                int amount = dataStream.readInt();

                for (int i = 0; i < amount; i++) {
                    filters.add(TransporterFilter.readFromPacket(dataStream));
                }
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);

        data.add(0);

        data.add(isActive);
        data.add(controlType.ordinal());

        if (color != null) {
            data.add(TransporterUtils.colors.indexOf(color));
        } else {
            data.add(-1);
        }

        data.add(autoEject);
        data.add(roundRobin);

        data.add(filters.size());

        for (TransporterFilter filter : filters) {
            filter.write(data);
        }

        return data;
    }

    public TileNetworkList getGenericPacket(TileNetworkList data) {
        super.getNetworkedData(data);

        data.add(1);

        data.add(isActive);
        data.add(controlType.ordinal());

        if (color != null) {
            data.add(TransporterUtils.colors.indexOf(color));
        } else {
            data.add(-1);
        }

        data.add(autoEject);
        data.add(roundRobin);

        return data;

    }

    public TileNetworkList getFilterPacket(TileNetworkList data) {
        super.getNetworkedData(data);

        data.add(2);

        data.add(filters.size());

        for (TransporterFilter filter : filters) {
            filter.write(data);
        }

        return data;
    }

    public boolean canSendHome(ItemStack stack) {
        TileEntity back = Coord4D.get(this).offset(facing.getOpposite()).getTileEntity(world);
        return InventoryUtils.canInsert(back, null, stack, facing.getOpposite(), true);
    }

    public boolean hasInventory() {
        TileEntity tile = Coord4D.get(this).offset(facing.getOpposite()).getTileEntity(world);
        return TransporterUtils.isValidAcceptorOnSide(tile, facing.getOpposite());
    }

    public TransitResponse sendHome(ItemStack stack) {
        TileEntity back = Coord4D.get(this).offset(facing.getOpposite()).getTileEntity(world);
        return InventoryUtils.putStackInInventory(back, TransitRequest.getFromStack(stack), facing.getOpposite(), true);
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        if (side == facing || side == facing.getOpposite()) {
            return new int[]{0};
        }

        return InventoryUtils.EMPTY;
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
    public boolean getActive() {
        return isActive;
    }

    @Override
    public void setActive(boolean active) {
        isActive = active;

        if (clientActive != active) {
            Mekanism.packetHandler
                  .sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                        new Range4D(Coord4D.get(this)));

            if (active && client.enableMachineSounds) {
                world.playSound(null, getPos().getX(), getPos().getY(), getPos().getZ(), MekanismSounds.CLICK,
                      SoundCategory.BLOCKS, 0.3F, 1);
            }

            clientActive = active;
        }
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public boolean lightUpdate() {
        return false;
    }

    @Override
    public boolean sideIsConsumer(EnumFacing side) {
        return false;
    }

    @Override
    public boolean canSetFacing(int facing) {
        return true;
    }

    @Override
    public TileComponentSecurity getSecurity() {
        return securityComponent;
    }

    @Override
    public NBTTagCompound getConfigurationData(NBTTagCompound nbtTags) {
        if (color != null) {
            nbtTags.setInteger("color", TransporterUtils.colors.indexOf(color));
        }

        nbtTags.setBoolean("autoEject", autoEject);
        nbtTags.setBoolean("roundRobin", roundRobin);

        nbtTags.setInteger("rrIndex", rrIndex);

        NBTTagList filterTags = new NBTTagList();

        for (TransporterFilter filter : filters) {
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
        if (nbtTags.hasKey("color")) {
            color = TransporterUtils.colors.get(nbtTags.getInteger("color"));
        }

        autoEject = nbtTags.getBoolean("autoEject");
        roundRobin = nbtTags.getBoolean("roundRobin");

        rrIndex = nbtTags.getInteger("rrIndex");

        if (nbtTags.hasKey("filters")) {
            NBTTagList tagList = nbtTags.getTagList("filters", NBT.TAG_COMPOUND);

            for (int i = 0; i < tagList.tagCount(); i++) {
                filters.add(TransporterFilter.readFromNBT(tagList.getCompoundTagAt(i)));
            }
        }
    }

    @Override
    public String getDataType() {
        return getBlockType().getTranslationKey() + "." + fullName + ".name";
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        ItemDataUtils.setBoolean(itemStack, "hasSorterConfig", true);

        if (color != null) {
            ItemDataUtils.setInt(itemStack, "color", TransporterUtils.colors.indexOf(color));
        }

        ItemDataUtils.setBoolean(itemStack, "autoEject", autoEject);
        ItemDataUtils.setBoolean(itemStack, "roundRobin", roundRobin);

        NBTTagList filterTags = new NBTTagList();

        for (TransporterFilter filter : filters) {
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
        if (ItemDataUtils.hasData(itemStack, "hasSorterConfig")) {
            if (ItemDataUtils.hasData(itemStack, "color")) {
                color = TransporterUtils.colors.get(ItemDataUtils.getInt(itemStack, "color"));
            }

            autoEject = ItemDataUtils.getBoolean(itemStack, "autoEject");
            roundRobin = ItemDataUtils.getBoolean(itemStack, "roundRobin");

            if (ItemDataUtils.hasData(itemStack, "filters")) {
                NBTTagList tagList = ItemDataUtils.getList(itemStack, "filters");

                for (int i = 0; i < tagList.tagCount(); i++) {
                    filters.add(TransporterFilter.readFromNBT(tagList.getCompoundTagAt(i)));
                }
            }
        }
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) {
        if (arguments.length > 0) {
            if (method == 0) {
                if (!(arguments[0] instanceof String)) {
                    return new Object[]{"Invalid parameters."};
                }

                color = EnumColor.getFromDyeName((String) arguments[0]);

                if (color == null) {
                    return new Object[]{"Default color set to null"};
                } else {
                    return new Object[]{"Default color set to " + color.dyeName};
                }
            } else if (method == 1) {
                if (!(arguments[0] instanceof Boolean)) {
                    return new Object[]{"Invalid parameters."};
                }

                roundRobin = (Boolean) arguments[0];

                return new Object[]{"Round-robin mode set to " + roundRobin};
            } else if (method == 2) {
                if (!(arguments[0] instanceof Boolean)) {
                    return new Object[]{"Invalid parameters."};
                }

                autoEject = (Boolean) arguments[0];

                return new Object[]{"Auto-eject mode set to " + autoEject};
            } else if (method == 3) {
                if (arguments.length != 6 || !(arguments[0] instanceof String) || !(arguments[1] instanceof Double) ||
                      !(arguments[2] instanceof String) || !(arguments[3] instanceof Boolean) ||
                      !(arguments[4] instanceof Double) || !(arguments[5] instanceof Double)) {
                    return new Object[]{"Invalid parameters."};
                }

                TItemStackFilter filter = new TItemStackFilter();
                filter.itemType = new ItemStack(Item.getByNameOrId((String) arguments[0]), 1,
                      ((Double) arguments[1]).intValue());

                filter.color = EnumColor.getFromDyeName((String) arguments[2]);
                filter.sizeMode = (Boolean) arguments[3];
                filter.min = ((Double) arguments[4]).intValue();
                filter.max = ((Double) arguments[5]).intValue();
                filters.add(filter);

                return new Object[]{"Added filter."};
            } else if (method == 4) {
                if (arguments.length != 2 || !(arguments[0] instanceof String) || !(arguments[1] instanceof Double)) {
                    return new Object[]{"Invalid parameters."};
                }

                ItemStack stack = new ItemStack(Item.getByNameOrId((String) arguments[0]), 1,
                      ((Double) arguments[1]).intValue());
                Iterator<TransporterFilter> iter = filters.iterator();

                while (iter.hasNext()) {
                    TransporterFilter filter = iter.next();

                    if (filter instanceof TItemStackFilter) {
                        if (StackUtils.equalsWildcard(((TItemStackFilter) filter).itemType, stack)) {
                            iter.remove();
                            return new Object[]{"Removed filter."};
                        }
                    }
                }

                return new Object[]{"Couldn't find filter."};
            } else if (method == 5) {
                if (arguments.length != 2 || !(arguments[0] instanceof String) || !(arguments[1] instanceof String)) {
                    return new Object[]{"Invalid parameters."};
                }

                TOreDictFilter filter = new TOreDictFilter();
                filter.setOreDictName((String) arguments[0]);
                filter.color = EnumColor.getFromDyeName((String) arguments[1]);
                filters.add(filter);

                return new Object[]{"Added filter."};
            } else if (method == 6) {
                if (arguments.length != 1 || !(arguments[0] instanceof String)) {
                    return new Object[]{"Invalid parameters."};
                }

                String ore = (String) arguments[0];
                Iterator<TransporterFilter> iter = filters.iterator();

                while (iter.hasNext()) {
                    TransporterFilter filter = iter.next();

                    if (filter instanceof TOreDictFilter) {
                        if (((TOreDictFilter) filter).getOreDictName().equals(ore)) {
                            iter.remove();
                            return new Object[]{"Removed filter."};
                        }
                    }
                }

                return new Object[]{"Couldn't find filter."};
            }
        }

        for (EntityPlayer player : playersUsing) {
            Mekanism.packetHandler
                  .sendTo(new TileEntityMessage(Coord4D.get(this), getGenericPacket(new TileNetworkList())),
                        (EntityPlayerMP) player);
        }

        return null;
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

    private class StrictFilterFinder extends Finder {

        @Override
        public boolean modifies(ItemStack stack) {
            for (TransporterFilter filter : filters) {
                if (filter.canFilter(stack, false) && !filter.allowDefault) {
                    return false;
                }
            }

            return true;
        }
    }
}
