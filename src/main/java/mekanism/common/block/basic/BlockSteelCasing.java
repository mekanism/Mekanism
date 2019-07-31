package mekanism.common.block.basic;

import mekanism.common.Mekanism;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.interfaces.IBlockDescriptive;
import mekanism.common.block.interfaces.IHasModel;
import mekanism.common.util.LangUtils;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockSteelCasing extends BlockTileDrops implements IBlockDescriptive, IHasModel {

    private final String name;

    public BlockSteelCasing() {
        super(Material.IRON);
        setHardness(5F);
        setResistance(10F);
        setCreativeTab(Mekanism.tabMekanism);
        this.name = "steel_casing";
        setTranslationKey(this.name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, this.name));
    }

    @Override
    public String getDescription() {
        //TODO: Should name just be gotten from registry name
        return LangUtils.localize("tooltip.mekanism." + this.name);
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
        return 9F;
    }
}