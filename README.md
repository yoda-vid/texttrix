# Text Trix

The Text Trix editor is an open-source, cross-platform text editor whose goal is to make file and text navigation easier for coding and general editing.

Text Trix started out as a homegrown project, originally at https://sourceforge.net/projects/texttrix/ and ported here as of August 2017. A central design philosophy is to simplify text editing without making hidden or unexpected changes to your text.

## Features

* Tabbed files and group tabbing
* Syntax highlighter and spell-checker
* Develop and install your own plugins
* Quick find by word or line
* Line bookmarks for navigating within files
* Emacs/Vi-style shortcuts available
* Cross-platform and completely free

## Run

* Binaries of older version (Text Trix 1.0.2) available [here](https://sourceforge.net/projects/texttrix/files/1%29%20Text%20Trix/TextTrix-1.0.2)
* Launch ``TextTrix.jar``, or use ``TextTrix.exe`` for Windows shortcut

## Compile

### Dependencies

* Java 1.5+
* [JSyntaxPaneTTx](https://github.com/the4thchild/jsyntaxpanettx)
* [OsterTTx](https://github.com/the4thchild/osterttx)

**Note**: We are in the process of migrating from Sourceforge, including several plugins and a more seamless build process. In the meantime, you can grab the most recent plugin .jar files from [Text Trix 1.0.2](https://sourceforge.net/projects/texttrix/files/1%29%20Text%20Trix/TextTrix-1.0.2/texttrix-1.0.2.zip/download) can copy the `plugins` folders into the git clone root folder.

### Build

```
# get Git repos
git clone https://github.com/the4thchild/texttrix.git
git clone https://github.com/the4thchild/jsyntaxpanettx.git
git clone https://github.com/the4thchild/osterttx.git

# build JSyntaxPaneTTx
cd jsyntaxpanettx
mvn package
cp target/jsyntaxpane-0.9.6.jar ../texttrix/lib/jsyntaxpane.jar

# build OsterTTx
cd ../osterttx
./build.sh --jar
cp oster.jar ../texttrix/lib

# build and run Text Trix
cd ../texttrix
./build.sh
./run.sh
```

To package the file for portability:

```
./pkg.sh
```

## Writing a plugin

We're in the process of updating the documentation, but the old one is [here](https://sourceforge.net/p/texttrix/wiki/PlugIn/).
