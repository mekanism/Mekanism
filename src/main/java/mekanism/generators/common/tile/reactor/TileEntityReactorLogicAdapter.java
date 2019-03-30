package mekanism.generators.common.tile.reactor;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.util.LangUtils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityReactorLogicAdapter extends TileEntityReactorBlock implements IComputerIntegration {

    private static final String[] methods = new String[]{"isIgnited", "canIgnite", "getPlasmaHeat", "getMaxPlasmaHeat",
          "getCaseHeat", "getMaxCaseHeat", "getInjectionRate", "setInjectionRate", "hasFuel", "getProducing",
          "getIgnitionTemp",
          "getEnergy", "getMaxEnergy", "getWater", "getSteam", "getFuel", "getDeuterium", "getTritium"};
    public ReactorLogic logicType = ReactorLogic.DISABLED;
    public boolean activeCooled;
    public boolean prevOutputting;

    public TileEntityReactorLogicAdapter() {
        super();
        fullName = "ReactorLogicAdapter";
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!world.isRemote) {
            boolean outputting = checkMode();

            if (outputting != prevOutputting) {
                world.notifyNeighborsOfStateChange(getPos(), getBlockType(), true);
            }

            prevOutputting = outputting;
        }
    }

    public boolean isFrame() {
        return false;
    }

    public boolean checkMode() {
        if (world.isRemote) {
            return prevOutputting;
        }

        if (getReactor() == null || !getReactor().isFormed()) {
            return false;
        }

        switch (logicType) {
            case DISABLED:
                return false;
            case READY:
                return getReactor().getPlasmaTemp() >= getReactor().getIgnitionTemperature(activeCooled);
            case CAPACITY:
                return getReactor().getPlasmaTemp() >= getReactor().getMaxPlasmaTemperature(activeCooled);
            case DEPLETED:
                return (getReactor().getDeuteriumTank().getStored() < getReactor().getInjectionRate() / 2) ||
                      (getReactor().getTritiumTank().getStored() < getReactor().getInjectionRate() / 2);
            default:
                return false;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        logicType = ReactorLogic.values()[nbtTags.getInteger("logicType")];
        activeCooled = nbtTags.getBoolean("activeCooled");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        nbtTags.setInteger("logicType", logicType.ordinal());
        nbtTags.setBoolean("activeCooled", activeCooled);

        return nbtTags;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            int type = dataStream.readInt();

            if (type == 0) {
                activeCooled = !activeCooled;
            } else if (type == 1) {
                logicType = ReactorLogic.values()[dataStream.readInt()];
            }

            return;
        }

        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            logicType = ReactorLogic.values()[dataStream.readInt()];
            activeCooled = dataStream.readBoolean();
            prevOutputting = dataStream.readBoolean();
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);

        data.add(logicType.ordinal());
        data.add(activeCooled);
        data.add(prevOutputting);

        return data;
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws Exception {
        if (getReactor() == null || !getReactor().isFormed()) {
            return new Object[]{"Unformed."};
        }

        switch (method) {
            case 0:
                return new Object[]{getReactor().isBurning()};
            case 1:
                return new Object[]{getReactor().getPlasmaTemp() >= getReactor().getIgnitionTemperature(activeCooled)};
            case 2:
                return new Object[]{getReactor().getPlasmaTemp()};
            case 3:
                return new Object[]{getReactor().getMaxPlasmaTemperature(activeCooled)};
            case 4:
                return new Object[]{getReactor().getCaseTemp()};
            case 5:
                return new Object[]{getReactor().getMaxCasingTemperature(activeCooled)};
            case 6:
                return new Object[]{getReactor().getInjectionRate()};
            case 7:
                if (arguments[0] instanceof Double) {
                    getReactor().setInjectionRate(((Double) arguments[0]).intValue());
                    return new Object[]{"Injection rate set."};
                } else {
                    return new Object[]{"Invalid parameters."};
                }
            case 8:
                return new Object[]{
                      (getReactor().getDeuteriumTank().getStored() >= getReactor().getInjectionRate() / 2) &&
                            (getReactor().getTritiumTank().getStored() >= getReactor().getInjectionRate() / 2)};
            case 9:
                return new Object[]{getReactor().getPassiveGeneration(false, true)};
            case 10:
                return new Object[]{getReactor().getIgnitionTemperature(activeCooled)};
            case 11:
                return new Object[]{getReactor().getBufferedEnergy()};
            case 12:
                return new Object[]{getReactor().getBufferSize()};
            case 13:
                return new Object[]{getReactor().getWaterTank().getFluidAmount()};
            case 14:
                return new Object[]{getReactor().getSteamTank().getFluidAmount()};
            case 15:
                return new Object[]{getReactor().getFuelTank().getStored()};
            case 16:
                return new Object[]{getReactor().getDeuteriumTank().getStored()};
            case 17:
                return new Object[]{getReactor().getTritiumTank().getStored()};
            default:
                throw new NoSuchMethodException();
        }
    }

    public enum ReactorLogic {
        DISABLED("disabled", new ItemStack(Items.GUNPOWDER)),
        READY("ready", new ItemStack(Items.REDSTONE)),
        CAPACITY("capacity", new ItemStack(Items.REDSTONE)),
        DEPLETED("depleted", new ItemStack(Items.REDSTONE));

        private String name;
        private ItemStack renderStack;

        ReactorLogic(String s, ItemStack stack) {
            name = s;
            renderStack = stack;
        }

        public ItemStack getRenderStack() {
            return renderStack;
        }

        public String getLocalizedName() {
            return LangUtils.localize("reactor." + name);
        }

        public String getDescription() {
            return LangUtils.localize("reactor." + name + ".desc");
        }
    }
}
