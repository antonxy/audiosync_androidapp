package org.audiosync;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class PlusListener implements OnClickListener {
	
	private EditText inputField;

	public PlusListener(EditText inputField) {
		this.inputField = inputField;
	}

	@Override
	public void onClick(View v) {
		try {
			inputField.setText(String.valueOf(Math.min(Integer.parseInt(inputField.getText().toString()) + 1, 255)));
		} catch (NumberFormatException e) {
			inputField.setText(R.string.scene_default_value);
		}
	}

}
