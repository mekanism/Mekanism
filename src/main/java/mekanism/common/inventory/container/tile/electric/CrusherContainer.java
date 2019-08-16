package mekanism.common.inventory.container.tile.electric;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.recipe.machines.CrusherRecipe;
import mekanism.common.tile.TileEntityCrusher;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class CrusherContainer extends ElectricMachineContainer<CrusherRecipe, TileEntityCrusher> {

    public CrusherContainer(int id, PlayerInventory inv, TileEntityCrusher tile) {
        super(MekanismContainerTypes.CRUSHER, id, inv, tile);
    }

    public CrusherContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityCrusher.class));
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.crusher");
    }
}