# Text Trix

The Text Trix editor is an open-source, cross-platform text editor whose goal is to make file and text navigation easier for coding and general editing. A central design philosophy is to simplify text editing without making hidden or unexpected changes to your text.

Text Trix started out as a homegrown project, originally at [SourceForge](https://sourceforge.net/projects/texttrix/) and ported here in August 2017.

## Features

* Tabbed files and group tabbing
* Syntax highlighter and spell-checker
* Develop and install your own simple plugins
* Quick find by word or line
* Line bookmarks for navigating within files
* Simplified Vi/Emacs-style shortcuts options
* Tab/space/mixed auto-indent
* Detect and preserve line endings
* Cross-platform, open-source, and completely free

## Run

* Binaries available in [releases](https://github.com/yoda-vid/texttrix/releases)
* Launch ``TextTrix.jar``
* ``run.bat`` batch script provided to use for shortcuts on Windows

## Compile

### Dependencies

* Java 8+ (tested on Java 8-13)
* [JSyntaxPaneTTx](https://github.com/yoda-vid/jsyntaxpanettx), which requires Maven
* [OsterTTx](https://github.com/yoda-vid/osterttx)

### Build



```
# place Text Trix and all dependencies in the same folder
mkdir ttx
cd ttx
git clone https://github.com/yoda-vid/texttrix.git
git clone https://github.com/yoda-vid/jsyntaxpanettx.git
git clone https://github.com/yoda-vid/osterttx.git

# build Text Trix and all of its dependencies
texttrix/build.sh --jsyn --oster

# run Text Trix
texttrix/run.sh
```

To package the file for portability:

```
texttrix/pkg.sh
```

## Plugins

The [`PlugIn`](https://github.com/yoda-vid/texttrix/blob/master/com/textflex/texttrix/PlugIn.java) class provides a simple API for direct text manipulation. [`PlugInWindow`](https://github.com/yoda-vid/texttrix/blob/master/com/textflex/texttrix/PlugInWindow.java) allows access through a simple GUI dialog window.

### Get plugins

Plugin repos are collected in the [plugin-texttrix](https://github.com/topics/plugin-texttrix) topic on GitHub. We recommend adding the [Search](https://github.com/yoda-vid/ttx_search) plugin for Find/Replace functionality.

To start adding plugins, create a plugins folder in your main folder (alongside `texttrix`) and clone in a plugin repo:

```
# assumed to be in the folder containing texttrix
mkdir plugins
git clone https://github.com/yoda-vid/ttx_search.git
mv ttx_search.git plugins
texttrix/build.sh --plug
```

The resulting plugin JAR will be placed in `texttrix/plugins/ttx_your_plugin.jar`, which will be loaded automatically by Text Trix at runtime.

### Writing a plugin

We're in the process of updating the documentation, but the old one is [here](https://sourceforge.net/p/texttrix/wiki/PlugIn/).
