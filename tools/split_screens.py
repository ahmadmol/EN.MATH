from pathlib import Path
import re
root=Path(__file__).resolve().parents[1]/'app/src/main/java/com/ahmadmol/enmath'
p=root/'features/teacher/TeacherScreens.kt'
if p.exists():
    s=p.read_text(encoding='utf-8');matches=list(re.finditer(r'@Composable fun (\w+)\(',s));header=s[:matches[0].start()]
    helper=header[header.index('private fun classAverage'):];header=header[:header.index('private fun classAverage')]
    groups={
      'TeacherDashboard':'TeacherDashboard.kt','ClassesScreen':'ClassScreens.kt','ClassDetailsScreen':'ClassScreens.kt','StudentDetailsScreen':'ClassScreens.kt',
      'TeacherAssignmentsScreen':'TeacherAssignmentScreens.kt','TeacherResultsScreen':'TeacherAssignmentScreens.kt','AssignmentBuilderScreen':'AssignmentBuilderScreen.kt','TeacherResourcesScreen':'TeacherResourcesScreen.kt'}
    assert set(m.group(1) for m in matches)==set(groups)
    outputs={}
    for i,m in enumerate(matches):outputs.setdefault(groups[m.group(1)],[]).append(s[m.start():matches[i+1].start() if i+1<len(matches) else len(s)])
    for name,parts in outputs.items():(p.parent/name).write_text(header+'\n'.join(parts),encoding='utf-8')
    (p.parent/'TeacherSelectors.kt').write_text('package com.ahmadmol.enmath.features.teacher\nimport com.ahmadmol.enmath.core.data.*\nimport com.ahmadmol.enmath.core.model.*\n'+helper.replace('private fun','internal fun'),encoding='utf-8')
    p.unlink()
p=root/'features/home/StudentHubScreens.kt'
if p.exists():
    s=p.read_text(encoding='utf-8');first=s.index('@Composable fun StudentHomeScreen');second=s.index('@Composable fun ProductProgressScreen');header=s[:first]
    (p.parent/'StudentHomeScreen.kt').write_text(header+s[first:second],encoding='utf-8')
    progress=root/'features/progress';progress.mkdir(exist_ok=True)
    (progress/'ProductProgressScreen.kt').write_text(header.replace('features.home','features.progress')+s[second:],encoding='utf-8');p.unlink()
p=root/'core/navigation/ProductGraphs.kt';s=p.read_text(encoding='utf-8').replace('import com.ahmadmol.enmath.features.home.*','import com.ahmadmol.enmath.features.home.*\nimport com.ahmadmol.enmath.features.progress.*');p.write_text(s,encoding='utf-8')
print('Screens split by feature responsibility')
