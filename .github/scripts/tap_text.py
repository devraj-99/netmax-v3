import sys, re, subprocess, os, time
dryrun = os.environ.get('TAP_TEXT_DRYRUN') == '1'

def bounds_of(tag):
    bm = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', tag)
    if not bm:
        return None
    x1, y1, x2, y2 = map(int, bm.groups())
    return (x1 + x2) // 2, (y1 + y2) // 2

def do_tap(c, what):
    if not dryrun:
        subprocess.run(['adb', 'shell', 'input', 'tap', str(c[0]), str(c[1])], check=True)
    print('TAPPED', what, c)

def find_center(xml, target):
    tags = re.findall(r'<node\b[^>]*>', xml)
    if target.startswith('@clickable'):
        want = int(target.split(':')[1]) if ':' in target else 0
        clickable = [t for t in tags if 'clickable="true"' in t]
        if want < len(clickable):
            return bounds_of(clickable[want]), 'clickable#%d at' % want
        return None, None
    if target.startswith('@checkable'):
        nodes = [t for t in tags if 'checkable="true"' in t]
        if nodes:
            return bounds_of(nodes[0]), 'checkable'
        return None, None
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
                return bounds_of(tag), repr(cand)
    return None, None

def live_dump():
    subprocess.run(['adb', 'shell', 'uiautomator', 'dump', '/sdcard/ui_live.xml'], capture_output=True)
    return subprocess.run(['adb', 'exec-out', 'cat', '/sdcard/ui_live.xml'], capture_output=True).stdout.decode('utf-8', 'ignore')

def screen_center():
    out = subprocess.run(['adb', 'shell', 'wm', 'size'], capture_output=True).stdout.decode()
    m = re.search(r'(\d+)x(\d+)', out)
    return (int(m.group(1)) // 2, int(m.group(2)) // 2) if m else (540, 1200)

if sys.argv[1] == '@live':
    target = sys.argv[2]
    for attempt in range(2):
        c, what = find_center(live_dump(), target)
        if c:
            do_tap(c, what)
            sys.exit(0)
        if attempt == 0:
            cx, cy = screen_center()
            subprocess.run(['adb', 'shell', 'input', 'tap', str(cx), str(cy)], capture_output=True)
            time.sleep(1)
    print('NOT FOUND:', target)
    sys.exit(1)

xml = open(sys.argv[1], encoding='utf-8', errors='ignore').read()
target = sys.argv[2]
c, what = find_center(xml, target)
if c:
    do_tap(c, what)
    sys.exit(0)
print('NOT FOUND:', target)
sys.exit(1)
