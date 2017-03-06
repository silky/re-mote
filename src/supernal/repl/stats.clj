(comment
  Celestial, Copyright 2017 Ronen Narkis, narkisr.com
  Licensed under the Apache License,
  Version 2.0  (the "License") you may not use this file except in compliance with the License.
  You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.)

(ns supernal.repl.stats
  "General stats"
  (:require
    [clojure.string :refer (split join)]
    [supernal.sshj :refer (get-log)]
    [supernal.repl.base :refer (run-hosts)]
    [pallet.stevedore :refer (script)])
  (:import [supernal.repl.base Hosts]))

(defprotocol Stats
  (cpu [this])
  (free [this]))

(defn get-logs [hosts]
  (doall (map (fn [{:keys [uuid] :as m}] (dissoc (assoc m :out (join "" (get-log uuid))) :uuid)) hosts)))

(defn collect
  "Collecting output into a hash, must be defined outside protocoal because of var args"
  [this {:keys [success] :as res} k & ks]
    (let [zipped (fn [{:keys [out] :as m}] (assoc m k (zipmap ks (split out #"\s"))))
          success' (map zipped (get-logs success))] 
      [this (assoc res :success success')]))

(defn cpu-script []
   (script 
     (set! R @("mpstat" "1" "1")) 
     (if (not (= $? 0)) ("exit" 1))
     (pipe ((println (quoted "${R}"))) ("awk" "'NR==4 { print $4 \" \" $6 \" \" $13 }'"))))

(defn free-script []
   (script 
     (set! R @("free" "-m")) 
     (if (not (= $? 0)) ("exit" 1))
     (pipe ((println (quoted "${R}"))) ("awk" "'NR==2 { print $2 \" \" $3 \" \" $4 }'"))))

(extend-type Hosts
  Stats
  (cpu [this]
    (collect this (run-hosts this (cpu-script)) :cpu :usr :sys :idle))

  (free [this]
    (collect this (run-hosts this (free-script)) :free :total :used :free)))

(defn refer-stats []
  (require '[supernal.repl.stats :as stats :refer (cpu free collect)]))
