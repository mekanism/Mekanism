package buildcraft.api.transport.pluggable;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IPluggableStaticBaker<K extends PluggableModelKey> {
    List<BakedQuad> bake(K key);
}
