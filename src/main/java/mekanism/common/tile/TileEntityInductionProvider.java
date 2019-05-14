package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityInductionProvider extends TileEntityBasicBlock {

    public InductionProviderTier tier = InductionProviderTier.getDefault();

    @Override
    public void onUpdate() {
    }

    public String getName() {
        return LangUtils.localize(getBlockType().getTranslationKey() + ".InductionProvider" + tier.getBaseTier().getSimpleName() + ".name");
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            InductionProviderTier prevTier = tier;
            tier = InductionProviderTier.get(dataStream.readInt());
            if (prevTier != tier) {
                MekanismUtils.updateBlock(world, getPos());
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(tier.ordinal());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);
        tier = InductionProviderTier.get(nbtTags.getInteger("tier"));
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);
        nbtTags.setInteger("tier", tier.ordinal());
        return nbtTags;
    }
}