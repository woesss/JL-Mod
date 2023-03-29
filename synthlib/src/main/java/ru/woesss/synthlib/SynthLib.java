package ru.woesss.synthlib;

public class SynthLib {

	private static volatile SynthLib synthLib;

	private final long handle;

	public SynthLib(String soundFont) {
		handle = setupSynth(soundFont);
	}

	public static SynthLib getInstance(String soundFont) {
		if (synthLib == null) {
			synchronized (SynthLib.class) {
				if (synthLib == null) {
					System.loadLibrary("synthlib");
					synthLib = new SynthLib(soundFont);
				}
			}
		}
		return synthLib;
	}

	public synchronized void render(String inputPath, String outPath) {
		midiToWav(handle, inputPath, outPath);
	}

	private native long setupSynth(String soundFont);
	private native void closeSynth(long handle);
	private native void noteOn (long handle, int channel, int note);
	private native void noteOff(long handle, int channel, int note);
	private native void midiToWav(long handle, String inputPath, String outPath);
	private native void changeInstrument(long handle, int channel, int instrument);
}