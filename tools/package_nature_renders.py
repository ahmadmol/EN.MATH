"""Package unchanged Layoutlib PNGs plus labeled QA contact sheets; no synthetic UI."""
import argparse
import hashlib
import html
import json
from pathlib import Path
import re
import shutil
from PIL import Image, ImageChops, ImageDraw


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("references", type=Path)
    parser.add_argument("output", type=Path)
    args = parser.parse_args()
    raw = args.output / "renders"
    sheets = args.output / "contact-sheets"
    raw.mkdir(parents=True, exist_ok=True)
    sheets.mkdir(parents=True, exist_ok=True)
    records = []
    pattern = r"Nature_((?:Q0[1-6]_(?:question|correct|wrong|hint)|Results)_(?:Dark|Light)_(?:150|200))"
    for source in args.references.rglob("*.png"):
        match = re.search(pattern, str(source))
        if not match:
            continue
        name = match[1]
        target = raw / (name + ".png")
        shutil.copy2(source, target)
        with Image.open(target) as picture:
            assert picture.width >= 360, (name, picture.size)
            records.append({"name": name, "file": str(target), "width": picture.width,
                            "height": picture.height, "width_dp": 360, "density": picture.width / 360,
                            "sha256": hashlib.sha256(target.read_bytes()).hexdigest()})
    assert len(records) == len({row["name"] for row in records}) == 100, len(records)

    def content(name):
        picture = Image.open(raw / (name + ".png")).convert("RGB")
        background = Image.new("RGB", picture.size, picture.getpixel((0, picture.height - 1)))
        bounds = ImageChops.difference(picture, background).getbbox()
        picture = picture.crop((0, 0, picture.width, min(picture.height, (bounds[3] if bounds else picture.height) + 20)))
        return picture.resize((360, round(picture.height * 360 / picture.width)), Image.Resampling.LANCZOS)

    for q in range(1, 7):
        for theme in ("Light", "Dark"):
            for scale in (150, 200):
                names = [f"Q0{q}_{scene}_{theme}_{scale}" for scene in ("question", "correct", "wrong", "hint")]
                pictures = [content(name) for name in names]
                height = max(p.height for p in pictures)
                for part, top in enumerate(range(0, height, 1500), 1):
                    page_height = min(1500, height - top)
                    canvas = Image.new("RGB", (4 * 370, page_height + 42), "#dddddd")
                    draw = ImageDraw.Draw(canvas)
                    for col, (name, picture) in enumerate(zip(names, pictures)):
                        draw.text((col * 370 + 5, 8), f"{name} / y={top}", fill="black")
                        if top < picture.height:
                            canvas.paste(picture.crop((0, top, 360, min(top + page_height, picture.height))), (col * 370 + 5, 36))
                    canvas.save(sheets / f"Q0{q}_{theme}_{scale}_part{part}.png")
    result_names = [f"Results_{theme}_{scale}" for theme in ("Light", "Dark") for scale in (150, 200)]
    result_pictures = [content(name) for name in result_names]
    canvas = Image.new("RGB", (4 * 370, max(p.height for p in result_pictures) + 42), "#dddddd")
    draw = ImageDraw.Draw(canvas)
    for col, (name, picture) in enumerate(zip(result_names, result_pictures)):
        draw.text((col * 370 + 5, 8), name, fill="black")
        canvas.paste(picture, (col * 370 + 5, 36))
    canvas.save(sheets / "Results_all.png")
    (args.output / "render-manifest.json").write_text(json.dumps(records, ensure_ascii=False, indent=2), encoding="utf-8")
    lines = ["# Nature Practice — real Compose / Layoutlib captures", "",
             "Arabic, 360dp, Light/Dark, 150%/200%. Tall content captures; runtime/device scrolling is a separate test.", "",
             "Raw PNG files are unchanged renderer output. Contact sheets crop blank tails, scale to 360 pixels, and label slices for review.", ""]
    lines += [f"- [{row['name']}](renders/{row['name']}.png)" for row in sorted(records, key=lambda r: r["name"])]
    (args.output / "SCREENSHOTS.md").write_text("\n".join(lines) + "\n", encoding="utf-8")
    cards = "\n".join(
        f'<article data-name="{row["name"]}"><h2>{html.escape(row["name"])}</h2>'
        f'<a href="renders/{row["name"]}.png"><img loading="lazy" src="renders/{row["name"]}.png" '
        f'alt="{html.escape(row["name"])}"></a></article>' for row in sorted(records, key=lambda r: r["name"]))
    page = '''<!doctype html><html lang="ar" dir="rtl"><meta charset="utf-8">
<title>EN.MATH — Compose QA</title><style>
body{font-family:system-ui;background:#eef3f3;color:#142c30;margin:24px}h1{margin-bottom:8px}
header{position:sticky;top:0;background:#eef3f3;padding:12px 0;z-index:1}select{font:inherit;padding:8px;margin:4px}
main{display:grid;grid-template-columns:repeat(auto-fit,minmax(300px,1fr));gap:18px}article{background:white;padding:12px;border-radius:12px}
h2{font-size:15px;direction:ltr}img{width:100%;max-height:600px;object-fit:contain;object-position:top}article[hidden]{display:none}
</style><header><h1>صور Compose الفعلية — طبيعة الأعداد</h1>
<p>100 صورة Layoutlib بالعربية، بعرض 360dp. اللقطات الطويلة تعرض المحتوى الكامل. اختبار التمرير منفصل عبر Robolectric.</p>
<p>اضغط الصورة لفتح ملف PNG الأصلي.</p><label>السؤال <select id="question"><option value="">الكل</option>
<option>Q01</option><option>Q02</option><option>Q03</option><option>Q04</option><option>Q05</option><option>Q06</option><option>Results</option></select></label>
<label>الثيم <select id="theme"><option value="">الكل</option><option>Light</option><option>Dark</option></select></label>
<label>الخط <select id="scale"><option value="">الكل</option><option>150</option><option>200</option></select></label></header><main>'''
    page += cards + '''</main><script>
const filters=['question','theme','scale'].map(id=>document.getElementById(id));
function filter(){document.querySelectorAll('article').forEach(card=>card.hidden=!filters.every(f=>!f.value||card.dataset.name.includes(f.value)))}
filters.forEach(f=>f.addEventListener('change',filter));</script></html>'''
    (args.output / "index.html").write_text(page, encoding="utf-8")
    print(json.dumps({"renders": len(records), "contact_sheets": len(list(sheets.glob('*.png'))),
                      "path": str(args.output)}, ensure_ascii=False))


if __name__ == "__main__":
    main()
