(ns certecomp.utils)

(defn get-current-time []
  (let [now (. java.time.Instant now)]
    (.getEpochSecond now)))
