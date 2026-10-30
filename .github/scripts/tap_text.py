import sys, re, subprocess
xml = open(sys.argv[1], encoding='utf-8', errors='ignore').read()
target = sys.argv[2].lower()
for tag in re.findall(r'<node\b[^>]*>', xml):
    tm = re.search(r'text="([^"]*)"', tag)
    bm = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', tag)
    if not (tm and bm):
        continue
    if target in tm.group(1).lower():
        x1, y1, x2, y2 = map(int, bm.groups())
        cx, cy = (x1 + x2) // 2, (y1 + y2) // 2
        subprocess.run(['adb', 'shell', 'input', 'tap', str(cx), str(cy)], check=True)
        print('TAPPED', repr(tm.group(1)), cx, cy)
        sys.exit(0)
print('NOT FOUND:', target)
sys.exit(1)
