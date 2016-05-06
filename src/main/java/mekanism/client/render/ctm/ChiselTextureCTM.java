package mekanism.client.render.ctm;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.Getter;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.ResourceLocation;

public class ChiselTextureCTM {

	@Getter
    protected BlockRenderTypeCTM type;
    @Getter
    protected EnumWorldBlockLayer layer;

    protected TextureSpriteCallback[] sprites;
    
    public ChiselTextureCTM(BlockRenderTypeCTM type, EnumWorldBlockLayer layer, TextureSpriteCallback[] sprites) {
    	this.type = type;
        this.layer = layer;
        this.sprites = sprites;
    }

    public List<BakedQuad> transformQuad(BakedQuad bq, CTMBlockRenderContext context, int quadGoal) {
        Quad quad = Quad.from(bq, DefaultVertexFormats.ITEM);
        if (context == null) {
            return Collections.singletonList(quad.transformUVs(sprites[0].getSprite()).rebake());
        }
        
        Quad[] quads = quad.subdivide(4);
        
        int[] ctm = ((CTMBlockRenderContext)context).getCTM(bq.getFace()).getSubmapIndices();
        
        for (int i = 0; i < quads.length; i++) {
            Quad q = quads[i];
            if (q != null) {
                int ctmid = q.getUvs().normalize().getQuadrant();
                quads[i] = q.grow().transformUVs(sprites[ctm[ctmid] > 15 ? 0 : 1].getSprite(), CTM.uvs[ctm[ctmid]].normalize());
            }
        }
        return Arrays.stream(quads).filter(Objects::nonNull).map(q -> q.rebake()).collect(Collectors.toList());
    }
    
    public TextureAtlasSprite getParticle(){
        return sprites[0].getSprite();
    }
    
    public Collection<ResourceLocation> getTextures() {
        return Arrays.stream(sprites).map(TextureSpriteCallback::getLocation).collect(Collectors.toList());
    }
}
