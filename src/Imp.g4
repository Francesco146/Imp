grammar Imp;

prog : com EOF ;

com : IF LPAR exp RPAR THEN LBRACE com RBRACE ELSE LBRACE com RBRACE    # if
    | ID ASSIGN exp                                                     # assign
    | ID LSQUARE exp RSQUARE ASSIGN exp                                 # arrayAssignCom
    | SKIPP                                                             # skip
    | com SEMICOLON com                                                 # seq
    | WHILE LPAR exp RPAR LBRACE com RBRACE                             # while
    | OUT LPAR exp RPAR                                                 # out
    ;

exp : NAT                                 # nat
    | BOOL                                # bool
    | STRING                              # string
    | LPAR exp RPAR                       # parExp
    | <assoc=right> exp POW exp           # pow
    | NOT exp                             # not
    | exp op=(DIV | MUL | MOD) exp        # divMulMod
    | exp op=(PLUS | MINUS) exp           # plusMinus
    | exp op=(LT | LEQ | GEQ | GT) exp    # cmpExp
    | exp op=(EQQ | NEQ) exp              # eqExp
    | exp op=(AND | OR) exp               # logicExp
    | TOSTRING LPAR exp RPAR              # toString
    | exp CONC exp                        # concString
    | ID LSQUARE exp RSQUARE              # arrayExp
    | ID                                  # id
    ;

NAT : '0' | [1-9][0-9]* ;
BOOL : 'true' | 'false' ;
STRING: '"'STRCHR*'"' ;
fragment STRCHR : ~["\\] | ESC ;
fragment ESC    : '\\' [btnfr" '\\] ;

PLUS  : '+' ;
MINUS : '-';
MUL   : '*' ;
DIV   : '/' ;
MOD   : 'mod' ;
POW   : '^' ;

CONC : '.' ;

AND : '&' ;
OR  : '|' ;

EQQ : '==' ;
NEQ : '!=' ;
LEQ : '<=' ;
GEQ : '>=' ;
LT  : '<' ;
GT  : '>' ;
NOT : '!' ;

IF        : 'if' ;
THEN      : 'then' ;
ELSE      : 'else' ;
WHILE     : 'while' ;
SKIPP     : 'skip' ;
ASSIGN    : '=' ;
OUT       : 'out' ;
TOSTRING  : 'tostr' ;

LPAR      : '(' ;
RPAR      : ')';
LBRACE    : '{' ;
RBRACE    : '}' ;
LSQUARE   : '[' ;
RSQUARE   : ']' ;
SEMICOLON : ';' ;


ID : [a-z]+ ;

WS : [ \t\r\n]+ -> skip ;