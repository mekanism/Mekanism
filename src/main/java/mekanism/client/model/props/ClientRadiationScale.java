package mekanism.client.model.props;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import mekanism.common.lib.radiation.ClientRadiation;
import mekanism.common.lib.radiation.RadiationScale;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class ClientRadiationScale  implements SelectItemModelProperty<RadiationScale> {

    public static final ClientRadiationScale INSTANCE = new ClientRadiationScale();
    public static final Type<ClientRadiationScale, RadiationScale> TYPE = SelectItemModelProperty.Type.create(MapCodec.unit(INSTANCE), RadiationScale.CODEC);

    private ClientRadiationScale(){}

    @Nullable
    @Override
    public RadiationScale get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity entity, int i, ItemDisplayContext itemDisplayContext) {
        if (entity instanceof Player) {
            return ClientRadiation.getClientScale();
        }
        return null;
    }

    @Override
    public Codec<RadiationScale> valueCodec() {
        return RadiationScale.CODEC;
    }

    @Override
    public Type<ClientRadiationScale, RadiationScale> type() {
        return TYPE;
    }
}
