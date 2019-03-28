package mekanism.common;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.EnumColor;
import mekanism.api.gas.GasTank;
import mekanism.common.base.ITankManager;
import mekanism.common.util.LangUtils;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

public class SideData {

    /**
     * The color of this SideData
     */
    public EnumColor color;

    /**
     * The name of this SideData
     */
    public String name;

    /**
     * int[] of available side slots, can be used for items, gases, or items
     */
    public int[] availableSlots;

    /**
     * IOState representing this SideData
     */
    public IOState ioState;

    public SideData(String n, EnumColor colour, int[] slots) {
        name = n;
        color = colour;
        availableSlots = slots;
    }

    public SideData(String n, EnumColor colour, IOState state) {
        name = n;
        color = colour;
        ioState = state;
    }

    public String localize() {
        return LangUtils.localize("sideData." + name);
    }

    public boolean hasSlot(int... slots) {
        for (int i : availableSlots) {
            for (int slot : slots) {
                if (i == slot) {
                    return true;
                }
            }
        }

        return false;
    }

    public FluidTankInfo[] getFluidTankInfo(ITankManager manager) {
        Object[] tanks = manager.getTanks();
        List<FluidTankInfo> infos = new ArrayList<>();

        if (tanks == null) {
            return infos.toArray(new FluidTankInfo[]{});
        }

        for (int slot : availableSlots) {
            if (slot <= tanks.length - 1 && tanks[slot] instanceof IFluidTank) {
                infos.add(((IFluidTank) tanks[slot]).getInfo());
            }
        }

        return infos.toArray(new FluidTankInfo[]{});
    }

    public GasTank getGasTank(ITankManager manager) {
        Object[] tanks = manager.getTanks();

        if (tanks == null || tanks.length < 1 || !(tanks[0] instanceof GasTank)) {
            return null;
        }

        return (GasTank) tanks[0];
    }

    public enum IOState {
        INPUT,
        OUTPUT,
        OFF
    }
}
