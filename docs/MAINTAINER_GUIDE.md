# Maintainer guide (publishing an approved module)

This is the reviewer's checklist for adding a module after it passes review on Discord.

## 1. Add the module folder

1. Create `modules/<module-id>/` and place the approved files inside it, in the standard layout.
2. Make sure the folder has a `README.md` (the public page) with a clear description, screenshots, and install
   notes. Put screenshots in `modules/<module-id>/assets/`.
3. Confirm `module.json` is valid and its `id`, `version`, `author`, and `description` are correct, because the
   catalog is generated from it.

## 2. Regenerate the catalog

Run the catalog builder from the repo root:

```bash
python3 tools/build_catalog.py
```

It scans every `modules/*/module.json`, writes `catalog.json`, and refreshes the table in `README.md` between
the `CATALOG:START` and `CATALOG:END` markers. Commit the changes it makes.

## 3. Commit

```bash
git add modules/<module-id> README.md catalog.json
git commit -m "Add <module-name> module"
git push
```

## 4. Cut a release (the installable zip)

Each module version gets a GitHub Release carrying a `.zip` a player can drop into `game/modules/`.

1. Build the zip so it extracts to `<module-id>/...` (zip the folder itself, not its contents):

   ```bash
   cd modules && zip -r "../<module-id>-<version>.zip" "<module-id>" && cd ..
   ```

2. Create a release tagged `<module-id>-v<version>` (for example `example-module-v1.0.0`), give it a short
   changelog, and attach the zip.
3. Link the release from the module's `README.md` if you want a direct download button.

Keeping one tag per module version means several modules can be released independently without clashing.
