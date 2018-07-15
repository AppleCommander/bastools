10 gosub 50000:goto 1000

20 for i=1 to mm:if dx(i) then next i:return
30 dx(i)=x:dy(i)=wc:ds(i)=dc:return

40 a$="0000"+str$(qq):a$=mid$(a$,len(a$)-3)
50 ly=peek(cv)*lh:lx=peek(ch)*lw
60 for c=1 to len(a$):ls=cb+asc(mid$(a$,c,1))-sp:draw ru at lx,ly:xdraw ls at lx,ly:lx=lx+lw:next c:return

90 vtab 1:htab 37:qq=qh:gosub 40:vtab 2:qq=qs:gosub 40:vtab 3:qq=qd:gosub 40:return

100 x=140:sd=0:s=sh:qs=0:qd=30
110 di=1:mm=5:dim dx(mm),dy(mm),ds(mm),ox(mm),oy(mm),os(mm)
120 for i=1 to mm:dx(i)=0:dy(i)=0:ds(i)=0:next i
130 bs=0:bx=0:by=0:bd=0
140 xs=0:xx=0:xy=0
150 gosub 90

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
360 if ky=160 and qd > 0 then qd=qd-1:gosub 20:vtab 3:htab 37:qq=qd:gosub 40 

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
710 if cx then xdraw cs at cx,cy:a=cx:cx=0:if peek(234) then xs=cs+2:xx=a:xy=cy:bx=0:qs=qs+1:qd=qd+5:vtab 2:htab 37:qq=qs:gosub 40:vtab 3:qq=qd:gosub 40
720 if zx then xdraw zs at zx,xy:zx=0
730 xdraw os at ox,ws
740 if qd > 0 then 200 

800 if qs > qh then qh = qs 

1000 hgr:poke -16302,0:ch=36:cv=37:lw=7:lh=8:sp=asc(" "):wl=40:ws=wl-3:wc=wl+2:ru=95+cb
1010 hcolor=0:vtab 1:htab 1:a$="DESTROYER!":gosub 50
1020 vtab 22:htab 4:a$="| LEFT, } RIGHT, SPACEBAR TO FIRE":gosub 50 
1030 vtab 1:htab 28:a$="HISCORE:":gosub 50:vtab 2:a$="SCORE:":gosub 50:vtab 3:a$="CHARGES:":gosub 50:gosub 90
1050 hcolor=6:hplot 0,wl to 279,wl:hplot 0,159 to 279,159:hcolor=0
1060 ly=wl+15:xdraw sh at 45,ly+3:a$="YOUR DESTROYER":lx=70:gosub 60 
1070 ly=ly+10:xdraw sb at 45,ly+3:a$="ENEMY SUBMARINE (1 point)":lx=70:gosub 60
1080 ly=ly+10:lx=40:for s=dc to dc+3:xdraw s at lx,ly+3:lx=lx+5:next s:a$="DEPTH CHARGES (hit = +5!)":lx=70:gosub 60
1090 ly=ly+20:lx=40:a$="Demo code for 'bastools'":gosub 60
1100 ly=ly+10:lx=40:a$="Visit applecommander.github.io":gosub 60
1110 ly=ly+20:lx=40:a$="PRESS ANY KEY TO BEGIN!":gosub 60 
1120 if peek(-16384)<128 then 1120
1130 poke -16368,0
1140 hcolor=0:for y=wl+15 to 150:hplot 0,y to 279,y:next y
1150 goto 100 

50000 $shape src="destroyer.st", poke=yes, init=yes, assign=(sb="sub-left", sh="ship-left", dc="depthcharge-1", cb="characters")
