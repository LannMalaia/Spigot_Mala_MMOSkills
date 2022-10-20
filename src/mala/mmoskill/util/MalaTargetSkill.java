package mala.mmoskill.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import mala.mmoskill.manager.Not_Skill;
import mala_mmoskill.main.MalaMMO_Skill;

public abstract class MalaTargetSkill extends SkillHandler<TargetSkillResult> implements Not_Skill
{
	public double range = 50.0;
	
	public MalaTargetSkill(String id, String str, Material material, String... str2)
	{
		super(makeConfig(str, material, str2), id);
		Registering(str2);
	}
	
	@Override
	public TargetSkillResult getResult(SkillMetadata cast)
	{
		return new TargetSkillResult(cast, 50.0, InteractionType.OFFENSE_SKILL);
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
	
	public static ConfigurationSection makeConfig(String str, Material material, String... str2)
	{
		ConfigurationSection cs = MalaMMO_Skill.plugin.getConfig();
		cs.set("name", str);
		cs.set("material", material.toString());
		List<String> lores = new ArrayList<String>();
		for (String st : str2)
			lores.add(st);
		cs.set("lore", lores);
		return cs;
	}
}
