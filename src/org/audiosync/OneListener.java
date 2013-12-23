package org.audiosync;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class OneListener implements OnClickListener {
	
	private EditText inputField;

	public OneListener(EditText inputField) {
		this.inputField = inputField;
	}

	@Override
	public void onClick(View v) {
		try {
			inputField.setText(String.valueOf(1));
		} catch (NumberFormatException e) {
			inputField.setText(R.string.scene_default_value);
		}
	}

}
