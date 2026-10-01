# Contributing a module

Thank you for building for L2 Living Worlds. This page explains how to submit a module and what it must contain
to pass review.

## How submission works

1. Join the server's Discord.
2. In the submissions channel, click the **Submit a Module** button.
3. A private channel opens between you and the reviewer. There you:
   - Upload your module files (a `.zip` of the module folder is easiest).
   - Write a short summary of what the module does.
   - Add screenshots if you have them. They are optional but help a lot.
4. The reviewer checks the module. If it passes, it is published in this repository and a release is cut for it.

You can have a limited number of open submissions at a time. Close or finish one before opening more.

## What a module must be

A module is one self-contained folder that follows the Living World Module Framework. The full specification
lives in the main project docs; a short version is in [docs/MODULE_FORMAT.md](docs/MODULE_FORMAT.md).

The required shape:

```text
<module-id>/
  module.json          # manifest: identity, ownership, lifecycle
  config/
    module.ini         # the module's config, including its enable flag
  scripts/             # entry point implementing GameModule, package modules.<id>
  data/                # optional: module-owned item, skill, NPC, HTML, multisell files
  sql/
    install.sql        # optional: additive, namespaced tables
    remove.sql         # optional: the explicit opt-in cleanup
  MODULE.md            # notes for maintainers
  README.md            # the public page shown in this repo (what it does, screenshots, install)
```

Copy [`template-module/`](template-module) to start from a working skeleton.

## Review checklist

A module is more likely to pass review on the first try if:

- It never edits a stock game class. It attaches only through the published extension points.
- Its entry point checks its enable flag first and does nothing when it is off.
- Every file, id range, and database table it owns is declared in `module.json`, and every path stays inside the
  module folder.
- Any id ranges it reserves do not collide with the base game or with modules already published here.
- Disabling it (enable flag off) and removing it (folder deleted) both leave the server behaving exactly as
  stock.
- The `README.md` clearly explains what it does and how to use it.

## License

By submitting a module you agree to publish it under the GNU General Public License v3.0, the same license as
this repository. Keep the GPL header in your source files.
