package mala.mmoskill.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.manager.Not_Skill;
import mala_mmoskill.main.MalaMMO_Skill;

public abstract class MalaPassiveSkill extends SkillHandler<SimpleSkillResult> implements Not_Skill
{
	public MalaPassiveSkill(String id, String str, Material material, String... str2)
	{
		super(makeConfig(str, material, str2), id);
		Registering(str2);
	}

	public void Registering(String... lores)
	{
		List<String> modifiers = new ArrayList<>();
		for (String lore : lores)
		{
			StringTokenizer stk = new StringTokenizer(lore, "{");
			while(true)
			{
				if (!stk.hasMoreTokens())
					break;
				String tok = stk.nextToken();
				if (tok.indexOf("}") == -1)
					continue;
				modifiers.add(tok.substring(0, tok.indexOf("}")));
			}
		}
		registerModifiers(modifiers);
	}
	
	@Override
	public SimpleSkillResult getResult(SkillMetadata arg0)
	{
		throw new RuntimeException("Not supported");
	}
	
	@Override
	public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta)
	{
		throw new RuntimeException("Not supported");
	}

	@Override
	public boolean isTriggerable()
	{
		return false;
	}
	
	public static ConfigurationSection makeConfig(String str, Material material, String... str2)
	{
		ConfigurationSection cs = MalaMMO_Skill.plugin.getConfig();
		cs.set("name", str);
		cs.set("material", material.toString());
		List<String> lores = new ArrayList<String>();
		lores.add("&9[ ÆÐ½Ãºê ]");
		lores.add("");
		for (String st : str2)
			lores.add(st);
		cs.set("lore", lores);
		cs.set("passive-type", true);
		return cs;
	}
}
