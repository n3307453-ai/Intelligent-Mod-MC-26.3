"""Odtwarza strukturę folderów z płaskich plików.
Uruchom:  python restore.py   (albo python3 restore.py)
Pliki o nazwach typu  src__main__java__X.java  trafią do  src/main/java/X.java
Po wszystkim możesz usunąć płaskie pliki i restore.py."""
import os, shutil

here = os.path.dirname(os.path.abspath(__file__))
out = os.path.join(here, "intelligent-mod")
n = 0
for name in sorted(os.listdir(here)):
    if name == "restore.py" or not os.path.isfile(os.path.join(here, name)):
        continue
    parts = name.split("__")
    if parts[0] == "github":      # folder .github (kropka na początku bywa ukrywana)
        parts[0] = ".github"
    dest = os.path.join(out, *parts)
    os.makedirs(os.path.dirname(dest), exist_ok=True)
    shutil.copyfile(os.path.join(here, name), dest)
    n += 1
print(f"Gotowe: {n} plików -> {out}")
