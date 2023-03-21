package mala.mmoskill.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mala_mmoskill.main.MalaMMO_Skill;

public class Buff_Manager
{
	public static void Run()
	{
		Bukkit.getScheduler().runTaskTimer(MalaMMO_Skill.plugin, new Buff_Title_Sender(), 100, 5);
	}
	
	static boolean Reduce_Amp(LivingEntity _target, PotionEffectType _type, int _amp)
	{
		if(_target.hasPotionEffect(_type))
		{
			PotionEffect pe = _target.getPotionEffect(_type);
			int level = pe.getAmplifier() - _amp - 1;
			_target.removePotionEffect(_type);
			if(level >= 0) // amp ���µ��� ��� �����ִ� ��쿡�� �ٽ� �߰�
			{
				pe = new PotionEffect(_type, pe.getDuration(), pe.getAmplifier());
				_target.addPotionEffect(pe);
			}
			if(_target instanceof Player)
				Buff_Title_Sender.Add_Msg((Player)_target, Get_EffectName(_type), level);
			return true;
		}
		return false;
	}
	
	public static String Get_EffectName(PotionEffectType _type)
	{
		if(_type == PotionEffectType.ABSORPTION)
			return "���";
		if(_type == PotionEffectType.BAD_OMEN)
			return "����";
		if(_type == PotionEffectType.BLINDNESS)
			return "�Ǹ�";
		if(_type == PotionEffectType.CONDUIT_POWER)
			return "����ü�� ��";
		if(_type == PotionEffectType.CONFUSION)
			return "�ֹ�";
		if(_type == PotionEffectType.DAMAGE_RESISTANCE)
			return "����";
		if(_type == PotionEffectType.DOLPHINS_GRACE)
			return "������ ����";
		if(_type == PotionEffectType.FAST_DIGGING)
			return "������";
		if(_type == PotionEffectType.FIRE_RESISTANCE)
			return "ȭ�� ����";
		if(_type == PotionEffectType.GLOWING)
			return "�߱�";
		if(_type == PotionEffectType.HARM)
			return "����";
		if(_type == PotionEffectType.HEAL)
			return "ȸ��";
		if(_type == PotionEffectType.HEALTH_BOOST)
			return "����� ��ȭ";
		if(_type == PotionEffectType.HERO_OF_THE_VILLAGE)
			return "������ ����";
		if(_type == PotionEffectType.HUNGER)
			return "���";
		if(_type == PotionEffectType.INCREASE_DAMAGE)
			return "��";
		if(_type == PotionEffectType.INVISIBILITY)
			return "����ȭ";
		if(_type == PotionEffectType.JUMP)
			return "������ ��ȭ";
		if(_type == PotionEffectType.LEVITATION)
			return "����";
		if(_type == PotionEffectType.LUCK)
			return "���";
		if(_type == PotionEffectType.NIGHT_VISION)
			return "�߰� ����";
		if(_type == PotionEffectType.POISON)
			return "��";
		if(_type == PotionEffectType.REGENERATION)
			return "���";
		if(_type == PotionEffectType.SATURATION)
			return "��ȭ";
		if(_type == PotionEffectType.SLOW)
			return "�ӵ� ����";
		if(_type == PotionEffectType.SLOW_DIGGING)
			return "ä�� �Ƿ�";
		if(_type == PotionEffectType.SLOW_FALLING)
			return "���� ����";
		if(_type == PotionEffectType.SPEED)
			return "�ӵ� ����";
		if(_type == PotionEffectType.UNLUCK)
			return "�ҿ�";
		if(_type == PotionEffectType.WATER_BREATHING)
			return "���� ȣ��";
		if(_type == PotionEffectType.WEAKNESS)
			return "������";
		if(_type == PotionEffectType.WITHER)
			return "�õ�";
		return "";
	}
	
