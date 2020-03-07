package mekanism.common.block;

import javax.annotation.Nonnull;
import mekanism.common.MekanismLang;
import mekanism.common.Resource;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.interfaces.IHasDescription;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;

/**
 * Block class for handling multiple ore block IDs. 0: Osmium Ore 1: Copper Ore 2: Tin Ore
 *
 * @author AidanBrady
 */
public class BlockOre extends Block implements IHasDescription {

    private final Resource resource;

    public BlockOre(Resource resource) {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(3F, 5F));
        this.resource = resource;
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        switch (resource) {
            case OSMIUM:
                return MekanismLang.DESCRIPTION_OSMIUM_ORE;
            case COPPER:
                return MekanismLang.DESCRIPTION_COPPER_ORE;
            case TIN:
                return MekanismLang.DESCRIPTION_TIN_ORE;
            default:
                return MekanismLang.INVALID;
        }
    }

    @Override
    public ToolType getHarvestTool(BlockState state) {
        return ToolType.PICKAXE;
    }

    @Override
    public int getHarvestLevel(BlockState state) {
        return 1;
    }
}