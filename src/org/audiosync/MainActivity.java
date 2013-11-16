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
				byte[] bytes_to_send = new byte[3];
				try {
					bytes_to_send[0] = Byte.parseByte(sceneInp.getText().toString());
					bytes_to_send[1] = Byte.parseByte(shotInp.getText().toString());
					bytes_to_send[2] = Byte.parseByte(takeInp.getText().toString());
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
	
	private AudioTrack createAudio(byte[] bytes_to_send){
		boolean[] bits = bytesToBits(bytes_to_send);
		
		int sampleRate = 44100;  // 1/s
		
		int carrierFrequency = 4000;  // 1/s
		double chunkLength = 0.05;  // s
		
		int samplesPerChunk = (int) (sampleRate * chunkLength);
		//                                     + 1 chunk for leading 0
		int minBufferSize = (samplesPerChunk * (bits.length + 1)) * 2;
		AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STATIC);
		
		
		
		byte[] zeroChunck = generateTone(carrierFrequency, sampleRate, samplesPerChunk, Math.PI);
		byte[] oneChunck = generateTone(carrierFrequency, sampleRate, samplesPerChunk, 0);
		
		byte[] sound = new byte[0];
		sound = concatArrays(sound, zeroChunck);  // Additional 0 for differential phase shift
		
		for(int b = 0; b < bits.length; b++){
			if(bits[b])
				sound = concatArrays(sound, oneChunck);
			else
				sound = concatArrays(sound, zeroChunck);
		}
        
        track.write(sound, 0, sound.length);
		
		return track;
	}

	private boolean[] bytesToBits(byte[] bytes){
		boolean[] bits = new boolean[bytes.length*8];
		for (int b = 0; b < bytes.length; b++){
			int val = bytes[b];
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

}