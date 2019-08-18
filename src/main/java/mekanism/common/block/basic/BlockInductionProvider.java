package mekanism.common.block.basic;

import java.util.Locale;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tile.TileEntityInductionProvider;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockInductionProvider extends BlockTileDrops implements ITieredBlock<InductionProviderTier>, IHasTileEntity<TileEntityInductionProvider> {

    private final InductionProviderTier tier;

    public BlockInductionProvider(InductionProviderTier tier) {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(5F, 10F));
        this.tier = tier;
        setRegistryName(new ResourceLocation(Mekanism.MODID, tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT) + "_induction_provider"));
    }

    @Override
    public InductionProviderTier getTier() {
        return tier;
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            TileEntity tileEntity = new Coord4D(pos, world).getTileEntity(world);
            if (tileEntity instanceof TileEntityMekanism) {
                ((TileEntityMekanism) tileEntity).onNeighborChange(neighborBlock);
            }
        }
    }

    @Override
    public TileEntityType<TileEntityInductionProvider> getTileType() {
        switch (tier) {
            case ADVANCED:
                return MekanismTileEntityTypes.ADVANCED_INDUCTION_PROVIDER;
            case ELITE:
                return MekanismTileEntityTypes.ELITE_INDUCTION_PROVIDER;
            case ULTIMATE:
                return MekanismTileEntityTypes.ULTIMATE_INDUCTION_PROVIDER;
            case BASIC:
            default:
                return MekanismTileEntityTypes.BASIC_INDUCTION_PROVIDER;
        }
    }
}