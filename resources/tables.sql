CREATE TABLE player_login_event (id INT PRIMARY KEY AUTO_INCREMENT, character_id BIGINT NOT NULL, world_id TINYINT NOT NULL, timestamp INT NOT NULL);
CREATE TABLE player_logout_event (id INT PRIMARY KEY AUTO_INCREMENT, character_id BIGINT NOT NULL, world_id TINYINT NOT NULL, timestamp INT NOT NULL);

CREATE TABLE gain_experience_event (id INT PRIMARY KEY AUTO_INCREMENT, amount SMALLINT NOT NULL, loadout_id TINYINT NOT NULL, experience_id SMALLINT NOT NULL, other_id BIGINT NOT NULL, character_id BIGINT NOT NULL, zone_id INT NOT NULL, world_id TINYINT NOT NULL, timestamp INT NOT NULL);

CREATE TABLE death_event (id INT PRIMARY KEY AUTO_INCREMENT, is_headshot TINYINT NOT NULL, attacker_loadout_id TINYINT NOT NULL, attacker_fire_mode_id INT NOT NULL, attacker_weapon_id INT NOT NULL, attacker_vehicle_id SMALLINT NOT NULL, attacker_character_id BIGINT NOT NULL, character_loadout_id TINYINT NOT NULL, character_id BIGINT NOT NULL, zone_id INT NOT NULL, world_id TINYINT NOT NULL, timestamp INT NOT NULL);
CREATE TABLE vehicle_destroy_event (id INT PRIMARY KEY AUTO_INCREMENT, faction_id TINYINT NOT NULL, attacker_loadout_id TINYINT NOT NULL, attacker_weapon_id INT NOT NULL, attacker_vehicle_id SMALLINT NOT NULL, attacker_character_id BIGINT NOT NULL, character_vehicle_id SMALLINT NOT NULL, character_id BIGINT NOT NULL, zone_id INT NOT NULL, world_id TINYINT NOT NULL, timestamp INT NOT NULL);

CREATE TABLE facility_defend_event (id INT PRIMARY KEY AUTO_INCREMENT, character_id BIGINT NOT NULL, outfit_id BIGINT NOT NULL, facility_id INT NOT NULL, zone_id INT NOT NULL, world_id TINYINT NOT NULL, timestamp INT NOT NULL);
CREATE TABLE facility_capture_event (id INT PRIMARY KEY AUTO_INCREMENT, character_id BIGINT NOT NULL, outfit_id BIGINT NOT NULL, facility_id INT NOT NULL, zone_id INT NOT NULL, world_id TINYINT NOT NULL, timestamp INT NOT NULL);

CREATE TABLE facility_control_event (id INT PRIMARY KEY AUTO_INCREMENT, duration_held INT NOT NULL, facility_id INT NOT NULL, old_faction_id INT NOT NULL, new_faction_id INT NOT NULL, outfit_id BIGINT NOT NULL, zone_id INT NOT NULL, world_id TINYINT NOT NULL, timestamp INT NOT NULL);

CREATE TABLE character_info (character_id BIGINT PRIMARY KEY, name VARCHAR(255) NOT NULL, outfit_id BIGINT NOT NULL, member_since INT NOT NULL, created_at INT NOT NULL, minutes_played INT NOT NULL, battle_rank INT NOT NULL, is_prestige TINYINT NOT NULL, world_id TINYINT NOT NULL, last_login INT NOT NULL);
CREATE TABLE outfit_info (outfit_id BIGINT PRIMARY KEY, alias VARCHAR(4) NOT NULL, name VARCHAR(255) NOT NULL, faction_id SMALLINT NOT NULLde);
CREATE TABLE weapon_info (item_id INT PRIMARY KEY, weapon_id INT, name VARCHAR(50) NOT NULL, faction_id SMALLINT NOT NULL, vehicle_id INT NOT NULL, vehicle_slot_id INT NOT NULL, is_used TINYINT NOT NULL);
--UPDATE weapon_info w JOIN (SELECT DISTINCT attacker_weapon_id FROM vehicle_destroy_event) t ON w.item_id = t.attacker_weapon_id SET is_used = 1;

CREATE TABLE loadout_info (loadout_id SMALLINT PRIMARY KEY, profile_id SMALLINT NOT NULL, faction_id SMALLINT NOT NULL, description VARCHAR(20) NOT NULL);
CREATE TABLE facility_info (facility_id INT PRIMARY KEY, zone_id INT NOT NULL, name VARCHAR(50) NOT NULL);

CREATE TABLE death_event_aggregate (num_kills INT NOT NULL, attacker_weapon_id INT NOT NULL, attacker_vehicle_id INT NOT NULL, character_loadout_id SMALLINT NOT NULL, world_id SMALLINT NOT NULL);

