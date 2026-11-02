import re, subprocess, sys, time
frac = float(sys.argv[1]); dur = float(sys.argv[2]) if len(sys.argv) > 2 else 180000.0
def pos():
    out = subprocess.run(['adb','shell','dumpsys','media_session'],capture_output=True).stdout.decode('utf-8','ignore')
    m = re.search(r'com\.nuvio\.app.*?state=PlaybackState \{state=(?:\w+\()?\d+\)?, position=(\d+)', out, re.S)
    return int(m.group(1)) if m else 0
tag = None
for attempt in range(4):
    subprocess.run(['adb','shell','uiautomator','dump','/sdcard/ui_seek.xml'],capture_output=True)
    xml = subprocess.run(['adb','exec-out','cat','/sdcard/ui_seek.xml'],capture_output=True).stdout.decode('utf-8','ignore')
    tag = next((t for t in re.findall(r'<node\b[^>]*>', xml) if 'class="android.widget.SeekBar"' in t), None)
    if tag: break
    subprocess.run(['adb','shell','input','tap','1200','540']); time.sleep(1.2)
if not tag:
    print('NO SEEKBAR'); sys.exit(1)
x1,y1,x2,y2 = map(int, re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', tag).groups())
cy=(y1+y2)//2
p0=pos()
sx = x1 + int((x2-x1)*min(max(p0/dur,0.0),1.0))
ex = x1 + int((x2-x1)*frac)
subprocess.run(['adb','shell','input','swipe',str(sx),str(cy),str(ex),str(cy),'600'],check=True)
print('DRAGGED seekbar from', sx, 'to', ex, 'p0_ms', p0, 'target_ms', int(frac*dur))
