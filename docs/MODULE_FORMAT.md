# Module format (short reference)

This is a quick reference for the folder a module must ship as. It is a summary of the Living World Module
Framework. The authoritative specification and authoring guide live in the main project repository
(`docs/MODULE_FRAMEWORK.md` and `docs/MODULE_AUTHORING_GUIDE.md`).

## Folder layout

```text
<module-id>/
  module.json
  config/
    module.ini
  scripts/
  data/
  sql/
    install.sql
    remove.sql
  MODULE.md
  README.md
```

- `module.json` - the manifest. Identity, entry point, version, api version, declared resources, reserved id
  ranges, database tables. Every path is relative to the module folder; `../` and absolute paths are rejected.
- `config/module.ini` - a fixed filename the platform loads and exposes to the module. Holds the enable flag
  and the module's settings.
- `scripts/` - the entry point class implementing `GameModule`, in package `modules.<id>` with hyphens removed
  (id `beast-taming` uses package `modules.beasttaming`). Never uses the stock `custom.*` packages.
- `data/` - optional module-owned item, skill, NPC, HTML, and multisell definitions.
- `sql/install.sql` - optional additive, namespaced tables. Must be idempotent (`CREATE TABLE IF NOT EXISTS`).
- `sql/remove.sql` - optional explicit, opt-in cleanup. The platform never runs it automatically.
- `MODULE.md` - maintainer notes.
- `README.md` - the public page for this repository: what the module does, screenshots, install notes.

## Manifest example

```json
{
  "id": "example-module",
  "name": "Example Module",
  "version": "1.0.0",
  "apiVersion": "1",
  "entrypoint": "modules.examplemodule.ExampleModule",
  "description": "A one-line summary of what the module does.",
  "author": "Author Name",
  "priority": 100,
  "dependencies": [],
  "conflicts": [],
  "resources": {},
  "reserves": {},
  "database": {}
}
```

## Rules that matter for review

- No stock class is edited. Behavior the engine does not expose becomes a generic platform hook in the main
  project, never a module edit.
- The entry point checks its enable flag first and does nothing when it is false.
- Every owned file, id range, and table is declared in the manifest, all paths inside the module folder.
- No id range is used without a reservation that does not collide with the base game or other modules.
- Disable (enable flag off) and remove (folder deleted) both leave the server behaving exactly as stock.
