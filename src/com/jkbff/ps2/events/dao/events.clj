(ns com.jkbff.ps2.events.dao.events
    (:require [next.jdbc :as jdbc]
              [next.jdbc.sql :as sql]
              [com.jkbff.helper :as helper]))

(defn create-event-tables
    [ds]
    (with-open [db-conn (jdbc/get-connection ds)]
        (jdbc/execute! db-conn ["
            CREATE TABLE IF NOT EXISTS player_login_event (id INT PRIMARY KEY AUTO_INCREMENT, character_id BIGINT NOT NULL, world_id TINYINT NOT NULL, timestamp INT NOT NULL);
            CREATE TABLE IF NOT EXISTS player_logout_event (id INT PRIMARY KEY AUTO_INCREMENT, character_id BIGINT NOT NULL, world_id TINYINT NOT NULL, timestamp INT NOT NULL);
            CREATE TABLE IF NOT EXISTS gain_experience_event (id INT PRIMARY KEY AUTO_INCREMENT, amount SMALLINT NOT NULL, loadout_id TINYINT NOT NULL, experience_id SMALLINT NOT NULL, other_id BIGINT NOT NULL, character_id BIGINT NOT NULL, zone_id INT NOT NULL, world_id TINYINT NOT NULL, timestamp INT NOT NULL);
            CREATE TABLE IF NOT EXISTS death_event (id INT PRIMARY KEY AUTO_INCREMENT, is_headshot TINYINT NOT NULL, attacker_loadout_id TINYINT NOT NULL, attacker_fire_mode_id INT NOT NULL, attacker_weapon_id INT NOT NULL, attacker_vehicle_id SMALLINT NOT NULL, attacker_character_id BIGINT NOT NULL, character_loadout_id TINYINT NOT NULL, character_id BIGINT NOT NULL, zone_id INT NOT NULL, world_id TINYINT NOT NULL, timestamp INT NOT NULL);
            CREATE TABLE IF NOT EXISTS kill_event (id INT PRIMARY KEY AUTO_INCREMENT, is_headshot TINYINT NOT NULL, attacker_loadout_id TINYINT NOT NULL, attacker_fire_mode_id INT NOT NULL, attacker_weapon_id INT NOT NULL, attacker_vehicle_id SMALLINT NOT NULL, attacker_character_id BIGINT NOT NULL, character_loadout_id TINYINT NOT NULL, character_id BIGINT NOT NULL, zone_id INT NOT NULL, world_id TINYINT NOT NULL, timestamp INT NOT NULL);
            CREATE TABLE IF NOT EXISTS vehicle_death_event (id INT PRIMARY KEY AUTO_INCREMENT, faction_id TINYINT NOT NULL, attacker_loadout_id TINYINT NOT NULL, attacker_weapon_id INT NOT NULL, attacker_vehicle_id SMALLINT NOT NULL, attacker_character_id BIGINT NOT NULL, character_vehicle_id SMALLINT NOT NULL, character_id BIGINT NOT NULL, zone_id INT NOT NULL, world_id TINYINT NOT NULL, timestamp INT NOT NULL);
            CREATE TABLE IF NOT EXISTS vehicle_destroy_event (id INT PRIMARY KEY AUTO_INCREMENT, faction_id TINYINT NOT NULL, attacker_loadout_id TINYINT NOT NULL, attacker_weapon_id INT NOT NULL, attacker_vehicle_id SMALLINT NOT NULL, attacker_character_id BIGINT NOT NULL, character_vehicle_id SMALLINT NOT NULL, character_id BIGINT NOT NULL, zone_id INT NOT NULL, world_id TINYINT NOT NULL, timestamp INT NOT NULL);
            CREATE TABLE IF NOT EXISTS facility_defend_event (id INT PRIMARY KEY AUTO_INCREMENT, character_id BIGINT NOT NULL, outfit_id BIGINT NOT NULL, facility_id INT NOT NULL, zone_id INT NOT NULL, world_id TINYINT NOT NULL, timestamp INT NOT NULL);
            CREATE TABLE IF NOT EXISTS facility_capture_event (id INT PRIMARY KEY AUTO_INCREMENT, character_id BIGINT NOT NULL, outfit_id BIGINT NOT NULL, facility_id INT NOT NULL, zone_id INT NOT NULL, world_id TINYINT NOT NULL, timestamp INT NOT NULL);
            CREATE TABLE IF NOT EXISTS facility_control_event (id INT PRIMARY KEY AUTO_INCREMENT, duration_held INT NOT NULL, facility_id INT NOT NULL, old_faction_id INT NOT NULL, new_faction_id INT NOT NULL, outfit_id BIGINT NOT NULL, zone_id INT NOT NULL, world_id TINYINT NOT NULL, timestamp INT NOT NULL);
            CREATE TABLE IF NOT EXISTS loadout (loadout_id TINYINT NOT NULL, profile_id TINYINT NOT NULL, faction_id TINYINT NOT NULL, code_name VARCHAR(20) NOT NULL);
            "])))

(defn populate-loadout-table
  [loadouts ds]
  (with-open [db-conn (jdbc/get-connection ds)]
    (doseq [row loadouts]
      (sql/insert! db-conn
                   "loadout"
                   {"loadout_id" (:loadout-id row)
                    "profile_id"     (:profile-id row)
                    "faction_id"    (:faction-id row)
                    "code_name"  (:code-name row)}))))

(defn delete-player-events
    [ds character-id timestamp]
    (with-open [db-conn (jdbc/get-connection ds)]
        (sql/delete! db-conn "player_login_event" ["character_id = ? AND timestamp <= ?" character-id timestamp])
        (sql/delete! db-conn "player_logout_event" ["character_id = ? AND timestamp <= ?" character-id timestamp])
        (sql/delete! db-conn "gain_experience_event" ["character_id = ? AND timestamp <= ?" character-id timestamp])
        (sql/delete! db-conn "death_event" ["character_id = ? AND timestamp <= ?" character-id timestamp])
        (sql/delete! db-conn "kill_event" ["attacker_character_id = ? AND timestamp <= ?" character-id timestamp])
        (sql/delete! db-conn "vehicle_death_event" ["character_id = ? AND timestamp <= ?" character-id timestamp])
        (sql/delete! db-conn "vehicle_destroy_event" ["attacker_character_id = ? AND timestamp <= ?" character-id timestamp])
        (sql/delete! db-conn "facility_defend_event" ["character_id = ? AND timestamp <= ?" character-id timestamp])
        (sql/delete! db-conn "facility_capture_event" ["character_id = ? AND timestamp <= ?" character-id timestamp])))

(defn save-player-login-event
    [ds event]
    (with-open [db-conn (jdbc/get-connection ds)]
        (sql/insert! db-conn
                     "player_login_event"
                     {"character_id" (:character-id event)
                      "world_id"     (:world-id event)
                      "timestamp"    (:timestamp event)})))

(defn save-player-logout-event
    [ds event]
    (with-open [db-conn (jdbc/get-connection ds)]
        (sql/insert! db-conn
                     "player_logout_event"
                     {"character_id" (:character-id event)
                      "world_id"     (:world-id event)
                      "timestamp"    (:timestamp event)})))

(defn save-experience-event
    [ds event]
    (with-open [db-conn (jdbc/get-connection ds)]
        (sql/insert! db-conn
                     "gain_experience_event"
                     {"amount"        (:amount event)
                      "loadout_id"    (:loadout-id event)
                      "experience_id" (:experience-id event)
                      "other_id"      (:other-id event)
                      "character_id"  (:character-id event)
                      "zone_id"       (:zone-id event)
                      "world_id"      (:world-id event)
                      "timestamp"     (:timestamp event)})))

(defn save-death-event
    [ds event]
    (with-open [db-conn (jdbc/get-connection ds)]
        (sql/insert! db-conn
                     "death_event"
                     {"is_headshot"           (:is-headshot event)
                      "attacker_loadout_id"   (:attacker-loadout-id event)
                      "attacker_fire_mode_id" (:attacker-fire-mode-id event)
                      "attacker_weapon_id"    (:attacker-weapon-id event)
                      "attacker_vehicle_id"   (:attacker-vehicle-id event)
                      "attacker_character_id" (:attacker-character-id event)
                      "character_loadout_id"  (:character-loadout-id event)
                      "character_id"          (:character-id event)
                      "zone_id"               (:zone-id event)
                      "world_id"              (:world-id event)
                      "timestamp"             (:timestamp event)})

        (if (not= (:attacker-character-id event) (:character-id event))
            (sql/insert! db-conn
                         "kill_event"
                         {"is_headshot"           (:is-headshot event)
                          "attacker_loadout_id"   (:attacker-loadout-id event)
                          "attacker_fire_mode_id" (:attacker-fire-mode-id event)
                          "attacker_weapon_id"    (:attacker-weapon-id event)
                          "attacker_vehicle_id"   (:attacker-vehicle-id event)
                          "attacker_character_id" (:attacker-character-id event)
                          "character_loadout_id"  (:character-loadout-id event)
                          "character_id"          (:character-id event)
                          "zone_id"               (:zone-id event)
                          "world_id"              (:world-id event)
                          "timestamp"             (:timestamp event)}))))

(defn save-vehicle-destroy-event
    [ds event]
    (with-open [db-conn (jdbc/get-connection ds)]
        (sql/insert! db-conn
                     "vehicle_death_event"
                     {"faction_id"            (:faction-id event)
                      "attacker_loadout_id"   (:attacker-loadout-id event)
                      "attacker_weapon_id"    (:attacker-weapon-id event)
                      "attacker_vehicle_id"   (:attacker-vehicle-id event)
                      "attacker_character_id" (:attacker-character-id event)
                      "character_vehicle_id"  (:vehicle-id event) ; renamed in db
                      "character_id"          (:character-id event)
                      "zone_id"               (:zone-id event)
                      "world_id"              (:world-id event)
                      "timestamp"             (:timestamp event)})

        (if (not= (:attacker-character-id event) (:character-id event))
            (sql/insert! db-conn
                         "vehicle_destroy_event"
                         {"faction_id"            (:faction-id event)
                          "attacker_loadout_id"   (:attacker-loadout-id event)
                          "attacker_weapon_id"    (:attacker-weapon-id event)
                          "attacker_vehicle_id"   (:attacker-vehicle-id event)
                          "attacker_character_id" (:attacker-character-id event)
                          "character_vehicle_id"  (:vehicle-id event) ; renamed in db
                          "character_id"          (:character-id event)
                          "zone_id"               (:zone-id event)
                          "world_id"              (:world-id event)
                          "timestamp"             (:timestamp event)}))))

(defn save-facility-defend-event
    [ds event]
    (with-open [db-conn (jdbc/get-connection ds)]
        (sql/insert! db-conn
                     "facility_defend_event"
                     {"character_id" (:character-id event)
                      "outfit_id"    (:outfit-id event)
                      "facility_id"  (:facility-id event)
                      "zone_id"      (:zone-id event)
                      "world_id"     (:world-id event)
                      "timestamp"    (:timestamp event)})))

(defn save-facility-capture-event
    [ds event]
    (with-open [db-conn (jdbc/get-connection ds)]
        (sql/insert! db-conn
                     "facility_capture_event"
                     {"character_id" (:character-id event)
                      "outfit_id"    (:outfit-id event)
                      "facility_id"  (:facility-id event)
                      "zone_id"      (:zone-id event)
                      "world_id"     (:world-id event)
                      "timestamp"    (:timestamp event)})))

(defn save-facility-control-event
    [ds event]
    (with-open [db-conn (jdbc/get-connection ds)]
        (sql/insert! db-conn
                     "facility_control_event"
                     {"duration_held"  (:duration-held event)
                      "facility_id"    (:facility-id event)
                      "old_faction_id" (:old-faction-id event)
                      "new_faction_id" (:new-faction-id event)
                      "outfit_id"      (:outfit-id event)
                      "zone_id"        (:zone-id event)
                      "world_id"       (:world-id event)
                      "timestamp"      (:timestamp event)})))

(defn get-char-activity
    [ds character-id]
    (with-open [db-conn (jdbc/get-connection ds)]
        {:character-id character-id
         :logon (sql/query db-conn ["SELECT timestamp, 0 AS inferred FROM player_login_event WHERE character_id = ? UNION ALL
                                     SELECT timestamp, 1 AS inferred FROM gain_experience_event WHERE character_id = ?
                                     ORDER BY timestamp ASC" character-id character-id])
         :xp (sql/query db-conn ["SELECT * FROM gain_experience_event WHERE character_id = ?" character-id])
         :kills (sql/query db-conn ["SELECT * FROM kill_event WHERE attacker_character_id = ?" character-id])
         :deaths (sql/query db-conn ["SELECT * FROM death_event WHERE character_id = ?" character-id])
         :vehicle-kills (sql/query db-conn ["SELECT * FROM vehicle_destroy_event WHERE attacker_character_id = ?" character-id])
         :vehicle-deaths (sql/query db-conn ["SELECT * FROM vehicle_death_event WHERE character_id = ?" character-id])
         :facility-captures (sql/query db-conn ["SELECT * FROM facility_capture_event WHERE character_id = ?" character-id])
         :facility-defends (sql/query db-conn ["SELECT * FROM facility_defend_event WHERE character_id = ?" character-id])}))
