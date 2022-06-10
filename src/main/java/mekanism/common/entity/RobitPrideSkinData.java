package mekanism.common.entity;

import java.util.Locale;

public enum RobitPrideSkinData {
    PRIDE(0xFFD12229, 0xFFF68A1E, 0xFFFDE01A, 0xFF007940, 0xFF24408E, 0xFF732982),
    LESBIAN(0xFFD52D00, 0xFFEA6820, 0xFFFA8F47, 0xFFFDD1B3, 0xFFE9B8D6, 0xFFC85E9E, 0xFFB24687, 0xFFA30262),
    TRANS(0xFF732982, 0xFFF5A9B8, 0xFFFFFFFF, 0xFFF5A9B8, 0xFF732982),
    ARO(0xFF3DA745, 0xFFA7D37A, 0xFFFFFFFF, 0xFFABABAB, 0xFF000000),
    ACE(0xFF000000, 0xFFABABAB, 0xFFFFFFFF, 0xFF7E287F),
    BI(0xFFD51673, 0xFFD51673, 0xFF9B4F97, 0xFF1E419B, 0xFF1E419B),
    ENBY(0xFFF7EE37, 0xFFFFFFFF, 0xFF8A5FA7, 0xFF000000),
    PAN(0xFFED1F8C, 0xFFED1F8C, 0xFFFCDB04, 0xFFFCDB04, 0xFF48ADE3, 0xFF48ADE3),
    GAY(0xFF078D70, 0xFF26CEAA, 0xFF99E8C2, 0xFFFFFFFF, 0xFF7BADE3, 0xFF5049CB, 0xFF3E1A78);

    private final int[] color;
    RobitPrideSkinData(int... color) {
        this.color = color;
    }

    public int[] getColor() {
        return color;
    }

    public String lowerCaseName() {
        return name().toLowerCase(Locale.ENGLISH);
    }

    public String displayName() {
        if (this == PRIDE)
            return "Pride";
        StringBuilder builder = new StringBuilder();
        for (char character: name().toCharArray()) {
            if (builder.isEmpty()) {
                builder.append(character);
            } else {
                builder.append(Character.toLowerCase(character));
            }
        }
        builder.append(" Pride");
        return builder.toString();
    }
}
