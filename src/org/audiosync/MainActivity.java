package org.audiosync;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final Button goBtn = (Button) findViewById(R.id.goButton);
		final Button nextTakeBtn = (Button) findViewById(R.id.nextButton);
		final Button clearTakeBtn = (Button) findViewById(R.id.clearTakeBtn);
		final EditText sceneInp = (EditText) findViewById(R.id.sceneInp);
		final EditText shotInp = (EditText) findViewById(R.id.shotInp);
		final EditText takeInp = (EditText) findViewById(R.id.takeInp);
		goBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				int[] bytes_to_send = new int[4];
				try {
					bytes_to_send[0] = Integer.parseInt(sceneInp.getText().toString());
					bytes_to_send[1] = Integer.parseInt(shotInp.getText().toString());
					bytes_to_send[2] = Integer.parseInt(takeInp.getText().toString());
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
		nextTakeBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					takeInp.setText(String.valueOf(Integer.parseInt(takeInp.getText().toString()) + 1));
				} catch (NumberFormatException e) {
					takeInp.setText(R.string.scene_default_value);
				}
			}
		});
		clearTakeBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				takeInp.setText(R.string.scene_default_value);
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
		boolean[] data_bits = bytesToBits(bytes_to_send);
		
		//Sync word                                                                                            one additional false
		boolean[] barker13 = {true, true, true, true, true, false, false, true, true, false, true, false, true, false};
		
		boolean[] bits = concatArrays(new boolean[]{false}, data_bits);
		//             Additional 0 for initial phase  ^
		
		int sampleRate = 44100;  // 1/s
		
		int carrierFrequency = 4000;  // 1/s
		double chunkLength = 0.05;  // s
		
		int syncToneFrequency = 5000;
		int syncChunkSamples = (int) (sampleRate * 0.05);
		
		int samplesPerChunk = (int) (sampleRate * chunkLength);
		
		int minBufferSize = (samplesPerChunk * bits.length + syncChunkSamples * barker13.length) * 2;
		AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STATIC);
		
		
		
		byte[] zeroChunck = generateTone(carrierFrequency, sampleRate, samplesPerChunk, Math.PI);
		byte[] oneChunck = generateTone(carrierFrequency, sampleRate, samplesPerChunk, 0);
		
		byte[] syncChunk = generateTone(syncToneFrequency, sampleRate, syncChunkSamples, 0);
		byte[] silence = new byte[syncChunkSamples * 2];
		
		byte[] sound = new byte[0];
		
		//Sync code
		for(int b = 0; b < barker13.length; b++){
			if(barker13[b])
				sound = concatArrays(sound, syncChunk);
			else
				sound = concatArrays(sound, silence);
		}
		
		//Data
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
	
	private byte[] generateTone(int frequency, int sampleRate, int samples, double phaseShift){
		final byte generatedSnd[] = new byte[2 * samples];
		int idx = 0;
        for (int i = 0; i < samples; ++i) {
	        double sample = Math.sin(frequency * (2 * Math.PI) * i / sampleRate + phaseShift);
	        
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
