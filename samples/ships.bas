10 gosub 50000

20 hgr
30 hcolor=6:hplot 0,13 to 279,13

100 hcolor=3:x=140:sd=0:s=sh
110 dx=0:dy=0

200 xdraw s at x,10:ox=x:os=s
210 ky=peek(-16384):if ky > 127 then 300
220 x=x+sd
225 gosub 400
230 if x < 10 then x=10
240 if x > 270 then x=270
250 xdraw os at ox,10
260 goto 200

300 poke -16368,0
310 if ky=136 then s=sh:sd=sd-1:goto 399
320 if ky=149 then s=sh+1:sd=sd+1:goto 390
330 if ky=160 and not dx and not dy then dx=x:dy=15:ds=dc:xdraw ds at dx,dy:goto 390 
390 if abs(sd) > 3 then sd = sgn(sd)*3
399 xdraw os at ox,10:goto 200

400 if not dx and not dy then return
410 ax=dx:ay=dy:as=ds
420 dy=dy+2:if dy > 160 then dx=0:dy=0
430 ds=ds+1:if ds > dc+3 then ds=dc
430 xdraw as at ax,ay
440 if not dx and not dy then return
450 xdraw ds at dx,dy
460 return

49999 end
50000 $shape src="ships.st", poke=yes, init=yes, assign=(sb="sub-left", sh="ship-left", dc="depthcharge-1")
 