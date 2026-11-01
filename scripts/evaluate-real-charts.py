#!/usr/bin/env python3
"""Pixel-hashed development evaluation. Source inputs and labels are fixed in the manifest."""
from pathlib import Path
import argparse,concurrent.futures,hashlib,json,shutil,struct,subprocess,urllib.request
from PIL import Image
from verified_images import validate_image
ROOT=Path(__file__).resolve().parents[1];DATA=ROOT/'benchmarks/real_charts'
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def acquire(source):
 p=ROOT/source['file']
 if not p.exists():
  request=urllib.request.Request(source['url'],headers={'User-Agent':'Mozilla/5.0 (compatible; QuantraVisionResearch/1.0)','Referer':source['source']})
  with urllib.request.urlopen(request,timeout=25) as response:raw=response.read(5_000_000)
  validate_image(raw,source)
  p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(raw)
 validate_image(p.read_bytes(),source)
 return p
def compile_engine(baseline=False):
 out=ROOT/'.local/evaluation'/('baseline' if baseline else 'candidate');out.mkdir(parents=True,exist_ok=True)
 if baseline:
  source=out/'src/ChartEngine.java';source.parent.mkdir(exist_ok=True);shutil.copyfile(ROOT/'benchmarks/baseline/ChartEngine.java.txt',source);sources=[source]
 else:sources=list((ROOT/'app/src/main/java/com/lamontlabs/quantravision/next/engine').glob('*.java'))
 subprocess.run(['java','-m','jdk.compiler/com.sun.tools.javac.Main','-d',str(out),*map(str,sources),str(ROOT/'benchmarks/ScanImage.java')],check=True)
 return out
def scan(classes,cases):
 results={}
 for mode in ['candles','trace']:
  selected=[c for c in cases if c['mode']==mode]
  run=subprocess.run(['java','-cp',str(classes),'com.lamontlabs.quantravision.benchmark.ScanImage',mode,*[str(c['_input']) for c in selected]],check=True,capture_output=True,text=True)
  lines=[json.loads(line) for line in run.stdout.splitlines() if line.strip()]
  if len(lines)!=len(selected):raise RuntimeError('Incomplete scanner output')
  results.update((c['id'],r) for c,r in zip(selected,lines))
 return results
def located(match,target,crop):
 if match['kind']!=target['pattern']:return False
 l,t,r,b=target['region'];l-=crop[0];r-=crop[0];t-=crop[1];b-=crop[1]
 box=match['box'];overlap=max(0,min(r,box[2])-max(l,box[0]));inside=sum(l-3<=x<=r+3 and t-3<=y<=b+3 for x,y in match['points'])
 return overlap>=min(r-l,box[2]-box[0])*.45 and inside>=max(1,len(match['points'])*.5)
def assess(case,result):
 expected=case.get('targets',[]);hits=sum(any(located(m,t,case['crop']) for m in result['matches']) for t in expected);alarms=[m for m in result['matches'] if m['kind'] in case.get('forbidden',[])]
 if result.get('unsupported_mode'):status='unsupported_mode'
 elif expected:status='matched' if hits==len(expected) else 'missed'
 elif case.get('negative'):status='negative_failed' if alarms or case.get('require_rejection') and result['readable'] else 'negative_pass'
 else:status='readable' if result['readable'] else 'unreadable'
 return status,hits,alarms
