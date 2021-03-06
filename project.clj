(defproject com.fishfrog/api-builder "0.3.2-SNAPSHOT"
  :description "Write api functions with domain information."
  :url "http://github.com/palletops/api-builder"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/tools.macro "0.1.2"]
                 [potemkin "0.3.12"]
                 [prismatic/schema "0.3.7"]]
  :prep-tasks ["cljx" "javac" "compile"]
  :test-paths ["test/clj" "target/generated/test/clj"]
  :aliases {"auto-test" ["do" "clean," "cljx," "cljsbuild" "auto" "test"]
            "jar" ["do" "cljx," "jar"]
            "install" ["do" "cljx," "install"]
            "test" ["do" "cljx," "test"]}
  :cljsbuild {:builds []})
