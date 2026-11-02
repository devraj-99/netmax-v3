import re, subprocess, sys
frac = float(sys.argv[1])
subprocess.run(['adb', 'shell', 'uiautomator', 'dump', '/sdcard/ui_seek.xml'], capture_output=True)
xml = subprocess.run(['adb', 'exec-out', 'cat', '/sdcard/ui_seek.xml'], capture_output=True).stdout.decode('utf-8', 'ignore')
tag = next((t for t in re.findall(r'<node\b[^>]*>', xml) if 'class="android.widget.SeekBar"' in t), None)
if not tag:
    print('NO SEEKBAR'); sys.exit(1)
x1, y1, x2, y2 = map(int, re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', tag).groups())
cy = (y1 + y2) // 2
x = x1 + int((x2 - x1) * frac)
subprocess.run(['adb', 'shell', 'input', 'tap', str(x), str(cy)], check=True)
print('TAPPED seekbar at', frac, (x, cy))
