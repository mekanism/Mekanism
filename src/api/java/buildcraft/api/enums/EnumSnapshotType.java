package buildcraft.api.enums;

public enum EnumSnapshotType {
    TEMPLATE(9),
    BLUEPRINT(3);

    public final int maxPerTick;

    EnumSnapshotType(int maxPerTick) {
        this.maxPerTick = maxPerTick;
    }
}
