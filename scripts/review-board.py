#!/usr/bin/env python3
"""Compose untouched Android screenshots into a labeled review sheet."""
from pathlib import Path
import argparse
from PIL import Image,ImageDraw,ImageFont
p=argparse.ArgumentParser();p.add_argument('folder',type=Path);p.add_argument('output',type=Path);args=p.parse_args()
frames=[('01-overview.png','Workspace'),('03-analysis.png','Generated example · measured pixels'),('11-pattern-atlas.png','Pattern atlas'),('16-real-evaluation-image.png','ONCS · real market image')]
width=400;margin=28;top=110;bottom=66
images=[]
for name,label in frames:
 im=Image.open(args.folder/name).convert('RGB');im=im.resize((width,round(im.height*width/im.width)),Image.Resampling.LANCZOS);images.append((im,label))
height=top+max(im.height for im,_ in images)+bottom
board=Image.new('RGB',(margin*(len(images)+1)+width*len(images),height),'#0b1012');draw=ImageDraw.Draw(board)
font=ImageFont.load_default(size=17)
title=ImageFont.load_default(size=27)
draw.text((margin,20),'QuantraVision Studio / Native Android review',font=title,fill='#eeeae1')
for i,(im,label) in enumerate(images):
 x=margin+i*(width+margin);draw.text((x,top-32),label,font=font,fill='#cdb38b');board.paste(im,(x,top))
credit=ImageFont.load_default(size=13)
draw.text((margin,height-bottom+12),'ONCS image: Paulakozoolioo · CC BY-SA 4.0. Crops/app overlays added; modified image CC BY-SA 4.0.',font=credit,fill='#8b9b9d')
draw.text((margin,height-bottom+34),'Source: commons.wikimedia.org/wiki/File:Hammer_candlestick_pattern_example.png · creativecommons.org/licenses/by-sa/4.0/',font=credit,fill='#8b9b9d')
args.output.parent.mkdir(parents=True,exist_ok=True);board.save(args.output)
