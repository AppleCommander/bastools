10 REM DEMO CODE FOR SHAPE TABLE GENERATED FROM '$SOURCE$'
20 TEXT:HOME
30 PRINT CHR$(4);"BLOAD SHAPES.BIN,A$6000"
40 POKE 232,0:POKE 233,96
50 SCALE=1:ROT=0
60 HGR:HCOLOR=3
70 X=10+$WIDTH$:Y=10+$HEIGHT$
80 FOR S=1 TO $COUNT$
90 DRAW S AT X,Y
100 X=X+$WIDTH$+5:IF X < 279 THEN 140 
110 X=10+$WIDTH$:Y=Y+$HEIGHT$+5:IF Y < 159 THEN 140 
120 HOME:VTAB 21:PRINT "PRESS ANY KEY":GET A$
130 X=10+$WIDTH$:Y=10+$WIDTH$
140 NEXT S 
150 HOME:VTAB 21:PRINT "DONE":GET A$
160 TEXT:HOME
170 END
