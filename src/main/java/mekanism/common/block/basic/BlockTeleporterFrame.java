package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasDescription;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockTeleporterFrame extends BlockMekanism implements IHasDescription {

    public BlockTeleporterFrame() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(5F, 10F).lightValue(12));
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return MekanismLang.DESCRIPTION_TELEPORTER_FRAME;
    }
}