package mekanism.client.render.tileentity;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.Mekanism;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.TileEntityPersonalChest;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

@NothingNullByDefault
public class RenderPersonalChest extends ChestRenderer<TileEntityPersonalChest> {

    //nb: this is stitched by the Item's use of the texture
    private static final Identifier TEXTURE = Mekanism.rl("models/personal_chest");
    public static final SpriteId MATERIAL = Sheets.BLOCKS_MAPPER.apply(TEXTURE);

    public RenderPersonalChest(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Nullable
    @Override
    protected SpriteId getCustomSprite(TileEntityPersonalChest chest, ChestRenderState state) {
        return MATERIAL;
    }

    protected String getProfilerSection() {
        return ProfilerConstants.PERSONAL_CHEST;
    }
}