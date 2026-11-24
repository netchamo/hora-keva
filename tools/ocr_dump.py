import sys,json,subprocess,io
from PIL import Image,ImageOps
def prep(path):
    im=Image.open(path).convert('RGB');w,h=im.size;sc=min(3,max(1,2800/w));im=im.resize((round(w*sc),round(h*sc)),Image.LANCZOS)
    g=ImageOps.grayscale(im);px=list(g.getdata());n=len(px);mean=sum(px)/n
    hist=g.histogram();acc=0;lo=0
    for i,c in enumerate(hist):
        acc+=c
        if acc>n*.02: lo=i;break
    acc=0;hi=255
    for i in range(255,-1,-1):
        acc+=hist[i]
        if acc>n*.02: hi=i;break
    span=max(1,hi-lo);g=g.point(lambda v:max(0,min(255,(v-lo)*255/span)))
    if mean<115:g=ImageOps.invert(g)
    return g
def tsv(img,lang,psm):
    b=io.BytesIO();img.save(b,'PNG')
    out=subprocess.run(['tesseract','stdin','stdout','-l',lang,'--psm',psm,'-c','preserve_interword_spaces=1','--dpi','300','tsv'],input=b.getvalue(),capture_output=True).stdout.decode()
    rows=[r.split('\t') for r in out.strip().split('\n')[1:]]
    words=[];lines={}
    for r in rows:
        if len(r)<12 or r[0]!='5' or not r[11].strip():continue
        x,y,w,h=map(int,r[6:10]);wd={'text':r[11],'confidence':float(r[10]),'bbox':{'x0':x,'y0':y,'x1':x+w,'y1':y+h}}
        words.append(wd);k=(r[2],r[3],r[4]);lines.setdefault(k,[]).append(wd)
    L=[]
    for k,ws in lines.items():
        L.append({'text':' '.join(w['text'] for w in ws),'words':ws,'bbox':{'x0':min(w['bbox']['x0'] for w in ws),'y0':min(w['bbox']['y0'] for w in ws),'x1':max(w['bbox']['x1'] for w in ws),'y1':max(w['bbox']['y1'] for w in ws)}})
    return {'lines':L,'words':words,'text':'\n'.join(l['text'] for l in L)}
g=prep(sys.argv[1])
json.dump({'W':g.size[0],'A':tsv(g,'heb+eng','6'),'B':tsv(g,'eng','6'),'A4':tsv(g,'heb+eng','4')},open(sys.argv[2],'w'),ensure_ascii=False)
