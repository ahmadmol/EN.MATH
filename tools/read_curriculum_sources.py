"""Verify the four local PDFs and OCR scans into a private cache outside the repository.

Only source provenance and page audit metadata are written to docs; textbook text/images stay local.
"""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess
from pypdf import PdfReader

ROOT = Path(__file__).resolve().parents[1]
CACHE = ROOT.parent / 'curriculum-source-cache'
DOC = ROOT / 'docs/curriculum/education'
RUNTIME = Path.home() / '.cache/codex-runtimes/codex-primary-runtime/dependencies'
POPLER = RUNTIME / 'native/poppler/Library/bin/pdftoppm.exe'

OCR = r'''
Add-Type -AssemblyName System.Runtime.WindowsRuntime
[Windows.Storage.StorageFile,Windows.Storage,ContentType=WindowsRuntime] > $null
[Windows.Graphics.Imaging.BitmapDecoder,Windows.Graphics.Imaging,ContentType=WindowsRuntime] > $null
[Windows.Media.Ocr.OcrEngine,Windows.Foundation,ContentType=WindowsRuntime] > $null
[Windows.Globalization.Language,Windows.Globalization,ContentType=WindowsRuntime] > $null
$asTask = [System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.IsGenericMethod -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1' } | Select-Object -First 1
function Await($operation, $type) {
    $task = $asTask.MakeGenericMethod($type).Invoke($null, @($operation))
    $task.Wait()
    $task.Result
}
$engine = [Windows.Media.Ocr.OcrEngine]::TryCreateFromLanguage([Windows.Globalization.Language]::new('ar-SA'))
if ($null -eq $engine) { throw 'Arabic OCR is not available' }
$directory = [Environment]::GetEnvironmentVariable('ENMATH_OCR_CACHE')
Get-ChildItem -LiteralPath $directory -Filter '*.png' -Recurse | ForEach-Object {
    $output = $_.FullName + '.ocr.json'
    if (-not (Test-Path -LiteralPath $output)) {
        $file = Await ([Windows.Storage.StorageFile]::GetFileFromPathAsync($_.FullName)) ([Windows.Storage.StorageFile])
        $stream = Await ($file.OpenAsync([Windows.Storage.FileAccessMode]::Read)) ([Windows.Storage.Streams.IRandomAccessStream])
        $decoder = Await ([Windows.Graphics.Imaging.BitmapDecoder]::CreateAsync($stream)) ([Windows.Graphics.Imaging.BitmapDecoder])
        $bitmap = Await ($decoder.GetSoftwareBitmapAsync()) ([Windows.Graphics.Imaging.SoftwareBitmap])
        $result = Await ($engine.RecognizeAsync($bitmap)) ([Windows.Media.Ocr.OcrResult])
        $lines = @($result.Lines | ForEach-Object {
            @{ text=$_.Text; words=@($_.Words | ForEach-Object { @{ text=$_.Text; x=$_.BoundingRect.X; y=$_.BoundingRect.Y; width=$_.BoundingRect.Width; height=$_.BoundingRect.Height } }) }
        })
        @{ text=$result.Text; lines=$lines; engine='Windows.Media.Ocr/ar-SA'; verified=$false } | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $output -Encoding UTF8
        $bitmap.Dispose(); $stream.Dispose()
    }
}
'''


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--ocr', action='store_true')
    args = parser.parse_args()
    DOC.mkdir(parents=True, exist_ok=True)
    g9 = json.loads((ROOT / 'docs/curriculum/g9/source/MANIFEST.json').read_bytes())['books']
    bac = json.loads((ROOT / 'docs/curriculum/bac/source/BAC_CURRICULUM_CATALOG.json').read_bytes())['sources']
    sources = [(k,v['fileName'],v['sha256'],v['pages'],'2025-2026',0) for k,v in g9.items()]
    sources += [(s['id'], 'Sci-Math-1.pdf' if s['volume']==1 else 'زميلك - الرياضيات الجزء الثاني.pdf',
        s['sha256'],s['pdf_page_count'],s['cover_edition_label'],1 if s['volume']==1 else 0) for s in bac]
    evidence=[]
    for sid,name,sha,count,edition,offset in sources:
        pdf = Path.home() / 'Downloads' / name
        if not pdf.is_file():
            evidence.append(dict(sourceId=sid,status='MISSING',requiredPath=str(pdf)))
            continue
        actual=hashlib.sha256(pdf.read_bytes()).hexdigest()
        assert actual==sha, f'Source mismatch: {sid}'
        reader=PdfReader(pdf)
        assert len(reader.pages)==count
        bookdir=CACHE/sid
        bookdir.mkdir(parents=True,exist_ok=True)
        if len(list(bookdir.glob('page-*.png')))!=count:
            subprocess.run([str(POPLER),'-scale-to','1600','-png',str(pdf),str(bookdir/'page')],check=True)
        evidence.append(dict(sourceId=sid,fileName=name,sha256=actual,pdfPages=count,editionOnSource=edition,
            printedToPdfOffset=offset,status='HASH_AND_PAGE_COUNT_VERIFIED',edition2026_2027='NOT_VERIFIED',
            scanTextExtraction='SCANNED_IMAGE_REQUIRES_OCR_AND_VISUAL_REVIEW',fullPedagogicalReview=False))
        print(json.dumps(evidence[-1],ensure_ascii=False),flush=True)
    (DOC/'SOURCE_PROVENANCE.json').write_text(json.dumps(dict(sources=evidence,cacheExcludedFromRepository=True),ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    assert all(s['status']!='MISSING' for s in evidence), 'Required book missing; see SOURCE_PROVENANCE.json'
    if args.ocr:
        import os
        env=dict(os.environ,ENMATH_OCR_CACHE=str(CACHE.resolve()))
        subprocess.run(['C:/Windows/System32/WindowsPowerShell/v1.0/powershell.exe','-NoProfile','-Command',OCR],env=env,check=True)
        print('OCR cached pages:',len(list(CACHE.rglob('*.ocr.json'))),flush=True)


if __name__=='__main__':
    main()
