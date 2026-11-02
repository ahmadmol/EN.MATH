"""Catalog actual Layoutlib PNGs and make clearly labeled visual-QA contact sheets."""
import argparse
from collections import defaultdict
import hashlib
import html
import json
import os
from pathlib import Path
import re
from PIL import Image, ImageChops, ImageDraw, ImageStat

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("input", type=Path); p.add_argument("output", type=Path)
    p.add_argument("--screens", type=int, default=15)
    a = p.parse_args(); a.output.mkdir(parents=True, exist_ok=True)
    pattern = re.compile(r"([^_]+)_(360|393) (ar|en) (light|dark) (100|150|200)pct_[a-f0-9]+_0\.png")
    rows, groups = [], defaultdict(list)
    for file in sorted(a.input.rglob("*.png")):
        match = pattern.fullmatch(file.name)
        if not match: raise ValueError("Unexpected/colliding preview name: " + file.name)
        screen, width, language, theme, scale = match.groups()
        with Image.open(file) as image:
            rgb = image.convert("RGB")
            difference = ImageChops.difference(rgb, Image.new("RGB", rgb.size, rgb.getpixel((0, 0))))
            if sum(ImageStat.Stat(difference).mean) < .1: raise ValueError("Blank preview: " + file.name)
            row = dict(screen=screen, widthDp=int(width), language=language, theme=theme, fontPercent=int(scale),
                pixels=list(image.size), sha256=hashlib.sha256(file.read_bytes()).hexdigest(),
                path=str(file.resolve()), blank=False)
            rows.append(row); groups[(width, language, theme, scale)].append(row)
    if len(rows) != a.screens * 24 or len({(r['screen'],r['widthDp'],r['language'],r['theme'],r['fontPercent']) for r in rows}) != a.screens * 24:
        raise ValueError("Expected the requested number of actual screens/states × 24 distinct configurations")
    cards = []
    for key, cases in sorted(groups.items()):
        sheet = Image.new("RGB", (1300, 32 + ((a.screens+4)//5)*626), "#e5eaed")
        draw = ImageDraw.Draw(sheet)
        draw.text((12, 8), "ACTUAL COMPOSE RENDERS | " + " / ".join(key) + " | scrollable viewports", fill="black")
        for i, row in enumerate(sorted(cases, key=lambda r:r['screen'])):
            x, y = (i % 5) * 260, 32 + (i // 5) * 626
            draw.text((x+8,y+3),row['screen'],fill="black")
            with Image.open(row['path']) as image:
                image.thumbnail((252, 594))
                sheet.paste(image.convert("RGB"), (x+4,y+24))
        name = "contact-"+"-".join(key)+".jpg"
        sheet.save(a.output/name, quality=92)
        cards.append(f'<h2>{html.escape(" / ".join(key))}</h2><img class="sheet" src="{name}">')
    (a.output/'manifest.json').write_text(json.dumps(dict(rendered=len(rows),configurations=len(groups),screens=a.screens,cases=rows),ensure_ascii=False,indent=2),encoding='utf-8')
    links = []
    for row in rows:
        relative = os.path.relpath(row['path'], a.output).replace(os.sep, '/')
        label = f"{row['screen']} / {row['widthDp']}dp / {row['language']} / {row['theme']} / {row['fontPercent']}%"
        links.append(f'<li><a href="{html.escape(relative,quote=True)}">{html.escape(label)}</a></li>')
    (a.output/'index.html').write_text('<!doctype html><meta charset="utf-8"><title>Grade 9 actual Compose renders</title><style>body{font:16px system-ui;margin:24px;background:#f4faf9;color:#153631}.sheet{max-width:100%;height:auto}a{color:#006e6b}</style><h1>Actual Compose previews</h1><p>Generated candidates, not automatic visual approval or device QA. Contact sheets show scrollable viewports; links below open original PNGs.</p>'+''.join(cards)+'<h2>Original PNGs</h2><ul>'+''.join(links)+'</ul>',encoding='utf-8')
    print(json.dumps(dict(rendered=len(rows),configurations=len(groups),contactSheets=len(groups),blank=0)))

if __name__ == '__main__': main()
