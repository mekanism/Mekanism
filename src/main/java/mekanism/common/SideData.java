package mekanism.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import mekanism.api.gas.GasTank;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.base.ITankManager;
import net.minecraftforge.fluids.IFluidTank;

public class SideData implements IHasTranslationKey {

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

    //TODO: Make slots be a list of IInventorySlots
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

    @Override
    public String getTranslationKey() {
        return "side_data.mekanism." + name.toLowerCase(Locale.ROOT);
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

    public IFluidTank[] getFluidTankInfo(ITankManager manager) {
        Object[] tanks = manager.getTanks();
        List<IFluidTank> infos = new ArrayList<>();
        if (tanks == null) {
            return infos.toArray(new IFluidTank[]{});
        }
        for (int slot : availableSlots) {
            if (slot <= tanks.length - 1 && tanks[slot] instanceof IFluidTank) {
                infos.add(((IFluidTank) tanks[slot]));
            }
        }
        return infos.toArray(new IFluidTank[]{});
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