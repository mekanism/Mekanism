package mekanism.common.resource;

public enum OreType {
    COPPER(PrimaryResource.COPPER, 16, 8, 0, 0, 60),
    TIN(PrimaryResource.TIN, 14, 8, 0, 0, 60),
    OSMIUM(PrimaryResource.OSMIUM, 12, 8, 0, 0, 60),
    URANIUM(PrimaryResource.URANIUM, 8, 8, 0, 0, 60),
    FLUORITE(MiscResource.FLUORITE, 6, 12, 0, 0, 32),
    LEAD(PrimaryResource.LEAD, 8, 8, 0, 0, 48);

    private final IResource resource;
    private final int perChunk;
    private final int maxVeinSize;
    private final int bottomOffset;
    private final int topOffset;
    private final int maxHeight;

    OreType(IResource resource, int perChunk, int maxVeinSize, int bottomOffset, int topOffset, int maxHeight) {
        this.resource = resource;
        this.perChunk = perChunk;
        this.maxVeinSize = maxVeinSize;
        this.bottomOffset = bottomOffset;
        this.topOffset = topOffset;
        this.maxHeight = maxHeight;
    }

    public IResource getResource() {
        return resource;
    }

    public int getPerChunk() {
        return perChunk;
    }

    public int getMaxVeinSize() {
        return maxVeinSize;
    }

    public int getBottomOffset() {
        return bottomOffset;
    }

    public int getTopOffset() {
        return topOffset;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public static OreType get(IResource resource) {
        for (OreType ore : values()) {
            if (resource == ore.resource) {
                return ore;
            }
        }
        return null;
    }
}
