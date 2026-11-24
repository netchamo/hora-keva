# בדיקת הסריקה מול צילום אמיתי

הכלים כאן מריצים את אותו מנוע זיהוי (Tesseract) על צילום, ומעבירים את התוצאה דרך הקוד של האפליקציה.

```
python3 tools/ocr_dump.py statement.jpg /tmp/dump.json
python3 -c "s=open('index.html').read();a=s.index('const GROUPS=');b=s.index('const score=rows=>');open('/tmp/scan_cur.js','w').write(s[a:b])"
node tools/parse_dump.js /tmp/dump.json
```
