/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.engine;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author Krcma
 */
public class SoundPlayer {

    private final Clip[] clip;
    private boolean run = false;

    /**
     * Create instance of this sound
     *
     * @param sound URL of sound
     * @param size Size = number of clips (Size 5 -> you can play 5 sound in
     * same time)
     * @param looping True -> when is end then play again
     * @param vol Volume
     */
    public SoundPlayer(URL sound, int size, boolean looping, float vol) {
        this.clip = new Clip[size];
        size = looping ? 1 : size;
        try {
            for (int i = 0; i < size; i++) {
                AudioInputStream ais = AudioSystem.getAudioInputStream(sound);
                AudioFormat baseFormat = ais.getFormat();
                AudioFormat decodeFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(),
                        16,
                        baseFormat.getChannels(),
                        baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate(),
                        false
                );
                AudioInputStream dais = AudioSystem.getAudioInputStream(decodeFormat, ais);
                this.clip[i] = AudioSystem.getClip();
                this.clip[i].open(dais);
                FloatControl volume = (FloatControl) this.clip[i].getControl(FloatControl.Type.MASTER_GAIN);
                float gain = (float) ((volume.getMaximum() - volume.getMinimum()) * Math.log10(vol * 9f + 1f) + volume.getMinimum());
                gain = Math.min(Math.max(gain, volume.getMinimum()), volume.getMaximum());
                volume.setValue(gain);
                if (looping) {
                    this.clip[0].addLineListener((LineEvent event) -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            if (run) {
                                playSurroundSound(0, 50);
                            }
                        }
                    });
                }
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            Logger.getLogger(SoundPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //stop this sound
    public void stop() {
        if (this.clip == null) {
            return;
        }
        this.run = false;
        for (Clip c : this.clip) {
            if (c.isRunning()) {
                c.stop();
            }
        }
    }

    //close this sound
    public void close() {
        if (this.clip == null) {
            return;
        }
        stop();
        for (Clip c : this.clip) {
            if (c.isRunning()) {
                c.close();
            }
        }
    }

    public void playSurroundSound(float f, float volume) {
        if (this.clip == null) {
            return;
        }
        this.run = true;
        for (Clip c : this.clip) {
            if (!c.isRunning()) {
                c.setFramePosition(0);
                //position of sound
                FloatControl pan = (FloatControl) c.getControl(FloatControl.Type.PAN);
                pan.setValue(f);
                FloatControl master = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
                //set volume
                float gain = (float) ((master.getMaximum() - master.getMinimum()) * Math.log10(volume * 9f + 1f) + master.getMinimum());
                gain = Math.min(Math.max(gain, master.getMinimum()), master.getMaximum());
                master.setValue(gain);
                //play sound
                c.start();
                break;
            }
        }
    }

}
