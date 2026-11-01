#!/usr/bin/env python3
"""Small SDK-only compile for fast iteration; GitHub also runs the Gradle/lint/device gates."""
import os,sys,subprocess,zipfile,shutil
import xml.etree.ElementTree as ET
from pathlib import Path
root=Path(__file__).resolve().parents[1]
sdk=Path(os.environ.get('ANDROID_HOME') or os.environ.get('ANDROID_SDK_ROOT') or Path.home()/'Android/Sdk')
tools=sdk/'build-tools/35.0.1';android=sdk/'platforms/android-35/android.jar'
out=root/'.local/android';out.mkdir(parents=True,exist_ok=True)
for folder in ['gen','classes','dex']:
 p=out/folder
 if p.exists():shutil.rmtree(p)
 p.mkdir()
manifest=ET.parse(root/'app/src/main/AndroidManifest.xml');manifest.getroot().set('package','com.lamontlabs.quantravision.studio')
for node in manifest.iter():
 for key,value in list(node.attrib.items()):
  if key=='{http://schemas.android.com/apk/res/android}name' and value.startswith('.'):value='com.lamontlabs.quantravision.next'+value
  node.set(key,value.replace('${applicationId}','com.lamontlabs.quantravision.studio'))
manifest.write(out/'AndroidManifest.xml',encoding='utf-8',xml_declaration=True)
def run(args):subprocess.run(list(map(str,args)),check=True)
run([sys.executable,root/'scripts/prepare-assets.py'])
run([tools/'aapt2','compile','--dir',root/'app/src/main/res','-o',out/'res.zip'])
run([tools/'aapt2','link','-I',android,'--manifest',out/'AndroidManifest.xml','--min-sdk-version','26','--target-sdk-version','35','--version-code','40101','--version-name','4.1.0-preview.2','-A',root/'app/src/main/assets','--custom-package','com.lamontlabs.quantravision.next','--java',out/'gen','-o',out/'unsigned.apk',out/'res.zip'])
sources=list((root/'app/src/main/java').rglob('*.java'))+list((out/'gen').rglob('*.java'))
run(['java','-m','jdk.compiler/com.sun.tools.javac.Main','-source','8','-target','8','-classpath',android,'-d',out/'classes']+sources)
run(['java','-cp',tools/'lib/d8.jar','com.android.tools.r8.D8','--min-api','26','--lib',android,'--output',out/'dex']+list((out/'classes').rglob('*.class')))
with zipfile.ZipFile(out/'unsigned.apk','a') as apk:
 for dex in (out/'dex').glob('*.dex'):apk.write(dex,dex.name)
run([tools/'zipalign','-f','4',out/'unsigned.apk',out/'QuantraVision-v4-unsigned.apk'])
print('Compiled APK:',out/'QuantraVision-v4-unsigned.apk')
