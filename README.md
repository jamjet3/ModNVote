# ModNVote

**Modern, transparent community voting for PaperMC 1.21.x**

A privacy-first, lightweight, and production-ready **yes/no voting plugin** for Minecraft servers — designed for communities that value fairness, trust, and auditability.

This version includes enhanced **tamper detection**, **vote-privacy protection**, and **automatic integrity alerts**.

---

## ✨ Key Features

- Anonymous voting (`/modnvote yes|no`) — *no vote choice is ever logged to console by the plugin*.
- Live status: `/modnvote status`
    - Shows YES/NO tallies
    - Confirms whether **this tally includes a vote from you**
    - Shows whether the tally is **cryptographically VALID or COMPROMISED**
- Cryptographic verification: `/modnvote verify`
    - Recomputes HMAC from current participants and tallies
    - Broadcasts a **tamper warning** server-wide if integrity fails
- Votes are only accepted when:
    - The current tally/HMAC are cryptographically valid, or
    - No votes have yet been recorded (fresh round)
- After each accepted vote:
    - The tally is updated
    - A **new cryptographic seal (HMAC)** is applied
    - The player is told that their vote has been applied and the seal updated
- Admin tools:
    - `/modnvote reset`
    - `/modnvote reload`
    - `/modnvote audit`
    - `/modnvote fullaudit`
- IP duplicate-vote prevention with configurable bypass permission
- SQLite persistence for reliability and restart safety
- PlaceholderAPI support

> ModNVote is a successor to the PineVote plugin, rebuilt for broader use under the MODN METL brand.

---

## 🔐 Privacy & Integrity

### Vote secrecy

ModNVote does **not** log:

- which player voted YES or NO
- any direct mapping of identity → vote choice

It only uses per-player UUIDs and IPs internally to enforce “one vote per person / per location” and for audit tools like `/audit` and `/fullaudit`.

Minecraft itself will still log commands like `/modnvote yes` to the server log — this is handled by the server, not by ModNVote.

### Tamper detection & protection

ModNVote maintains:

- A list of all participants’ UUIDs for the current round
- An internal tally: YES and NO counts
- A cryptographic HMAC (using a per-round secret pepper) over:
    - round id
    - YES count
    - NO count
    - the sorted list of participant UUIDs

If any of this is altered offline (for example via manual database editing):

- `/modnvote verify` reports **Verification failed**
- A **server-wide warning broadcast** is sent
- An error is logged to console
- Future votes are **blocked** until the integrity issue is resolved (e.g. by restoring a valid backup)

### Verified before and after each vote

When a player casts a vote:

1. The plugin first checks the current tally’s integrity.
    - If **no votes exist yet**, it reports that it is starting a fresh sealed tally.
    - If integrity is **valid**, it tells the player that the tally has been cryptographically verified and that their vote is now being applied.
    - If integrity is **compromised or an error occurs**, the vote is blocked and the player is advised to contact staff.
2. After the vote is accepted:
    - The tally is updated
    - A new HMAC is computed
    - The player is told that their vote has been recorded and the cryptographic seal has been updated.

---

## 📝 Commands

| Command | Permission | Description |
|--------|------------|-------------|
| `/modnvote yes` | `modnvote.vote` | Cast a YES vote (only if integrity is valid or no votes exist yet) |
| `/modnvote no` | `modnvote.vote` | Cast a NO vote (same integrity rules as above) |
| `/modnvote status` | `modnvote.status` | View tallies, integrity status, and whether this tally includes your vote |
| `/modnvote verify` | `modnvote.verify` | Run a full integrity check against stored HMAC |
| `/modnvote reset` | `modnvote.admin.reset` | Reset all votes for the current round |
| `/modnvote reload` | `modnvote.admin.reload` | Reload configuration from `config.yml` |
| `/modnvote audit` | `modnvote.admin.audit` | View totals, bypass counts, and tallies |
| `/modnvote fullaudit` | `modnvote.admin.fullaudit` | Group voters by IP (without revealing which way they voted) |

---

## ⚙️ Configuration (excerpt)

