10 gosub 50000:goto 100

20 for i=1 to mm:if dx(i) then next i:return
30 dx(i)=x:dy(i)=15:ds(i)=dc:return

100 hgr:vtab 21
110 hcolor=6:hplot 0,13 to 279,13

120 hcolor=3
130 x=140:sd=0:s=sh
140 di=0:mm=3:dim dx(mm),dy(mm),ds(mm),ox(mm),oy(mm),os(mm)
150 for i=1 to mm:dx(i)=0:dy(i)=0:ds(i)=0:next i

200 xdraw s at x,10:ox=x:os=s
210 for i=1 to mm
220 ox(i)=dx(i):oy(i)=dy(i):os(i)=ds(i)
230 if dx(i) then xdraw ds(i) at dx(i),dy(i)
240 next i

300 ky=peek(-16384):if ky < 128 then 400
310 poke -16368,0
320 if ky=136 then s=sh:sd=sd-1
330 if ky=149 then s=sh+1:sd=sd+1
340 if sd > 3 then sd=3
350 if sd < -3 then sd=-3
360 if ky=160 then gosub 20 

400 x=x+sd
410 if x < 10 then x=10
420 if x > 270 then x=270

500 di=di+1:if di > mm then di = 1
510 if not dx(di) then 600
520 ds(di)=ds(di)+1:if ds(di) > dc+3 then ds(di)=dc
530 dy(di)=dy(di)+2:if dy(di) > 160 then dx(di)=0:dy(di)=0:ds(di)=0

600 xdraw os at ox,10
610 for i=1 to mm
620 if ox(i) then xdraw os(i) at ox(i),oy(i)
630 next i
640 goto 200 

49999 end
50000 $shape src="ships.st", poke=yes, init=yes, assign=(sb="sub-left", sh="ship-left", dc="depthcharge-1")
