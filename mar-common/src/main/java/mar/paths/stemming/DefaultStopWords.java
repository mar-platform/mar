package mar.paths.stemming;

import java.util.HashSet;

public class DefaultStopWords implements IStopWords {

	private final HashSet<String> sw;
	
	public final static DefaultStopWords INSTANCE = new DefaultStopWords();
	
	public DefaultStopWords() {
		sw = new HashSet<String>();
		sw.add("a");
		sw.add("about");
		sw.add("above");
		sw.add("after");
		sw.add("again");
		sw.add("against");
		sw.add("all");
		sw.add("am");
		sw.add("an");
		sw.add("and");
		sw.add("any");
		sw.add("are");
		sw.add("as");
		sw.add("at");
		sw.add("be");
		sw.add("because");
		sw.add("been");
		sw.add("before");
		sw.add("being");
		sw.add("below");
		sw.add("between");
		sw.add("both");
		sw.add("but");
		sw.add("by");
		sw.add("can");
		sw.add("did");
		sw.add("do");
		sw.add("does");
		sw.add("doing");
		sw.add("don");
		sw.add("down");
		sw.add("during");
		sw.add("each");
		sw.add("few");
		sw.add("for");
		sw.add("from");
		sw.add("further");
		sw.add("had");
		sw.add("has");
		sw.add("have");
		sw.add("having");
		sw.add("he");
		sw.add("her");
		sw.add("here");
		sw.add("hers");
		sw.add("herself");
		sw.add("him");
		sw.add("himself");
		sw.add("his");
		sw.add("how");
		sw.add("i");
		sw.add("if");
		sw.add("in");
		sw.add("into");
		sw.add("is");
		sw.add("it");
		sw.add("its");
		sw.add("itself");
		sw.add("just");
		sw.add("me");
		sw.add("more");
		sw.add("most");
		sw.add("my");
		sw.add("myself");
		sw.add("no");
		sw.add("nor");
		sw.add("not");
		sw.add("now");
		sw.add("of");
		sw.add("off");
		sw.add("on");
		sw.add("once");
		sw.add("only");
		sw.add("or");
		sw.add("other");
		sw.add("our");
		sw.add("ours");
		sw.add("ourselves");
		sw.add("out");
		sw.add("over");
		sw.add("own");
		sw.add("s");
		sw.add("same");
		sw.add("she");
		sw.add("should");
		sw.add("so");
		sw.add("some");
		sw.add("such");
		sw.add("t");
		sw.add("than");
		sw.add("that");
		sw.add("the");
		sw.add("their");
		sw.add("theirs");
		sw.add("them");
		sw.add("themselves");
		sw.add("then");
		sw.add("there");
		sw.add("these");
		sw.add("they");
		sw.add("this");
		sw.add("those");
		sw.add("through");
		sw.add("to");
		sw.add("too");
		sw.add("under");
		sw.add("until");
		sw.add("up");
		sw.add("very");
		sw.add("was");
		sw.add("we");
		sw.add("were");
		sw.add("what");
		sw.add("when");
		sw.add("where");
		sw.add("which");
		sw.add("while");
		sw.add("who");
		sw.add("whom");
		sw.add("why");
		sw.add("will");
		sw.add("with");
		sw.add("you");
		sw.add("your");
		sw.add("yours");
		sw.add("yourself");
		sw.add("yourselves");	
	}

	@Override
	public boolean isStopWord(String word) {
		return sw.contains(word.toLowerCase());
	}
	
}
