package mekanism.common.block.basic;

import mekanism.common.Mekanism;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.interfaces.IBlockDescriptive;
import mekanism.common.block.interfaces.IHasModel;
import mekanism.common.util.LangUtils;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

public class BlockTeleporterFrame extends BlockTileDrops implements IBlockDescriptive, IHasModel {

    private final String name;

    public BlockTeleporterFrame() {
        super(Material.IRON);
        setHardness(5F);
        setResistance(10F);
        //It gets multiplied by 15 when being set
        setLightLevel(12F / 15.0F);
        setCreativeTab(Mekanism.tabMekanism);
        this.name = "teleporter_frame";
        setTranslationKey(this.name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, this.name));
    }

    @Override
    public String getDescription() {
        //TODO: Should name just be gotten from registry name
        return LangUtils.localize("tooltip.mekanism." + this.name);
    }
}