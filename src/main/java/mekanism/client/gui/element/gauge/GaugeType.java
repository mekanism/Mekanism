package mekanism.client.gui.element.gauge;

public enum GaugeType {
    STANDARD(GaugeInfo.STANDARD, GaugeOverlay.STANDARD),
    STANDARD_RED(GaugeInfo.RED, GaugeOverlay.STANDARD),
    STANDARD_YELLOW(GaugeInfo.YELLOW, GaugeOverlay.STANDARD),
    WIDE(GaugeInfo.STANDARD, GaugeOverlay.WIDE),
    SMALL(GaugeInfo.STANDARD, GaugeOverlay.SMALL),
    SMALL_BLUE(GaugeInfo.BLUE, GaugeOverlay.SMALL);

    private final GaugeInfo gaugeInfo;
    private final GaugeOverlay gaugeOverlay;

    GaugeType(GaugeInfo gaugeInfo, GaugeOverlay gaugeOverlay) {
        this.gaugeInfo = gaugeInfo;
        this.gaugeOverlay = gaugeOverlay;
    }

    public GaugeInfo getGaugeInfo() {
        return gaugeInfo;
    }

    public GaugeOverlay getGaugeOverlay() {
        return gaugeOverlay;
    }
}