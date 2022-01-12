(ns com.jkbff.ps2.events.dao.character-info
	(:require [next.jdbc.sql :as sql]
			  [com.jkbff.helper :as helper]))

(defn delete
	[db-conn character-id]
	(sql/delete! db-conn
			   :character-info
			   ["character_id = ?" character-id]
			   {:entities helper/entities-fn}))

(defn save
	[db-conn char-info]
	(sql/insert! db-conn
			   :character-info
			   {:character-id (:character-id char-info)
				:name (get-in char-info [:name :first] "(Unknown)")
				:outfit-id (:outfit-id char-info)
				:member-since (:member-since char-info)
				:created-at (get-in char-info [:times :creation] 0)
				:minutes-played (get-in char-info [:times :minutes-played] 0)
				:battle-rank (get-in char-info [:battle-rank :value] 0)
				:is-prestige (:prestige-level char-info 0)
				:world-id (:world-id char-info)
				:last-login (get-in char-info [:times :last-login] 0)}
			   {:entities helper/entities-fn}))

(defn save-individual
	[db-conn char-info]
	(sql/insert! db-conn
			   :character-info
			   {:character-id (:character-id char-info)
				:name (get-in char-info [:name :first] "(Unknown)")
				:outfit-id (get-in char-info [:outfit-member :outfit-id] 0)
				:member-since (get-in char-info [:outfit-member :member-since] 0)
				:created-at (get-in char-info [:times :creation] 0)
				:minutes-played (get-in char-info [:times :minutes-played] 0)
				:battle-rank (get-in char-info [:battle-rank :value] 0)
				:is-prestige (:prestige-level char-info 0)
				:world-id (:world-id char-info 0)
				:last-login (get-in char-info [:times :last-login] 0)}
			   {:entities helper/entities-fn}))

(defn get-chars-not-loaded
	[db-conn max-num]
	(sql/query db-conn
			 ["SELECT DISTINCT e.character_id FROM vehicle_destroy_event e
			   LEFT JOIN character_info c ON e.character_id = c.character_id
			   WHERE c.character_id IS NULL
			   LIMIT ?" max-num]
			 {:identifiers helper/identifiers-fn}))