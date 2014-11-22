// lex file für jflex
// Zusätzlich definierte Regeln: 
// 1. Operatoren und Operanden durch Leerzeichen getrennt
// 2. Steht ein + oder - ohne Leerzeichen direkt vor einer Zahl, wird es als Vorzeichen dieser Zahl gewertet
// 3. Mehrere Operationen werden von links nach rechts abgearbeitet, die  Punkt-vor-Strich-Regel gilt nicht
// 4. Eine Operation kann durch beliebig viele Leerzeichen/Tabs/Newlines unterbrochen werden

package de.fhmannheim.com.blatt2.uebung3;

import java_cup.runtime.*;
import java.io.IOException;

import de.fhmannheim.com.blatt2.uebung3.State;
import java.lang.Float;

%%

%class Uebung3Lex

%unicode
%line
%column

// options&declarations

%type Float

WHITE = [ \t\n\r$<<EOF>>]
FLOAT1 = [0-9]+\.[0-9]*f?
FLOAT2 = \.[0-9]+f?
FLOAT3 = [0-9]+f
INT = [0-9]+
NUMBER_POS1 = {FLOAT1}|{FLOAT2}|{FLOAT3}|{INT}
NUMBER_POS2 = \+{NUMBER_POS1}
NUMBER_NEG = \-{NUMBER_POS1}
NUMBER={NUMBER_POS1}|{NUMBER_POS2}|{NUMBER_NEG}
PLUS = \+
MINUS = -
MAL = \*
GLEICH = \=
SL_KOMMENTAR = \/\/.*
ML_KOMMENTAR = \/\*(.|{WHITE})*\*\/

%{
public float current = 0.f;
public State state = State.INIT;
public StringBuffer b = new StringBuffer();
%}

%%

// lexical rules

{WHITE}+ {}
{ML_KOMMENTAR} {}
{SL_KOMMENTAR} {}
{NUMBER}{WHITE}+ {current = state.calc(current, yytext());}
{PLUS}{WHITE}+ {state = State.PLUS;}
{MINUS}{WHITE}+ {state = State.MINUS;}
{MAL}{WHITE}+ {state = State.MAL;}
{GLEICH}{WHITE}+ {state = State.INIT; float ret = current; current = 0.f; return ret;}
