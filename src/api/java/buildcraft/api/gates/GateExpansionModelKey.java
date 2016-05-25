package buildcraft.api.gates;

import java.util.Arrays;

import net.minecraft.util.BlockRenderLayer;

public class GateExpansionModelKey<K extends GateExpansionModelKey<K>> {
    public final BlockRenderLayer layer;
    public final IExpansionBaker<K> baker;
    private final int hash;

    public GateExpansionModelKey(BlockRenderLayer layer, IExpansionBaker<K> baker) {
        if (layer != BlockRenderLayer.CUTOUT && layer != BlockRenderLayer.TRANSLUCENT) {
            throw new IllegalArgumentException("Unsuported layer! Was " + layer + ", wanted CUTOUT or TRANSLUCENT");
        }
        if (baker == null) throw new NullPointerException("baker");
        this.layer = layer;
        this.baker = baker;
        hash = Arrays.hashCode(new int[] { layer.hashCode(), System.identityHashCode(baker) });
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        GateExpansionModelKey<?> other = (GateExpansionModelKey<?>) obj;
        if (baker != other.baker) return false;
        return true;
    }
}
