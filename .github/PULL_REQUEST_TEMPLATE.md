## What this changes

Describe what module you are adding or updating.

## Module checklist

- [ ] The module lives in one self-contained folder under `modules/<module-id>/`.
- [ ] It does not edit any stock game class.
- [ ] The entry point checks its enable flag first and does nothing when it is off.
- [ ] Every owned file, id range, and table is declared in `module.json`, all paths inside the folder.
- [ ] Reserved id ranges do not collide with the base game or modules already published here.
- [ ] Disabling it and removing its folder both leave the server behaving exactly as stock.
- [ ] The module has a `README.md` with a clear description and screenshots.
- [ ] I ran `python3 tools/build_catalog.py` and committed the updated `README.md` and `catalog.json`.

## Notes

Anything the reviewer should know.
