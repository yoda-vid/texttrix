package net.sourceforge.texttrix;

public class NonPrintingChars implements PlugIn {
    private String name = "Non-Printing Characters";

    public NonPrintingChars() {
    }

    public String getName() { return name; }

    /**Shows non-printing characters.
     * Adds String representations for non-printing characters, such as 
     * paragraph and tab markers.  The representations become part of the text.
     * @param text text to convert
     * @return text with added String representations
     */
    public String run(String text, int start, int end) {
	int len = text.length();
	StringBuffer s = new StringBuffer(len);
	char c;

	// append text preceding the selection
	s.append(text.substring(0, start));
	// progress char by char, revealing newlines and tabs explicitly
	for (int i = start; i < end; i++) {
	    c = text.charAt(i);
	    switch (c) {
	    case '\n':
		s.append("\\n" + c);
		break;
	    case '\t':
		s.append("\\t" + c);
		break;
	    default:
		s.append(c);
		break;
	    }
	}
	return s.toString() + text.substring(end);
    }
    public static String print(String text, int start, int end) {
	int len = text.length();
	StringBuffer s = new StringBuffer(len);
	char c;

	// append text preceding the selection
	s.append(text.substring(0, start));
	// progress char by char, revealing newlines and tabs explicitly
	for (int i = start; i < end; i++) {
	    c = text.charAt(i);
	    switch (c) {
	    case '\n':
		s.append("\\n" + c);
		break;
	    case '\t':
		s.append("\\t" + c);
		break;
	    default:
		s.append(c);
		break;
	    }
	}
	return s.toString() + text.substring(end);
    }

}
