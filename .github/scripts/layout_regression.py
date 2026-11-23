import os, re, subprocess, time, xml.etree.ElementTree as ET
os.makedirs('evidence', exist_ok=True)
def adb(*args, check=True):
    return subprocess.run(['adb',*args],capture_output=True,check=check).stdout

def capture(name):
    adb('shell','uiautomator','dump','/sdcard/ui.xml',check=False)
    raw=adb('exec-out','cat','/sdcard/ui.xml',check=False)
    open('evidence/'+name+'.xml','wb').write(raw)
    open('evidence/'+name+'.png','wb').write(adb('exec-out','screencap','-p'))
    open('evidence/logcat.txt','wb').write(adb('logcat','-d'))
    try: return ET.fromstring(raw)
    except ET.ParseError: return ET.Element('empty')

def tap(root, *labels):
    for label in labels:
        for node in root.iter('node'):
            if any(label.lower() == node.get(k,'').lower() for k in ('text','content-desc')):
                v=list(map(int,re.findall(r'\d+',node.get('bounds',''))))
                if len(v)==4:
                    adb('shell','input','tap',str((v[0]+v[2])//2),str((v[1]+v[3])//2));return True
    return False

adb('logcat','-c')
adb('shell','am','start','-n','com.nuvio.app/.MainActivity')
time.sleep(25)
for i in range(24):
    root=capture('step_%02d'%i)
    texts=[n.get('text') or n.get('content-desc') for n in root.iter('node')]
    print(i,texts,flush=True)
    if tap(root,'Layout','Appearance'):
        time.sleep(5); root=capture('appearance_open')
        log=adb('logcat','-d').decode(errors='replace')
        if 'FATAL EXCEPTION' in log:
            print('REPRODUCED CRASH',flush=True);raise SystemExit(1)
        print('LAYOUT OPENED WITHOUT FATAL',flush=True)
        tap(root,'Liquid Glass Controls');time.sleep(5);capture('glass_controls')
        raise SystemExit(0)
    if tap(root,'Continue Without Account','Continue without an account','Close app','Allow','While using the app','Create Profile','Profile','Settings'):
        time.sleep(8);continue
    if any("Who's watching" in (t or '') for t in texts):
        nodes=[n for n in root.iter('node') if n.get('clickable')=='true']
        if nodes:
            v=list(map(int,re.findall(r'\d+',nodes[0].get('bounds',''))));adb('shell','input','tap',str((v[0]+v[2])//2),str((v[1]+v[3])//2));time.sleep(8);continue
    # The last navigation item is the profile/settings tab.
    if any((t or '')=='Library' for t in texts):
        out=adb('shell','wm','size').decode();w,h=map(int,re.search(r'(\d+)x(\d+)',out).groups())
        adb('shell','input','tap',str(int(w*.89)),str(int(h*.94)));time.sleep(8);continue
    time.sleep(6)
capture('navigation_failed')
raise SystemExit('Could not reach Layout; inspect evidence')
