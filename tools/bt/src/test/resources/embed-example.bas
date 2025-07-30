1 GOSUB 10
2 PRINT "BEFORE CALL"
3 CALL 768
4 PRINT "AFTER CALL"
5 END
10 $embed file="embed-rts.bin", moveto="$300"
