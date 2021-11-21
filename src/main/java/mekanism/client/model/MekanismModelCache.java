package mekanism.client.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.api.providers.IRobitSkinProvider;
import mekanism.api.robit.RobitSkin;
import mekanism.common.Mekanism;
import mekanism.common.tile.qio.TileEntityQIODriveArray.DriveStatus;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;

public class MekanismModelCache extends BaseModelCache {

    public static final MekanismModelCache INSTANCE = new MekanismModelCache();
    private final Set<Runnable> callbacks = new HashSet<>();

    public final OBJModelData MEKASUIT = registerOBJ(Mekanism.rl("models/entity/mekasuit.obj"));
    public final OBJModelData MEKASUIT_MODULES = registerOBJ(Mekanism.rl("models/entity/mekasuit_modules.obj"));
    public final OBJModelData MEKATOOL = registerOBJ(Mekanism.rl("models/entity/mekatool.obj"));

    public final JSONModelData[] QIO_DRIVES = new JSONModelData[DriveStatus.STATUSES.length];
    private final Map<ResourceLocation, JSONModelData> ROBIT_SKINS = new HashMap<>();
    private IBakedModel BASE_ROBIT;

    private MekanismModelCache() {
        for (DriveStatus status : DriveStatus.STATUSES) {
            if (status == DriveStatus.NONE) {
                continue;
            }
            QIO_DRIVES[status.ordinal()] = registerJSON(status.getModel());
        }
    }

    @Override
    public void setup() {
        for (RobitSkin skin : MekanismAPI.robitSkinRegistry()) {
            ResourceLocation customModel = skin.getCustomModel();
            if (customModel != null) {
                ROBIT_SKINS.put(skin.getRegistryName(), registerJSON(customModel));
            }
        }
        super.setup();
    }

    @Override
    public void onBake(ModelBakeEvent evt) {
        super.onBake(evt);
        callbacks.forEach(Runnable::run);
        BASE_ROBIT = getBakedModel(evt, new ModelResourceLocation(Mekanism.rl("robit"), "inventory"));
    }

    public void reloadCallback(Runnable callback) {
        callbacks.add(callback);
    }

    @Nullable
    public IBakedModel getRobitSkin(@Nonnull IRobitSkinProvider skin) {
        JSONModelData data = ROBIT_SKINS.get(skin.getRegistryName());
        return data == null ? BASE_ROBIT : data.getBakedModel();
    }
}
