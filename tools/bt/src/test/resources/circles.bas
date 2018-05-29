10 goto 100

20 rem draw circle routine
30 for a = 0 to pt
40 x = x(a) * sz:y = y(a) * sz
50 hplot xo + x,yo + y
60 hplot xo - x,yo + y
70 hplot xo + x,yo - y
80 hplot xo - x,yo - y
90 next a
95 return

100 rem main program
110 hgr
115 c(0)=1:c(1)=2:c(2)=3:c(3)=5:c(4)=6:c(5)=7
120 home : vtab 21: inverse : print "JUST A MOMENT": normal
130 pi = 3.14159
140 pt = 30: dim x(pt),y(pt)
150 for a = 0 to pt
160 b = pi * (a / (pt * 2))
170 x(a) = sin (b)
180 y(a) = cos (b)
190 next a
200 home : vtab 21
210 for q = 1 to 100
215 c = 6 * rnd(1) : hcolor= c(c)
220 sz = 10 + (40 * rnd (1))
230 xo = (279 - sz*2) * rnd (1) + sz
240 yo = (159 - sz*2) * rnd (1) + sz
250 gosub 30
260 next q
