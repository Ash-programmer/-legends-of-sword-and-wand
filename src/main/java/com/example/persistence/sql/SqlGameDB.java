package com.example.persistence.sql;

import com.example.domain.Campaign;
import com.example.domain.Party;
import com.example.domain.PvPInvite;
import com.example.domain.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqlGameDB {

    private static final String DB_URL = "jdbc:sqlite:identifier.sqlite";
    private static SqlGameDB instance;

    private final Gson gson;

    private SqlGameDB() {
        this.gson = new GsonBuilder().create();
        initDatabase();
    }

    public static SqlGameDB getInstance() {
        if (instance == null) {
            instance = new SqlGameDB();
        }
        return instance;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void initDatabase() {
        String createUsersTable = """
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT NOT NULL UNIQUE,
            password TEXT NOT NULL,
            gold INTEGER NOT NULL DEFAULT 0,
            score INTEGER NOT NULL DEFAULT 0,
            ranking INTEGER NOT NULL DEFAULT 0
        );
        """;

        String createCampaignsTable = """
        CREATE TABLE IF NOT EXISTS campaigns (
            user_id INTEGER PRIMARY KEY,
            campaign_json TEXT NOT NULL,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
        );
        """;

        String createPartiesTable = """
        CREATE TABLE IF NOT EXISTS parties (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            party_json TEXT NOT NULL,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
        );
        """;

        String createInvitesTable = """
        CREATE TABLE IF NOT EXISTS pvp_invites (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            from_user_id INTEGER NOT NULL,
            from_username TEXT NOT NULL,
            from_party_id INTEGER NOT NULL,
            to_user_id INTEGER NOT NULL,
            to_username TEXT NOT NULL,
            to_party_id INTEGER,
            status TEXT NOT NULL,
            FOREIGN KEY (from_user_id) REFERENCES users(id) ON DELETE CASCADE,
            FOREIGN KEY (to_user_id) REFERENCES users(id) ON DELETE CASCADE,
            FOREIGN KEY (from_party_id) REFERENCES parties(id) ON DELETE CASCADE,
            FOREIGN KEY (to_party_id) REFERENCES parties(id) ON DELETE CASCADE
        );
        """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createCampaignsTable);
            stmt.execute(createPartiesTable);
            stmt.execute(createInvitesTable);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize SQLite database", e);
        }
    }

    public void saveUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (user.getId() <= 0) {
            insertUser(user);
        } else {
            updateUser(user);
        }
    }

    private void insertUser(User user) {
        String sql = """
            INSERT INTO users (username, password, gold, score, ranking)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setInt(3, user.getGold());
            ps.setInt(4, user.getScore());
            ps.setInt(5, user.getRanking());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert user", e);
        }
    }

    private void updateUser(User user) {
        String sql = """
            UPDATE users
            SET username = ?, password = ?, gold = ?, score = ?, ranking = ?
            WHERE id = ?
            """;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setInt(3, user.getGold());
            ps.setInt(4, user.getScore());
            ps.setInt(5, user.getRanking());
            ps.setInt(6, user.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    public User loadUser(String username) {
        String sql = """
            SELECT id, username, password, gold, score, ranking
            FROM users
            WHERE username = ?
            """;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return buildUser(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load user", e);
        }
    }

    public User loadUserById(int userId) {
        String sql = """
            SELECT id, username, password, gold, score, ranking
            FROM users
            WHERE id = ?
        """;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return buildUser(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load user by id", e);
        }
    }

    public List<User> loadAllUsers() {
        String sql = """
            SELECT id, username, password, gold, score, ranking
            FROM users
            ORDER BY score DESC, username ASC
        """;

        List<User> users = new ArrayList<>();
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(buildUser(rs));
            }

            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load users", e);
        }
    }

    private User buildUser(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password")
        );
        user.setGold(rs.getInt("gold"));
        user.setScore(rs.getInt("score"));
        user.setRanking(rs.getInt("ranking"));
        return user;
    }

    public void saveCampaign(int userId, Campaign campaign) {
        String sql = """
            INSERT INTO campaigns (user_id, campaign_json)
            VALUES (?, ?)
            ON CONFLICT(user_id) DO UPDATE SET campaign_json = excluded.campaign_json
            """;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, gson.toJson(campaign));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save campaign", e);
        }
    }

    public Campaign loadCampaign(int userId) {
        String sql = "SELECT campaign_json FROM campaigns WHERE user_id = ?";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return gson.fromJson(rs.getString("campaign_json"), Campaign.class);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load campaign", e);
        }
    }

    public void deleteCampaign(int userId) {
        String sql = "DELETE FROM campaigns WHERE user_id = ?";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete campaign", e);
        }
    }

    public void saveParty(int userId, Party party) {
        if (party.getId() <= 0) {
            insertParty(userId, party);
        } else {
            updateParty(userId, party);
        }
    }

    private void insertParty(int userId, Party party) {
        String sql = "INSERT INTO parties (user_id, party_json) VALUES (?, ?)";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setString(2, gson.toJson(party));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    party.setId(rs.getInt(1));
                }
            }

            updateParty(userId, party);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert party", e);
        }
    }

    private void updateParty(int userId, Party party) {
        String sql = "UPDATE parties SET user_id = ?, party_json = ? WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, gson.toJson(party));
            ps.setInt(3, party.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update party", e);
        }
    }

    public Party loadParty(int id) {
        String sql = "SELECT party_json FROM parties WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Party party = gson.fromJson(rs.getString("party_json"), Party.class);
                party.setId(id);
                return party;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load party", e);
        }
    }

    public List<Party> loadPartiesForUser(int userId) {
        String sql = "SELECT id, party_json FROM parties WHERE user_id = ? ORDER BY id ASC";
        List<Party> parties = new ArrayList<>();

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Party party = gson.fromJson(rs.getString("party_json"), Party.class);
                    party.setId(rs.getInt("id"));
                    parties.add(party);
                }
            }
            return parties;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load parties for user", e);
        }
    }

    public void deleteParty(int id) {
        String sql = "DELETE FROM parties WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete party", e);
        }
    }

    public PvPInvite saveInvite(PvPInvite invite) {
        String sql = """
            INSERT INTO pvp_invites (from_user_id, from_username, from_party_id, to_user_id, to_username, to_party_id, status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, invite.getFromUserId());
            ps.setString(2, invite.getFromUsername());
            ps.setInt(3, invite.getFromPartyId());
            ps.setInt(4, invite.getToUserId());
            ps.setString(5, invite.getToUsername());

            if (invite.getToPartyId() == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, invite.getToPartyId());
            }

            ps.setString(7, invite.getStatus());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    invite.setId(rs.getInt(1));
                }
            }

            return invite;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save invite", e);
        }
    }

    public PvPInvite loadInvite(int inviteId) {
        String sql = "SELECT * FROM pvp_invites WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, inviteId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return buildInvite(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load invite", e);
        }
    }

    public List<PvPInvite> loadPendingInvitesForUser(int userId) {
        String sql = "SELECT * FROM pvp_invites WHERE to_user_id = ? AND status = 'PENDING' ORDER BY id DESC";
        List<PvPInvite> invites = new ArrayList<>();

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    invites.add(buildInvite(rs));
                }
            }

            return invites;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load pending invites", e);
        }
    }

    public void updateInvite(PvPInvite invite) {
        String sql = """
            UPDATE pvp_invites
            SET to_party_id = ?, status = ?
            WHERE id = ?
        """;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (invite.getToPartyId() == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, invite.getToPartyId());
            }
            ps.setString(2, invite.getStatus());
            ps.setInt(3, invite.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update invite", e);
        }
    }

    private PvPInvite buildInvite(ResultSet rs) throws SQLException {
        Integer toPartyId = rs.getObject("to_party_id") == null ? null : rs.getInt("to_party_id");
        return new PvPInvite(
                rs.getInt("id"),
                rs.getInt("from_user_id"),
                rs.getString("from_username"),
                rs.getInt("from_party_id"),
                rs.getInt("to_user_id"),
                rs.getString("to_username"),
                toPartyId,
                rs.getString("status")
        );
    }
}
