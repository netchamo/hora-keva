const now=new Date(),CM=now.getMonth();const S={map:{}};
eval(require('fs').readFileSync('/tmp/scan_cur.js','utf8').replace(/^const GROUPS/,'var GROUPS')+';globalThis.T={mergeLatin,chargeCol,parseOcr}');
for(const f of process.argv.slice(2)){const d=JSON.parse(require('fs').readFileSync(f));
 const L=T.mergeLatin(d.A,d.B,0);const col=T.chargeCol(L,d.W);
 console.log('==',f,'col',col&&col.toFixed(2));console.log(L.map(l=>'  | '+l.text).join('\n'));
 console.log(T.parseOcr(L,col,d.W).map(r=>JSON.stringify([r.bill,r.a,r.day+'/'+r.mo,r.foreign,r.calc||'',r.kind||'',r.hint||'',r.debDay||'',r.sumOk||0].concat([]) )).join('\n'))}
