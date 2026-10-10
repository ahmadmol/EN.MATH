"""Move bilingual UI literals to Android resource files; stable typed keys for reuse."""
from pathlib import Path
import hashlib, re, html
import xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
src=root/'app/src/main/java/com/ahmadmol/enmath'
pairs={}
en_file=root/'app/src/main/res/values/ui_strings.xml'
ar_file=root/'app/src/main/res/values-ar/ui_strings.xml'
if en_file.exists() and ar_file.exists():
    english={e.attrib['name']:e.text or '' for e in ET.parse(en_file).getroot()}
    arabic={e.attrib['name']:e.text or '' for e in ET.parse(ar_file).getroot()}
    pairs={k:(v.replace("\\'","'").replace('\\"','"'),arabic[k].replace("\\'","'").replace('\\"','"')) for k,v in english.items()}
def read_string(s,start):
    i=start+1
    while i<len(s):
        if s[i]=='\\': i+=2;continue
        if s[i:i+2]=='${':
            depth=1;i+=2
            while depth:
                if s[i]=='{':depth+=1
                elif s[i]=='}':depth-=1
                i+=1
            continue
        if s[i]=='"':return s[start+1:i],i+1
        i+=1
    raise ValueError('unclosed string')
def templates(raw):
    tokens=[];out=[];i=0
    while i<len(raw):
        if raw[i:i+2]=='${':
            start=i+2;depth=1;j=start
            while depth:
                if raw[j]=='{':depth+=1
                elif raw[j]=='}':depth-=1
                j+=1
            expr=raw[start:j-1];i=j
        elif raw[i]=='$' and re.match(r'[A-Za-z_]',raw[i+1:i+2]):
            m=re.match(r'\$([A-Za-z_]\w*)',raw[i:]);expr=m.group(1);i+=len(m.group())
        else:out.append(raw[i]);i+=1;continue
        if expr not in tokens:tokens.append(expr)
        out.append('%'+str(tokens.index(expr)+1)+'$s')
    return ''.join(out),tokens
def parse_pair(s,start):
    a,end=read_string(s,start)
    comma=re.match(r'\s*,\s*"',s[end:])
    if not comma:return None
    b,end2=read_string(s,end+comma.end()-1)
    close=re.match(r'\s*\)',s[end2:])
    if not close:return None
    return a,b,end2+close.end()
for path in src.rglob('*.kt'):
    if path.name in ('Theme.kt','TextResources.kt'):continue
    s=path.read_text(encoding='utf-8-sig');out=[];pos=0
    for match in re.finditer(r'\btr\(\s*"',s):
        if match.start()<pos:continue
        parsed=parse_pair(s,match.end()-1)
        if not parsed:continue
        a,b,end=parsed;en,tokens=templates(a);ar,ar_tokens=templates(b)
        if set(tokens)!=set(ar_tokens):continue
        for i,t in enumerate(ar_tokens):ar=ar.replace('%'+str(i+1)+'$s','@@'+str(tokens.index(t)+1)+'@@')
        ar=re.sub(r'@@(\d+)@@',r'%\1$s',ar)
        if tokens:
            en=re.sub(r'%(?!\d+\$s|%)','%%',en)
            ar=re.sub(r'%(?!\d+\$s|%)','%%',ar)
        key='t_'+hashlib.sha1((en+'|'+ar).encode()).hexdigest()[:10]
        pairs[key]=(en,ar);out.append(s[pos:match.start()]);out.append('label(TextKey.'+key+('' if not tokens else ', '+', '.join(tokens))+')');pos=end
    if pos:out.append(s[pos:]);path.write_text(''.join(out),encoding='utf-8')
path=src/'features/onboarding/OnboardingScreens.kt'
s=path.read_text(encoding='utf-8');out=[];pos=0
for match in re.finditer(r'\bCopy\(\s*"',s):
    parsed=parse_pair(s,match.end()-1)
    if not parsed:continue
    a,b,end=parsed;key='c_'+hashlib.sha1((a+'|'+b).encode()).hexdigest()[:10]
    pairs[key]=(a,b);out.append(s[pos:match.start()]);out.append('TextKey.'+key);pos=end
if pos:
    out.append(s[pos:]);s=''.join(out).replace('val title: Copy, val body: Copy','val title: TextKey, val body: TextKey');path.write_text(s,encoding='utf-8')
# UI text choices that are passed as expressions cannot be migrated automatically.
def android(value):
    if re.search(r'%\d+\$s',value):value=re.sub(r'%\d+\$s|%+',lambda m:m.group() if m.group().endswith('$s') else '%%',value)
    return html.escape(value,quote=False).replace("'","\\'").replace('"','\\"')
repairs={
 't_4a0914c00b':('%1$s / %2$s study actions','%1$s / %2$s أنشطة دراسية'),
 't_4a5c00c5b2':('%1$s / %2$s complete','%1$s / %2$s مكتمل'),
 't_570d74456a':('Average score %1$s/%2$s','متوسط العلامة %1$s/%2$s'),
 't_9e729bd400':('Students %1$s','الطلاب %1$s'),
 't_d34a495278':('%1$s submissions','%1$s تسليمات'),
 't_e5ef7b2777':('Active assignments %1$s','واجبات فعالة %1$s'),
}
pairs.update(repairs)
for locale,col in [('values',0),('values-ar',1)]:
    path=root/'app/src/main/res'/locale/'ui_strings.xml';path.parent.mkdir(exist_ok=True)
    path.write_text('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n'+''.join(f'    <string name="{k}"'+('' if re.search(r'%\d+\$s',v[col]) else ' formatted="false"')+f'>{android(v[col])}</string>\n' for k,v in sorted(pairs.items()))+'</resources>\n',encoding='utf-8')
keys='\n'.join('    '+k+'(R.string.'+k+'),' for k in sorted(pairs))
text='''package com.ahmadmol.enmath.core.design

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import com.ahmadmol.enmath.R
import com.ahmadmol.enmath.core.model.Language
import java.util.Locale

enum class TextKey(val resource:Int) {
KEYS
}
@Composable fun label(key:TextKey,vararg args:Any):String {
    val context=LocalContext.current
    val configuration=Configuration(LocalConfiguration.current)
    configuration.setLocale(Locale(if(LocalLanguage.current==Language.Arabic) "ar" else "en"))
    val resources=context.createConfigurationContext(configuration).resources
    return if(args.isEmpty()) resources.getString(key.resource) else resources.getString(key.resource,*args)
}
@Composable fun TextKey.text() = label(this)
'''.replace('KEYS',keys)
(src/'core/design/TextResources.kt').write_text(text,encoding='utf-8')
print(f'Migrated {len(pairs)} localized resource keys')
