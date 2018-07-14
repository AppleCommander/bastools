10 gosub 50000:goto 100

20 for i=1 to mm:if dx(i) then next i:return
30 dx(i)=x:dy(i)=wc:ds(i)=dc:return

100 hgr:vtab 21
110 wl=40:ws=wl-3:wc=wl+2
110 hcolor=6:hplot 0,wl to 279,wl:hplot 0,159 to 279,159

120 hcolor=3
130 x=140:sd=0:s=sh
140 di=1:mm=5:dim dx(mm),dy(mm),ds(mm),ox(mm),oy(mm),os(mm)
150 for i=1 to mm:dx(i)=0:dy(i)=0:ds(i)=0:next i
160 bs=0:bx=0:by=0:bd=0
170 xs=0:xx=0:xy=0

200 xdraw s at x,ws:ox=x:os=s
210 if bx then xdraw bs at bx,by:cs=bs:cx=bx:cy=by
220 ox(di)=dx(di):oy(di)=dy(di):os(di)=ds(di)
230 if dx(di) then xdraw ds(di) at dx(di),dy(di)
240 if xx then xdraw xs at xx,xy:zs=xs:zx=xx:zy=xy

300 ky=peek(-16384):if ky < 128 then 400
310 poke -16368,0
320 if ky=136 then s=sh:sd=sd-1
330 if ky=149 then s=sh+1:sd=sd+1
340 if sd > 3 then sd=3
350 if sd < -3 then sd=-3
360 if ky=160 then gosub 20 

400 if not bx then 450
410 bx=bx+bd
420 if bx < 10 or bx > 270 then bs=0:bx=0:by=0:bd=0
430 goto 500
450 rr=rnd(1):if rr < 0.10 then 480
460 if rr > 0.20 then 500
470 bx=10:by=wl+10+rnd(1)*80:bd=2:bs=sb+1:goto 500
480 bx=270:by=wl+10+rnd(1)*80:bd=-2:bs=sb

500 x=x+sd
510 if x < 10 then x=10
520 if x > 270 then x=270

550 if xs then xs=xs+2:if xs >= sh then xx=0

600 if not dx(di) then 690
610 ds(di)=ds(di)+1:if ds(di) > dc+3 then ds(di)=dc
620 dy(di)=dy(di)+5:if dy(di) > 155 then dx(di)=0:dy(di)=0:ds(di)=0
690 di=di+1:if di > mm then di = 1

700 if ox(di) then xdraw os(di) at ox(di),oy(di):if peek(234) then dx(di)=0:dy(di)=0:ds(di)=0
710 if cx then xdraw cs at cx,cy:a=cx:cx=0:if peek(234) then print "BOOM!":xs=cs+2:xx=a:xy=cy:bx=0
720 if zx then xdraw zs at zx,xy:zx=0
730 xdraw os at ox,ws
740 goto 200 

50000 $shape src="ships.st", poke=yes, init=yes, assign=(sb="sub-left", sh="ship-left", dc="depthcharge-1")
