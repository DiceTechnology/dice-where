# AGENTS.md — Deltatre platform engineering framework

This repository follows the Deltatre platform engineering framework and the
Deltatre Foundations rulebook. This file contains no rules. Read them from GitHub.

## Where
- Entry point: `DiceTechnology/deltatre-platform-engineering-framework`, file `README.md`, branch `main`.
- Follow the README to the rulebook (`DiceTechnology/deltatre-foundations`) and to component repositories.
- Read the framework's own files at `main`. When this repository pins a component version (in its
  EaC file), read that component's repository at the pinned tag.
- For versions (which modules and component releases exist, the pipeline library release, what a
  Jenkins agent runs), ask the MCP server: `deltatre-agent-context` in your client, built from
  `DiceTechnology/deltatre-pef-agent-context-mcp`. Read everything else from GitHub.

## How
- Read files with the GitHub API:
  `gh api -H "Accept: application/vnd.github.raw" "repos/DiceTechnology/<repo>/contents/<path>?ref=<branch-or-tag>"`
- Resolve a relative link against the linking file's directory, in the same repository and at the same ref.
- Requires `gh` logged in with an account in the `DiceTechnology` organisation.
- Cache each file at `~/.cache/deltatre-framework/<repo>/<ref>/<path>`; the cached file's modification
  time is when you fetched it.

## How often
- At the start of every session, fetch the README from GitHub.
- Read other files when a task needs them.
- Re-fetch any cached file older than 24 hours.

## If a file cannot be read from GitHub
- A file missing from a repository you can read is a gap: say so, and carry on without it.
- If GitHub cannot be reached, or you cannot read the repository, use the cached copy, if there is
  one, and tell the human how old it is.
- If there is no cached copy, stop any framework-related work and ask the human for access or for
  the file's content. Do not guess framework rules.
