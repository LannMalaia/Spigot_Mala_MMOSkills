package mala.mmoskill.manager;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;

import mala_mmoskill.main.MalaMMO_Skill;

public class Particle_Manager implements Listener
{
	private static Particle_Manager Instance = null;
	
	public static Particle_Manager Get_Instance()
	{
		if (Instance == null)
			Instance = new Particle_Manager();
		return Instance;
	}

	public Particle_Manager() {
	}
	
	public static boolean isReduceMode(LivingEntity player) {
		return player.hasMetadata("mala_mmoskill.particle_reduce");
	}
	
	public static void toggleReduceMode(LivingEntity player) {
		if (player.hasMetadata("mala_mmoskill.particle_reduce")) {
			player.removeMetadata("mala_mmoskill.particle_reduce", MalaMMO_Skill.plugin);
			player.sendMessage("§b[ 스킬 이펙트 간소화 모드 OFF ]");
		}
		else {
			player.setMetadata("mala_mmoskill.particle_reduce", new FixedMetadataValue(MalaMMO_Skill.plugin, true));
			player.sendMessage("§b[ 스킬 이펙트 간소화 모드 ON ]");
		}
	}
}