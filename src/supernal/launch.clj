(comment 
   Celestial, Copyright 2012 Ronen Narkis, narkisr.com
   Licensed under the Apache License,
   Version 2.0  (the "License") you may not use this file except in compliance with the License.
   You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.)

(ns supernal.launch
  (:refer-clojure :exclude  [list])
  (:require  [cliopatra.command :as command :refer  [defcommand]])
  (:gen-class true))


(defn list-tasks []
  (map (juxt identity (comp ns-publics symbol))
     (filter #(.startsWith % "supernal.user") (map #(-> % ns-name str) (all-ns)))))

(defn tasks-print []
   (doseq [[n ts] (list-tasks)]
     (println (.replace n "supernal.user." "") ":") 
     (doseq [[name* fn*] ts]
       (println " " name* "-" (meta (resolve (:ns (meta fn*)) 'stop))) 
       )
     ))

(defcommand run
  "Run a single task or an entire lifecycle"
  {
   :opts-spec [] 
   :bind-args-to [script]}
  (load-string (slurp script))
  )

(defcommand list
  "List available tasks"
  {
   :opts-spec [] 
   :bind-args-to [script]}
  (load-string (slurp script))
  (println (ns-resolve supernal.user.deploy))
  (tasks-print)
  )

(defn -main [& args]
  (binding [*ns* (create-ns 'supernal.adhoc)] 
    (use '[clojure.core])
    (use '[supernal.core :only (ns- execute execute-task run copy env)])
    (use '[supernal.baseline])
    (use '[taoensso.timbre :only (warn debug)]) 
    ;; (println (list-tasks))
    (command/dispatch 'supernal.launch args)
    ))



