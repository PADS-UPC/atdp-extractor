# ATDP Extractor

This tool automatically extracts the Annotated Textual Descriptions of Processes ([ATDP](https://github.com/PADS-UPC/atdplib-model)) from a natural language text.

## Prerequisites

* To parse text we have used [Freeling](http://nlp.lsi.upc.edu/freeling/) (v4.1)
* To develop the project, [Eclipse IDE](https://www.eclipse.org/downloads/) as a development environment was used. 
* As Runtime Environment we have used [Java JRE 1.8](https://java.com/en/download/help/index_installing.xml). 
* The proyect was set up as a [Maven Project](https://maven.apache.org/install.html) (v.3.6.0).


```
Freeling: http://nlp.lsi.upc.edu/freeling/
Eclipse: https://www.eclipse.org/downloads/
Java JRE: https://java.com/en/download/help/index_installing.xml
Maven: https://maven.apache.org/install.html
```

## Running project
There are 2 mandatory input parameters to run the program correctly.
1. The path of the file to be parsed (e.g.: ``input/text/myprocesstext.txt``). If the path is not indicated, the program will automatically parse all the files that are in the folder: 
``` 
input/texts/ 
```
2. Instruction of whether we want to apply all the patterns or in a reduced way. This is specified in the file: 
```
input/patterns/applypatterns.txt
```

The results will always be stored with the same input name in the following path: 
```
output/atdp/
```

## Versioning

For the versions available, see the [tags on this repository](https://github.com/PADS-UPC/atdpextractor/tags). 

