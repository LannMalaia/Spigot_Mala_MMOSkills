package mala.mmoskill.skills.passive;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.damage.DamageType;
import laylia_core.main.Damage;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Weapon_Identify;

public class Whip_Bounce extends RegisteredSkill
{
	public static Whip_Bounce skill;
	
	public Whip_Bounce()
	{	
		super(new Whip_Bounce_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("move_count", new LinearValue(0.25, 0.25, 0, 4));
		addModifier("dam_reduce", new LinearValue(58, -2));
		skill = this;
	}
	
	public static void Draw_Whip_Line(Location _start, Location _end)
	{
		Particle_Drawer.Draw_Line(_start, _end, Particle.CRIT, 0.1);
	}
}

class Whip_Bounce_Handler extends MalaPassiveSkill implements Listener
{
	public Whip_Bounce_Handler()
	{
		super(	"WHIP_BOUNCE",
				"튕기는 채찍",
				Material.LEAD,
				MsgTBL.PHYSICAL + MsgTBL.SKILL,
				"",
				"&7채찍으로 스킬, 마법 공격이 아닌 공격을 했을 때",
				"&7공격이 주변 3m 내 적에게 총 &e{move_count}&7번 튕깁니다.",
				"&7튕길 때마다 피해량이 &e{dam_reduce}&7% 감소합니다.",
				"&7한 번 맞은 적은 다시 공격하지 않습니다.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void attack_whipbounce(PlayerAttackEvent event)
	{
		Player player = event.getPlayer();
		PlayerData data = PlayerData.get(player);

		if (event.getDamage().hasType(DamageType.MAGIC)
			|| event.getDamage().hasType(DamageType.SKILL))
			return;
		
		// 스킬 체크
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("WHIP_BOUNCE");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;

		// 아이템 체크
		if (!Weapon_Identify.Hold_MMO_Whip(player))
			return;
		
		
		int moveCount = (int)skill.getModifier("move_count", level);
		double damReduce = skill.getModifier("dam_reduce", level) * 0.01;
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin,
				new Whip_Bounce_Chain(player, event.getEntity(), event.getDamage().getDamage(), moveCount, damReduce), 2);
	}
}

class Whip_Bounce_Chain implements Runnable
{
	Player player;
	LivingEntity target;
	double damage;
	boolean is_weapon;
	
	Set<LivingEntity> damaged_entities; // 쳐맞은 애들
	int chain_count; // 튕기는 횟수
	double reduce_percent; // 피해 감소치 0.0~1.0

	Location line_start_pos;
	Location line_end_pos;
	
	public Whip_Bounce_Chain(Player _player, LivingEntity _start, double _damage, int _count, double _reduce)
	{
		this(_player, _start, _damage, _count, _reduce, false);
	}
	public Whip_Bounce_Chain(Player _player, LivingEntity _start, double _damage, int _count, double _reduce, boolean _is_weapon)
	{
		player = _player;
		target = _start;
		damage = _damage;
		chain_count = _count;
		reduce_percent = _reduce;
		is_weapon = _is_weapon;
		
		damaged_entities = new HashSet<LivingEntity>();
		
		line_start_pos = player.getEyeLocation().clone();
		line_end_pos = target.getEyeLocation().clone();
	}
	
	public void run()
	{
		// 이거 튕겨야 하는지 체크
		if(chain_count > 0)
		{
			// 피해 목록에 체크하고, 피해량은 감소시킴
			damaged_entities.add(target);
			damage *= 1.0 - reduce_percent;
			chain_count -= 1;
			
			// 다음 적 찾기
			boolean searched = false;
			for(Entity e : target.getNearbyEntities(3, 3, 3))
			{
				if(!(e instanceof LivingEntity))
					continue;
				if(e instanceof ArmorStand)
					continue;
				if(damaged_entities.contains(e))
					continue;
				if(e == player)
					continue;
				LivingEntity le = (LivingEntity)e;
				target = le;
				line_start_pos = line_end_pos.clone();
				line_end_pos = le.getEyeLocation();
				searched = true;
				break;
			}
			
			if(!searched)
				return;
			
			// 타겟을 찾은 경우
			
			// 타겟에게 피해 줄 것
			Whip_Bounce.Draw_Whip_Line(line_start_pos, line_end_pos);
			if (Damage.Is_Possible(player, target))
				Damage.Attack(player, target, damage, DamageType.PHYSICAL, DamageType.SKILL);
			
			// 소리
			World world = target.getWorld();
			world.playSound(target.getEyeLocation(), Sound.ENTITY_PLAYER_HURT, 1, 2f);
			// world.playSound(target.getEyeLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 2f);
			
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 2);
		}
	}
	
}