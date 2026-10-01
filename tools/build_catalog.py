#!/usr/bin/env python3
"""Build the module catalog.

Scans every modules/*/module.json, writes catalog.json, and refreshes the
markdown table in README.md between the CATALOG:START and CATALOG:END markers.

Run from the repository root:

    python3 tools/build_catalog.py

It makes no network calls and only reads module.json files, so it is safe to run
any time. Commit the files it changes (catalog.json and README.md).
"""

import json
import pathlib
import sys

REPO_ROOT = pathlib.Path(__file__).resolve().parent.parent
MODULES_DIR = REPO_ROOT / "modules"
README = REPO_ROOT / "README.md"
CATALOG_JSON = REPO_ROOT / "catalog.json"

START_MARKER = "<!-- CATALOG:START"
END_MARKER = "<!-- CATALOG:END -->"

EMPTY_TABLE = "No modules have been published yet. Approved modules will appear here."


def load_modules():
    """Return a list of module metadata dicts, sorted by display name."""
    modules = []
    if not MODULES_DIR.is_dir():
        return modules

    for manifest_path in sorted(MODULES_DIR.glob("*/module.json")):
        folder = manifest_path.parent.name
        try:
            data = json.loads(manifest_path.read_text(encoding="utf-8"))
        except (json.JSONDecodeError, OSError) as exc:
            print(f"warning: skipping {manifest_path}: {exc}", file=sys.stderr)
            continue

        modules.append(
            {
                "id": data.get("id", folder),
                "folder": folder,
                "name": data.get("name", data.get("id", folder)),
                "version": data.get("version", ""),
                "author": data.get("author", ""),
                "apiVersion": data.get("apiVersion", ""),
                "description": data.get("description", ""),
            }
        )

    modules.sort(key=lambda m: m["name"].lower())
    return modules


def build_table(modules):
    """Render the catalog markdown table (or the empty placeholder)."""
    if not modules:
        return EMPTY_TABLE

    lines = [
        "| Module | Version | Author | Description |",
        "|---|---|---|---|",
    ]
    for m in modules:
        name_cell = f"[{m['name']}](modules/{m['folder']})"
        desc = (m["description"] or "").replace("|", "\\|").replace("\n", " ")
        lines.append(
            f"| {name_cell} | {m['version']} | {m['author']} | {desc} |"
        )
    return "\n".join(lines)


def update_readme(table):
    """Replace the content between the catalog markers in README.md."""
    text = README.read_text(encoding="utf-8")

    start = text.find(START_MARKER)
    end = text.find(END_MARKER)
    if start == -1 or end == -1 or end < start:
        print(
            "error: could not find CATALOG markers in README.md; leaving it untouched.",
            file=sys.stderr,
        )
        return False

    # Keep the whole START comment line, replace the body, keep the END comment.
    start_line_end = text.find("\n", start)
    if start_line_end == -1:
        start_line_end = end
    new_text = (
        text[: start_line_end + 1]
        + "\n"
        + table
        + "\n\n"
        + text[end:]
    )
    README.write_text(new_text, encoding="utf-8")
    return True


def main():
    modules = load_modules()

    CATALOG_JSON.write_text(
        json.dumps(modules, indent=2, ensure_ascii=False) + "\n",
        encoding="utf-8",
    )
    print(f"Wrote {CATALOG_JSON.name} with {len(modules)} module(s).")

    if update_readme(build_table(modules)):
        print("Updated the catalog table in README.md.")


if __name__ == "__main__":
    main()
