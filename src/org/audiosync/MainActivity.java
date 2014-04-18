package org.audiosync;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	private NumberChooser sceneInp;
	private NumberChooser shotInp;
	private NumberChooser takeInp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final Button goBtn = (Button) findViewById(R.id.goButton);
		
		sceneInp = (NumberChooser) findViewById(R.id.sceneChooser);
		shotInp = (NumberChooser) findViewById(R.id.shotChooser);
		takeInp = (NumberChooser) findViewById(R.id.takeChooser);
		
		goBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				int[] bytes_to_send = new int[4];
				try {
					bytes_to_send[0] = sceneInp.getNumber();
					bytes_to_send[1] = shotInp.getNumber();
					bytes_to_send[2] = takeInp.getNumber();
					for (int i : bytes_to_send) {
						if(i < 0 || i > 255){
							Toast.makeText(MainActivity.this, "Numbers have to be between 0 and 255", Toast.LENGTH_SHORT).show();
							return;
						}
					}
					
					int checksum = 0;
					for (int i : bytes_to_send) {
						checksum += i;
					}
					checksum %= 255;
					bytes_to_send[3] = checksum;
					
				} catch (NumberFormatException e) {
					Toast.makeText(MainActivity.this, "invalid numbers in textfields", Toast.LENGTH_SHORT).show();
					return;
				}
				AudioTrack audio = createAudio(bytes_to_send);
				audio.play();
			}
		});

		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private AudioTrack createAudio(int[] bytes_to_send){
		boolean[] bits = bytesToBits(bytes_to_send);
		
		int sampleRate = 48000;  // 1/s
		
		int carrierFrequency0 = 5500;  // 1/s
		int carrierFrequency1 = 6000;  // 1/s
		double chunkLength = 0.02;  // s
		
		double syncToneFrequency0 = 3000;
		double syncToneFrequency1 = 6000;
		double syncToneDuration = 0.2;
		
		int samplesPerChunk = (int) (sampleRate * chunkLength);
		
		int minBufferSize = (int) ((samplesPerChunk * bits.length + syncToneDuration * sampleRate) * 2);
		AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STATIC);
		
		//Sync tone
		byte[] sound = generateChirp(syncToneFrequency0, syncToneFrequency1, syncToneDuration, sampleRate);
		
		//Data
		byte[] zeroChunck = generateTone(carrierFrequency0, sampleRate, samplesPerChunk);
		byte[] oneChunck = generateTone(carrierFrequency1, sampleRate, samplesPerChunk);
		
		for(int b = 0; b < bits.length; b++){
			if(bits[b])
				sound = concatArrays(sound, oneChunck);
			else
				sound = concatArrays(sound, zeroChunck);
		}
        
        track.write(sound, 0, sound.length);
		
		return track;
	}

	private boolean[] bytesToBits(int[] bytes_to_send){
		boolean[] bits = new boolean[bytes_to_send.length*8];
		for (int b = 0; b < bytes_to_send.length; b++){
			int val = bytes_to_send[b];
			for (int i = 0; i < 8; i++)
			{
			   bits[b*8 + i] = (val & 128) == 0 ? false : true;
			   val <<= 1;
			}
		}
		return bits;
	}
	
	private byte[] generateTone(int frequency, int sampleRate, int samples){
		final byte generatedSnd[] = new byte[2 * samples];
		int idx = 0;
        for (int i = 0; i < samples; ++i) {
	        double sample = Math.sin(frequency * (2 * Math.PI) * i / sampleRate);
	        
	        // convert to 16 bit pcm sound array
	        // assumes the sample buffer is normalised.
	        // scale to maximum amplitude
	        final short val = (short) ((sample * 32767));
	        // in 16 bit wav PCM, first byte is the low order byte
	        generatedSnd[idx++] = (byte) (val & 0x00ff);
	        generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
        return generatedSnd;
	}
	
	private byte[] generateChirp(double freq0, double freq1, double duration, int sampleRate){
		int samples = (int) (duration * sampleRate);
		final byte generatedSnd[] = new byte[2 * samples];
		int idx = 0;
		double beta = (freq1-freq0)/duration;
        for (int i = 0; i < samples; ++i) {
        	double t = ((double)i) / sampleRate;
	        double sample = Math.cos(2 * Math.PI * (freq0 * t + 0.5 * beta * t * t));
	        
	        // convert to 16 bit pcm sound array
	        // assumes the sample buffer is normalised.
	        // scale to maximum amplitude
	        final short val = (short) ((sample * 32767));
	        // in 16 bit wav PCM, first byte is the low order byte
	        generatedSnd[idx++] = (byte) (val & 0x00ff);
	        generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
        return generatedSnd;
	}
	
	private byte[] concatArrays(byte[] a, byte[] b){
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}
	
	private boolean[] concatArrays(boolean[] a, boolean[] b){
		boolean[] c = new boolean[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

}
