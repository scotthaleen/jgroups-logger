(ns scott.jgroups.logger-test
  (:require [clojure.test :refer :all]
            [clojure.tools.logging :as logging]
            [clojure.tools.logging.impl :as impl])
  (:import [scott.jgroups Logger]))

(defn memory-logger
  "Memory logger for testing"
  [enabled-levels buffer]
  (reify impl/Logger
    (enabled? [_ level] (contains? @enabled-levels level))
    (write! [_ level throwable message]
      (swap! buffer conj [level throwable message]))))

(defn memory-log-factory
  "Memory logger factory"
  [enabled-levels buffer]
  (reify impl/LoggerFactory
    (name [_] "memory logger")
    (get-logger [_ _]
      (memory-logger enabled-levels buffer))))

(def log-buffer (atom []))
(def enabled-levels (atom #{:trace :debug :info :warn :error :fatal}))

(defn logger-setup
  "test-fixture - run each test in the binding of the memory logger and reset atoms after each test"
  [f]
  (binding [logging/*logger-factory* (memory-log-factory enabled-levels log-buffer)]
    (f))
  (reset! log-buffer [])
  (reset! enabled-levels #{:trace :debug :info :warn :error :fatal}))

(use-fixtures :each logger-setup)

(deftest test-contstructors
  (is (= "java.lang.String" (:classname (.state (Logger. (.getClass ""))))))
  (is (= "a.b.c" (:classname (.state (Logger. "a.b.c")))))
  (is (= "scott.jgroups.logger-test" (:classname (.state (Logger. 'scott.jgroups.logger-test))))))

(deftest test-enabled-levels
  (let [log (Logger. "test")]
    (is (.isInfoEnabled log))
    (is (.isWarnEnabled log))
    (is (.isErrorEnabled log))
    (is (.isFatalEnabled log))
    (is (.isDebugEnabled log))
    (is (.isTraceEnabled log))
    (swap! enabled-levels disj :trace :debug)
    (is (not (.isDebugEnabled log)))
    (is (not (.isTraceEnabled log)))))

(deftest test-get-level
  (let [log (Logger. "test")]
    (is (= "TRACE" (.getLevel log)))
    (swap! enabled-levels disj :trace :debug)
    (is (= "INFO" (.getLevel log)))
    (reset! enabled-levels #{})
    (is (= "NONE" (.getLevel log)))))

(deftest test-set-level
  (let [log (Logger. "test")]
    (is (thrown? UnsupportedOperationException
                 (.setLevel log "WARN")))))

(deftest test-log-level-info
  (let [log (Logger. (.getClass (Object.)))]
    (testing "log message at info"
      (.info log "test")
      (is (= [:info nil "test"] (last @log-buffer))))

    (testing "log formatted message at info"
      (.info log "%s %s %s" (into-array Object ["a" "b" "c"]))
      (is (= [:info nil "a b c"] (last @log-buffer))))

    (is (= 2 (count @log-buffer)))

    (swap! enabled-levels disj :info)
    (reset! log-buffer [])

    (testing "log formatting does not happen when level is disabled"
      (.info log "%s %s %s" (into-array Object ["a" "b" "c"]))
      (is (= 0 (count @log-buffer))))))


(defmacro deftest-log-method
  "
  Generates a group of tests for a log level

  ```
  (deftest-log-method :trace)

  (deftest test-log-level-trace ...
     (.trace log msg)

  ```
  "
  [level]
  (let [level# (name level)
        method (str "." level#)]
    `(deftest ~(symbol (str "test-log-level-" (name level)))
       (let [log# (Logger. (.getClass (Object.)))]

         ;; Tests String ex. log.warn(msg)
         (testing ~(str "log message at " level#)
           (~(symbol method) log# "test")
           (is (= [~level nil "test"] (last @log-buffer))))

         ;; Tests String, String... ex. log.warn(frmt, arg1, arg2, arg3)
         (testing ~(str "log formatted message at " level#)
           (~(symbol method) log# "%s %s %s" (into-array Object ["a" "b" "c"]))
           (is (= [~level nil "a b c"] (last @log-buffer))))

         ;; Tests String, Throwable log.warn(msg, ex)
         (testing ~(str "log Throwable message at " level#)
           (as->
               (try
                 (throw (Exception. "testing"))
                 (catch Exception ex#
                   (~(symbol method) log# "test error" ex#)
                   ex#)) e#
             (is (= [~level e# "test error"] (last @log-buffer)))))

         (is (= 3 (count @log-buffer)))

         ;; Test disabling the level which should prevent the log call
         (swap! enabled-levels disj ~level)
         (reset! log-buffer [])

         (testing "log formatting does not happen when level is disabled"
           (~(symbol method) log# "%s %s %s" (into-array Object ["a" "b" "c"]))
           (is (= 0 (count @log-buffer))))))))


;; generate test for each method
(deftest-log-method :trace)
(deftest-log-method :debug)
(deftest-log-method :error)
(deftest-log-method :warn)
(deftest-log-method :fatal)

