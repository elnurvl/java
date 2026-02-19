## Building

```shell
./gradlew build          # Build and run tests
./gradlew test           # Run tests only
./gradlew jacocoTestReport  # Generate coverage report
```

## Code style

This project follows the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html), enforced by [Checkstyle](https://checkstyle.org/) and [Spotless](https://github.com/diffplug/spotless).

- **Spotless** auto-fixes formatting (indentation, imports, whitespace)
- **Checkstyle** validates structural rules (naming, Javadoc, braces)

```bash
./gradlew spotlessApply                 # Auto-fix formatting
./gradlew spotlessCheck                 # Check without fixing
./gradlew checkstyleMain checkstyleTest # Validate against style guide
```

## Git hooks

The repository includes pre-configured Git hooks in `.githooks/` that run automatically on each commit and push:

1. **Spotless** — auto-fixes formatting and re-stages corrected files
2. **Checkstyle** — validates staged Java files against Google Java Style Guide
3. **Tests** — runs the full test suite
4. **Coverage** — verifies instruction coverage is at least 80%
5. **Commit message** — enforces Conventional Commits format

To enable the hooks, configure Git to use the `.githooks` directory:

```bash
git config core.hooksPath .githooks
```

## Commit messages
The repo follows the [Conventional Commit](https://www.conventionalcommits.org/en/v1.0.0/) standard:
```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```
The type must be one of these:
- `build`
- `chore`
- `ci`
- `docs`
- `feat`
- `fix`
- `perf`
- `refactor`
- `revert`
- `style`
- `test`

Example:
```
fix: prevent racing of requests

Introduce a request id and a reference to latest request. Dismiss
incoming responses other than from latest request.

Remove timeouts which were used to mitigate the racing issue but are
obsolete now.

Reviewed-by: Z
Refs: #123
```

## Branches

Each branch corresponds to an independent project with an exception of `main` branch acting as a template.
Individual projects use [GitHub flow](https://docs.github.com/en/get-started/using-github/github-flow)
and can have their feature branches under `project-name/optional-label/feature-name`.

## Spec-Driven Development

This repository uses [OpenSpec](https://openspec.dev) to plan changes through a structured artifact workflow.

### Setup

1. Install [Node.js](https://nodejs.org/en/download/) (v18+) if not already available.

2. Install OpenSpec globally:

   ```bash
   npm install -g @fission-ai/openspec@latest
   ```

3. Initialize OpenSpec in the project:

   ```bash
   openspec init
   ```

### Directory structure

```
openspec/
├── config.yaml              # Global settings and rules for generated artifacts
├── specs/                   # Canonical specs (synced from completed changes)
└── changes/
    ├── <active-change>/     # In-progress changes
    │   ├── .openspec.yaml
    │   ├── proposal.md
    │   ├── design.md
    │   ├── specs/
    │   └── tasks.md
    └── archive/             # Completed changes
```

### Configuration

The `openspec/config.yaml` file defines:

- **schema** — artifact workflow type (`spec-driven`)
- **context** — project-level context passed to every artifact (tech stack, conventions)
- **rules** — per-artifact constraints:
    - `proposal` — keep under 500 words, include a "Non-goals" section
    - `specs` — use EARS notation for requirements, Given/When/Then for scenarios
    - `design` — use Mermaid syntax for diagrams, include sequence diagrams for complex flows

### Slash commands

Use these slash commands to drive the workflow:

| Command | Purpose |
|---|---|
| `/opsx:explore` | Think through ideas before starting |
| `/opsx:new` | Start a new change |
| `/opsx:continue` | Create the next artifact |
| `/opsx:ff` | Fast-forward through all artifacts |
| `/opsx:apply` | Implement tasks from a change |
| `/opsx:verify` | Verify implementation matches artifacts |
| `/opsx:archive` | Archive a completed change |

### Typical flow

1. `/opsx:explore` *(optional)* — investigate the problem and clarify requirements
2. `/opsx:new` — describe the change and generate a proposal
3. `/opsx:continue` — step through design, specs, and tasks
4. `/opsx:apply` — implement the tasks
5. `/opsx:verify` — validate implementation against specs
6. `/opsx:archive` — archive the change and sync specs
