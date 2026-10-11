package si.um.feri.backgammon.persistence;

/** A leaderboard row. Public mutable fields + no-arg constructor for JSON (de)serialization. */
public class PlayerScore {
    public String username;
    public int wins;
    /** Sum of won game values: 1 per single game, 2 per gammon, 3 per backgammon. */
    public int points;

    public PlayerScore() {
    }

    public PlayerScore(String username) {
        this.username = username;
    }
}
