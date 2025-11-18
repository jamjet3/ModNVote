# Changelog – ModNVote

All notable changes to this project will be documented in this file.

The format is loosely based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [1.1.5] – GUI voting & stronger privacy

- Added a **GUI-based voting flow** triggered via `/modnvote`.
    - Players now click **Yes** or **No** in a menu instead of typing `/modnvote yes` or `/modnvote no`.
    - This avoids vote choices appearing as chat or command entries in the server console/logs.
- Removed the `/modnvote yes` and `/modnvote no` subcommands entirely.
- Ensured vote handling:
    - Verifies the existing tally’s integrity before accepting a new vote.
    - Applies the vote only if the tally is cryptographically valid.
    - Recomputes and stores a new HMAC after the vote to maintain the integrity seal.
- Improved player feedback:
    - On voting, players are told whether the tally was valid before their vote and that the integrity seal has been re‑applied after.
    - `/modnvote status` now reports:
        - whether the tally is **cryptographically valid** or not, and
        - whether the tally currently **includes a vote from the viewer**.
- Kept admin tools (`audit`, `fullaudit`, `reset`, `reload`, `verify`) while tightening their messaging around integrity status and compromised tallies.

---

## [1.1.4] – Integrity messaging & docs

- Refined integrity checks and error handling around the HMAC tally seal.
- Improved messages when verification fails, including clearer guidance for staff.
- Updated README and documentation to better describe the privacy and integrity model.
- Added a structured `CHANGELOG.md` to track future changes.

---

## [1.1.3] – Clean rebuild & repository hygiene

- Rebuilt the project cleanly in a fresh Gradle setup.
- Restored and fixed CI workflows for GitHub Actions.
- Tightened SQLite schema and DAO handling.
- Updated README to reflect the production‑ready state of the plugin.

---

## [1.1.2] – Initial public release

- First public release of ModNVote as a PaperMC voting plugin.
- Core features:
    - Yes/No voting with per‑UUID and per‑IP checks.
    - SQLite persistence.
    - Basic cryptographic sealing over tallies and participant lists.
    - Admin audit commands and PlaceholderAPI support.
