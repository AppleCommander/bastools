' One-time initializations
10 gosub 50000: \
   chargemax=5: \
   dim chargex(chargemax),chargey(chargemax),chargeshape(chargemax),\
       priorchargex(chargemax),priorchargey(chargemax),priorchargeshape(chargemax): \
   ch=36:cv=37: \
   charwidth=7:charheight=8: \
   space=asc(" "):rubshape=95+charbase: \
   waterline=40:shipy=waterline-3:chargestarty=waterline+2: \
   goto 1000

' Search list of depth charges for open slot; if found, queue up the next depth charge!
20 for i=1 to chargemax: \
   if chargex(i) then next i:return
30 chargex(i)=shipx: \
   chargey(i)=chargestarty: \
   chargeshape(i)=constdepthchargeshape: \
   remainingcharges=remainingcharges-1: \
   gosub 40: \
   vtab 3:htab 37:qq=remainingcharges: \
   return

' Display number in QQ zero-padded to 4 digits
40 a$="0000"+str$(qq): \
   a$=mid$(a$,len(a$)-3)

' Display string at current HTAB,VTAB location. Note the erase done with the rub character; assumption is color is black!
50 ly=peek(cv)*charheight: \
   lx=peek(ch)*charwidth
60 for c=1 to len(a$): \
       ls=charbase+asc(mid$(a$,c,1))-space: \
       draw rubshape at lx,ly: \
       xdraw ls at lx,ly: \
       lx=lx+charwidth: \
   next c: \
   return

' Update ALL states on the screen
90 vtab 1:htab 37:qq=hiscore:gosub 40: \
   vtab 2:qq=score:gosub 40: \
   vtab 3:qq=remainingcharges:gosub 40: \
   return

' Game initialization
100 shipx=140:shipdirection=0:shipshape=constdestroyershape: \
    score=0:remainingcharges=30:chargeindex=1
120 for i=1 to chargemax: \
        chargex(i)=0: \
        chargey(i)=0: \
        chargeshape(i)=0: \
    next i
130 subshape=0:subx=0:suby=0:subdirection=0
140 explosionshape=0:explosionx=0:explosiony=0
150 gosub 90

' Draw all active shapes (based on X coordinate)
200 xdraw shipshape at shipx,shipy: \
    oldshipx=shipx:oldshipshape=shipshape
210 if subx then \
       xdraw subshape at subx,suby: \
       oldsubshape=subshape:oldsubx=subx:oldsuby=suby
220 priorchargex(chargeindex)=chargex(chargeindex): \
    priorchargey(chargeindex)=chargey(chargeindex): \
    priorchargeshape(chargeindex)=chargeshape(chargeindex)
230 if chargex(chargeindex) then \
       xdraw chargeshape(chargeindex) at chargex(chargeindex),chargey(chargeindex)
240 if explosionx then \
       xdraw explosionshape at explosionx,explosiony:zs=explosionshape:zx=explosionx:zy=explosiony

' Handle keyboard
300 keypress=peek(-16384): \
    if keypress < 128 then 400
310 poke -16368,0
320 if keypress=136 then \
       shipshape=constdestroyershape: \
       shipdirection=shipdirection-1
330 if keypress=149 then \
       shipshape=constdestroyershape+1: \
       shipdirection=shipdirection+1
340 if shipdirection > 3 then shipdirection=3
350 if shipdirection < -3 then shipdirection=-3
360 if keypress=160 and remainingcharges > 0 then gosub 20 

' Submarine. Only one at a time. If it doesn't exist, pick a random number to see if one shows up!
400 if not subx then 450
410 subx=subx+subdirection
420 if subx < 10 or subx > 270 then subshape=0:subx=0:suby=0:subdirection=0
430 goto 500
' Test for a new sub
450 rr=rnd(1):if rr < 0.10 then 480
460 if rr > 0.20 then 500
470 subx=10:suby=waterline+10+rnd(1)*80:subdirection=2:subshape=constsubshape+1:goto 500
480 subx=270:suby=waterline+10+rnd(1)*80:subdirection=-2:subshape=constsubshape

