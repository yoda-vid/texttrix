package net.sourceforge.texttrix;

public interface PlugIn {

    String run(String s, int start, int end);

    String getName();

}
