package mala.mmoskill.manager;

public enum SpellChainType {
	FIRE, ICE, LIGHTNING;
	
	public static String GetName(SpellChainType _type)
	{
		switch (_type)
		{
		case FIRE:
			return "¡×c¡×lÈ­¿°";
		case ICE:
			return "¡×b¡×l³Ã±â";
		case LIGHTNING:
			return "¡×e¡×lÀü°Ý";
		}
		return "?";
	}
}
