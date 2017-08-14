# Text Trix

The Text Trix editor is an open-source, cross-platform text editor whose goal is to make file and text navigation easier for coding and general editing.

Text Trix started out as a homegrown project, originally at https://sourceforge.net/projects/texttrix/ and ported here as of August 2017. A central design philosphy is to simplify text editing without making hidden or unexpected changes to your text.

## Features

* Tabbed files and group tabbing
* Syntax highlighter and spell-checker
* Develop and install your own plugins
* Quick find by word or line
* Line bookmarks for navigating within files
* Emacs/Vi-style shortcuts available
* Cross-platform and completely free

## Building and running

**Note**: We are in the process of migrating from Sourceforge, including migration of several libraries and plugins for Text Trix. You can grab the most recent .jar files from [Text Trix 1.0.2](https://sourceforge.net/projects/texttrix/files/1%29%20Text%20Trix/TextTrix-1.0.2/texttrix-1.0.2.zip/download) can copy the `lib` and `plugins` folders into the git clone root folder.

```
git clone https://github.com/the4thchild/texttrix.git
cd texttrix
./build.sh
./run.sh
```

To package the file for portability:

```
./pkg.sh
```

## Writing a plugin

We're in the process of updating the documentation, but the old one is [here](https://sourceforge.net/p/texttrix/wiki/PlugIn/).
