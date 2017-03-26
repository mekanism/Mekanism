package buildcraft.api.transport;

import net.minecraft.item.EnumDyeColor;

import buildcraft.api.transport.pipe.IPipeHolder;

public interface IWireManager {

    IPipeHolder getHolder();

    void updateBetweens(boolean recursive);

    EnumDyeColor getColorOfPart(EnumWirePart part);

    EnumDyeColor removePart(EnumWirePart part);

    boolean addPart(EnumWirePart part, EnumDyeColor colour);

    boolean hasPartOfColor(EnumDyeColor color);

    boolean isPowered(EnumWirePart part);

    boolean isAnyPowered(EnumDyeColor color);
}
