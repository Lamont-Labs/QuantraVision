#!/usr/bin/env python3
"""Run the pure image engine locally without Android or Gradle dependencies."""
from pathlib import Path
import subprocess
root=Path(__file__).resolve().parents[1]
out=root/'.local/core-tests';out.mkdir(parents=True,exist_ok=True)
sources=list((root/'app/src/main/java/com/lamontlabs/quantravision/next/engine').glob('*.java'))
sources.append(root/'app/src/test/java/com/lamontlabs/quantravision/next/engine/EngineChecks.java')
subprocess.run(['java','-m','jdk.compiler/com.sun.tools.javac.Main','-d',str(out)]+list(map(str,sources)),check=True)
subprocess.run(['java','-cp',str(out),'com.lamontlabs.quantravision.next.engine.EngineChecks'],check=True)
