package mekanism.common.multiblock;

import mekanism.api.Coord4D;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;

public interface IStructuralMultiblock {

    boolean onActivate(EntityPlayer player, EnumHand hand, ItemStack stack);

    boolean canInterface(TileEntity controller);

    void setController(Coord4D coord);

    void doUpdate();
}