```yaml
# plugins/ModNVote/config.yml

messages:
  voted_yes: "&aThanks — your &2YES &avote has been recorded and the cryptographic seal has been updated."
  voted_no: "&aThanks — your &cNO &avote has been recorded and the cryptographic seal has been updated."
  already_voted: "&cYou have already voted."
  duplicate_ip: "&cA vote from your location has already been recorded."
  reset_done: "&eAll votes reset."
  reloaded: "&eModNVote configuration reloaded."

  audit_summary: "Audit » Total: {total}, With bypass: {bypass} ({percent}%) | YES: {yes}, NO: {no}"
  fullaudit_header: "Full audit (grouped by IP). Showing voters with bypass; non-bypass on same IP listed under each group."

  verify_no_votes: "&7No votes have been cast yet; nothing to verify."
  verify_valid: "&aVerification passed: tally matches participant set."
  verify_invalid: "&cVerification failed: tally does NOT match participant set."
  verify_error: "&cVerification failed due to an internal error; please contact staff."
  verify_broadcast_compromised: "[ModNVote] &cWARNING: vote integrity check FAILED. Vote data may have been altered. Please contact staff."

  integrity_compromised_vote_block: "&cVoting is currently disabled because the vote integrity check has failed. Please contact staff."
  integrity_error_vote_block: "&cYour vote could not be processed due to an internal integrity error. Please try again later or contact staff."
  integrity_ok_before_vote: "&aCurrent tally integrity has been cryptographically validated. Applying your vote..."
  integrity_ok_first_vote: "&aNo votes recorded yet. Starting a fresh cryptographically sealed tally with your vote."

  status_includes_you: "&bThis tally &aDOES &binclude a vote from you."
  status_excludes_you: "&bThis tally &cDOES NOT &binclude a vote from you."

permissions:
  # Default bypass node used to allow multiple players on the same IP to vote
  # (e.g. siblings in the same household).
  bypass_node: "modnvote.bypass"
  # If you already use an alt-protection plugin, you can set this to its bypass node instead
  # (for example: "noaltsexploits.bypass").

cache:
  refresh_seconds: 30

logging:
  # To protect privacy, ModNVote no longer logs votes to console.
  audit_votes_to_console: false
  # You may still log bypass usage if desired (does not reveal vote choice).
  audit_bypass_to_console: true

integrity:
  # Pattern for storing per-round pepper keys on disk
  pepper_file_pattern: "round-%d.key"
```

---

## 🧩 PlaceholderAPI

If PlaceholderAPI is present, ModNVote registers:

| Placeholder | Description |
|------------|-------------|
| `%modnvote_yes%` | Number of YES votes |
| `%modnvote_no%` | Number of NO votes |
| `%modnvote_total%` | Total number of votes |

Example usage (scoreboard or GUI):

```text
Yes Votes: %modnvote_yes%
No Votes: %modnvote_no%
Total: %modnvote_total%
```

---

## 📦 Installation

1. Download the latest `modnvote-x.x.x.jar` from GitHub Releases.
2. Drop the jar into your server’s `plugins/` directory.
3. Start (or restart) your Paper server.
4. Edit `plugins/ModNVote/config.yml` if needed.
5. Run `/modnvote reload` to apply changes.

---

## 📜 License

This project is licensed under the **MIT License** — see the [LICENSE](./LICENSE) file for details.  
You are free to use, modify, and distribute this software with attribution.

```text
Copyright (c) 2025
MODN METL LTD
Developed by Jamie E. Thompson (@jamjet3)
```

---

## 🏗️ Credits

- **Development Lead:** Jamie E. Thompson ([@jamjet3](https://github.com/jamjet3))
- **Maintainer:** [MODN METL LTD](https://github.com/MODNMETL)
- **Community Testing:** Pinecraft Equestrian SMP
- **Build System:** Gradle
- **Supported Platforms:** PaperMC 1.21.x

---

> “Trust, but verify.” — The guiding principle behind **ModNVote**  
> Built to help communities make fair, transparent decisions — the modern way.
