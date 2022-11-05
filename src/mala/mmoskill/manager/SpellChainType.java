package mala.mmoskill.manager;

public enum SpellChainType {
	FIRE, ICE, LIGHTNING;
	
	public static String GetName(SpellChainType _type)
	{
		switch (_type)
		{
		case FIRE:
			return "��c��lȭ��";
		case ICE:
			return "��b��l�ñ�";
		case LIGHTNING:
			return "��e��l����";
		}
		return "?";
	}
}
