package net.mathias2246.buildmc.team;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.api.event.team.TeamMemberAddEvent;
import net.mathias2246.buildmc.api.event.team.TeamMemberRemovedEvent;
import net.mathias2246.buildmc.api.event.team.TeamRegisteredEvent;
import net.mathias2246.buildmc.api.event.team.TeamUnregisterEvent;
import net.mathias2246.buildmc.api.team.Team;
import net.mathias2246.buildmc.database.DatabaseTable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Stream;

public class TeamManager implements net.mathias2246.buildmc.api.team.TeamManager, DatabaseTable {

    @Override
    public void createTable(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS teams (
                    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name          VARCHAR(255) NOT NULL,
                    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    color         INT,
                    display_name  VARCHAR(MAX),
                    metadata      VARCHAR(MAX),
                    CONSTRAINT uq_teams_name UNIQUE (name)
                );
                CREATE TABLE IF NOT EXISTS team_members (
                    member_uuid UUID   NOT NULL,
                    team_id     BIGINT NOT NULL,
                    PRIMARY KEY (member_uuid, team_id),
                    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE
                );
                CREATE INDEX IF NOT EXISTS idx_team_members_team_id ON team_members(team_id);
           """);
        }
    }

    /** Looks up a team's numerical id by its unique name.
     *
     * @return the team's id, or {@code null} if no team with that name exists.
     **/
    public @Nullable Long getTeamId(@NotNull Connection conn, @NotNull String name) {
        String sql = "SELECT id FROM teams WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("id") : null;
            }
        } catch (SQLException ignor) {
            return null;
        }
    }

    public long insertTeam(@NotNull Connection conn, @NotNull String name, @Nullable Integer color,
                           @Nullable Component displayName, @NotNull Map<String, Object> metadata) throws SQLException {
        String sql = "INSERT INTO teams (name, color, display_name, metadata) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            setNullableInt(ps, 2, color);
            ps.setString(3, displayName == null ? null : GsonComponentSerializer.gson().serialize(displayName));
            ps.setString(4, CoreMain.gson.toJson(metadata));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
                throw new SQLException("Insert did not return a generated id for team '" + name + "'");
            }
        }
    }

    /** Updates all mutable fields of an existing team by id. */
    public void updateTeam(@NotNull Connection conn, long id, @NotNull String name, @Nullable Integer color,
                           @Nullable Component displayName, @NotNull Map<String, Object> metadata) throws SQLException {
        String sql = "UPDATE teams SET name = ?, color = ?, display_name = ?, metadata = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            setNullableInt(ps, 2, color);
            ps.setString(3, displayName == null ? null : GsonComponentSerializer.gson().serialize(displayName));
            ps.setString(4, CoreMain.gson.toJson(metadata));
            ps.setLong(5, id);
            ps.executeUpdate();
        }
    }

    public long updateOrInsertTeam(@NotNull Connection conn, @NotNull String name, @Nullable Integer color,
                           @Nullable Component displayName, @NotNull Map<String, Object> metadata) throws SQLException {
        Long id = getTeamId(conn, name);
        if (id == null) return insertTeam(conn, name, color, displayName, metadata);
        else updateTeam(conn, id, name, color, displayName, metadata);
        return id;
    }

    public long updateOrInsertTeam(@NotNull Connection conn, @NotNull Team team) throws SQLException {
        return updateOrInsertTeam(
                conn,
                team.getName(),
                team.getColor() == null ? null : team.getColor().value(),
                team.getDisplayName(),
                team.getMetadata()
                );
    }

    /** Inserts or updates a team member inside the database.
     * When teamId is {@code null}, the member will be deleted from the database.
     * **/
    public boolean updateOrInsertMember(@NotNull Connection conn, @Nullable Long teamId, @NotNull UUID uuid) {
        try (PreparedStatement del = conn.prepareStatement(
                "DELETE FROM team_members WHERE MEMBER_UUID = ?")) {
            del.setObject(1, uuid);
            del.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (teamId == null) return false;

        try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO team_members (member_uuid, team_id) VALUES (?, ?)")) {
                ins.setObject(1, uuid);
                ins.setLong(2, teamId);
                ins.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public void deleteTeam(@NotNull Connection conn, long id) throws SQLException {
        // team_members rows are removed automatically via ON DELETE CASCADE
        String sql = "DELETE FROM teams WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public void insertMember(@NotNull Connection conn, long teamId, @NotNull UUID memberUuid) throws SQLException {
        String sql = "INSERT INTO team_members (member_uuid, team_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, memberUuid);
            ps.setLong(2, teamId);
            ps.executeUpdate();
        }
    }

    public void removeMember(@NotNull Connection conn, long teamId, @NotNull UUID memberUuid) throws SQLException {
        String sql = "DELETE FROM team_members WHERE team_id = ? AND member_uuid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, teamId);
            ps.setObject(2, memberUuid);
            ps.executeUpdate();
        }
    }

    public void removeMembers(@NotNull Connection conn, long teamId, @NotNull Stream<UUID> members) throws SQLException {
        String sql = "DELETE FROM team_members WHERE team_id = ? AND member_uuid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            members.forEach(
                    memberUuid -> {
                        try {
                            ps.setLong(1, teamId);
                            ps.setObject(2, memberUuid);
                            ps.addBatch();
                        } catch (SQLException e) {
                            CoreMain.plugin.getLogger().log(Level.SEVERE, "Failed to remove team member from database: " + memberUuid, e);
                        }
                    }
            );
            ps.executeBatch();
        }
    }

    /** Bulk-inserts members for a team, e.g. when registering an already-populated in-memory team. */
    public void insertMembers(@NotNull Connection conn, long teamId, @NotNull Iterator<UUID> memberUuids) throws SQLException {
        if (!memberUuids.hasNext()) return;
        String sql = "INSERT INTO team_members (member_uuid, team_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (UUID uuid = memberUuids.next(); uuid != null; uuid = memberUuids.next()) {
                ps.setObject(1, uuid);
                ps.setLong(2, teamId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void saveMembers(@NotNull Connection conn, long teamId, @NotNull Collection<UUID> members) throws SQLException {
        try {
            try (PreparedStatement del = conn.prepareStatement(
                    "DELETE FROM team_members WHERE MEMBER_UUID = ?")) {
                for (UUID uuid : members) {
                    del.setObject(1, uuid);
                    del.addBatch();
                }
                del.executeBatch();
            }

            if (!members.isEmpty()) {
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO team_members (member_uuid, team_id) VALUES (?, ?)")) {
                    for (UUID uuid : members) {
                        ins.setObject(1, uuid);
                        ins.setLong(2, teamId);
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
            }

        } catch (SQLException ignore) {
        }
    }

    /** Loads every team with its full member set. Intended for the one bulk startup load. */
    public void loadAllTeams(@NotNull Connection conn) throws SQLException {
        Map<Long, TeamRecord> byId = new LinkedHashMap<>();

        String teamsSql = "SELECT id, name, created_at, color, display_name, metadata FROM teams";
        try (
                PreparedStatement ps = conn.prepareStatement(teamsSql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {
                TeamRecord record = readTeamRecord(rs);
                byId.put(record.id(), record);
            }
        }

        // one query for all membership rows, grouped in memory, instead of one query per team
        String membersSql = "SELECT team_id, member_uuid FROM team_members";
        try (
            PreparedStatement ps2 = conn.prepareStatement(membersSql);
            ResultSet rs2 = ps2.executeQuery();
        ) {

            while (rs2.next()) {
                long teamId = rs2.getLong("team_id");
                UUID uuid = (UUID) rs2.getObject("member_uuid");
                TeamRecord record = byId.get(teamId);
                if (record != null) record.members().add(uuid);
            }
        }

        var teamsList = new ArrayList<>(byId.values());

        for (TeamRecord record : teamsList) {
            Team team = new Team(record.name);

            if (record.color != null) team.setColor(TextColor.color(record.color));
            if (record.displayName != null) team.setDisplayName(record.displayName);

            for (UUID uuid : record.members()) {
                team.addMember(uuid, false, false);
            }

            for (var metadataEntry : record.metadata().entrySet()) {
                team.putMetadata(metadataEntry.getKey(), metadataEntry.getValue());
            }

            addTeamToManager(record.name(), team);
        }
    }

    public @Nullable TeamRecord loadTeam(@NotNull Connection conn, long id) throws SQLException {
        String sql = "SELECT id, name, created_at, color, display_name, metadata FROM teams WHERE id = ?";
        TeamRecord record;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                record = readTeamRecord(rs);
            }
        }


        String membersSql = "SELECT member_uuid FROM team_members WHERE team_id = ?";
        try (PreparedStatement ps2 = conn.prepareStatement(membersSql)) {
            ps2.setLong(1, id);
            try (ResultSet rs = ps2.executeQuery()) {
                while (rs.next()) record.members().add((UUID) rs.getObject("member_uuid"));
            }
        }

        return record;
    }

    private TeamRecord readTeamRecord(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        Timestamp createdAt = rs.getTimestamp("created_at");
        int rawColor = rs.getInt("color");
        Integer color = rs.wasNull() ? null : rawColor;

        String displayNameJson = rs.getString("display_name");
        Component displayName = displayNameJson == null ? null
                : CoreMain.gsonComponentSerializer.deserialize(displayNameJson);

        String metadataJson = rs.getString("metadata");
        Map<String, Object> metadata = metadataJson == null
                ? new HashMap<>()
                : CoreMain.gson.fromJson(metadataJson, new TypeToken<Map<String, Object>>() {}.getType());

        return new TeamRecord(id, name, createdAt, color, displayName, metadata, new HashSet<>());
    }

    private void setNullableInt(PreparedStatement ps, int index, @Nullable Integer value) throws SQLException {
        if (value == null) ps.setNull(index, Types.INTEGER);
        else ps.setInt(index, value);
    }

    /** Plain data carrier for a row read from the DB — map this to your Team class where you construct it. */
    public record TeamRecord(long id, String name, Timestamp createdAt, @Nullable Integer color,
                             @Nullable Component displayName, Map<String, Object> metadata, Set<UUID> members) {}

    public TeamManager() {

    }

    private final Map<String, Team> teams = new ConcurrentHashMap<>();
    private final Map<UUID, Team> memberTeams = new ConcurrentHashMap<>();
    private final Map<String, Boolean> persistentTeams = new ConcurrentHashMap<>();

    private void addTeamToManager(@NotNull String teamIdentifier, @NotNull Team team) {
        var result = teams.putIfAbsent(teamIdentifier, team);
        if (result == null) {
            team.register(this, true);
            persistentTeams.put(teamIdentifier, true);

            for (Iterator<UUID> it = team.members().iterator(); it.hasNext(); ) {
                UUID member = it.next();
                memberTeams.put(member, team);
            }
        }
    }

    @Override
    public @Nullable Team getTeam(@NotNull String teamIdentifier) {
        return teams.get(teamIdentifier);
    }

    @Override
    public boolean registerTeam(@NotNull Team team, boolean persist) {
        var result = teams.putIfAbsent(team.getName(), team);
        if (result == null) {
            persistentTeams.put(team.getName(), persist);

            if (team.getTeamManager() == null) team.register(this, persist);

            for (Iterator<UUID> it = team.members().iterator(); it.hasNext(); ) {
                UUID member = it.next();

                if (memberTeams.containsKey(member)) {
                    Bukkit.getPluginManager().callEvent(new TeamMemberRemovedEvent(member, team));
                }
                memberTeams.put(member, team);
                updateOrInsertMember(CoreMain.databaseManager.getConnection(), getTeamId(CoreMain.databaseManager.getConnection(), team.getName()), member);

                Bukkit.getPluginManager().callEvent(new TeamMemberAddEvent(member, team, false));
            }
            
            Bukkit.getPluginManager().callEvent(new TeamRegisteredEvent(team));
            try {
                var nId = insertTeam(
                        CoreMain.databaseManager.getConnection(),
                        team.getName(),
                        team.getColor() == null ? null : team.getColor().value(),
                        team.getDisplayName(),
                        team.getMetadata()
                        );
                insertMembers(CoreMain.databaseManager.getConnection(), nId, team.members().iterator());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return result == null;
    }

    @Override
    public boolean isRegisteredTeam(@NotNull String teamIdentifier) {
        return teams.containsKey(teamIdentifier);
    }

    @Override
    public @Nullable Boolean isPersistent(@NotNull String teamIdentifier) {
        return persistentTeams.get(teamIdentifier);
    }

    @Override
    public void unregisterTeam(@NotNull String teamIdentifier) {
        var team = getTeam(teamIdentifier);
        if (team == null) return;
        unregisterTeam(team);
    }

    @Override
    public void unregisterTeam(@NotNull Team team) {

        if (teams.containsKey(team.getName())) {
            var events = team.getTeamManager() != null && team.getTeamManager().isRegisteredTeam(team.getName());
            for (Iterator<UUID> it = team.members().iterator(); it.hasNext(); ) {
                var member = it.next();
                memberTeams.remove(member);
                if (events) Bukkit.getPluginManager().callEvent(new TeamMemberRemovedEvent(member, team));
            }

            if (events) {
                Bukkit.getPluginManager().callEvent(new TeamUnregisterEvent(team));
                team.unregister();
            }

            teams.remove(team.getName());
            persistentTeams.remove(team.getName());

            try {
                var conn = CoreMain.databaseManager.getConnection();

                var nId = getTeamId(conn, team.getName());
                if (nId == null) return;

                deleteTeam(conn, nId);

                removeMembers(conn, nId, team.members());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public @Nullable Team getTeamFromMember(@NotNull Entity entity) {
        return getTeamFromMember(entity.getUniqueId());
    }

    @Override
    public @Nullable Team getTeamFromMember(@NotNull UUID uuid) {
        return memberTeams.get(uuid);
    }

    @Override
    public boolean isMember(@NotNull UUID uuid) {
        return memberTeams.containsKey(uuid);
    }

    @Override
    public boolean isMember(@NotNull Entity entity) {
        return isMember(entity.getUniqueId());
    }

    @Override
    public @NotNull ImmutableMap<String, Team> getTeams() {
        return ImmutableMap.copyOf(teams);
    }

    @Override
    public @NotNull Stream<Map.Entry<String, Team>> teams() {
        return teams.entrySet().stream();
    }

    @Override
    public boolean joinTeam(@NotNull UUID uuid, @NotNull Team team) {
        var previous = memberTeams.get(uuid);
        if (previous != null) {
            if (previous.equals(team)) return false;

            leaveTeam(uuid, team);
        }

        if (team.isMember(uuid) && memberTeams.containsKey(uuid)) {
            return true;
        }
        if (!team.isMember(uuid)) if (!team.addMember(uuid)) return false;
        memberTeams.put(uuid, team);


        var in = memberTeams.get(uuid);
        var result = in != null && in.equals(team) && team.isMember(uuid);

        if (result) {
            var nId = getTeamId(CoreMain.databaseManager.getConnection(), team.getName());
            if (!updateOrInsertMember(
                    CoreMain.databaseManager.getConnection(),
                    nId,
                    uuid
            )) {
                CoreMain.plugin.getLogger().log(
                        Level.WARNING,
                        "The team member " + uuid + " could not be added to the database!\nThis member will not persist."
                );
            }
        }

        return result;
    }

    @Override
    public boolean joinTeam(@NotNull Entity entity, @NotNull Team team) {
        return joinTeam(entity.getUniqueId(), team);
    }

    @Override
    public boolean leaveTeam(@NotNull UUID uuid, @NotNull Team team) {
        var v = memberTeams.remove(uuid);
        if (v != null) {
            var result = team.removeMember(uuid);

            try {
                var nId = getTeamId(CoreMain.databaseManager.getConnection(), team.getName());
                if (nId != null) {
                    removeMember(
                            CoreMain.databaseManager.getConnection(),
                            nId,
                            uuid
                    );
                }
            } catch (SQLException e) {
                CoreMain.plugin.getLogger().log(Level.WARNING, "Could not remove team member " + uuid + " from database", e);
            }

            return result;
        }

        return false;
    }

    @Override
    public boolean leaveTeam(@NotNull Entity entity, @NotNull Team team) {
        return leaveTeam(entity.getUniqueId(), team);
    }

    public ItemStack buildDebugBook() {
        ItemStack bookStack = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta bookMeta = bookStack.getItemMeta();
        bookMeta.itemName(Component.text("TeamManager DEBUG"));
        if (bookMeta instanceof BookMeta book) {
            Component listTeams = Component.text("Teams & Persistence");
            for (var teamEntry : teams.entrySet()) {
                listTeams = listTeams.appendNewline()
                        .append(Component.text(teamEntry.getKey())).append(Component.text(" - P:")
                        .append(Component.text(persistentTeams.get(teamEntry.getKey()) ? "t" : "f"))
                        );
            }
            Component members = Component.text("Members");
            for (var member : memberTeams.entrySet()) {
                OfflinePlayer memberPlayer = Bukkit.getOfflinePlayer(member.getKey());
                var name = memberPlayer.getName();
                members = members.append(Component.text(Objects.requireNonNullElseGet(name, () -> member.getKey().toString().substring(0, 6))));
                members = members.append(Component.text(" -> ").append(member.getValue().getDisplayName() == null ? Component.text("?") : member.getValue().getDisplayName()));
            }

            book.addPages(listTeams, members);

            bookStack.setItemMeta(bookMeta);
        }
        return bookStack;
    }
}