	public static int Get_Debuff_Count(LivingEntity _target)
	{
		int result = 0;
		for (PotionEffect pes : _target.getActivePotionEffects())
		{
			PotionEffectType pet = pes.getType();
			if (pet.equals(PotionEffectType.BAD_OMEN) ||
				pet.equals(PotionEffectType.BLINDNESS) ||
				pet.equals(PotionEffectType.CONFUSION) ||
				pet.equals(PotionEffectType.GLOWING) ||
				pet.equals(PotionEffectType.HUNGER) ||
				pet.equals(PotionEffectType.LEVITATION) ||
				pet.equals(PotionEffectType.POISON) ||
				pet.equals(PotionEffectType.SLOW) ||
				pet.equals(PotionEffectType.SLOW_DIGGING) ||
				pet.equals(PotionEffectType.UNLUCK) ||
				pet.equals(PotionEffectType.WEAKNESS) ||
				pet.equals(PotionEffectType.WITHER))
				result++;
		}
		return result;
	}
	
	public static void Add_Buff(LivingEntity _target, PotionEffectType _type, int _amp, int _ticks, PotionEffectType _conflict_type)
	{
		Add_Buff(_target, _type, _amp, _ticks, _conflict_type, false);
	}
	public static void Add_Buff(LivingEntity _target, PotionEffectType _type, int _amp, int _ticks, PotionEffectType _conflict_type, boolean _silence)
	{
		boolean reduced = false;
		if(_conflict_type != null) // �� Ÿ���� �ִ� ���
		{
			if(Reduce_Amp(_target, _conflict_type, _amp))
			{
				reduced = true;
			}
		}
		if (!reduced) // �� ���Ұ� �ȵƴٸ�
		{
			if (!_target.hasPotionEffect(_type)) // �ƿ� ȿ���� ������ �׳� �߰�
			{
				_target.addPotionEffect(new PotionEffect(_type, _ticks, _amp));
				if (_target instanceof Player && !_silence)
					Buff_Title_Sender.Add_Msg((Player)_target, Get_EffectName(_type), (_amp + 1));
				return;
			}
			// ȿ���� ������ ��
			PotionEffect pe = _target.getPotionEffect(_type);
			if (pe.getAmplifier() <= _amp) // �� ���� ȿ���� �ִ� ��쿡�� �ƿ� �������
			{
				if (pe.getDuration() < _ticks) // ���ӽð��� �� ª�� ��쿡�� ����
				{
					_target.removePotionEffect(_type);
					_target.addPotionEffect(new PotionEffect(_type, _ticks, _amp));
					if (_target instanceof Player && !_silence)
						Buff_Title_Sender.Add_Msg((Player)_target, Get_EffectName(_type), 0);
				}
			}
		}
	}
	
	
	/**
	 * @author jimja
	 * @version 2020. 8. 19.
	 * @apiNote add_buff�� ����ѵ�, �̰� max_amp���� ����ؼ� ����
	 * @param _target
	 * @param _type
	 * @param _amp
	 * @param _ticks
	 * @param _conflict_type
	 * @param _max_amp
	 */
	public static void Increase_Buff(LivingEntity _target, PotionEffectType _type, int _amp, int _ticks, PotionEffectType _conflict_type, int _max_amp)
	{
		boolean reduced = false;
		if(_conflict_type != null) // �� Ÿ���� �ִ� ���
		{
			if(Reduce_Amp(_target, _conflict_type, _amp))
			{
				reduced = true;
			}
		}
		if(!reduced) // �� ���Ұ� �ȵƴٸ�
		{
			if(!_target.hasPotionEffect(_type)) // �ƿ� ȿ���� ������ �׳� �߰�
			{
				_target.addPotionEffect(new PotionEffect(_type, _ticks, _amp));
				if(_target instanceof Player)
					Buff_Title_Sender.Add_Msg((Player)_target, Get_EffectName(_type), (_amp + 1));
				return;
			}
			// ȿ���� ������ ��
			PotionEffect pe = _target.getPotionEffect(_type);
			if(pe.getAmplifier() <= _max_amp) // ���� ��ø �ִ�ġ�� �Ѿ�� ���� ��쿡�� ����
			{
				int amp = Math.min(_max_amp, pe.getAmplifier() + _amp + 1);
				int duration = pe.getDuration() < _ticks ? _ticks : pe.getDuration();
				_target.removePotionEffect(_type);
				_target.addPotionEffect(new PotionEffect(_type, duration, amp));
				if(_target instanceof Player)
					Buff_Title_Sender.Add_Msg((Player)_target, Get_EffectName(_type), (amp + 1));
			}
		}
	}
	/**
	 * ��� ���� ������ �ð��� ����
	 * @param _target
	 * @param _ticks
	 * @param _max_ticks
	 */
	public static void Increase_All_Good_Buff(LivingEntity _target, int _ticks, int _max_ticks)
	{
		for (PotionEffect pes : _target.getActivePotionEffects())
		{
			PotionEffectType pet = pes.getType();
			if (pet.equals(PotionEffectType.ABSORPTION) ||
				pet.equals(PotionEffectType.CONDUIT_POWER) ||
				pet.equals(PotionEffectType.DAMAGE_RESISTANCE) ||
				pet.equals(PotionEffectType.DOLPHINS_GRACE) ||
				pet.equals(PotionEffectType.FAST_DIGGING) ||
				pet.equals(PotionEffectType.FIRE_RESISTANCE) ||
				pet.equals(PotionEffectType.HEALTH_BOOST) ||
				pet.equals(PotionEffectType.HERO_OF_THE_VILLAGE) ||
				pet.equals(PotionEffectType.INCREASE_DAMAGE) ||
				pet.equals(PotionEffectType.INVISIBILITY) ||
				pet.equals(PotionEffectType.JUMP) ||
				pet.equals(PotionEffectType.LUCK) ||
				pet.equals(PotionEffectType.NIGHT_VISION) ||
				pet.equals(PotionEffectType.REGENERATION) ||
				pet.equals(PotionEffectType.SATURATION) ||
				pet.equals(PotionEffectType.SLOW_FALLING) ||
				pet.equals(PotionEffectType.SPEED) ||
				pet.equals(PotionEffectType.WATER_BREATHING)) {

				int tick = pes.getDuration() + _ticks;
				if (tick < _max_ticks) {
					_target.removePotionEffect(pet);
					_target.addPotionEffect(new PotionEffect(pet, tick, pes.getAmplifier()));
				}
			}
		}
	}

