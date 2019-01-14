(ns scott.jgroups.logger
  (:require [clojure.string :refer [upper-case]]
            [clojure.tools.logging :as log]
            [clojure.tools.logging.impl :as log-impl])
  (:import [org.jgroups.logging Log])
  (:gen-class
   :state state
   :name scott.jgroups.Logger
   :init init
   :implements [org.jgroups.logging.Log]
   ;; documentation says the constructor will only be passed String or Class
   :constructors {[Object] []
                  [Class] []
                  [String] []}))

(defmulti classname class)
(defmethod classname Class [x] (print-str x))
(defmethod classname String [x] x)
(defmethod classname clojure.lang.Namespace [x] (str x))
(defmethod classname clojure.lang.Symbol [x] (str x))

(defn logf*
  "Helper for string formatted log messages"
  [logger level frmt args]
  (when (log-impl/enabled? logger level)
    (log/log* logger level nil (apply (partial format frmt) args))))

(defn init*
  "constructor"
  [cl]
  [[] (let [clazz (classname cl)
            logger (log-impl/get-logger log/*logger-factory* clazz)]
        {:classname clazz
         :logger logger
         :log (partial log/log* logger)
         :logf (partial logf* logger)
         :enabled? (partial log-impl/enabled? logger)})])

(def -init init*)
(def -init-Class init*)
(def -init-String init*)
(def -init-Object init*)

(defn -info
  [this msg]
  ((:log (.state this)) :info nil msg))

(defn -info-String-Object<>
  [this frmt args]
  ((:logf (.state this)) :info frmt args))

(defn -debug
  [this msg]
  ((:log (.state this)) :debug nil msg))

(defn -debug-String-Throwable
  [this msg t]
  ((:log (.state this)) :debug t msg))

(defn -debug-String-Object<>
  [this frmt args]
  ((:logf (.state this)) :debug frmt args))

(defn -fatal
  [this msg]
  ((:log (.state this)) :fatal nil msg))

(defn -fatal-String-Throwable
  [this msg t]
  ((:log (.state this)) :fatal t msg))

(defn -fatal-String-Object<>
  [this frmt args]
  ((:logf (.state this)) :fatal frmt args))

(defn -error
  [this msg]
  ((:log (.state this)) :error nil msg))

(defn -error-String-Throwable
  [this msg t]
  ((:log (.state this)) :error t msg))

(defn -error-String-Object<>
  [this frmt args]
  ((:logf (.state this)) :error frmt args))

(defn -warn
  [this msg]
  ((:log (.state this)) :warn nil msg))

(defn -warn-String-Throwable
  [this msg t]
  ((:log (.state this)) :warn t msg))

(defn -warn-String-Object<>
  [this frmt args]
  ((:logf (.state this)) :warn frmt args))

(defn -trace
  [this msg]
  ((:log (.state this)) :trace nil msg))

(defn -trace-String-Throwable
  [this msg t]
  ((:log (.state this)) :trace t msg))

(defn -trace-String-Object<>
  [this frmt args]
  ((:logf (.state this)) :trace frmt args))

(defn -isInfoEnabled [this]
  ((:enabled? (.state this)) :info))
(defn -isDebugEnabled [this]
  ((:enabled? (.state this)) :debug))
(defn -isFatalEnabled [this]
  ((:enabled? (.state this)) :fatal))
(defn -isErrorEnabled [this]
  ((:enabled? (.state this)) :error))
(defn -isWarnEnabled [this]
  ((:enabled? (.state this)) :warn))
(defn -isTraceEnabled [this]
  ((:enabled? (.state this)) :trace))

(def ^{:private true
       :doc "vector of log levels sorted by priority for .getLevel"}
  levels [:trace :debug :info :warn :error :fatal])

(defn -getLevel
  ""
  [this]
  (->> levels
       (reduce
        (fn [_ lvl]
          (when ((:enabled? (.state this)) lvl)
            (reduced lvl)))
        nil)
       ((fnil name "none"))
       (upper-case)))