CREATE TABLE zone_info (zone_id TINYINT NOT NULL, name VARCHAR(50) NOT NULL);
INSERT INTO zone_info (zone_id, name) VALUES (2, 'Indar');
INSERT INTO zone_info (zone_id, name) VALUES (4, 'Hossin');
INSERT INTO zone_info (zone_id, name) VALUES (6, 'Amerish');
INSERT INTO zone_info (zone_id, name) VALUES (8, 'Esamir');
INSERT INTO zone_info (zone_id, name) VALUES (96, 'VR training zone (NC)');
INSERT INTO zone_info (zone_id, name) VALUES (97, 'VR training zone (TR)');
INSERT INTO zone_info (zone_id, name) VALUES (98, 'VR training zone (VS)');

CREATE TABLE world_info (world_id INT PRIMARY KEY, name VARCHAR(20) NOT NULL);
INSERT INTO world_info (world_id, name) VALUES (1, 'Connery');
INSERT INTO world_info (world_id, name) VALUES (13, 'Cobalt');
INSERT INTO world_info (world_id, name) VALUES (10, 'Miller');
INSERT INTO world_info (world_id, name) VALUES (40, 'SolTech');
INSERT INTO world_info (world_id, name) VALUES (17, 'Emerald');
INSERT INTO world_info (world_id, name) VALUES (19, 'Jaeger');
INSERT INTO world_info (world_id, name) VALUES (24, 'Apex');
INSERT INTO world_info (world_id, name) VALUES (25, 'Briggs');

CREATE TABLE matches (name VARCHAR(255) NOT NULL, zone_id INT NOT NULL);

--DROP TABLE player_login_event; DROP TABLE player_logout_event; DROP TABLE gain_experience_event; DROP TABLE death_event; DROP TABLE vehicle_destroy_event; DROP TABLE facility_capture_event; DROP TABLE facility_defend_event; DROP TABLE character_info;

CREATE INDEX idx1 ON gain_experience_event(character_id);
CREATE INDEX idx2 ON death_event(character_id);
CREATE INDEX idx3 ON death_event(attacker_character_id);
CREATE INDEX idx4 ON vehicle_destroy_event(character_id);
CREATE INDEX idx5 ON vehicle_destroy_event(attacker_character_id);
CREATE INDEX idx6 ON player_login_event(character_id);
CREATE INDEX idx7 ON player_logout_event(character_id);
CREATE INDEX idx8 ON character_info(character_id);
CREATE INDEX idx9 ON character_info(outfit_id);
CREATE INDEX idx10 ON vehicle_destroy_event(world_id, character_vehicle_id);
CREATE INDEX idx11 ON death_event(world_id, timestamp);
CREATE INDEX idx12 ON death_event_aggregate(world_id, character_loadout_id);
CREATE INDEX idx13 ON death_event(attacker_weapon_id);

CREATE TABLE vehicle_info (vehicle_id INT PRIMARY KEY, name VARCHAR(50) NOT NULL);
INSERT INTO vehicle_info (vehicle_id, name) VALUES (1, 'Flash');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (2, 'Sunderer');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (3, 'Lightning');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (4, 'Magrider');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (5, 'Vanguard');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (6, 'Prowler');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (7, 'Scythe');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (8, 'Reaver');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (9, 'Mosquito');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (10, 'Liberator');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (11, 'Galaxy');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (12, 'Harasser');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (14, 'Valkyrie');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (15, 'Ant');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (2007, 'Colossus');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (2010, 'Flash XS-1');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (2019, 'Bastion Fleet Carrier');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (2033, 'Javelin');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (2122, 'Mosquito Interceptor');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (2123, 'Reaver Interceptor');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (2124, 'Scythe Interceptor');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (2125, 'Javelin');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (2129, 'Javelin');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (2130, 'Reclaimed Sunderer');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (2131, 'Reclaimed Galaxy');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (2132, 'Reclaimed Valkyrie');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (2133, 'Reclaimed Magrider');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (2134, 'Reclaimed Vanguard');
INSERT INTO vehicle_info (vehicle_id, name) VALUES (2135, 'Reclaimed Prowler');

CREATE TABLE faction_info (faction_id INT PRIMARY KEY, name VARCHAR(50) NOT NULL, alias VARCHAR(4) NOT NULL);
INSERT INTO faction_info (faction_id, name, alias) VALUES (0, "None", "None");
INSERT INTO faction_info (faction_id, name, alias) VALUES (1, "Vanu Sovereignty", "VS");
INSERT INTO faction_info (faction_id, name, alias) VALUES (2, "New Conglomerate", "NC");
INSERT INTO faction_info (faction_id, name, alias) VALUES (3, "Terran Republic", "TR");
INSERT INTO faction_info (faction_id, name, alias) VALUES (4, "NS Operatives", "NSO");

