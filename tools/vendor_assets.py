from pathlib import Path
import urllib.request, re, hashlib, json
root=Path(__file__).resolve().parents[1]
asset=root/'app/src/main/assets/katex';asset.mkdir(parents=True,exist_ok=True)
fonts=root/'app/src/main/res/font';fonts.mkdir(exist_ok=True)
licenses=root/'docs/licenses';licenses.mkdir(parents=True,exist_ok=True)
records=[]
def download(url,path):
    if not path.exists():
        with urllib.request.urlopen(url,timeout=45) as response:data=response.read()
        path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(data)
    records.append({'path':str(path.relative_to(root)),'url':url,'sha256':hashlib.sha256(path.read_bytes()).hexdigest()})
base='https://cdn.jsdelivr.net/npm/katex@0.16.22/'
download(base+'dist/katex.min.js',asset/'katex.min.js')
download(base+'dist/katex.min.css',asset/'katex.min.css')
css=(asset/'katex.min.css').read_text()
for name in set(re.findall(r'fonts/([^\)]+\.woff2)',css)):
    download(base+'dist/fonts/'+name,asset/'fonts'/name)
# Only bundled woff2; remove fallback URLs so offline rendering has no missing requests.
css=re.sub(r'url\(fonts/[^)]+\.(?:woff|ttf)\) format\("[^"]+"\),?', '',css).replace(',;', ';').replace(',}', '}')
(asset/'katex.min.css').write_text(css)
download(base+'LICENSE',licenses/'KaTeX-MIT.txt')
google='https://raw.githubusercontent.com/google/fonts/main/ofl/'
download(google+'ibmplexsansarabic/IBMPlexSansArabic-Regular.ttf',fonts/'ibm_plex_arabic.ttf')
download(google+'ibmplexsansarabic/OFL.txt',licenses/'IBM-Plex-OFL.txt')
download(google+'inter/Inter%5Bopsz,wght%5D.ttf',fonts/'inter.ttf')
download(google+'inter/OFL.txt',licenses/'Inter-OFL.txt')
(root/'docs/vendor-assets.json').write_text(json.dumps(records,indent=2))
print('Vendored',len(records),'offline assets')
