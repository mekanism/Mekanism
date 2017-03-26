package ic2.api.item;

public enum HudMode {
	DISABLED("ic2.hud.disabled"), BASIC("ic2.hud.basic"), EXTENDED("ic2.hud.extended"), ADVANCED("ic2.hud.advanced");

	private HudMode(String key) {
		translationKey = key;
	}

	private final String translationKey;

	public boolean shouldDisplay() {
		return this != DISABLED;
	}

	public boolean hasTooltip() {
		return this == EXTENDED || this == ADVANCED;
	}

	public String getTranslationKey() {
		return translationKey;
	}

	public int getID() {
		return ordinal();
	}

	public static HudMode getFromID(int ID) {
		return VALUES[ID % VALUES.length];
	}

	public static int getMaxMode() {
		return VALUES.length - 1;
	}

	private static final HudMode[] VALUES = values();
}