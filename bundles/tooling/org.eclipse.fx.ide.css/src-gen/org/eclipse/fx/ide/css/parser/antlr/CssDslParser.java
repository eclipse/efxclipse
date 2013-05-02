/*
* generated by Xtext
*/
package org.eclipse.fx.ide.css.parser.antlr;

import com.google.inject.Inject;

import org.eclipse.fx.ide.css.services.CssDslGrammarAccess;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;

public class CssDslParser extends org.eclipse.xtext.parser.antlr.AbstractAntlrParser {
	
	@Inject
	private CssDslGrammarAccess grammarAccess;
	
	@Override
	protected void setInitialHiddenTokens(XtextTokenStream tokenStream) {
		tokenStream.setInitialHiddenTokens("RULE_WS", "RULE_ML_COMMENT");
	}
	
	@Override
	protected org.eclipse.fx.ide.css.parser.antlr.internal.InternalCssDslParser createParser(XtextTokenStream stream) {
		return new org.eclipse.fx.ide.css.parser.antlr.internal.InternalCssDslParser(stream, getGrammarAccess());
	}
	
	@Override 
	protected String getDefaultRuleName() {
		return "stylesheet";
	}
	
	public CssDslGrammarAccess getGrammarAccess() {
		return this.grammarAccess;
	}
	
	public void setGrammarAccess(CssDslGrammarAccess grammarAccess) {
		this.grammarAccess = grammarAccess;
	}
	
}
