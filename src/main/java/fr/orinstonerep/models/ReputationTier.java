package fr.orinstonerep.models;

public enum ReputationTier {
    HORRIBLE   ("horrible",   -120, -81),
    MAUVAISE   ("mauvaise",    -80, -31),
    DANGEREUSE ("dangereuse",  -30,  -1),
    NEUTRE     ("neutre",        0,   0),
    CORRECTE   ("correcte",      1,  40),
    BONNE      ("bonne",        41,  80),
    HONORABLE  ("honorable",    81, 120);

    private final String key;
    private final int min;
    private final int max;

    ReputationTier(String key, int min, int max) {
        this.key = key;
        this.min = min;
        this.max = max;
    }

    public String getKey() { return key; }

    public static ReputationTier fromScore(int score) {
        for (ReputationTier t : values())
            if (score >= t.min && score <= t.max) return t;
        return score < 0 ? HORRIBLE : HONORABLE;
    }
}