	public static void Remove_Buff(LivingEntity _target, PotionEffectType _type)
	{
		_target.removePotionEffect(_type);
		// Reduce_Amp(_target, _type, 999);
	}
}

class Buff_Title_Sender implements Runnable
{
	private static Buff_Title_Sender Instance;
	Map<Player, String> dict;
	
	public Buff_Title_Sender()
	{
		Instance = this;
		dict = new HashMap<Player,String>();
	}
	
	public static void Add_Msg(Player _player, String _effect_name, int _additive)
	{
		String msg = "";
		String add = _additive > 0 ? "��b��l+" : (_additive < 0 ? "��7��l-" : "��e��l=");
		if(!Instance.dict.containsKey(_player))
			msg += add + _effect_name;
		else
		{
			msg = Instance.dict.get(_player);
			if (msg.contains(_effect_name))
				return;
			msg += "  " + add + _effect_name;
		}
		Instance.dict.put(_player, msg);
	}
	
	public void run()
	{
		if (dict == null || dict.keySet() == null)
			return;
		
		Iterator<Player> keyset = dict.keySet().iterator();
		try
		{
			while (keyset.hasNext())
			{
				Player player = keyset.next();
				String msg = dict.get(player);
				player.sendTitle("", msg, 0, 40, 10);
				dict.remove(player);
			}
		}
		catch (Exception e)
		{
			
		}
	}
}
