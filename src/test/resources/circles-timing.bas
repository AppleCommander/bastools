0 REM Provide a somewhat consistent runtime and take out the randomness
1 REM to allow for timing and verify that the "optimizations" improve runtime
2 REM to some degree... 

5 $embed "src/test/resources/read.time.bin", "0x0260"

10 call 608: rem initialize clock routine
15 call 768,t1$:goto 100

20 rem draw circle routine
30 for a = 0 to pt
40 x = x(a) * r:y = y(a) * r
50 hplot xo + x,yo + y
60 hplot xo - x,yo + y
70 hplot xo + x,yo - y
80 hplot xo - x,yo - y
90 next a
95 return

100 rem main program
110 hgr:hcolor=3
120 home : vtab 21: inverse : print "JUST A MOMENT": normal
130 pi = 3.14159
140 pt = 30: dim x(pt),y(pt)
150 for a = 0 to pt
160 b = pi * (a / (pt * 2))
170 x(a) = sin (b)
180 y(a) = cos (b)
190 next a

199 rem draw code...
200 d = 160
210 home : vtab 21 : print "DIAMETER = ";d
220 for xx = 0 to 279 step d
225 if xx+d > 280 then 270
230 for yy = 0 to 159 step d
235 if yy+d > 160 then 260
240 r=d/2:xo=xx + r:yo=yy + r:gosub 30
250 next yy
260 next xx
270 d = d / 2
280 if d > 10 then 210 

300 rem display time
310 call 768,t2$
320 home : vtab 21
330 print "START TIME = ";t1$
340 print "END TIME =   ";t2$
