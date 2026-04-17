package fr.orinstonerep.models;

import java.io.Serializable;
import java.util.UUID;

public class PlayerReputation implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String uuid;
    private String name;
    private int score;
    private long lastDailyBonus;

    public PlayerReputation(UUID uuid, String name, int score, long lastDailyBonus) {
        this.uuid = uuid.toString();
        this.name = name;
        this.score = score;
        this.lastDailyBonus = lastDailyBonus;
    }

    public UUID getUuid()              { return UUID.fromString(uuid); }
    public String getName()            { return name; }
    public void setName(String name)   { this.name = name; }
    public int getScore()              { return score; }
    public void setScore(int score)    { this.score = score; }
    public long getLastDailyBonus()    { return lastDailyBonus; }
    public void setLastDailyBonus(long t) { this.lastDailyBonus = t; }

    public ReputationTier getTier() {
        return ReputationTier.fromScore(score);
    }

    public boolean hasReceivedDailyBonusToday() {
        return (System.currentTimeMillis() - lastDailyBonus) < 86_400_000L;
    }
}
