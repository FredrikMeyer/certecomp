(ns certecomp.core
  (:require
   [certecomp.system :as system]))

(defn -main [& args]
  (system/start-system!))

(comment
  (do
    (system/halt-system!)
    (system/start-system!))
  ;;
  )
