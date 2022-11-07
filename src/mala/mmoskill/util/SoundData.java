package mala.mmoskill.util;

import org.bukkit.Sound;

public class SoundData
{
	public Sound sound;
	public float volume = 1.0f;
	public float pitch = 1.0f;
	
	public SoundData(Sound sound)
	{
		this(sound, 1.0, 1.0);
	}
	public SoundData(Sound sound, double volume, double pitch)
	{
		this.sound = sound;
		this.volume = (float)volume;
		this.pitch = (float)pitch;
	}
}
