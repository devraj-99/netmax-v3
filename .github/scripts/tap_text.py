import sys, re, subprocess
xml = open(sys.argv[1], encoding='utf-8', errors='ignore').read()
target = sys.argv[2]
def bounds_of(tag):
    bm = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', tag)
    if not bm:
        return None
    x1, y1, x2, y2 = map(int, bm.groups())
    return (x1 + x2) // 2, (y1 + y2) // 2
tags = re.findall(r'<node\b[^>]*>', xml)
if target.startswith('@clickable'):
    want = int(target.split(':')[1]) if ':' in target else 0
    clickable = [t for t in tags if 'clickable="true"' in t]
    if want < len(clickable):
        c = bounds_of(clickable[want])
        if c:
            subprocess.run(['adb', 'shell', 'input', 'tap', str(c[0]), str(c[1])], check=True)
            print('TAPPED clickable#%d at' % want, c)
            sys.exit(0)
    print('NOT FOUND:', target)
    sys.exit(1)
for tag in tags:
    tm = re.search(r'text="([^"]*)"', tag)
    if not tm:
        continue
    if target.lower() in tm.group(1).lower():
        c = bounds_of(tag)
        if c:
            subprocess.run(['adb', 'shell', 'input', 'tap', str(c[0]), str(c[1])], check=True)
            print('TAPPED', repr(tm.group(1)), c)
            sys.exit(0)
print('NOT FOUND:', target)
sys.exit(1)
