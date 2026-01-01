# Small script to validate completeness of the language files
import os

import yaml

# The file assumed to be complete
REFERENCE_FILE = "en-US.yml"

# Path to the language file
LANG_DIR = os.path.join( os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..")), "core", "src", "main", "resources", "lang")

def load_yaml(path: str) -> dict:
    with open(path, "r", encoding="utf-8") as f:
        return yaml.safe_load(f) or {}


def flatten_keys(d: dict, prefix=""):
    keys = []
    for k, v in d.items():
        full_key = f"{prefix}.{k}" if prefix else k
        if isinstance(v, dict):
            keys.extend(flatten_keys(v, full_key))
        else:
            keys.append(full_key)
    return keys


def main():

    if not os.path.isdir(LANG_DIR):
        print(f"Language folder not found: {LANG_DIR}")
        return

    lang_files = [f for f in os.listdir(LANG_DIR) if f.endswith(".yml")]

    if REFERENCE_FILE not in lang_files:
        print("Reference file not found in lang folder!")
        return

    reference_path = os.path.join(LANG_DIR, REFERENCE_FILE)
    reference_data = load_yaml(reference_path)
    reference_keys = set(flatten_keys(reference_data))

    print(f"Loaded reference file: en-US.yml ({len(reference_keys)} keys)")

    problems_found = False

    for file in lang_files:
        if file == REFERENCE_FILE:
            continue

        path = os.path.join(LANG_DIR, file)
        data = load_yaml(path)
        keys = set(flatten_keys(data))

        missing = reference_keys - keys

        if missing:
            problems_found = True
            print(f"\nMissing keys in {file}:")
            for key in sorted(missing):
                print(f"  - {key}")
        else:
            print(f"{file} is complete!")

    if not problems_found:
        print("\nAll language files are complete!")
    else:
        print("\nSome language files are missing keys. Review output above.")


if __name__ == "__main__":
    main()
