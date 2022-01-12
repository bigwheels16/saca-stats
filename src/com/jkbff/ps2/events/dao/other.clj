(ns com.jkbff.ps2.events.dao.other
	(:require [next.jdbc.sql :as sql]
			  [com.jkbff.helper :as helper]))

(defn insert-weapon
	[db-conn weapon-info]
	(sql/insert! db-conn
			   :weapon-info
			   {:item-id (:item-id weapon-info)
				:weapon-id (get-in weapon-info [:item-to-weapon :weapon-id])
				:name (get-in weapon-info [:name :en] "")
				:faction-id (get weapon-info :faction-id 0)
				:vehicle-id (get-in weapon-info [:vehicle-attachment :vehicle-id] 0)
				:vehicle-slot-id (get-in weapon-info [:vehicle-attachment :slot-id] 0)
				:is-used 0}
			   {:entities helper/entities-fn}))

(defn insert-loadout
	[db-conn loadout-info]
	(sql/insert! db-conn
			   :loadout-info
			   {:loadout-id (:loadout-id loadout-info)
				:profile-id (:profile-id loadout-info)
				:faction-id (:faction-id loadout-info)
				:description (:code-name loadout-info)}
			   {:entities helper/entities-fn}))

(defn insert-facility
	[db-conn facility-info]
	(sql/insert! db-conn
			   :facility-info
			   {:facility-id (:facility-id facility-info)
				:zone-id (:zone-id facility-info)
				:name (:facility-name facility-info)}
			   {:entities helper/entities-fn}))