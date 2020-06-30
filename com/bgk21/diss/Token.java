package com.bgk21.diss;

public class Token {
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}
	
}

class T_If extends Token {
} // represents if

class T_Goto extends Token {
} // represents if

class T_Else extends Token {
} // represents else

class T_Semicolon extends Token {
} // represents ;

class T_Colon extends Token {
} // represents :

class T_LeftBracket extends Token {
} // represents (

class T_RightBracket extends Token {
} // represents )

class T_EqualDefines extends Token {
} // represents =

interface T_Comp {
}

class T_Equal extends Token implements T_Comp  {
} // represents ==

class T_NotEqual extends Token implements T_Comp  {
} // represents !=


class T_LessThan extends Token implements T_Comp  {
} // represents <

class T_GreaterThan extends Token implements T_Comp  {
} // represents >

class T_LessEq extends Token implements T_Comp  {
} // represents <=

class T_GreaterEq extends Token implements T_Comp  {
} // represents >=


class T_LeftCurlyBracket extends Token {
} // represents {

class T_RightCurlyBracket extends Token {
} // represents }

class T_Assign extends Token {
} // represents =

interface T_Binop {
}

class T_Plus extends Token implements T_Binop  {
} // represents +

class T_Times extends Token implements T_Binop  {
} // represents *

class T_Minus extends Token implements T_Binop  {
} // represents -

class T_Div extends Token implements T_Binop  {
} // represents /

class T_Mod extends Token implements T_Binop  {
} // represents %

class T_Identifier extends Token {
	String name;

	public T_Identifier(String word) {
		this.name = word;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		T_Identifier other = (T_Identifier) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}

class T_Integer extends Token {
	int number;

	public T_Integer(int number) {
		this.number = number;
	}
}

class T_Double extends Token {
	double number;
	
	public T_Double(double number) {
		this.number = number;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		T_Double other = (T_Double) obj;
		if (Double.doubleToLongBits(number) != Double.doubleToLongBits(other.number))
			return false;
		return true;
	}
	
}

class T_For extends Token {
}

class T_While extends Token {
}

class T_NewLine extends Token {
}

class T_Print extends Token {
}

class T_LeftSquareBracket extends Token {
}

class T_RightSquareBracket extends Token {
}

class T_Comma extends Token {
}