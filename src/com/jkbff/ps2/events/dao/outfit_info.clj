(ns com.jkbff.ps2.events.dao.outfit-info
	(:require [next.jdbc.sql :as sql]
			  [com.jkbff.helper :as helper]))

(defn delete
	[db-conn outfit-id]
	(sql/delete! db-conn
			   :outfit-info
			   ["outfit_id = ?" outfit-id]
			   {:entities helper/entities-fn}))

(defn save
	[db-conn outfit-info]
	(sql/insert! db-conn
			   :outfit-info
			   {:outfit-id (:outfit-id outfit-info)
				:alias (:alias outfit-info)
				:name (:name outfit-info)}
			   {:entities helper/entities-fn}))

(defn get-outfits-not-loaded
	[db-conn max-num]
	(sql/query db-conn
			 ["SELECT DISTINCT c.outfit_id FROM character_info c
			   LEFT JOIN outfit_info o ON c.outfit_id = o.outfit_id
			   WHERE c.outfit_id != 0 AND o.outfit_id IS NULL
			   LIMIT ?" max-num]
			 {:identifiers helper/identifiers-fn}))