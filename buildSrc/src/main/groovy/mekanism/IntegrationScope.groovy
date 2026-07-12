package mekanism

enum IntegrationScope {
    BASE_ONLY(true, false),
    DATA_ONLY(false, true),
    ALL(true, true);

    final boolean hasBase
    final boolean hasData

    private IntegrationScope(boolean hasBase, boolean hasData) {
        this.hasBase = hasBase
        this.hasData = hasData
    }
}