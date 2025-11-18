# ModNVote

**Modern, transparent community voting for PaperMC 1.21.x**

A privacy‑first, lightweight, and production‑ready **yes/no voting plugin** for Minecraft servers — designed for modern communities that value fairness, trust, and data integrity.

ModNVote is built by [**MODN METL LTD**](https://modnmetl.com) and open‑sourced to promote transparent community decision‑making.

![CI](https://github.com/MODNMETL/ModNVote/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-007396)
![Paper](https://img.shields.io/badge/Paper-1.21.x-blue)
![License: MIT](https://img.shields.io/badge/License-MIT-green)
![Release](https://img.shields.io/github/v/release/MODNMETL/ModNVote?display_name=tag)

---

## ✨ Features (v1.1.5)

- **GUI voting** — `/modnvote` opens a simple Yes/No GUI; players click to vote, in total privacy.
- **Single vote per player** (per round) using UUID.
- **IP‑based duplicate prevention** with configurable bypass permission.
- **Cryptographic integrity seal** over:
    - current YES / NO tallies, and
    - the full ordered list of participating UUIDs.
- **On‑vote integrity enforcement**:
    - Before a new vote is accepted, the plugin verifies that the existing tally and participant list still match the stored HMAC.
    - If verification fails, the vote is rejected and the system enters a **suspect** state (see below).
- **Suspect tally detection & broadcast**:
    - If anyone tampers with the SQLite database or tally table, integrity checks will fail.
    - Staff are warned and online players can be alerted that the vote has been compromised.
- **Status with self‑awareness**:
    - `/modnvote status` reports YES/NO counts **and** whether the tally currently has a valid integrity seal.
    - It also tells the viewer whether *their* own vote is included in the tally.
- **Admin audit tools**:
    - `/modnvote audit` — quick summary of total voters, bypass voters, and tallies.
    - `/modnvote fullaudit` — groups voters by IP to highlight clusters and potential alts (does not reveal how they voted).
    - `/modnvote reset` — admin command to clear all votes and reset the integrity seal.
- **Config‑driven messaging** — all player‑facing messages live in `config.yml`.
- **SQLite persistence** — votes survive restarts.
- **PlaceholderAPI support** — expose YES/NO counts and percentages for scoreboards, sidebars, etc.
- Built for **Paper 1.21.x** and Java 21.

> ModNVote is the spiritual successor to the older PineVote plugin, rebuilt for broader use under the MODN METL brand.

---

## 🕊️ Privacy & Integrity Model

ModNVote is designed around two principles:

1. **Privacy** — make it difficult for anyone (including staff) to link a player to a specific vote via logs alone.
2. **Integrity** — make it easy to detect if someone has quietly tampered with vote data.

### How privacy is protected

- Players vote **via GUI** (`/modnvote` → click Yes/No).
    - There is no command in chat or console logs which would reveal their vote.
    - The plugin writes votes directly to the database without echoing the exact choice to console.
- The database stores:
    - UUID, IP, bypass flag, vote choice and round; and
    - a tally row containing YES / NO counts plus an HMAC over (round, YES, NO, ordered UUIDs).
- Staff can still see **who participated** (via audits), but not which way they voted just from logs.

> ModNVote stores only who participated in a round and the overall YES/NO tallies.
It does not record per-player choices, so a server owner cannot directly see how any specific player voted, except in trivial cases like a unanimous vote or a round with a single voter.

### How integrity is enforced

- Every time votes change, ModNVote computes a **canonical string** from:
    - round id
    - YES and NO tallies
    - ordered list of all voter UUIDs
- It computes a **HMAC‑SHA256** using a per‑round secret pepper stored on disk and saves the hex digest.
- On key operations (vote, status, verify, admin actions), ModNVote recomputes the HMAC and compares:
    - If they match → the tally is **cryptographically valid**, and this is reported to the user.
    - If they don’t → the tally is flagged as **compromised**, votes are rejected, and alerts are shown.

There is **no force‑approve option**: if the tally has been tampered with, the only supported recovery path is to investigate and either restore from backup or reset the round.

---

## 🕹️ Commands

All commands are `/modnvote ...`.

### Player

- `/modnvote`
    - Opens the Yes/No GUI.
    - If the tally is currently cryptographically valid, the player is told this **before** their click is applied.
    - After the vote is stored, the player is told that:
        - their vote was accepted, and
        - the integrity seal has been re‑applied to cover the updated tallies.

### Utility / admin

- `/modnvote status`
    - Shows YES / NO tallies.
    - States whether the tally is **cryptographically valid** or **compromised**.
    - Tells the viewer:
        - “This tally **includes** a vote from you” or
        - “This tally **does not include** a vote from you”.

- `/modnvote verify`
    - Recomputes the canonical string and HMAC and reports validity.
    - If invalid, the system does **not** update the seal; it simply reports the mismatch.

- `/modnvote reset`
    - Clears participation, tallies, and stored HMAC for the current round.
    - A new integrity seal will be created as soon as the first valid vote is cast.

- `/modnvote reload`
    - Reloads `config.yml` and messages.

- `/modnvote audit`
    - Shows a concise summary, for example:
        - `Audit » Total: 42, With bypass: 3 (7.1%) | YES: 30, NO: 12`

- `/modnvote fullaudit`
    - Groups voters by IP and lists bypass vs non‑bypass users per IP.
    - Useful for spotting suspicious clusters without revealing vote choices.

---

## 🎛️ Permissions

```text
modnvote.use             – allow /modnvote (GUI voting)
modnvote.vote            – allow casting a vote via the GUI
modnvote.status          – allow /modnvote status
modnvote.verify          – allow /modnvote verify

modnvote.admin.reset     – allow /modnvote reset
modnvote.admin.reload    – allow /modnvote reload
modnvote.admin.audit     – allow /modnvote audit
modnvote.admin.fullaudit – allow /modnvote fullaudit

modnvote.bypass          – allow voting even if someone on the same IP has already voted
```

By default, only OPs get the `modnvote.admin.*` permissions. Regular players typically get `modnvote.use`, `modnvote.vote`, and `modnvote.status`.

The bypass node is configurable in `config.yml` — you can keep `modnvote.bypass` or point it at an existing alt‑account / VPN‑detection plugin’s bypass node.

---

## 🔌 PlaceholderAPI

If PlaceholderAPI is installed, ModNVote registers a small set of placeholders (under a `modnvote_...` prefix) so you can drop tallies into scoreboards, holograms, etc.

Examples (names may vary slightly depending on your expansion implementation):

```text
%modnvote_yes%          – current YES tally
%modnvote_no%           – current NO tally
%modnvote_total%        – total votes
%modnvote_yes_percent%  – YES percentage
%modnvote_no_percent%   – NO percentage
```

These are backed by the same cached tallies used in `/modnvote status` and are safe to use frequently.

---

## ⚙️ Configuration

On first run, ModNVote generates a `config.yml` with sections for:

- `messages.*` — all player‑facing messages, including:
    - voted_yes, voted_no
    - already_voted, duplicate_ip
    - reset_done, reloaded
    - audit and fullaudit texts
    - verify_valid / verify_invalid summaries
- `cache.refresh_seconds` — how often to refresh tally caches asynchronously.
- `logging.*` — whether to log votes and bypass usage to console (these avoid logging which option was chosen).
- `permissions.bypass_node` — the permission string treated as a bypass flag.
- `integrity.pepper_file_pattern` — where per‑round secret keys are stored on disk.

You are encouraged to customise messages to match your server’s tone.

---

## 🚀 Installation

1. Ensure your server is running **Paper 1.21.x** with **Java 21**.
2. Drop `modnvote-1.1.5.jar` into your `plugins/` folder.
3. (Optional) Install **PlaceholderAPI** if you want placeholders.
4. Start the server to generate `config.yml`.
5. Configure permissions with LuckPerms or your chosen permissions plugin.
6. Ask players to use `/modnvote` to open the GUI and vote.

---

## 🧭 Roadmap

- Multi‑question polls and richer GUI flows
- MySQL support for large networks
- Time‑boxed votes with automatic open/close windows
- Optional player‑visible confirmation tokens (for external audits)
- REST / webhook hooks for dashboards or external tooling

Suggestions are welcome — open an issue or discussion on GitHub.

---

## 🤝 Contributing

Contributions are very welcome.

1. Fork the repository on GitHub.
2. Create a feature branch from `main`.
3. Run `./gradlew clean build` before submitting.
4. Open a Pull Request with a clear description and rationale.

Please keep the code style close to the existing Java 21 patterns and avoid introducing unnecessary dependencies.

---

## 🔐 Security

If you discover a vulnerability or serious integrity issue:

- **Do not** post details publicly.
- Email: **security@modnmetl.com** with a clear description and steps to reproduce.
- We’ll aim to respond and patch as quickly as possible.

---

## 📜 License

This project is licensed under the **MIT License** — see the [LICENSE](./LICENSE) file for details.

```text
Copyright (c) 2025 MODN METL LTD
Developed by Jamie E. Thompson (@jamjet3)
```

---

## 🏗️ Credits

- **Development Lead:** [Jamie E. Thompson](https://github.com/jamjet3)
- **Maintainer:** [MODN METL LTD](https://github.com/MODNMETL)
- **Community Testing:** Pinecraft Equestrian SMP
- **Build System:** Gradle (Java 21, PaperMC 1.21.x)

> “Trust, but verify.” — The guiding principle behind **ModNVote**.  
> Built to help communities make fair, transparent decisions — the modern way.