' Move the destroyer/ship
500 shipx=shipx+shipdirection
510 if shipx < 10 then shipx=10
520 if shipx > 270 then shipx=270

' Make the explosion all explody
550 if explosionshape then explosionshape=explosionshape+2:if explosionshape >= constdestroyershape then explosionx=0

' Move one of the depth charges
600 if not chargex(chargeindex) then 690
610 chargeshape(chargeindex)=chargeshape(chargeindex)+1: \
    if chargeshape(chargeindex) > constdepthchargeshape+3 then \
       chargeshape(chargeindex)=constdepthchargeshape
620 chargey(chargeindex)=chargey(chargeindex)+5: \
    if chargey(chargeindex) > 155 then \
       chargex(chargeindex)=0: \
       chargey(chargeindex)=0: \
       chargeshape(chargeindex)=0
' Setup for next depth charge
690 chargeindex=chargeindex+1: \
    if chargeindex > chargemax then \
       chargeindex = 1

' Erase shapes
700 if oldsubx then \
       xdraw oldsubshape at oldsubx,oldsuby: \
       a=oldsubx:oldsubx=0: \
       if peek(234) then \
          explosionshape=oldsubshape+2: \
          explosionx=a: \
          explosiony=oldsuby: \
          subx=0: \
          score=score+1: \
          remainingcharges=remainingcharges+5: \
          vtab 2:htab 37:qq=score:gosub 40: \
          vtab 3:qq=remainingcharges:gosub 40
710 if priorchargex(chargeindex) then \
       xdraw priorchargeshape(chargeindex) at priorchargex(chargeindex),priorchargey(chargeindex): \
       if peek(234) then \
          chargex(chargeindex)=0: \
          chargey(chargeindex)=0: \
          chargeshape(chargeindex)=0
720 if zx then xdraw zs at zx,explosiony:zx=0
730 xdraw oldshipshape at oldshipx,shipy
740 if remainingcharges > 0 then 200 

' Stupidly, once we run out of charges, we immediately end the game :-/

' Check if a high score was set ...
800 if score > hiscore then hiscore = score 

' Display the title screen!
1000 hgr:poke -16302,0
1010 hcolor=0: \
     vtab 1:htab 1:a$="Destroyer!":gosub 50
1020 vtab 22:htab 4:a$="| Left, } Right, Spacebar to fire":gosub 50 
1030 vtab 1:htab 28:a$="Hiscore:":gosub 50:vtab 2:a$="Score:":gosub 50:vtab 3:a$="Charges:":gosub 50:gosub 90
1050 hcolor=6: \
     hplot 0,waterline to 279,waterline: \
     hplot 0,159 to 279,159:hcolor=0
1060 ly=waterline+15: \
     xdraw constdestroyershape at 45,ly+3: \
     a$="Your destroyer":lx=70:gosub 60 
1070 ly=ly+10: \
     xdraw constsubshape at 45,ly+3: \
     a$="Enemy submarine (1 point)":lx=70:gosub 60
1080 ly=ly+10:lx=40: \
     for s=constdepthchargeshape to constdepthchargeshape+3: \
         xdraw s at lx,ly+3: \
         lx=lx+5: \
     next s: \
     a$="Depth charges (hit = +5!)":lx=70:gosub 60
1090 ly=ly+20:lx=40: \
     a$="Demo code for 'bastools'":gosub 60
1100 ly=ly+10:lx=40: \
     a$="Visit applecommander.github.io":gosub 60
1110 ly=ly+20:lx=40: \
     a$="PRESS ANY KEY TO BEGIN!":gosub 60 
1120 if peek(-16384)<128 then 1120
1130 poke -16368,0
1140 hcolor=0: \
     for y=waterline+15 to 150: \
         hplot 0,y to 279,y: \
     next y
1150 goto 100

50000 $shape src="destroyer.st", \
             poke=yes, \
             init=yes, \
             assign=(constsubshape="sub-left", \
                     constdestroyershape="ship-left", \
                     constdepthchargeshape="depthcharge-1", \
                     charbase="characters")
