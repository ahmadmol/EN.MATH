"""Package the current verified debug build and portable project source."""
from pathlib import Path
import hashlib
import json
import shutil
import zipfile

root = Path(__file__).resolve().parents[1]
dist = root / 'dist'
dist.mkdir(exist_ok=True)
apk = root / 'app/build/outputs/apk/debug/app-debug.apk'
shutil.copy2(apk, dist / 'EN.MATH-debug.apk')
excluded = {'.git', '.gradle', '.kotlin', '.idea', 'build', 'dist', '__pycache__'}
with zipfile.ZipFile(dist / 'EN.MATH-source.zip', 'w', zipfile.ZIP_DEFLATED) as archive:
    for file in sorted(root.rglob('*')):
        if not file.is_file():
            continue
        relative = file.relative_to(root)
        if any(part in excluded for part in relative.parts):
            continue
        if file.name in {'local.properties', 'ktfmt.jar'} or file.suffix in {'.apk', '.log'}:
            continue
        archive.write(file, Path('EN.MATH') / relative)
manifest = []
for file in sorted(dist.iterdir()):
    if file.name == 'delivery.json':
        continue
    manifest.append({'file': file.name, 'bytes': file.stat().st_size,
                     'sha256': hashlib.sha256(file.read_bytes()).hexdigest()})
(dist / 'delivery.json').write_text(json.dumps(manifest, indent=2), encoding='utf-8')
print(json.dumps(manifest, indent=2))
