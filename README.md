![Java 8](https://img.shields.io/badge/java-8-brightgreen.svg)
---
<p align="center">
	<br />
	<font style="color: #FFFFFF; font-size: 50px; background:#336633; border: 25px solid #336633; border-radius: 10px;">NOAH</font>
</p>

# NOAH
The *Native Over-Approximation Handler (NOAH)* is a very simple Android app taint analysis tool that considers native method calls.
It is simple, because (I) it does not analyze any flows, (II) it only assumes connections once a native call is encountered.
Thus any taint-source is connected to any taint-sink in native code and vice versa.

## Example
The goal of the following query, for example, is to find any flows that start or end in the native library part of app `A`.

```
CONNECT [
	Flows IN App(’A.apk’ | 'UNCOVER') ?,
	Flows IN App(’A.apk’ | 'UNCOVER') FEATURING 'NATIVE' ?
]
```

The preprocessor keyword 'UNCOVER' tells the associated [AQL-System](https://github.com/FoelliX/AQL-System) to execute NOAH as preprocessor for app `A`.
The first question for flows in answered by an arbitrary analysis tool configured in the associated AQL-System.
The second one by NOAH, assuming that it has the highest priority for flow-questions which have the `'NATIVE'` feature assigned.
A complete and fully described example can be found in the referenced paper (see [Publications](#Publications)).

## Launch Parameters
The first launch parameter must always the be app to analyze/preprocess.
Furthermore two additional launch parameters can be specified:

| Parameter | Meaning |
| --------- | ------- |
| `-sas %FILE%`, `-sourcesandsinks %FILE%` | Provide a different source and sink file (`%FILE%`). |
| `-debug "X"`, `-d "X"` | The output generated during the execution of this tool can be set to different levels. `X` may be set to: `error`, `warning`, `normal`, `debug`, `detailed` (ascending precision from left to right). Additionally it can be set to `short`, the output will then be equal to `normal` but shorter at some points. By default it is set to `normal`. |

## Publications
- *Together Strong: Cooperative Android App Analysis* (Felix Pauck, Heike Wehrheim)  
t.b.a.

## License
NOAH is licensed under the *GNU General Public License v3* (see [LICENSE](https://github.com/FoelliX/NOAH/blob/master/LICENSE)).

## Contact
**Felix Pauck** (FoelliX)  
Paderborn University  
fpauck@mail.uni-paderborn.de  
[http://www.FelixPauck.de](http://www.FelixPauck.de)

## Links
- NOAH is employed in CoDiDroid: [https://github.com/FoelliX/CoDiDroid](https://github.com/FoelliX/CoDiDroid)