"""Catalog actual Compose PNGs, create review contact sheets and a local image gallery."""
import argparse
from collections import defaultdict
import hashlib
import html
import json
from pathlib import Path
import re
import shutil
from PIL import Image, ImageDraw

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('source',type=Path);p.add_argument('output',type=Path)
    args=p.parse_args();args.output.mkdir(parents=True,exist_ok=True)
    originals=args.output/'screens';originals.mkdir(exist_ok=True)
    contacts=args.output/'contacts';contacts.mkdir(exist_ok=True)
    pattern=re.compile(r'(ModernG9Home|ModernBacHome|ModernTools|ModernProfile)_(360|393|800)_(ar|en)_(light|dark)_(100|150|200)_[0-9a-f]+_0.png')
    rows=[];groups=defaultdict(list)
    for file in sorted(args.source.glob('*.png')):
        match=pattern.fullmatch(file.name)
        if not match:continue
        scene,width,language,theme,font=match.groups();name=f'{scene}-{width}-{language}-{theme}-{font}.png'
        target=originals/name;shutil.copyfile(file,target)
        with Image.open(target) as image: size=image.size
        row=dict(scene=scene,width=int(width),language=language,theme=theme,font=int(font),pixels=size,
            sha256=hashlib.sha256(target.read_bytes()).hexdigest(),path='screens/'+name)
        rows.append(row);groups[(width,language,theme)].append(row)
    assert len(rows)==144 and len({(r['scene'],r['width'],r['language'],r['theme'],r['font']) for r in rows})==144
    contact_names=[]
    for (width,language,theme),cases in sorted(groups.items()):
        w=230;h=round(2200*w/int(width));gap=8;header=28
        sheet=Image.new('RGB',(4*(w+gap)+gap,3*(h+header+gap)+header),'#dce3eb');draw=ImageDraw.Draw(sheet)
        draw.text((8,6),f'ACTUAL COMPOSE FIXTURES | {width}dp / {language} / {theme} | fonts 100 / 150 / 200%',fill='#101c32')
        for r in cases:
            col=['ModernG9Home','ModernBacHome','ModernTools','ModernProfile'].index(r['scene']);row=[100,150,200].index(r['font'])
            x=gap+col*(w+gap);y=header+row*(h+header+gap)
            draw.text((x,y+3),f"{r['scene']} / {r['font']}%",fill='#101c32')
            with Image.open(args.output/r['path']) as image:
                image=image.convert('RGB');image.thumbnail((w,h));sheet.paste(image,(x,y+header))
        name=f'{width}-{language}-{theme}.jpg';sheet.save(contacts/name,quality=94);contact_names.append('contacts/'+name)
    (args.output/'manifest.json').write_text(json.dumps(dict(images=len(rows),contact_sheets=contact_names,cases=rows),ensure_ascii=False,indent=2),encoding='utf-8')
    cards=[]
    for r in rows:
        label=f"{r['scene']} · {r['width']}dp · {r['language']} · {r['theme']} · {r['font']}%"
        cards.append(f'<article data-search="{html.escape(label.lower())}"><h2>{html.escape(label)}</h2><a href="{r["path"]}"><img loading="lazy" src="{r["path"]}" alt="{html.escape(label)}"></a></article>')
    page='''<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>EN.MATH actual Compose previews</title>
<style>body{margin:24px;background:#f4f7fb;color:#172339;font:16px system-ui}header{max-width:960px;margin-bottom:24px}input{padding:14px;border:1px solid #aabacc;border-radius:12px;width:min(90%,600px)}main{display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:16px}article{border:1px solid #d1dce9;border-radius:18px;background:white;padding:12px}h2{font-size:15px}img{width:100%;height:auto}a{color:#344bb2}</style>
<header><h1>EN.MATH · actual Compose previews</h1><p>144 renders of production components with isolated preview data. These are scrollable viewports, not phone screenshots or proof of device behavior. Temporary companion portraits are intentional. Wallet storage is not opened in previews, so the fixture keeps its loading state.</p><p>Filter by scene, width, ar/en, light/dark or font percentage. Open an image for the original resolution.</p><input id="filter" placeholder="e.g. ModernG9Home 360 ar dark 200" aria-label="Filter previews"></header><main>'''+''.join(cards)+'''</main><script>document.getElementById('filter').addEventListener('input',e=>{let words=e.target.value.toLowerCase().split(/\s+/).filter(Boolean);document.querySelectorAll('article').forEach(a=>a.hidden=!words.every(w=>a.dataset.search.includes(w)))})</script></html>'''
    (args.output/'index.html').write_text(page,encoding='utf-8')
    print(json.dumps(dict(images=len(rows),contacts=len(contact_names),bytes=sum((args.output/r['path']).stat().st_size for r in rows))))

if __name__=='__main__':main()
