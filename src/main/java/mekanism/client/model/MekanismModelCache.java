package mekanism.client.model;

import java.util.HashSet;
import java.util.Set;
import mekanism.common.Mekanism;
import mekanism.common.tile.qio.TileEntityQIODriveArray.DriveStatus;
import net.minecraftforge.client.event.ModelBakeEvent;

public class MekanismModelCache extends BaseModelCache {

    public static final MekanismModelCache INSTANCE = new MekanismModelCache();
    private final Set<Runnable> callbacks = new HashSet<>();

    public final OBJModelData MEKASUIT = registerOBJ(Mekanism.rl("models/entity/mekasuit.obj"));
    public final OBJModelData MEKASUIT_MODULES = registerOBJ(Mekanism.rl("models/entity/mekasuit_modules.obj"));
    public final OBJModelData MEKATOOL = registerOBJ(Mekanism.rl("models/entity/mekatool.obj"));

    public final JSONModelData[] QIO_DRIVES = new JSONModelData[DriveStatus.STATUSES.length];

    public MekanismModelCache() {
        for (DriveStatus status : DriveStatus.STATUSES) {
            if (status == DriveStatus.NONE) {
                continue;
            }
            QIO_DRIVES[status.ordinal()] = registerJSON(status.getModel());
        }
    }

    @Override
    public void onBake(ModelBakeEvent evt) {
        super.onBake(evt);
        callbacks.forEach(Runnable::run);
    }

    public void reloadCallback(Runnable callback) {
        callbacks.add(callback);
    }
}
