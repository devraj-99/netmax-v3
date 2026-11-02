import re, subprocess, sys
frac = float(sys.argv[1])
import time
tag = None
for attempt in range(4):
    subprocess.run(['adb', 'shell', 'uiautomator', 'dump', '/sdcard/ui_seek.xml'], capture_output=True)
    xml = subprocess.run(['adb', 'exec-out', 'cat', '/sdcard/ui_seek.xml'], capture_output=True).stdout.decode('utf-8', 'ignore')
    open('seekdump_%s.xml' % frac, 'w').write(xml)
    nodes = re.findall(r'<node\b[^>]*>', xml)
    tag = next((t for t in nodes if 'class="android.widget.SeekBar"' in t), None)
    if not tag:
        tag = next((t for t in nodes if re.search(r'content-desc="[^"]*(?i:seek|progress|slider|position)[^"]*"', t)), None)
    if not tag:
        for t in nodes:
            m = re.search(r'class="([^"]*)"[^>]*content-desc="([^"]*)"[^>]*bounds="([^"]*)"', t)
            if m and (m.group(2) or 'Slider' in m.group(1)):
                print('NODE', m.group(1), repr(m.group(2)), m.group(3))
    if tag:
        break
    subprocess.run(['adb', 'shell', 'input', 'tap', '1200', '540'])
    time.sleep(1.2)
if not tag:
    print('NO SEEKBAR'); sys.exit(1)
x1, y1, x2, y2 = map(int, re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', tag).groups())
cy = (y1 + y2) // 2
x = x1 + int((x2 - x1) * frac)
subprocess.run(['adb', 'shell', 'input', 'tap', str(x), str(cy)], check=True)
print('TAPPED seekbar at', frac, (x, cy))