def main():
 parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');args=parser.parse_args();manifest=json.loads((DATA/'manifest.json').read_text());sources={s['id']:s for s in manifest['sources']}
 with concurrent.futures.ThreadPoolExecutor(6) as pool:list(pool.map(acquire,sources.values()))
 folder=ROOT/'.local/evaluation/inputs';folder.mkdir(parents=True,exist_ok=True);cases=manifest['cases']
 for case in cases:
  image=Image.open(ROOT/sources[case['source_id']]['file']).convert('RGB');crop=case['crop'];assert 0<=crop[0]<crop[2]<=image.width and 0<=crop[1]<crop[3]<=image.height
  image=image.crop(crop);case['_pixel_hash']=hashlib.sha256(struct.pack('>II',*image.size)+image.tobytes()).hexdigest();case['_input']=folder/(case['id']+'.png');image.save(case['_input'])
 baseline=scan(compile_engine(True),cases);candidate=scan(compile_engine(),cases);(ROOT/'.local/evaluation/observations.json').write_text(json.dumps(candidate,indent=2))
 counts=dict(sourceCharts=len(sources),cases=len(cases),positiveTargets=0,matchedTargets=0,missedTargets=0,negativePatternChecks=0,negativeReadChecks=0,forbiddenPatternFlags=0,falseReadableInputs=0,baselineMatchedTargets=0,baselineUnsupportedTargets=0)
 patterns={k:dict(targets=0,matched=0,missed=0) for k in manifest['patterns']};rows=[]
 for case in cases:
  result=candidate[case['id']];status,hits,alarms=assess(case,result);old_status,old_hits,_=assess(case,baseline[case['id']]);n=len(case.get('targets',[]));counts['positiveTargets']+=n;counts['matchedTargets']+=hits;counts['missedTargets']+=n-hits;counts['baselineMatchedTargets']+=old_hits;counts['baselineUnsupportedTargets']+=n if baseline[case['id']].get('unsupported_mode') else 0
  if case.get('negative'):counts['negativePatternChecks']+=int(bool(case.get('forbidden')));counts['negativeReadChecks']+=int(bool(case.get('require_rejection')));counts['forbiddenPatternFlags']+=len(alarms);counts['falseReadableInputs']+=int(bool(case.get('require_rejection')) and result['readable'])
  for target in case.get('targets',[]):
   p=patterns[target['pattern']];ok=any(located(m,target,case['crop']) for m in result['matches']);p['targets']+=1;p['matched']+=int(ok);p['missed']+=int(not ok)
  source=sources[case['source_id']];names=', '.join(m['kind'] for m in result['matches']) or 'none';outcome=(str(result['count'])+' '+('trace samples' if case['mode']=='trace' else 'candles')+'; rules observed: '+names) if result['readable'] else result.get('problem','Reader rejected this input')
  rows.append(dict(id=case['id'],title=case['title'],source_id=case['source_id'],source=source['source'],publisher=source['publisher'],status=status,mode=case['mode'],format=source['format'],outcome=outcome,target_hits=hits,target_count=n,forbidden_pattern_flags=len(alarms),baseline_status=old_status,baseline_target_hits=old_hits,input_pixel_sha256=case['_pixel_hash'],crop=case['crop'],matches=result['matches'],targets=case.get('targets',[])))
 engine_paths=sorted((ROOT/'app/src/main/java/com/lamontlabs/quantravision/next/engine').glob('*.java'));report=dict(schema=1,dataset='External chart development set 1 · restored',engine_sha256=hashlib.sha256(b''.join(p.read_bytes() for p in engine_paths)).hexdigest(),manifest_sha256=digest(DATA/'manifest.json'),limitations=manifest['limitations'],counts=counts,patterns=patterns,cases=rows)
 content=json.dumps(report,indent=2,ensure_ascii=False)+'\n';targets=[ROOT/'app/src/main/assets/evaluation/report.json',DATA/'report.json']
 for target in targets:
  if args.check:
   if target.read_text()!=content:raise RuntimeError('Evaluation changed; review every outcome and regenerate before publication')
  else:target.parent.mkdir(parents=True,exist_ok=True);target.write_text(content)
 if not args.check:
  lines=['# External chart development evaluation','',manifest['limitations'],'','| Case | Format / reader | Outcome | Baseline |','|---|---|---|---|']
  for row in rows:lines.append('| ['+row['title']+']('+row['source']+') | '+row['format']+' / '+row['mode']+' | '+row['status']+' | '+row['baseline_status']+' |')
  lines+=['','## Recorded counts','','```json',json.dumps(counts,indent=2),'```','','## Reproduce','','`python3 scripts/evaluate-real-charts.py --check`','','Requires Python 3 + Pillow and JDK 17. Pixel-hash-verified publisher inputs are fetched into an ignored local cache, not redistributed. Full/cropped cases are correlated. A matching key must overlap the frozen target region; merely finding the same rule elsewhere does not score a hit. Source bytes, crops, labels and regions are recorded in manifest.json. The baseline is the exact shipped e07dee4 engine and lacks trace mode.']
  (DATA/'REPORT.md').write_text('\n'.join(lines)+'\n')
 print(json.dumps(counts));print(json.dumps(patterns))
if __name__=='__main__':main()
