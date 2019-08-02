package mekanism.common.block.basic;

import mekanism.common.Mekanism;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.interfaces.IBlockDescriptive;
import mekanism.common.block.interfaces.IHasModel;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

public class BlockTeleporterFrame extends BlockTileDrops implements IBlockDescriptive, IHasModel {

    public BlockTeleporterFrame() {
        super(Material.IRON);
        setHardness(5F);
        setResistance(10F);
        //It gets multiplied by 15 when being set
        setLightLevel(12F / 15.0F);
        setRegistryName(new ResourceLocation(Mekanism.MODID, "teleporter_frame"));
    }
}