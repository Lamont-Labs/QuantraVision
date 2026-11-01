#!/usr/bin/env python3
"""Checksum-pinned build inputs. The installed app has no network permission."""
from pathlib import Path
import concurrent.futures,hashlib,urllib.request
ROOT=Path(__file__).resolve().parents[1]
BASE='https://raw.githubusercontent.com/google/fonts/6c1dbe27dcf7d817d4b62cec033ba77fa8e2a5ec/'
ASSETS=[
 ('app/src/main/res/font/manrope.ttf',BASE+'ofl/manrope/Manrope%5Bwght%5D.ttf','3ae11c49db0455a3cc33e37d380f20fdb8c7f8b41dc07625c177e3d87a9d6ae6'),
 ('app/src/main/assets/fonts/InstrumentSerif.ttf',BASE+'ofl/instrumentserif/InstrumentSerif-Regular.ttf','498efd461f6ddfcb7a111bf9a565709d2085d48201d501ead960d93e84ffbb88'),
 ('app/src/main/assets/fonts/PlexMono.ttf',BASE+'ofl/ibmplexmono/IBMPlexMono-Regular.ttf','6a3412f058c7d8dfd9170c41e85ade48e5156ecb89356110ca57a0a27734af46')]
def fetch(item):
 name,url,expected=item;p=ROOT/name
 if p.exists() and hashlib.sha256(p.read_bytes()).hexdigest()==expected:return name
 request=urllib.request.Request(url,headers={'User-Agent':'Mozilla/5.0 QuantraVisionBuild/1.0'})
 with urllib.request.urlopen(request,timeout=25) as response:data=response.read(2_000_000)
 if hashlib.sha256(data).hexdigest()!=expected:raise RuntimeError('Asset checksum mismatch: '+name)
 p.parent.mkdir(parents=True,exist_ok=True);tmp=p.with_suffix('.download');tmp.write_bytes(data);tmp.replace(p)
 return name
if __name__=='__main__':
 for name in concurrent.futures.ThreadPoolExecutor(3).map(fetch,ASSETS):print('Verified:',name)
