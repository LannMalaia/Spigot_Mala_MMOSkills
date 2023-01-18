package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Listener;

import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Triple_Shot extends RegisteredSkill
{
	public Triple_Shot()
	{	
		super(new Triple_Shot_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("per", new LinearValue(3.0, 3.0, 1, 50));
	}
}

class Triple_Shot_Handler extends MalaPassiveSkill implements Listener
{
	public Triple_Shot_Handler()
	{
		super(	"TRIPLE_SHOT",
				"�ص��� ġ������ �� ��",
				Material.SPECTRAL_ARROW,
				"&7ġ������ �� ���� ������ ��,",
				"&7{per}%�� Ȯ���� &c2.0&7���� ���ظ� �ݴϴ�.");
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}
}

