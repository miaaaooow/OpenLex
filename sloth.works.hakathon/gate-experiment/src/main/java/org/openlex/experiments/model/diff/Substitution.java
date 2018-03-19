package org.openlex.experiments.model.diff;

import gate.Document;

/**
 * Created by mateva on 21.01.18.
 */
public class Substitution {

    public static final String RULE = "RuleSubstitute";
    public static final String WHAT = "what";
    public static final String WITH_WHAT = "what";

	private String alNum;
	private String articleNum;
	private String what;
	private String withWhat;
	private Document doc;

	public Substitution(String alNum, String articleNum, String what, String withWhat, Document doc) {
		this.alNum = alNum;
		this.articleNum = articleNum;
		this.what = what;
		this.withWhat = withWhat;
		this.doc = doc;
	}

	public Document getDoc() {
		return doc;
	}

	public String getAlNum() {
		return alNum;
	}

	public void setAlNum(String alNum) {
		this.alNum = alNum;
	}

	public String getArticleNum() {
		return articleNum;
	}

	public void setArticleNum(String articleNum) {
		this.articleNum = articleNum;
	}

	public String getWhat() {
		return what;
	}

	public void setWhat(String what) {
		this.what = what;
	}

	public String getWithWhat() {
		return withWhat;
	}

	public void setWithWhat(String withWhat) {
		this.withWhat = withWhat;
	}

	@Override
	public String toString() {
		return "Article " + articleNum + ", Alinea " + alNum + " : substitute " + withWhat + " with " + withWhat;
	}
}
