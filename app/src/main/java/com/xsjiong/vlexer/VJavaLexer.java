package com.xsjiong.vlexer;

public class VJavaLexer extends VLexer {
	// 这里应该使用Lazy Build，因为可能有子类继承，那时我们应该让子类来Build这个tree（只需要重写getKeywordTrie）
	private static Trie KEYWORD_TRIE = null;

	@Override
	public Trie getKeywordTrie() {
		if (KEYWORD_TRIE == null)
			KEYWORD_TRIE = Trie.BuildTrie(
					"abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do",
					"double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof",
					"int", "interface", "long", "native", "new", "package", "private", "protected", "public", "return", "strictfp", "short",
					"static", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while"
			);
		return KEYWORD_TRIE;
	}

	@Override
	protected boolean isIdentifierStart(char c) {
		return Character.isJavaIdentifierStart(c);
	}

	@Override
	protected boolean isIdentifierPart(char c) {
		return Character.isJavaIdentifierPart(c);
	}

	public VJavaLexer() {
	}

	public VJavaLexer(char[] cs) {
		super(cs);
	}

	@Override
	public short ProcessSymbol(char c) {
		switch (c) {
			// 有 ## 或者 #= 用法的运算符
			case '|':
			case '&':
			case '+':
			case '-': {
				if (P == S.length) return TYPE_OPERATOR;
				if (S[P] == S[P - 1] || S[P] == '=') {
					++P;
					return TYPE_OPERATOR;
				}
				return TYPE_OPERATOR;
			}
			// 有 # 或者 #= 用法的运算符
			case '<':
			case '>':
			case '!': {
				if (P == S.length) return TYPE_OPERATOR;
				if (S[P] == '=')
					++P;
				return TYPE_OPERATOR;
			}
			// 只有 # 用法的运算符
			case '~':
			case '?':
				return TYPE_OPERATOR;
			// 只有 #= 用法的运算符
			case '^':
			case '%':
			case '*': {
				if (P == S.length) return TYPE_OPERATOR;
				if (S[P] == '=') {
					++P;
					return TYPE_OPERATOR;
				}
				return TYPE_OPERATOR;
			}
			case '/': {
				if (P == S.length) return TYPE_OPERATOR;
				switch (S[P]) {
					case '*': {
						boolean star = false;
						do {
							if (++P == S.length) return TYPE_COMMENT;
							if (S[P] == '*') star = true;
							else {
								if (S[P] == '/' && star) {
									++P;
									return TYPE_COMMENT;
								}
								star = false;
							}
						} while (true);
					}
					case '/': {
						do {
							++P;
						} while (P != S.length && S[P] != '\n');
						return TYPE_COMMENT;
					}
					case '=':
						++P;
						return TYPE_OPERATOR;
					default:
						return TYPE_OPERATOR;
				}
			}
			case '"': {
				boolean z = false;
				do {
					if (P == S.length) return TYPE_STRING;
					if (S[P] == '\\')
						z = !z;
					else if (S[P] == '"' && !z) {
						++P;
						return TYPE_STRING;
					} else if (z) z = false;
					++P;
				} while (true);
			}
			case '\'': {
				// 尽管单引号里面只允许有一个字符，但考虑到转义（我懒得写判断了）和用户异常遍及，我还是把它当作里面可以放n个字符吧
				boolean z = false;
				do {
					if (P == S.length) return TYPE_CHAR;
					if (S[P] == '\\')
						z = !z;
					else if (S[P] == '\'' && !z) {
						++P;
						return TYPE_CHAR;
					} else if (z) z = false;
					++P;
				} while (true);
			}
			case '=': {
				if (P == S.length) return TYPE_ASSIGNMENT;
				if (S[P] == '=') {
					++P;
					return TYPE_OPERATOR;
				}
				return TYPE_ASSIGNMENT;
			}
			case '(':
				return TYPE_LEFT_PARENTHESIS;
			case ')':
				return TYPE_RIGHT_PARENTHESIS;
			case '[':
				return TYPE_LEFT_SQUARE_BRACKET;
			case ']':
				return TYPE_RIGHT_SQUARE_BRACKET;
			case '{':
				return TYPE_LEFT_BRACE;
			case '}':
				return TYPE_RIGHT_BRACE;
			case ';':
				return TYPE_SEMICOLON;
			case ':':
				return TYPE_COLON;
			case ',':
				return TYPE_COMMA;
		}
		return UNRESOLVED_TYPE;
		// 还是不要抛出错误吧——要是用户从外面粘贴过来乱码你就崩溃了是几个意思?
//		throw new RuntimeException();
	}

	@Override
	protected boolean isWhitespace(char c) {
		return (c == ' ' || c == '\n' || c == '\t' || c == '\r' || c == '\f' || c == '\uFFFF');
	}
}