import sys, re, subprocess, os
xml = open(sys.argv[1], encoding='utf-8', errors='ignore').read()
target = sys.argv[2]
dryrun = os.environ.get('TAP_TEXT_DRYRUN') == '1'
def bounds_of(tag):
    bm = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', tag)
    if not bm:
        return None
    x1, y1, x2, y2 = map(int, bm.groups())
    return (x1 + x2) // 2, (y1 + y2) // 2
def tap(c, what):
    if not dryrun:
        subprocess.run(['adb', 'shell', 'input', 'tap', str(c[0]), str(c[1])], check=True)
    print('TAPPED', what, c)
    sys.exit(0)
tags = re.findall(r'<node\b[^>]*>', xml)
if target.startswith('@clickable'):
    want = int(target.split(':')[1]) if ':' in target else 0
    clickable = [t for t in tags if 'clickable="true"' in t]
    if want < len(clickable):
        c = bounds_of(clickable[want])
        if c:
            tap(c, 'clickable#%d at' % want)
    print('NOT FOUND:', target)
    sys.exit(1)
if target.startswith('@checkable'):
    nodes = [t for t in tags if 'checkable="true"' in t]
    if nodes:
        c = bounds_of(nodes[0])
        if c:
            tap(c, 'checkable')
    print('NOT FOUND:', target)
    sys.exit(1)
exact = target.startswith('@exact:')
if exact:
    target = target[len('@exact:'):]
for tag in tags:
    t1 = re.search(r'text="([^"]*)"', tag)
    t2 = re.search(r'content-desc="([^"]*)"', tag)
    for cand in (t1.group(1) if t1 else '', t2.group(1) if t2 else ''):
        if not cand:
            continue
        hit = (cand.lower() == target.lower()) if exact else (target.lower() in cand.lower())
        if hit:
            c = bounds_of(tag)
            if c:
                tap(c, repr(cand))
print('NOT FOUND:', target)
sys.exit(1)
