"""Optional public Maven mirror for a host whose Gradle JVM cannot resolve Maven DNS.
Downloads only Google Maven/Maven Central artifacts; validates published SHA-1 before use.
No app/device state, Gradle caches or repository security settings are modified.
"""
import argparse
from concurrent.futures import ThreadPoolExecutor
import hashlib
from pathlib import Path
import re
import shutil
import urllib.request

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("log", type=Path); p.add_argument("mirror", type=Path)
    a = p.parse_args()
    bases = ("https://dl.google.com/dl/android/maven2/", "https://repo.maven.apache.org/maven2/")
    urls = sorted(set(re.findall(r"https://[^\s']+\.jar", a.log.read_text(encoding="utf-8-sig"))))
    def fetch(url):
        base = next((b for b in bases if url.startswith(b)), None)
        if base is None: raise ValueError("Unapproved Maven host")
        relative = url[len(base):]
        parts = relative.split("/"); artifact, version = parts[-3:-1]; group = ".".join(parts[:-3])
        target = a.mirror / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        cached = Path.home() / ".gradle/caches/modules-2/files-2.1" / group / artifact / version
        for file in cached.glob("*/*"):
            if file.suffix in (".pom", ".module", ".jar"):
                shutil.copy2(file, target.parent / file.name)
        with urllib.request.urlopen(url + ".sha1", timeout=30) as response:
            wanted = response.read().decode().strip().split()[0].lower()
        if not re.fullmatch("[a-f0-9]{40}", wanted): raise ValueError("Invalid Maven checksum")
        if not target.is_file() or hashlib.sha1(target.read_bytes()).hexdigest() != wanted:
            temporary = target.with_suffix(".download")
            with urllib.request.urlopen(urllib.request.Request(url, method="HEAD"), timeout=25) as response:
                total = int(response.headers["Content-Length"])
            # Small explicit byte ranges survive proxies closing a large download early.
            attempts = 0
            while not temporary.exists() or temporary.stat().st_size < total:
                start = temporary.stat().st_size if temporary.exists() else 0
                request = urllib.request.Request(url, headers={"Range": f"bytes={start}-{min(total-1,start+4*1024*1024-1)}"})
                attempts += 1
                if attempts > 100: raise ValueError("Incomplete Maven download: " + target.name)
                try:
                    with urllib.request.urlopen(request, timeout=25) as response:
                        if start and (response.status != 206 or not response.headers.get("Content-Range", "").startswith(f"bytes {start}-")):
                            raise ValueError("Server did not honor the resume range")
                        with temporary.open("ab") as output:
                            while chunk := response.read(256 * 1024): output.write(chunk)
                except (TimeoutError, OSError):
                    continue
            if temporary.stat().st_size != total or hashlib.sha1(temporary.read_bytes()).hexdigest() != wanted:
                raise ValueError("Maven checksum mismatch: " + target.name)
            temporary.replace(target)
        print("VERIFIED " + target.name, flush=True)
    with ThreadPoolExecutor(max_workers=4) as pool:
        list(pool.map(fetch, urls))
    print(f"Verified {len(urls)} public artifacts; use -Pg9RenderRepo={a.mirror}", flush=True)

if __name__ == "__main__": main()
