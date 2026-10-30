import re, subprocess, sys
subprocess.run(['adb', 'shell', 'uiautomator', 'dump', '/sdcard/ui_seek.xml'], capture_output=True)
xml = subprocess.run(['adb', 'exec-out', 'cat', '/sdcard/ui_seek.xml'], capture_output=True).stdout.decode('utf-8', 'ignore')
tag = None
for t in re.findall(r'<node\b[^>]*>', xml):
    if 'class="android.widget.SeekBar"' in t:
        tag = t
        break
if not tag:
    print('NO SEEKBAR')
    sys.exit(1)
x1, y1, x2, y2 = map(int, re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', tag).groups())
cy = (y1 + y2) // 2
sx = x1 + int((x2 - x1) * 0.40)
ex = x1 + int((x2 - x1) * 0.75)
subprocess.run(['adb', 'shell', 'input', 'swipe', str(sx), str(cy), str(ex), str(cy), '300'], check=True)
print('DRAGGED seekbar', (x1, y1, x2, y2), 'from', sx, 'to', ex)
