# Text Trix

The Text Trix editor is an open-source, cross-platform text editor whose goal is to make file and text navigation easier for coding and general editing.

Text Trix started out as a homegrown project, originally at [SourceForge](https://sourceforge.net/projects/texttrix/) and ported here as of August 2017. A central design philosophy is to simplify text editing without making hidden or unexpected changes to your text.

## Features

* Tabbed files and group tabbing
* Syntax highlighter and spell-checker
* Develop and install your own simple plugins
* Quick find by word or line
* Line bookmarks for navigating within files
* Emacs/Vi-style shortcuts available
* Tab/space/mixed auto-indent
* Cross-platform and completely free

## Run

* Binaries of older version (Text Trix 1.0.2) available [here](https://sourceforge.net/projects/texttrix/files/1%29%20Text%20Trix/TextTrix-1.0.2)
* Launch ``TextTrix.jar``, or use ``TextTrix.exe`` as a Windows shortcut

## Compile

### Dependencies

* Java 1.5+ (tested mostly on Java 8-10)
* [JSyntaxPaneTTx](https://github.com/the4thchild/jsyntaxpanettx)
* [OsterTTx](https://github.com/the4thchild/osterttx)

### Build



```
# place Text Trix and all dependencies in the same folder
mkdir ttx
cd ttx

# clone all repos
git clone https://github.com/the4thchild/texttrix.git
git clone https://github.com/the4thchild/jsyntaxpanettx.git
git clone https://github.com/the4thchild/osterttx.git

# build and run Text Trix
texttrix/build.sh
texttrix/run.sh
```

To package the file for portability:

```
texttrix/pkg.sh
```

## Plugins

### Get plugins

Plugin repos are collected in the [texttrix](https://github.com/topics/texttrix) topic on GitHub.

To start adding plugins, create a plugins folder in your main folder (alongside `texttrix`) and clone in a plugin repo:

```
# assumed to be in the folder containing texttrix
mkdir plugins
cd plugins
git clone https://github.com/the4thchild/ttx_songsheet.git
cd ..
texttrix/build.sh --plug
```

The resulting plugin JAR will be placed in `texttrix\plugins\ttx_your_plugin.jar`, which will be loaded automatically by Text Trix at runtime.

### Writing a plugin

We're in the process of updating the documentation, but the old one is [here](https://sourceforge.net/p/texttrix/wiki/PlugIn/).