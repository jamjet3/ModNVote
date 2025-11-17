## [1.1.4] — 2025-11-14
### Added
- Pre-vote integrity verification message.
- Post-vote confirmation including re-sealed cryptographic hash.
- `/modnvote status` now shows player-inclusion and tally integrity.

### Changed
- Removed vote-related console logs for privacy.
- Updated default bypass permission to `modnvote.bypass`.

### Fixed
- Tamper detection now prevents silent re-sealing after offline modification.
- More robust tally and UUID list canonicalisation.
