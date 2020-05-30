package mekanism.client.render.lib;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class QuadUtils {

    private static final float eps = 1F / 0x100;

    public static List<Quad> transformQuads(List<Quad> orig, QuadTransformation transformation) {
        return orig.stream().peek(q -> transformation.transform(q)).collect(Collectors.toList());
    }

    public static List<BakedQuad> transformBakedQuads(List<BakedQuad> orig, QuadTransformation transformation) {
        return orig.stream().map(q -> new Quad(q)).peek(q -> transformation.transform(q)).map(q -> q.bake()).collect(Collectors.toList());
    }

    public static List<BakedQuad> transformAndBake(List<Quad> orig, QuadTransformation transformation) {
        return orig.stream().peek(q -> transformation.transform(q)).map(q -> q.bake()).collect(Collectors.toList());
    }

    // this is an adaptation of fry's original UV contractor (pulled from BakedQuadBuilder).
    // ultimately this fixes UVs bleeding over the edge slightly when dealing with smaller models or tight UV bounds
    public static void contractUVs(Quad quad) {
        TextureAtlasSprite texture = quad.getTexture();
        float sizeX = texture.getWidth() / (texture.getMaxU() - texture.getMinU());
        float sizeY = texture.getHeight() / (texture.getMaxV() - texture.getMinV());
        float ep = 1F / (Math.max(sizeX, sizeY) * 0x100);
        float[] newUs = contract(quad, v -> v.getTexU(), ep);
        float[] newVs = contract(quad, v -> v.getTexV(), ep);
        for (int i = 0; i < quad.getVertices().length; i++) {
            quad.getVertices()[i].texRaw(newUs[i], newVs[i]);
        }
    }

    private static float[] contract(Quad quad, Function<Vertex, Float> uvf, float ep) {
        float center = 0;
        float[] ret = new float[4];
        for (int v = 0; v < 4; v++) {
            center += uvf.apply(quad.getVertices()[v]);
        }
        center /= 4;
        for (int v = 0; v < 4; v++) {
            float orig = uvf.apply(quad.getVertices()[v]);
            float shifted = orig * (1 - eps) + center * eps;
            float delta = orig - shifted;
            if(Math.abs(delta) < ep) { // not moving a fraction of a pixel
                float centerDelta = Math.abs(orig - center);
                if(centerDelta < 2 * ep)  { // center is closer than 2 fractions of a pixel, don't move too close
                    shifted = (orig + center) / 2;
                } else { // move at least by a fraction
                    shifted = orig + (delta < 0 ? ep : -ep);
                }
            }
            ret[v] = shifted;
        }
        return ret;
    }
}
