#!/usr/bin/env bb
;; reattach-head.bb — Reattach a detached HEAD to the best matching branch.
;; Handles: stashing dirty work trees, fast-forwarding behind branches,
;; creating missing local tracking branches, and prompting when ambiguous.

(require '[babashka.process :refer [shell sh]]
         '[clojure.string :as str])

;; ── helpers ──────────────────────────────────────────────────────────

(defn git
  "Run a git command, return trimmed stdout."
  [& args]
  (-> (apply sh "git" args) :out str/trim))

(defn non-blank-lines [s]
  (->> (str/split-lines s)
       (remove str/blank?)))

(defn detached?
  "True when HEAD is not on any branch.
   `git symbolic-ref` exits non-zero when HEAD is detached."
  []
  (-> (sh "git" "symbolic-ref" "--quiet" "HEAD") :exit (not= 0)))

(defn current-commit []
  (git "rev-parse" "HEAD"))

(defn local-branches
  "Local branch names (excluding detached-HEAD pseudo-entries)."
  []
  (->> (git "branch" "--format=%(refname:short)")
       non-blank-lines
       (remove #(str/starts-with? % "("))))

(defn remote-branches-at
  "Remote-tracking refs that point at `commit` (e.g. origin/main)."
  [commit]
  (->> (git "branch" "-r" "--points-at" commit "--format=%(refname:short)")
       non-blank-lines))

(defn branch-commit [branch]
  (git "rev-parse" branch))

(defn commits-between
  "Number of commits in (from..to]."
  [from to]
  (-> (git "rev-list" "--count" (str from ".." to))
      parse-long))

(defn dirty?
  "True when the working tree or index has uncommitted changes."
  []
  (not (str/blank? (git "status" "--porcelain"))))

(defn stash-save! []
  (println "Stashing uncommitted changes...")
  (shell "git" "stash" "push" "-m" "reattach-head: temp stash"))

(defn stash-pop! []
  (println "Restoring stashed changes...")
  (shell "git" "stash" "pop"))

(defn checkout! [branch]
  (shell "git" "checkout" branch))

(defn fast-forward! [target]
  (println (str "Fast-forwarding to " target "..."))
  (shell "git" "merge" "--ff-only" target))

(defn create-tracking-branch! [local remote]
  (println (str "Creating local branch " local " tracking " remote "..."))
  (shell "git" "checkout" "-b" local "--track" remote))

(defn prompt [msg]
  (print msg)
  (flush)
  (str/trim (read-line)))

;; ── main logic ───────────────────────────────────────────────────────

(when-not (detached?)
  (println "HEAD is already attached — nothing to do.")
  (System/exit 0))

(let [commit       (current-commit)
      _            (println (str "HEAD is detached at " (subs commit 0 7)))
      locals       (local-branches)
      remotes      (remote-branches-at commit)

      ;; 1. Local branches that point exactly at this commit
      exact-local  (filter #(= (branch-commit %) commit) locals)

      ;; 2. Local branches whose upstream matches a remote at this commit
      ;;    (i.e. the branch exists locally but may be behind)
      remote-names (set (map #(last (str/split % #"/" 2)) remotes))
      behind-local (filter (fn [b]
                             (and (remote-names b)
                                  (not= (branch-commit b) commit)))
                           locals)

      ;; 3. Rank candidates: exact first, then behind (by distance)
      candidates   (concat
                    (map (fn [b] {:branch b :ahead 0 :source :exact}) exact-local)
                    (map (fn [b] {:branch b
                                  :ahead  (commits-between (branch-commit b) commit)
                                  :source :behind})
                         behind-local))

      preferred    #{"main" "master" "develop"}
      sorted       (sort-by (juxt :ahead
                                  #(if (preferred (:branch %)) 0 1)
                                  #(count (:branch %)))
                            candidates)]

  (cond
    ;; ── single best candidate ────────────────────────────────────────
    (= 1 (count sorted))
    (let [{:keys [branch ahead]} (first sorted)
          needs-stash (dirty?)
          remote-ref  (first (filter #(str/ends-with? % (str "/" branch)) remotes))]
      (when needs-stash (stash-save!))
      (checkout! branch)
      (when (pos? ahead)
        (fast-forward! (or remote-ref (str "origin/" branch))))
      (when needs-stash (stash-pop!))
      (println (str "HEAD reattached to " branch ".")))

    ;; ── multiple candidates — let the user choose ────────────────────
    (pos? (count sorted))
    (do
      (println "\nCandidate branches:")
      (doseq [{:keys [branch ahead]} sorted]
        (println (str "  " branch
                      (when (pos? ahead) (str " (" ahead " commits behind HEAD)")))))
      (let [best   (:branch (first sorted))
            input  (prompt (str "Use \"" best "\"? [Y/n/other name]: "))
            target (cond
                     (or (str/blank? input) (= (str/lower-case input) "y")) best
                     (= (str/lower-case input) "n") nil
                     :else input)]
        (if target
          (let [needs-stash (dirty?)
                remote-ref  (first (filter #(str/ends-with? % (str "/" target)) remotes))]
            (when needs-stash (stash-save!))
            (checkout! target)
            (when (and remote-ref (pos? (commits-between (branch-commit target) commit)))
              (fast-forward! remote-ref))
            (when needs-stash (stash-pop!))
            (println (str "HEAD reattached to " target ".")))
          (do (println "Aborted.") (System/exit 1)))))

    ;; ── no local candidate but we have a remote ──────────────────────
    (seq remotes)
    (let [remote  (first remotes)                       ;; e.g. origin/main
          local   (last (str/split remote #"/" 2))      ;; e.g. main
          needs-stash (dirty?)]
      (println (str "No local branch at this commit, but remote " remote " matches."))
      (when needs-stash (stash-save!))
      (create-tracking-branch! local remote)
      (when needs-stash (stash-pop!))
      (println (str "HEAD reattached to new branch " local ".")))

    ;; ── nothing matches — offer to create a branch ───────────────────
    :else
    (let [name (prompt (str "No branch found for " (subs commit 0 7)
                            ". Create one? Enter name (blank to abort): "))]
      (if (seq name)
        (let [needs-stash (dirty?)]
          (when needs-stash (stash-save!))
          (shell "git" "checkout" "-b" name)
          (when needs-stash (stash-pop!))
          (println (str "HEAD reattached to new branch " name ".")))
        (do (println "Aborted.") (System/exit 1))))))
