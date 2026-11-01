#!/usr/bin/env python3
"""Compose untouched Android screenshots into a labeled review sheet."""
from pathlib import Path
import argparse
from PIL import Image,ImageDraw,ImageFont
p=argparse.ArgumentParser();p.add_argument('folder',type=Path);p.add_argument('output',type=Path);args=p.parse_args()
frames=[('01-overview.png','Workspace'),('03-analysis.png','Generated example · measured pixels'),('11-pattern-atlas.png','Pattern atlas')]
width=480;margin=32;top=110;bottom=32
images=[]
for name,label in frames:
 im=Image.open(args.folder/name).convert('RGB');im=im.resize((width,round(im.height*width/im.width)),Image.Resampling.LANCZOS);images.append((im,label))
height=top+max(im.height for im,_ in images)+bottom
board=Image.new('RGB',(margin*4+width*3,height),'#0b1012');draw=ImageDraw.Draw(board)
font=ImageFont.load_default(size=17)
title=ImageFont.load_default(size=27)
draw.text((margin,20),'QuantraVision Studio / Native Android review',font=title,fill='#eeeae1')
for i,(im,label) in enumerate(images):
 x=margin+i*(width+margin);draw.text((x,top-32),label,font=font,fill='#cdb38b');board.paste(im,(x,top))
args.output.parent.mkdir(parents=True,exist_ok=True);board.save(args.output)
