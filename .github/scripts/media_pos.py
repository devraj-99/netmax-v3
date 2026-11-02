import re, subprocess, sys
label = sys.argv[1] if len(sys.argv) > 1 else ''
out = subprocess.run(['adb', 'shell', 'dumpsys', 'media_session'], capture_output=True).stdout.decode('utf-8', 'ignore')
pos = None
for blk in out.split('\n\n'):
    if 'com.nuvio.app' in blk:
        m = re.search(r'state=PlaybackState \{state=(?:\w+\()?(\d+)\)?, position=(-?\d+)', blk)
        if m:
            pos = (int(m.group(1)), int(m.group(2)))
            break
if pos is None:
    m = re.search(r'state=PlaybackState \{state=(?:\w+\()?(\d+)\)?, position=(-?\d+)', out)
    if m:
        pos = (int(m.group(1)), int(m.group(2)))
print('POS', label, 'state=%s position_ms=%s' % pos if pos else 'none')
